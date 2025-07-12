package com.vtu.translate

import android.app.Application
import com.vtu.translate.data.repository.GroqRepository
import com.vtu.translate.data.repository.LogRepository
import com.vtu.translate.data.repository.PreferencesRepository
import com.vtu.translate.data.repository.TranslationRepository

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
        translationRepository = TranslationRepository(groqRepository, logRepository, this)
    }
}