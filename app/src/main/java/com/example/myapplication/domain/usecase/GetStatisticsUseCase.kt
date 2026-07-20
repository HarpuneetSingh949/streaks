package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class StatisticsData(
    val totalCompletions: Int,
    val activeHabits: Int,
    val missedDays: Int,
    val completionPercentage: Float,
    val averageStreak: Float,
    val consistencyScore: Float,
    val currentStreakTotal: Int,
    val longestStreakTotal: Int,
    val bestWeekday: String?,
    val worstWeekday: String?,
    val mostCompletedHabit: String?,
    val leastCompletedHabit: String?,
    val weeklyCompletionData: List<Float>, // Last 7 days
    val monthlyCompletionData: List<Float>, // Last 30 days
    val yearlyCompletionData: List<Float>, // Last 12 months
    val smartInsight: String?,
    val activeDays: Int,
    val categoryBreakdown: Map<String, Float> // Category Name -> Percentage (0.0 to 100.0)
)

class GetStatisticsUseCase @Inject constructor(
    private val calculateStreakUseCase: CalculateStreakUseCase
) {

    fun invoke(habits: List<Habit>, completions: List<Completion>): StatisticsData {
        if (habits.isEmpty()) {
            return emptyStats()
        }

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        val validCompletions = completions.filter { it.isCompleted }
        
        // 1. Basic Counts
        val totalCompletions = validCompletions.size
        val activeHabits = habits.size

        // 2. Map habits to their completions
        val completionsByHabit = validCompletions.groupBy { it.habitId }

        // 3. Streaks
        var maxCurrentStreak = 0
        var maxLongestStreak = 0
        var sumCurrentStreaks = 0

        habits.forEach { habit ->
            val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
            val streakInfo = calculateStreakUseCase.invoke(
                completions = habitCompletions,
                frequency = habit.frequency,
                createdAtMillis = habit.createdAt
            )
            
            if (streakInfo.currentStreak > maxCurrentStreak) maxCurrentStreak = streakInfo.currentStreak
            if (streakInfo.longestStreak > maxLongestStreak) maxLongestStreak = streakInfo.longestStreak
            sumCurrentStreaks += streakInfo.currentStreak
        }

        val averageStreak = if (activeHabits > 0) sumCurrentStreaks.toFloat() / activeHabits else 0f

        // 4. Most & Least completed
        val sortedHabitsByCompletions = habits.map { habit ->
            habit.name to (completionsByHabit[habit.id]?.size ?: 0)
        }.sortedByDescending { it.second }

        val mostCompletedHabit = sortedHabitsByCompletions.firstOrNull()?.first
        val leastCompletedHabit = sortedHabitsByCompletions.lastOrNull()?.first

        // 5. Completion Percentage & Missed Days
        // Calculate max possible days tracking (from oldest habit)
        val oldestHabitDateMillis = habits.minOfOrNull { it.createdAt } ?: System.currentTimeMillis()
        // Roughly convert millis to LocalDate (timezone naive, but good enough for general stats)
        val oldestHabitDate = LocalDate.ofEpochDay(oldestHabitDateMillis / 86400000L)
        
        // Days since tracking started (minimum 1)
        var daysSinceStart = ChronoUnit.DAYS.between(oldestHabitDate, today).toInt()
        if (daysSinceStart <= 0) daysSinceStart = 1
        
        val possibleCompletions = activeHabits * daysSinceStart
        val completionPercentage = if (possibleCompletions > 0) (totalCompletions.toFloat() / possibleCompletions) * 100 else 0f
        val missedDays = (possibleCompletions - totalCompletions).coerceAtLeast(0)

        // 6. Consistency Score (Last 30 days)
        val thirtyDaysAgo = today.minusDays(30)
        val recentCompletions = validCompletions.count { 
            LocalDate.parse(it.date, formatter).isAfter(thirtyDaysAgo) 
        }
        val possibleRecent = activeHabits * 30
        val consistencyScore = if (possibleRecent > 0) (recentCompletions.toFloat() / possibleRecent) * 100 else 0f

        // 7. Best & Worst Weekday
        val completionsByWeekday = validCompletions.groupBy { 
            LocalDate.parse(it.date, formatter).dayOfWeek 
        }.mapValues { it.value.size }
        
        val allWeekdays = DayOfWeek.values().associateWith { completionsByWeekday[it] ?: 0 }
        val bestWeekday = allWeekdays.maxByOrNull { it.value }?.key?.name?.take(3)
        val worstWeekday = allWeekdays.minByOrNull { it.value }?.key?.name?.take(3)

        // 8. Graph Data
        val weeklyData = (6 downTo 0).map { i ->
            val dateStr = today.minusDays(i.toLong()).format(formatter)
            validCompletions.count { it.date == dateStr }.toFloat()
        }
        
        val monthlyData = (29 downTo 0).map { i ->
            val dateStr = today.minusDays(i.toLong()).format(formatter)
            validCompletions.count { it.date == dateStr }.toFloat()
        }

        val yearlyData = (11 downTo 0).map { i ->
            val monthDate = today.minusMonths(i.toLong())
            validCompletions.count { 
                val d = LocalDate.parse(it.date, formatter)
                d.month == monthDate.month && d.year == monthDate.year
            }.toFloat()
        }
        
        val smartInsight = if (maxCurrentStreak > 0 && maxCurrentStreak < maxLongestStreak) {
            val diff = maxLongestStreak - maxCurrentStreak
            "You're $diff days away from your longest streak!"
        } else if (maxCurrentStreak == maxLongestStreak && maxLongestStreak > 0) {
            "You are currently on your longest streak of $maxLongestStreak days! Keep it up!"
        } else if (maxCurrentStreak == 0 && maxLongestStreak > 0) {
            "Start a new streak today to beat your record of $maxLongestStreak days!"
        } else {
            "Complete a habit today to start a streak!"
        }

        val activeDays = validCompletions.map { it.date }.distinct().size

        // Calculate category breakdown
        val habitCategories = habits.associate { it.id to it.category.displayName }
        val categoryCounts = mutableMapOf<String, Int>()
        validCompletions.forEach { comp ->
            val catName = habitCategories[comp.habitId] ?: "Custom"
            categoryCounts[catName] = categoryCounts.getOrDefault(catName, 0) + 1
        }
        val categoryBreakdown = categoryCounts.mapValues { if (totalCompletions > 0) (it.value.toFloat() / totalCompletions) * 100f else 0f }

        return StatisticsData(
            totalCompletions = totalCompletions,
            activeHabits = activeHabits,
            missedDays = missedDays,
            completionPercentage = completionPercentage,
            averageStreak = averageStreak,
            consistencyScore = consistencyScore,
            currentStreakTotal = maxCurrentStreak,
            longestStreakTotal = maxLongestStreak,
            bestWeekday = bestWeekday,
            worstWeekday = worstWeekday,
            mostCompletedHabit = mostCompletedHabit,
            leastCompletedHabit = leastCompletedHabit,
            weeklyCompletionData = weeklyData,
            monthlyCompletionData = monthlyData,
            yearlyCompletionData = yearlyData,
            smartInsight = smartInsight,
            activeDays = activeDays,
            categoryBreakdown = categoryBreakdown
        )
    }

    private fun emptyStats() = StatisticsData(
        0, 0, 0, 0f, 0f, 0f, 0, 0, null, null, null, null,
        List(7) { 0f }, List(30) { 0f }, List(12) { 0f }, "Create your first habit to get insights!", 0, emptyMap()
    )
}
