package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.Habit
import com.example.myapplication.domain.model.HabitCategory
import com.example.myapplication.domain.model.HabitFrequency
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class GetStatisticsUseCaseTest {

    private lateinit var classUnderTest: GetStatisticsUseCase
    private lateinit var streakUseCase: CalculateStreakUseCase

    @Before
    fun setUp() {
        streakUseCase = CalculateStreakUseCase()
        classUnderTest = GetStatisticsUseCase(streakUseCase)
    }

    @Test
    fun `Empty lists return zeroed stats`() {
        val stats = classUnderTest.invoke(emptyList(), emptyList())
        assertEquals(0, stats.totalCompletions)
        assertEquals(0, stats.activeHabits)
        assertEquals(0f, stats.completionPercentage)
    }

    @Test
    fun `Calculates stats correctly`() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        
        val habit1 = Habit(
            id = "habit1",
            userId = "user1",
            name = "Read",
            emoji = "📚",
            color = 0,
            dailyGoal = 1,
            notes = "",
            reminderTime = null,
            category = HabitCategory.LEARNING,
            frequency = HabitFrequency.Daily,
            isArchived = false,
            createdAt = today.minusDays(5).toEpochDay() * 86400000,
            updatedAt = 0
        )

        val completions = listOf(
            Completion(UUID.randomUUID().toString(), "habit1", "user1", today.format(formatter), true, null, null, null, 0),
            Completion(UUID.randomUUID().toString(), "habit1", "user1", today.minusDays(1).format(formatter), true, null, null, null, 0),
            Completion(UUID.randomUUID().toString(), "habit1", "user1", today.minusDays(2).format(formatter), true, null, null, null, 0)
        )

        val stats = classUnderTest.invoke(listOf(habit1), completions)

        assertEquals(3, stats.totalCompletions)
        assertEquals(1, stats.activeHabits)
        assertEquals(3, stats.activeDays)
        assertEquals("Read", stats.mostCompletedHabit)
        assertEquals(3, stats.currentStreakTotal)
        assertEquals(3, stats.longestStreakTotal)
        assertEquals(100f, stats.categoryBreakdown["Learning"] ?: 0f, 0.1f)
    }
}
