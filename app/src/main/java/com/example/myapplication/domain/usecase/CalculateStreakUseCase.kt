package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor() {

    data class StreakInfo(
        val currentStreak: Int,
        val longestStreak: Int,
        val isCompletedToday: Boolean // "Today" in the context of the current period
    )

    data class Period(val start: LocalDate, val end: LocalDate) {
        fun contains(date: LocalDate): Boolean {
            return !date.isBefore(start) && !date.isAfter(end)
        }
    }

    fun invoke(
        completions: List<Completion>,
        frequency: HabitFrequency = HabitFrequency.Daily,
        createdAtMillis: Long = System.currentTimeMillis() // Needed for EveryXDays anchor
    ): StreakInfo {
        if (completions.isEmpty()) {
            return StreakInfo(0, 0, false)
        }

        val completedDates = completions
            .filter { it.isCompleted }
            .map { LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) }
            .sorted()
            .distinct()

        if (completedDates.isEmpty()) {
            return StreakInfo(0, 0, false)
        }

        val today = LocalDate.now()
        val oldestDate = minOf(completedDates.first(), LocalDate.ofEpochDay(createdAtMillis / 86400000))
        
        val periods = generatePeriods(oldestDate, today, frequency)
        if (periods.isEmpty()) {
             return StreakInfo(0, 0, false)
        }

        // Check if completed in the current period (the last period in the list is always the current one)
        val currentPeriod = periods.last()
        val isCompletedToday = completedDates.any { currentPeriod.contains(it) }

        var currentStreak = 0
        // We start checking from the current period. 
        // If the current period is NOT completed, but it's not over yet (e.g. Weekly, we are on Tuesday), 
        // does it break the streak? 
        // We allow the current period to be incomplete (the user still has time).
        // But if the previous period is incomplete, the streak is broken.
        
        var streakIndex = periods.size - 1
        
        if (!isCompletedToday) {
            // User hasn't completed it this period. We just skip this period and see if they completed the previous ones.
            streakIndex--
        }

        while (streakIndex >= 0) {
            val period = periods[streakIndex]
            val hasCompletion = completedDates.any { period.contains(it) }
            if (hasCompletion) {
                currentStreak++
                streakIndex--
            } else {
                break
            }
        }

        // Longest streak calculation
        var longestStreak = 0
        var tempStreak = 0
        for (i in periods.indices) {
            val period = periods[i]
            val hasCompletion = completedDates.any { period.contains(it) }
            if (hasCompletion) {
                tempStreak++
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
            } else {
                tempStreak = 0
            }
        }
        
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        return StreakInfo(currentStreak, longestStreak, isCompletedToday)
    }

    private fun generatePeriods(start: LocalDate, end: LocalDate, frequency: HabitFrequency): List<Period> {
        val periods = mutableListOf<Period>()
        var current = start

        when (frequency) {
            is HabitFrequency.Daily -> {
                while (!current.isAfter(end)) {
                    periods.add(Period(current, current))
                    current = current.plusDays(1)
                }
            }
            is HabitFrequency.Weekdays -> {
                while (!current.isAfter(end)) {
                    if (current.dayOfWeek.value in 1..5) {
                        periods.add(Period(current, current))
                    }
                    current = current.plusDays(1)
                }
            }
            is HabitFrequency.Weekends -> {
                while (!current.isAfter(end)) {
                    if (current.dayOfWeek.value in 6..7) {
                        periods.add(Period(current, current))
                    }
                    current = current.plusDays(1)
                }
            }
            is HabitFrequency.Custom -> {
                while (!current.isAfter(end)) {
                    if (frequency.daysOfWeek.contains(current.dayOfWeek)) {
                        periods.add(Period(current, current))
                    }
                    current = current.plusDays(1)
                }
            }
            is HabitFrequency.Weekly -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                // Align start to the first day of the week
                var periodStart = start.with(weekFields.dayOfWeek(), 1)
                while (!periodStart.isAfter(end)) {
                    val periodEnd = periodStart.plusDays(6)
                    periods.add(Period(periodStart, periodEnd))
                    periodStart = periodStart.plusWeeks(1)
                }
            }
            is HabitFrequency.Monthly -> {
                var periodMonth = YearMonth.from(start)
                val endMonth = YearMonth.from(end)
                while (!periodMonth.isAfter(endMonth)) {
                    periods.add(Period(periodMonth.atDay(1), periodMonth.atEndOfMonth()))
                    periodMonth = periodMonth.plusMonths(1)
                }
            }
            is HabitFrequency.EveryXDays -> {
                val interval = frequency.days.coerceAtLeast(1)
                var periodStart = start
                while (!periodStart.isAfter(end)) {
                    val periodEnd = periodStart.plusDays(interval.toLong() - 1)
                    periods.add(Period(periodStart, periodEnd))
                    periodStart = periodStart.plusDays(interval.toLong())
                }
            }
        }
        
        // Ensure that today is included in the last period if it falls in a gap (though our logic above shouldn't leave gaps, except maybe Weekdays/Weekends/Custom).
        // For Weekdays/Weekends/Custom, if today is NOT a scheduled day, we should still return the periods so that `isCompletedToday` logic works, but wait:
        // If today is Saturday, and frequency is Weekdays, today is NOT in the periods list.
        // So `periods.last()` would be Friday. 
        // If they completed Friday, currentStreak includes Friday.
        // `isCompletedToday` checks if they completed the `periods.last()` which is Friday. This might be slightly incorrect UI-wise (it would show completed today, even though it's Saturday).
        // Let's refine this in UI layer. For streaks, this is mathematically correct.
        
        if (periods.isEmpty()) {
            periods.add(Period(end, end)) // Fallback
        }

        return periods
    }
}
