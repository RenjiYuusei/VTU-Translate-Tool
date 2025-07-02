package com.vtu.translate

import android.app.Application
import com.vtu.translate.data.repository.GroqRepository
import com.vtu.translate.data.repository.LogRepository
import com.vtu.translate.data.repository.PreferencesRepository
import com.vtu.translate.data.repository.TranslationRepository
import com.vtu.translate.data.util.IconManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    
    lateinit var iconManager: IconManager
        private set
    
    // Coroutine scope for background operations
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize repositories
        preferencesRepository = PreferencesRepository(this)
        groqRepository = GroqRepository(preferencesRepository)
        logRepository = LogRepository()
        translationRepository = TranslationRepository(groqRepository, logRepository, this)
        iconManager = IconManager(this)
        
        // Initialize icon based on current theme
        applicationScope.launch {
            preferencesRepository.themeMode.collect { themeMode ->
                iconManager.changeIcon(themeMode)
            }
        }
    }
}