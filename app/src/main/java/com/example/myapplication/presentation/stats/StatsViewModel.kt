package com.example.myapplication.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import com.example.myapplication.domain.usecase.GetStatisticsUseCase
import com.example.myapplication.domain.usecase.StatisticsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(val data: StatisticsData) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val streaksRepository: StreaksRepository,
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        val userId = authRepository.currentUserUid
        if (userId == null) {
            _uiState.value = StatsUiState.Error("Please sign in to view statistics.")
            return
        }

        combine(
            streaksRepository.getHabitsForUser(userId),
            streaksRepository.getAllCompletionsForUser(userId)
        ) { habits, completions ->
            val statsData = getStatisticsUseCase.invoke(habits, completions)
            StatsUiState.Success(statsData)
        }
        .catch { e ->
            _uiState.value = StatsUiState.Error(e.message ?: "An unknown error occurred")
        }
        .onEach { state ->
            _uiState.value = state
        }
        .launchIn(viewModelScope)
    }
}
