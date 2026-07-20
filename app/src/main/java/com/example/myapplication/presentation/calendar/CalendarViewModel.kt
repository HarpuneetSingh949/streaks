package com.example.myapplication.presentation.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Completion
import com.example.myapplication.domain.model.Habit
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import androidx.glance.appwidget.updateAll

data class DailyDetail(
    val habit: Habit,
    val isCompleted: Boolean,
    val completionId: String? // To delete or update existing completion
)

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(
        val allHabits: List<Habit>,
        val completionsByDate: Map<String, List<Completion>>,
        val heatmapIntensity: Map<String, Int>, // Date string -> Completion Count
        val selectedDateDetails: List<DailyDetail>,
        val selectedDate: String?
    ) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val streaksRepository: StreaksRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val selectedDateFlow = MutableStateFlow<String?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        val userId = authRepository.currentUserUid
        if (userId == null) {
            _uiState.value = CalendarUiState.Error("Please sign in.")
            return
        }

        combine(
            streaksRepository.getHabitsForUser(userId),
            streaksRepository.getAllCompletionsForUser(userId),
            selectedDateFlow
        ) { habits, completions, selectedDate ->
            val validCompletions = completions.filter { it.isCompleted }
            val completionsByDate = validCompletions.groupBy { it.date }
            
            val intensityMap = completionsByDate.mapValues { it.value.size }

            val details = if (selectedDate != null) {
                val dateCompletions = completions.filter { it.date == selectedDate }
                habits.map { habit ->
                    val completion = dateCompletions.find { it.habitId == habit.id }
                    DailyDetail(
                        habit = habit,
                        isCompleted = completion?.isCompleted == true,
                        completionId = completion?.id
                    )
                }
            } else {
                emptyList()
            }

            CalendarUiState.Success(
                allHabits = habits,
                completionsByDate = completions.groupBy { it.date }, // Including false for reference if needed
                heatmapIntensity = intensityMap,
                selectedDateDetails = details,
                selectedDate = selectedDate
            )
        }
        .catch { e ->
            _uiState.value = CalendarUiState.Error(e.message ?: "Unknown error")
        }
        .onEach { _uiState.value = it }
        .launchIn(viewModelScope)
    }

    fun selectDate(date: String?) {
        selectedDateFlow.value = date
    }

    fun toggleCompletion(habitId: String, date: String, isCompleted: Boolean) {
        val userId = authRepository.currentUserUid ?: return
        viewModelScope.launch {
            val existing = streaksRepository.getCompletionForHabitAndDate(habitId, date)
            if (existing != null) {
                if (!isCompleted) {
                    streaksRepository.deleteCompletion(existing)
                } else {
                    streaksRepository.saveCompletion(existing.copy(isCompleted = true, updatedAt = System.currentTimeMillis()))
                }
            } else if (isCompleted) {
                val newCompletion = Completion(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    userId = userId,
                    date = date,
                    isCompleted = true,
                    updatedAt = System.currentTimeMillis()
                )
                streaksRepository.saveCompletion(newCompletion)
            }
            
            // Trigger Widget Update
            com.example.myapplication.widget.StreaksWidget().updateAll(context)
            com.example.myapplication.widget.StreaksMinimalWidget().updateAll(context)
        }
    }
}
