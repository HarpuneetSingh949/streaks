package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habitId"),
        Index(value = ["habitId", "date"], unique = true)
    ]
)
data class CompletionEntity(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val userId: String,
    val date: String,
    val isCompleted: Boolean,
    val mood: String?,
    val notes: String?,
    val energyLevel: Int?,
    val updatedAt: Long
)
