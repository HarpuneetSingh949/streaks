package com.example.myapplication.domain.model

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val emoji: String,
    val color: Int,
    val dailyGoal: Int = 1,
    val notes: String = "",
    val reminderTime: String? = null,
    val category: HabitCategory = HabitCategory.CUSTOM,
    val frequency: HabitFrequency = HabitFrequency.Daily,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
