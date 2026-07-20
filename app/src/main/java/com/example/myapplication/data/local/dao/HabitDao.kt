package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getHabitsForUser(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedHabitsForUser(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: String): Flow<HabitEntity?>
    
    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsOneShot(): List<HabitEntity>
    
    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun clearUserData(userId: String)
}
