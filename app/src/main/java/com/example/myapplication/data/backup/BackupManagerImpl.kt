package com.example.myapplication.data.backup

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.local.dao.CompletionDao
import com.example.myapplication.data.local.dao.HabitDao
import com.example.myapplication.data.local.entity.CompletionEntity
import com.example.myapplication.data.local.entity.HabitEntity
import com.example.myapplication.domain.repository.BackupManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import javax.inject.Inject

data class BackupData(
    val habits: List<HabitEntity>,
    val completions: List<CompletionEntity>
)

class BackupManagerImpl @Inject constructor(
    private val context: Context,
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao,
    private val gson: Gson
) : BackupManager {

    override suspend fun exportToJson(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val habits = habitDao.getAllHabitsOneShot()
            val completions = completionDao.getAllCompletionsOneShot()

            val backupData = BackupData(habits, completions)
            val jsonString = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importFromJson(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))

            val backupData = gson.fromJson(jsonString, BackupData::class.java)

            // Insert all data
            backupData.habits.forEach { habitDao.insertHabit(it) }
            backupData.completions.forEach { completionDao.insertCompletion(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportToCsv(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val habits = habitDao.getAllHabitsOneShot()
            val completions = completionDao.getAllCompletionsOneShot()

            val csvBuilder = StringBuilder()
            // Habits Header
            csvBuilder.append("Type,Id,Name,Emoji,CreatedAt,Category\n")
            habits.forEach { h ->
                csvBuilder.append("Habit,${h.id},${h.name.replace(",", "")},${h.emoji},${h.createdAt},${h.category}\n")
            }
            
            // Completions Header
            csvBuilder.append("\nType,Id,HabitId,Date,IsCompleted,UpdatedAt\n")
            completions.forEach { c ->
                csvBuilder.append("Completion,${c.id},${c.habitId},${c.date},${c.isCompleted},${c.updatedAt}\n")
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(csvBuilder.toString())
                }
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
