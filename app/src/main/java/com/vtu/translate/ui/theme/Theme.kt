package com.vtu.translate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.vtu.translate.data.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Primary80,
    primaryContainer = PrimaryContainer80,
    secondary = Secondary80,
    secondaryContainer = SecondaryContainer80,
    tertiary = Tertiary80,
    tertiaryContainer = TertiaryContainer80,
    error = Error80,
    background = Background80,
    surface = Surface80,
    surfaceVariant = SurfaceVariant80
)

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    primaryContainer = PrimaryContainer40,
    secondary = Secondary40,
    secondaryContainer = SecondaryContainer40,
    tertiary = Tertiary40,
    tertiaryContainer = TertiaryContainer40,
    error = Error40,
    background = Background40,
    surface = Surface40,
    surfaceVariant = SurfaceVariant40
)


@Composable
fun VTUTranslateTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic colors to use our custom dark theme
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> darkTheme
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
