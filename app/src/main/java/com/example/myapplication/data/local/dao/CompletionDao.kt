package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.CompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity)

    @Update
    suspend fun updateCompletion(completion: CompletionEntity)

    @Delete
    suspend fun deleteCompletion(completion: CompletionEntity)

    @Query("SELECT * FROM completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE userId = :userId AND date = :date")
    fun getCompletionsForDate(userId: String, date: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE userId = :userId ORDER BY date DESC")
    fun getAllCompletionsForUser(userId: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletionForHabitAndDate(habitId: String, date: String): CompletionEntity?
    
    @Query("SELECT * FROM completions")
    suspend fun getAllCompletionsOneShot(): List<CompletionEntity>
    
    @Query("DELETE FROM completions WHERE userId = :userId")
    suspend fun clearUserData(userId: String)
}
