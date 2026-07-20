package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.CompletionEntity
import com.example.myapplication.data.local.entity.HabitEntity
import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.Habit

fun HabitEntity.toDomainModel(): Habit {
    return Habit(
        id = id,
        userId = userId,
        name = name,
        emoji = emoji,
        color = color,
        dailyGoal = dailyGoal,
        notes = notes,
        reminderTime = reminderTime,
        category = try { com.example.myapplication.domain.model.HabitCategory.valueOf(category) } catch (e: Exception) { com.example.myapplication.domain.model.HabitCategory.CUSTOM },
        frequency = com.example.myapplication.domain.model.HabitFrequency.fromJsonString(frequency),
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        userId = userId,
        name = name,
        emoji = emoji,
        color = color,
        dailyGoal = dailyGoal,
        notes = notes,
        reminderTime = reminderTime,
        category = category.name,
        frequency = frequency.toJsonString(),
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CompletionEntity.toDomainModel(): Completion {
    return Completion(
        id = id,
        habitId = habitId,
        userId = userId,
        date = date,
        isCompleted = isCompleted,
        mood = mood,
        notes = notes,
        energyLevel = energyLevel,
        updatedAt = updatedAt
    )
}

fun Completion.toEntity(): CompletionEntity {
    return CompletionEntity(
        id = id,
        habitId = habitId,
        userId = userId,
        date = date,
        isCompleted = isCompleted,
        mood = mood,
        notes = notes,
        energyLevel = energyLevel,
        updatedAt = updatedAt
    )
}
