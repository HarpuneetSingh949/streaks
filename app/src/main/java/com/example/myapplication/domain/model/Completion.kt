package com.example.myapplication.domain.model

data class Completion(
    val id: String,
    val habitId: String,
    val userId: String,
    val date: String,
    val isCompleted: Boolean,
    val mood: String? = null,
    val notes: String? = null,
    val energyLevel: Int? = null,
    val updatedAt: Long
)
