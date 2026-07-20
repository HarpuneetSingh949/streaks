package com.example.myapplication.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Habit
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val streaksRepository: StreaksRepository
) : ViewModel() {

    private val _archivedHabits = MutableStateFlow<List<Habit>>(emptyList())
    val archivedHabits: StateFlow<List<Habit>> = _archivedHabits.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUserFlow.collectLatest { userId ->
                if (userId != null) {
                    streaksRepository.getArchivedHabitsForUser(userId).collectLatest { habits ->
                        _archivedHabits.value = habits
                    }
                }
            }
        }
    }

    fun restoreHabit(habitId: String) {
        viewModelScope.launch {
            streaksRepository.getHabitById(habitId).first()?.let { habit ->
                streaksRepository.saveHabit(habit.copy(
                    isArchived = false,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }

    fun permanentlyDeleteHabit(habitId: String) {
        viewModelScope.launch {
            streaksRepository.getHabitById(habitId).first()?.let { habit ->
                streaksRepository.deleteHabit(habit)
            }
        }
    }
}
