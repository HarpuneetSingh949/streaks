package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.ThemeConfig
import com.example.myapplication.domain.model.UserPreferences
import com.example.myapplication.domain.model.WidgetStyle
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun updateThemeConfig(themeConfig: ThemeConfig)
    suspend fun updateDynamicColors(useDynamicColors: Boolean)
    suspend fun updateAnimationsEnabled(enabled: Boolean)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateWidgetStyle(style: WidgetStyle)
}
