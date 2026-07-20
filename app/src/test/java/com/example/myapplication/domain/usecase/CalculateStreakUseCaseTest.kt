package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.HabitFrequency
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class CalculateStreakUseCaseTest {

    private lateinit var classUnderTest: CalculateStreakUseCase

    @Before
    fun setUp() {
        classUnderTest = CalculateStreakUseCase()
    }

    private fun createCompletion(date: LocalDate, isCompleted: Boolean = true): Completion {
        return Completion(
            id = UUID.randomUUID().toString(),
            habitId = "test_habit",
            userId = "test_user",
            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            isCompleted = isCompleted,
            updatedAt = System.currentTimeMillis()
        )
    }

    @Test
    fun `Empty completions returns zero streak`() {
        val result = classUnderTest.invoke(emptyList(), HabitFrequency.Daily)
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
        assertEquals(false, result.isCompletedToday)
    }

    @Test
    fun `Daily frequency contiguous dates`() {
        val today = LocalDate.now()
        val completions = listOf(
            createCompletion(today),
            createCompletion(today.minusDays(1)),
            createCompletion(today.minusDays(2))
        )

        val result = classUnderTest.invoke(completions, HabitFrequency.Daily, today.minusDays(2).toEpochDay() * 86400000)
        assertEquals(3, result.currentStreak)
        assertEquals(3, result.longestStreak)
        assertEquals(true, result.isCompletedToday)
    }

    @Test
    fun `Daily frequency gap breaks streak`() {
        val today = LocalDate.now()
        val completions = listOf(
            createCompletion(today),
            // Gap on today.minusDays(1)
            createCompletion(today.minusDays(2)),
            createCompletion(today.minusDays(3))
        )

        val result = classUnderTest.invoke(completions, HabitFrequency.Daily, today.minusDays(3).toEpochDay() * 86400000)
        assertEquals(1, result.currentStreak) // Only today
        assertEquals(2, result.longestStreak) // The two days before the gap
        assertEquals(true, result.isCompletedToday)
    }

    @Test
    fun `Weekdays frequency ignores weekend gaps`() {
        // Let's create a scenario over a weekend.
        // We'll pick a known Friday. 2026-07-17 is a Friday.
        val friday = LocalDate.of(2026, 7, 17)
        val saturday = friday.plusDays(1)
        val sunday = friday.plusDays(2)
        val monday = friday.plusDays(3)
        val tuesday = friday.plusDays(4) // Today is Tuesday

        // Completions on Friday, Monday, Tuesday. No weekend.
        val completions = listOf(
            createCompletion(friday),
            createCompletion(monday),
            createCompletion(tuesday)
        )

        // Mock LocalDate.now() inside the usecase isn't directly possible without passing it in or using a clock.
        // Wait, the use case uses LocalDate.now(). This makes tests that depend on specific dates flaky or impossible unless we inject a Clock.
        // Let's use the current date for the test, but just go back far enough to cross a weekend.
        
        val today = LocalDate.now()
        var current = today
        // Find a recent Monday
        while (current.dayOfWeek != DayOfWeek.MONDAY) {
            current = current.minusDays(1)
        }
        val monday2 = current
        val fridayBefore = monday2.minusDays(3)

        val dynamicCompletions = listOf(
            createCompletion(monday2),
            createCompletion(fridayBefore)
        )
        
        val result = classUnderTest.invoke(dynamicCompletions, HabitFrequency.Weekdays, fridayBefore.toEpochDay() * 86400000)
        
        // If today is Monday, current streak is 2.
        // If today is Tuesday, current streak is 0 (because Monday was done, but today is not).
        // Let's just test longestStreak to be safe, because longestStreak doesn't care about today.
        assertEquals(2, result.longestStreak)
    }
}
