package com.example.myapplication.presentation.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Habit
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val streaksRepository: StreaksRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddHabitEvent) {
        when (event) {
            is AddHabitEvent.EnteredName -> {
                _uiState.value = _uiState.value.copy(name = event.name)
            }
            is AddHabitEvent.EnteredEmoji -> {
                _uiState.value = _uiState.value.copy(emoji = event.emoji)
            }
            is AddHabitEvent.EnteredNotes -> {
                _uiState.value = _uiState.value.copy(notes = event.notes)
            }
            is AddHabitEvent.SaveHabit -> {
                viewModelScope.launch {
                    val userId = authRepository.currentUserUid ?: return@launch
                    val state = uiState.value
                    
                    if (state.name.isBlank() || state.emoji.isBlank()) {
                        // Show error or something
                        return@launch
                    }

                    val newHabit = Habit(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        name = state.name,
                        emoji = state.emoji,
                        color = 0xFF2196F3.toInt(), // Default blue for now
                        dailyGoal = 1,
                        notes = state.notes,
                        reminderTime = null,
                        category = state.category,
                        frequency = state.frequency,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    streaksRepository.saveHabit(newHabit)
                    com.example.myapplication.widget.StreaksWidget().updateAll(context)
                    _uiState.value = _uiState.value.copy(isSaved = true)
                }
            }
            is AddHabitEvent.SelectedCategory -> {
                _uiState.value = _uiState.value.copy(category = event.category)
            }
            is AddHabitEvent.SelectedFrequency -> {
                _uiState.value = _uiState.value.copy(frequency = event.frequency)
            }
            is AddHabitEvent.ApplyTemplate -> {
                _uiState.value = _uiState.value.copy(
                    name = event.name,
                    emoji = event.emoji,
                    category = event.category
                )
            }
        }
    }
}

data class AddHabitUiState(
    val name: String = "",
    val emoji: String = "📚",
    val notes: String = "",
    val category: com.example.myapplication.domain.model.HabitCategory = com.example.myapplication.domain.model.HabitCategory.CUSTOM,
    val isSaved: Boolean = false,
    val frequency: com.example.myapplication.domain.model.HabitFrequency = com.example.myapplication.domain.model.HabitFrequency.Daily
)

sealed class AddHabitEvent {
    data class EnteredName(val name: String) : AddHabitEvent()
    data class EnteredEmoji(val emoji: String) : AddHabitEvent()
    data class EnteredNotes(val notes: String) : AddHabitEvent()
    data class SelectedCategory(val category: com.example.myapplication.domain.model.HabitCategory) : AddHabitEvent()
    data class SelectedFrequency(val frequency: com.example.myapplication.domain.model.HabitFrequency) : AddHabitEvent()
    data class ApplyTemplate(val name: String, val emoji: String, val category: com.example.myapplication.domain.model.HabitCategory) : AddHabitEvent()
    object SaveHabit : AddHabitEvent()
}

object HabitTemplates {
    val templates = listOf(
        Template("Read a Book", "📚", com.example.myapplication.domain.model.HabitCategory.LEARNING),
        Template("Morning Run", "🏃", com.example.myapplication.domain.model.HabitCategory.FITNESS),
        Template("Drink Water", "💧", com.example.myapplication.domain.model.HabitCategory.HEALTH),
        Template("Meditate", "🧘", com.example.myapplication.domain.model.HabitCategory.HEALTH),
        Template("Code Practice", "💻", com.example.myapplication.domain.model.HabitCategory.LEARNING),
        Template("Save Money", "💰", com.example.myapplication.domain.model.HabitCategory.FINANCE)
    )

    data class Template(val name: String, val emoji: String, val category: com.example.myapplication.domain.model.HabitCategory)
}
