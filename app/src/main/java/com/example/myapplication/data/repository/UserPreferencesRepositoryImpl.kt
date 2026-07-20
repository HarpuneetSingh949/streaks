package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.domain.model.ThemeConfig
import com.example.myapplication.domain.model.UserPreferences
import com.example.myapplication.domain.model.WidgetStyle
import com.example.myapplication.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val WIDGET_STYLE = stringPreferencesKey("widget_style")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("UserPreferencesRepo", "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeConfig = try {
                ThemeConfig.valueOf(preferences[PreferencesKeys.THEME_CONFIG] ?: ThemeConfig.SYSTEM_DEFAULT.name)
            } catch (e: Exception) {
                ThemeConfig.SYSTEM_DEFAULT
            }
            val useDynamicColors = preferences[PreferencesKeys.USE_DYNAMIC_COLORS] ?: true
            val animationsEnabled = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true
            val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
            val widgetStyle = try {
                WidgetStyle.valueOf(preferences[PreferencesKeys.WIDGET_STYLE] ?: WidgetStyle.HEATMAP.name)
            } catch (e: Exception) {
                WidgetStyle.HEATMAP
            }

            UserPreferences(
                themeConfig = themeConfig,
                useDynamicColors = useDynamicColors,
                animationsEnabled = animationsEnabled,
                notificationsEnabled = notificationsEnabled,
                widgetStyle = widgetStyle
            )
        }

    override suspend fun updateThemeConfig(themeConfig: ThemeConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = themeConfig.name
        }
    }

    override suspend fun updateDynamicColors(useDynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLORS] = useDynamicColors
        }
    }

    override suspend fun updateAnimationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun updateWidgetStyle(style: WidgetStyle) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIDGET_STYLE] = style.name
        }
    }
}
