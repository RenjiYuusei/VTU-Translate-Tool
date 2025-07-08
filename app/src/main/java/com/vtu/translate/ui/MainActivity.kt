package com.vtu.translate.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.vtu.translate.VtuTranslateApp
import com.vtu.translate.service.TranslationService
import com.vtu.translate.ui.navigation.AppNavigation
import com.vtu.translate.ui.theme.VTUTranslateTheme
import com.vtu.translate.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application as VtuTranslateApp)
    }
    
    private var currentLanguage: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Check and restart translation service if needed
            lifecycleScope.launch {
                try {
                    val isBackgroundEnabled = viewModel.isBackgroundTranslationEnabled.first()
                    if (isBackgroundEnabled) {
                        // Restart the service to ensure notification is showing
                        val serviceIntent = Intent(this@MainActivity, TranslationService::class.java)
                        startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error checking translation service", e)
                }
            }
            
            // Apply saved language setting on startup only
            lifecycleScope.launch {
                try {
                    val savedLanguage = viewModel.appLanguage.first()
                    if (currentLanguage != savedLanguage) {
                        currentLanguage = savedLanguage
                        applyLanguageConfiguration(savedLanguage)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error applying saved language", e)
                    // Apply default language if error occurs
                    if (currentLanguage != "vi") {
                        currentLanguage = "vi"
                        applyLanguageConfiguration("vi")
                    }
                }
            }
            
            // Listen for language changes
            lifecycleScope.launch {
                try {
                    viewModel.appLanguage.collect { languageCode ->
                        if (currentLanguage != languageCode) {
                            currentLanguage = languageCode
                            applyLanguage(languageCode)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error listening for language changes", e)
                }
            }
            
            setContent {
                val themeMode by viewModel.themeMode.collectAsState()
                
                VTUTranslateTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(viewModel)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            // If all else fails, try to create a basic UI
            setContent {
                VTUTranslateTheme(themeMode = com.vtu.translate.data.model.ThemeMode.DARK) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Minimal error UI
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "Error loading app. Please restart.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Apply language configuration without recreating activity
     */
    private fun applyLanguageConfiguration(languageCode: String) {
        val locale = when (languageCode) {
            "system" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    resources.configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    resources.configuration.locale
                }
            }
            "vi" -> Locale("vi")
            "en" -> Locale("en")
            else -> Locale("en") // Default to English
        }
        
        // Set default locale
        Locale.setDefault(locale)
        
        // Update configuration without recreating
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    /**
     * Apply language setting to the app with recreation
     */
    private fun applyLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "system" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    resources.configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    resources.configuration.locale
                }
            }
            "vi" -> Locale("vi")
            "en" -> Locale("en")
            else -> Locale("en") // Default to English
        }
        
        // Set default locale
        Locale.setDefault(locale)
        
        // Update configuration
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)
            // Update resources
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        
        // Recreate activity to apply language changes
        recreate()
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    
    Scaffold { paddingValues ->
        AppNavigation(
            viewModel = viewModel,
            currentTab = currentTab,
            onTabSelected = { viewModel.setCurrentTab(it) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}