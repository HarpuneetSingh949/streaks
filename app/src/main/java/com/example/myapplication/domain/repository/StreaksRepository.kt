package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.Habit
import kotlinx.coroutines.flow.Flow

interface StreaksRepository {
    fun getHabitsForUser(userId: String): Flow<List<Habit>>
    fun getArchivedHabitsForUser(userId: String): Flow<List<Habit>>
    fun getHabitById(id: String): Flow<Habit?>
    suspend fun saveHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    
    fun getCompletionsForHabit(habitId: String): Flow<List<Completion>>
    fun getCompletionsForDate(userId: String, date: String): Flow<List<Completion>>
    fun getAllCompletionsForUser(userId: String): Flow<List<Completion>>
    suspend fun saveCompletion(completion: Completion)
    suspend fun deleteCompletion(completion: Completion)
    suspend fun getCompletionForHabitAndDate(habitId: String, date: String): Completion?
    
    suspend fun syncWithRemote(userId: String)
    suspend fun clearAllUserData(userId: String)
}
