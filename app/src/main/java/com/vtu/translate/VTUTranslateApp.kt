package com.vtu.translate

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.core.os.LocaleListCompat
import com.vtu.translate.data.repository.GroqRepository
import com.vtu.translate.data.repository.LogRepository
import com.vtu.translate.data.repository.PreferencesRepository
import com.vtu.translate.data.repository.TranslationRepository
import java.util.Locale

class VtuTranslateApp : Application() {
    
    // Repositories
    lateinit var preferencesRepository: PreferencesRepository
        private set
    
    lateinit var groqRepository: GroqRepository
        private set
    
    lateinit var translationRepository: TranslationRepository
        private set
    
    lateinit var logRepository: LogRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize repositories
        preferencesRepository = PreferencesRepository(this)
        groqRepository = GroqRepository(preferencesRepository)
        logRepository = LogRepository()
        translationRepository = TranslationRepository(groqRepository, logRepository)
        
        // Apply saved language setting
        applyLanguageSetting()
    }
    
    /**
     * Apply the saved language setting
     */
    private fun applyLanguageSetting() {
        val savedLanguage = preferencesRepository.appLanguage.value
        updateLocale(savedLanguage)
    }
    
    /**
     * Update the app locale based on the selected language
     */
    private fun updateLocale(languageCode: String) {
        val locale = when (languageCode) {
            "en" -> Locale("en")
            "vi" -> Locale("vi")
            else -> Locale("vi") // Default to Vietnamese
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    /**
     * Attach base context with the saved locale
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Note: We can't access preferencesRepository here yet, so we apply locale in onCreate
    }
}