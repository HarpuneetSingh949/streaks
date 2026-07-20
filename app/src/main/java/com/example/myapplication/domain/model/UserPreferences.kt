package com.example.myapplication.domain.model

enum class ThemeConfig {
    SYSTEM_DEFAULT,
    LIGHT,
    DARK;
    
    val displayName: String
        get() = when (this) {
            SYSTEM_DEFAULT -> "System Default"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
}

enum class WidgetStyle {
    HEATMAP,
    MINIMAL;
    
    val displayName: String
        get() = when (this) {
            HEATMAP -> "GitHub Heatmap"
            MINIMAL -> "Minimal"
        }
}

data class UserPreferences(
    val themeConfig: ThemeConfig = ThemeConfig.SYSTEM_DEFAULT,
    val useDynamicColors: Boolean = true,
    val animationsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val widgetStyle: WidgetStyle = WidgetStyle.HEATMAP
)
