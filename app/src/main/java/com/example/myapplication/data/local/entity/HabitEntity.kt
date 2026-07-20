package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val emoji: String,
    val color: Int,
    val dailyGoal: Int,
    val notes: String,
    val reminderTime: String?,
    val category: String,
    val frequency: String,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
