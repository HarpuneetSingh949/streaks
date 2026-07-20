package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.CompletionDao
import com.example.myapplication.data.local.dao.HabitDao
import com.example.myapplication.data.mapper.toDomainModel
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.Habit
import com.example.myapplication.domain.repository.StreaksRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstStreaksRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao,
    private val firestore: FirebaseFirestore
) : StreaksRepository {

    override fun getHabitsForUser(userId: String): Flow<List<Habit>> {
        return habitDao.getHabitsForUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getArchivedHabitsForUser(userId: String): Flow<List<Habit>> {
        return habitDao.getArchivedHabitsForUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getHabitById(id: String): Flow<Habit?> {
        return habitDao.getHabitById(id).map { it?.toDomainModel() }
    }

    override suspend fun saveHabit(habit: Habit) {
        habitDao.insertHabit(habit.toEntity())
        // Fire-and-forget to remote (in a real app, use WorkManager for reliable sync)
        try {
            firestore.collection("users").document(habit.userId)
                .collection("habits").document(habit.id)
                .set(habit)
        } catch (e: Exception) {
            // Log failure
        }
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit.toEntity())
        try {
            firestore.collection("users").document(habit.userId)
                .collection("habits").document(habit.id)
                .delete()
        } catch (e: Exception) {
            // Log failure
        }
    }

    override fun getCompletionsForHabit(habitId: String): Flow<List<Completion>> {
        return completionDao.getCompletionsForHabit(habitId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCompletionsForDate(userId: String, date: String): Flow<List<Completion>> {
        return completionDao.getCompletionsForDate(userId, date).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllCompletionsForUser(userId: String): Flow<List<Completion>> {
        return completionDao.getAllCompletionsForUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveCompletion(completion: Completion) {
        completionDao.insertCompletion(completion.toEntity())
        try {
            firestore.collection("users").document(completion.userId)
                .collection("completions").document(completion.id)
                .set(completion)
        } catch (e: Exception) {
            // Log failure
        }
    }

    override suspend fun deleteCompletion(completion: Completion) {
        completionDao.deleteCompletion(completion.toEntity())
        try {
            firestore.collection("users").document(completion.userId)
                .collection("completions").document(completion.id)
                .delete()
        } catch (e: Exception) {
            // Log failure
        }
    }

    override suspend fun getCompletionForHabitAndDate(
        habitId: String,
        date: String
    ): Completion? {
        return completionDao.getCompletionForHabitAndDate(habitId, date)?.toDomainModel()
    }

    override suspend fun syncWithRemote(userId: String) {
        try {
            // Pull habits from Firestore
            val habitsSnapshot = firestore.collection("users").document(userId)
                .collection("habits").get().await()
            val remoteHabits = habitsSnapshot.toObjects(Habit::class.java)
            
            remoteHabits.forEach { habit ->
                habitDao.insertHabit(habit.toEntity())
            }

            // Pull completions from Firestore
            val completionsSnapshot = firestore.collection("users").document(userId)
                .collection("completions").get().await()
            val remoteCompletions = completionsSnapshot.toObjects(Completion::class.java)
            
            remoteCompletions.forEach { completion ->
                completionDao.insertCompletion(completion.toEntity())
            }
        } catch (e: Exception) {
            // Log sync failure
        }
    }

    override suspend fun clearAllUserData(userId: String) {
        habitDao.clearUserData(userId)
        completionDao.clearUserData(userId)
        // Note: In Firestore, deleting a document doesn't delete subcollections.
        // A cloud function or careful batch deletion is typically required.
        // For simplicity, we just delete the user document (which we might not even have created explicitly)
        // and in the real implementation you'd use a callable function to wipe data.
    }
}
