package com.example.myapplication.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.Habit
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import com.example.myapplication.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import com.example.myapplication.domain.model.HabitCategory
import com.example.myapplication.domain.model.Achievement

enum class SortOrder(val displayName: String) { 
    NAME("Name"), 
    STREAK("Streak"), 
    NEWEST("Newest") 
}

data class HabitUiModel(
    val habit: Habit,
    val currentStreak: Int,
    val longestStreak: Int,
    val isCompletedToday: Boolean
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val streaksRepository: StreaksRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val selectedCategory = MutableStateFlow<HabitCategory?>(null)
    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.NEWEST)

    private val _achievementEvent = MutableStateFlow<Achievement?>(null)
    val achievementEvent: StateFlow<Achievement?> = _achievementEvent.asStateFlow()

    fun clearAchievement() {
        _achievementEvent.value = null
    }

    init {
        viewModelScope.launch {
            authRepository.currentUserFlow.collectLatest { userId ->
                if (userId != null) {
                    loadReactiveData(userId)
                } else {
                    _uiState.value = HomeUiState.Error("User not logged in")
                }
            }
        }
    }

    private fun loadReactiveData(userId: String) {
        combine(
            streaksRepository.getHabitsForUser(userId),
            streaksRepository.getAllCompletionsForUser(userId),
            selectedCategory,
            searchQuery,
            sortOrder
        ) { habits, completions, category, query, sort ->
            val completionsByHabit = completions.filter { it.isCompleted }.groupBy { it.habitId }
            
            // Calculate global progress before any UI filtering
            var totalCount = 0
            var completedCount = 0
            
            val allUiModels = habits.map { habit ->
                val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
                val streakInfo = calculateStreakUseCase.invoke(
                    completions = habitCompletions,
                    frequency = habit.frequency,
                    createdAtMillis = habit.createdAt
                )
                totalCount++
                if (streakInfo.isCompletedToday) {
                    completedCount++
                }
                HabitUiModel(
                    habit = habit,
                    currentStreak = streakInfo.currentStreak,
                    longestStreak = streakInfo.longestStreak,
                    isCompletedToday = streakInfo.isCompletedToday
                )
            }

            var filtered = allUiModels
            
            if (category != null) {
                filtered = filtered.filter { it.habit.category == category }
            }
            
            if (query.isNotBlank()) {
                filtered = filtered.filter { it.habit.name.contains(query, ignoreCase = true) }
            }
            
            val sorted = when(sort) {
                SortOrder.NAME -> filtered.sortedBy { it.habit.name.lowercase() }
                SortOrder.STREAK -> filtered.sortedByDescending { it.currentStreak }
                SortOrder.NEWEST -> filtered.sortedByDescending { it.habit.createdAt }
            }
            
            HomeUiState.Success(sorted, totalCount, completedCount)
        }
        .catch { _uiState.value = HomeUiState.Error(it.message ?: "Error") }
        .onEach { _uiState.value = it }
        .launchIn(viewModelScope)
    }

    fun setCategory(category: HabitCategory?) { selectedCategory.value = category }
    fun setSearchQuery(query: String) { searchQuery.value = query }
    fun setSortOrder(order: SortOrder) { sortOrder.value = order }

    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            streaksRepository.getHabitById(habitId).first()?.let { habit ->
                streaksRepository.saveHabit(habit.copy(
                    isArchived = true,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }

    fun toggleCompletion(habitId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.currentUserUid ?: return@launch
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            val existingCompletion = streaksRepository.getCompletionForHabitAndDate(habitId, today)
            
            if (existingCompletion != null) {
                streaksRepository.saveCompletion(existingCompletion.copy(
                    isCompleted = isCompleted,
                    updatedAt = System.currentTimeMillis()
                ))
            } else {
                val newCompletion = Completion(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    userId = userId,
                    date = today,
                    isCompleted = isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
                streaksRepository.saveCompletion(newCompletion)
                
                // Check for achievements if it was just completed
                if (isCompleted) {
                    val habitCompletions = streaksRepository.getAllCompletionsForUser(userId).first()
                        .filter { it.habitId == habitId && it.isCompleted }
                    // Re-add the new one because it might not be emitted yet
                    val allComps = habitCompletions + newCompletion
                    
                    // We need the Habit object to know the frequency.
                    // We can fetch it from the repository.
                    val habit = streaksRepository.getHabitsForUser(userId).first().find { it.id == habitId }
                    if (habit != null) {
                        val info = calculateStreakUseCase.invoke(
                            completions = allComps,
                            frequency = habit.frequency,
                            createdAtMillis = habit.createdAt
                        )
                        
                        val streak = info.currentStreak
                        if (streak == 7 || streak == 30 || streak == 100 || streak == 365) {
                            _achievementEvent.value = Achievement(habitId, streak)
                        }
                    }
                }
            }
        }
    }

    fun updateJournal(habitId: String, mood: String, notes: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUserUid ?: return@launch
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            val existingCompletion = streaksRepository.getCompletionForHabitAndDate(habitId, today)
            
            if (existingCompletion != null) {
                streaksRepository.saveCompletion(existingCompletion.copy(
                    mood = mood,
                    notes = notes,
                    updatedAt = System.currentTimeMillis()
                ))
            } else {
                // If they add a journal but haven't completed it, maybe auto-complete it?
                val newCompletion = Completion(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    userId = userId,
                    date = today,
                    isCompleted = true,
                    mood = mood,
                    notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
                streaksRepository.saveCompletion(newCompletion)
            }
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val habits: List<HabitUiModel>,
        val totalHabitsCount: Int,
        val completedTodayCount: Int
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
