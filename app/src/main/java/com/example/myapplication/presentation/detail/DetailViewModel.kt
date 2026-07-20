package com.example.myapplication.presentation.detail

import androidx.lifecycle.SavedStateHandle
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

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val streaksRepository: StreaksRepository,
    private val authRepository: AuthRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val habitId: String = checkNotNull(savedStateHandle["habitId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadHabitData()
    }

    private fun loadHabitData() {
        viewModelScope.launch {
            combine(
                streaksRepository.getHabitById(habitId),
                streaksRepository.getCompletionsForHabit(habitId)
            ) { habit, completions ->
                if (habit == null) {
                    DetailUiState.Error("Habit not found")
                } else {
                    val streakInfo = calculateStreakUseCase.invoke(
                        completions = completions,
                        frequency = habit.frequency,
                        createdAtMillis = habit.createdAt
                    )
                    val completedDates = completions
                        .filter { it.isCompleted }
                        .map { LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) }
                        .toSet()

                    val totalCompletions = completions.count { it.isCompleted }
                    val completionPercentage = if (completions.isNotEmpty()) {
                        (totalCompletions.toFloat() / completions.size) * 100
                    } else 0f

                    DetailUiState.Success(
                        habit = habit,
                        currentStreak = streakInfo.currentStreak,
                        longestStreak = streakInfo.longestStreak,
                        isCompletedToday = streakInfo.isCompletedToday,
                        completedDates = completedDates,
                        totalCompletions = totalCompletions,
                        completionPercentage = completionPercentage
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleCompletion() {
        viewModelScope.launch {
            val userId = authRepository.currentUserUid ?: return@launch
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            val currentState = uiState.value as? DetailUiState.Success ?: return@launch
            val isCompleted = !currentState.isCompletedToday
            
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
            }
            // Update Widget
            com.example.myapplication.widget.StreaksWidget().updateAll(context)
            com.example.myapplication.widget.StreaksMinimalWidget().updateAll(context)
        }
    }
    
    fun deleteHabit(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val currentState = uiState.value as? DetailUiState.Success ?: return@launch
            streaksRepository.deleteHabit(currentState.habit)
            // Update Widget
            com.example.myapplication.widget.StreaksWidget().updateAll(context)
            com.example.myapplication.widget.StreaksMinimalWidget().updateAll(context)
            onDeleted()
        }
    }
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val habit: Habit,
        val currentStreak: Int,
        val longestStreak: Int,
        val isCompletedToday: Boolean,
        val completedDates: Set<LocalDate>,
        val totalCompletions: Int,
        val completionPercentage: Float
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}
