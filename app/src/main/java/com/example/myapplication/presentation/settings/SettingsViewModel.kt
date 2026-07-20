package com.example.myapplication.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.ThemeConfig
import com.example.myapplication.domain.model.UserPreferences
import com.example.myapplication.domain.model.WidgetStyle
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.BackupManager
import com.example.myapplication.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        userPreferencesRepository.userPreferencesFlow.onEach { prefs ->
            _uiState.value = _uiState.value.copy(preferences = prefs)
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SignOut -> {
                viewModelScope.launch {
                    authRepository.signOut()
                    _uiState.value = _uiState.value.copy(isSignedOut = true)
                }
            }
            is SettingsEvent.DeleteAccount -> {
                viewModelScope.launch {
                    authRepository.deleteAccount()
                    _uiState.value = _uiState.value.copy(isSignedOut = true)
                }
            }
            is SettingsEvent.SetTheme -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateThemeConfig(event.themeConfig)
                }
            }
            is SettingsEvent.ToggleDynamicColors -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateDynamicColors(event.enabled)
                }
            }
            is SettingsEvent.ToggleAnimations -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateAnimationsEnabled(event.enabled)
                }
            }
            is SettingsEvent.ToggleNotifications -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateNotificationsEnabled(event.enabled)
                }
            }
            is SettingsEvent.SetWidgetStyle -> {
                viewModelScope.launch {
                    userPreferencesRepository.updateWidgetStyle(event.style)
                }
            }
            is SettingsEvent.ExportJson -> {
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    backupManager.exportToJson(event.uri).onSuccess {
                        _uiState.value = _uiState.value.copy(isLoading = false, message = "Export successful")
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(isLoading = false, message = it.message)
                    }
                }
            }
            is SettingsEvent.ImportJson -> {
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    backupManager.importFromJson(event.uri).onSuccess {
                        _uiState.value = _uiState.value.copy(isLoading = false, message = "Import successful")
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(isLoading = false, message = it.message)
                    }
                }
            }
            is SettingsEvent.ExportCsv -> {
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    backupManager.exportToCsv(event.uri).onSuccess {
                        _uiState.value = _uiState.value.copy(isLoading = false, message = "CSV Export successful")
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(isLoading = false, message = it.message)
                    }
                }
            }
            is SettingsEvent.ClearMessage -> {
                _uiState.value = _uiState.value.copy(message = null)
            }
        }
    }
}

data class SettingsUiState(
    val isSignedOut: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val message: String? = null
)

sealed class SettingsEvent {
    object SignOut : SettingsEvent()
    object DeleteAccount : SettingsEvent()
    data class SetTheme(val themeConfig: ThemeConfig) : SettingsEvent()
    data class ToggleDynamicColors(val enabled: Boolean) : SettingsEvent()
    data class ToggleAnimations(val enabled: Boolean) : SettingsEvent()
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent()
    data class SetWidgetStyle(val style: WidgetStyle) : SettingsEvent()
    data class ExportJson(val uri: Uri) : SettingsEvent()
    data class ImportJson(val uri: Uri) : SettingsEvent()
    data class ExportCsv(val uri: Uri) : SettingsEvent()
    object ClearMessage : SettingsEvent()
}
