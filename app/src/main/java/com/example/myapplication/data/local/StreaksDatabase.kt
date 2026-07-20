package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.CompletionDao
import com.example.myapplication.data.local.dao.HabitDao
import com.example.myapplication.data.local.entity.CompletionEntity
import com.example.myapplication.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CompletionEntity::class],
    version = 4,
    exportSchema = false
)
abstract class StreaksDatabase : RoomDatabase() {
    abstract val habitDao: HabitDao
    abstract val completionDao: CompletionDao
}
