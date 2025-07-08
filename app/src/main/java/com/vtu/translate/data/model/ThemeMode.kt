package com.vtu.translate.data.model

/**
 * Enum representing different theme modes for the application
 */
enum class ThemeMode(val id: String, val displayNameRes: Int) {
    LIGHT("light", com.vtu.translate.R.string.theme_light),
    DARK("dark", com.vtu.translate.R.string.theme_dark),
    SYSTEM("system", com.vtu.translate.R.string.theme_system);
    
    companion object {
        fun fromId(id: String): ThemeMode {
            return values().find { it.id == id } ?: DARK
        }
        
    }
}
