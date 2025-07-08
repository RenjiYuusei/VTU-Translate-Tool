package com.vtu.translate.data.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.vtu.translate.data.model.ThemeMode

/**
 * Manager class for handling dynamic app icon changes based on theme mode
 */
class IconManager(private val context: Context) {
    
    companion object {
        // Activity aliases for different icons
        private const val ICON_LIGHT = "com.vtu.translate.ui.MainActivityLight"
        private const val ICON_DARK = "com.vtu.translate.ui.MainActivityDark"
        private const val MAIN_ACTIVITY = "com.vtu.translate.ui.MainActivity"
    }
    
    private val packageManager = context.packageManager
    
    /**
     * Change app icon based on selected theme mode
     */
    fun changeIcon(themeMode: ThemeMode) {
        val targetIcon = when (themeMode) {
            ThemeMode.LIGHT -> ICON_LIGHT
            ThemeMode.DARK -> ICON_DARK
            ThemeMode.SYSTEM -> {
                // For system mode, check current system theme and use appropriate icon
                val isSystemDark = android.content.res.Configuration().uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
                if (isSystemDark) ICON_DARK else ICON_LIGHT
            }
        }
        
        // Disable all icons first
        disableAllIcons()
        
        // Enable the target icon
        enableIcon(targetIcon)
    }
    
    /**
     * Disable all activity aliases
     */
    private fun disableAllIcons() {
        val icons = listOf(ICON_LIGHT, ICON_DARK)
        
        icons.forEach { iconAlias ->
            try {
                packageManager.setComponentEnabledSetting(
                    ComponentName(context, iconAlias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                // Icon alias might not exist, ignore error
            }
        }
    }
    
    /**
     * Enable specific icon alias
     */
    private fun enableIcon(iconAlias: String) {
        try {
            packageManager.setComponentEnabledSetting(
                ComponentName(context, iconAlias),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            // Fall back to default if alias doesn't exist
        }
    }
    
    /**
     * Enable main activity (default icon)
     */
    private fun enableMainActivity() {
        try {
            packageManager.setComponentEnabledSetting(
                ComponentName(context, MAIN_ACTIVITY),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            // Main activity should always exist
        }
    }
}
