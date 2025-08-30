package com.vtu.translate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vtu.translate.VtuTranslateApp
import com.vtu.translate.data.repository.LogRepository
import com.vtu.translate.data.repository.PreferencesRepository
import com.vtu.translate.data.repository.TranslationRepository
import com.vtu.translate.data.repository.GroqRepository
import com.vtu.translate.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the application
 */
class MainViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val translationRepository: TranslationRepository,
    private val logRepository: LogRepository,
    private val groqRepository: GroqRepository,
    private val application: VtuTranslateApp
) : ViewModel() {
    
    // Navigation state
    private val _currentTab = MutableStateFlow(NavigationTab.TRANSLATE)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()
    
    // Expose repository flows
    val apiKey = preferencesRepository.apiKey
    val geminiApiKey = preferencesRepository.geminiApiKey
    val selectedProvider = preferencesRepository.selectedProvider
    val selectedModel = preferencesRepository.selectedModel
    val appLanguage = preferencesRepository.appLanguage
    val isDarkTheme = preferencesRepository.isDarkTheme
    val themeMode = preferencesRepository.themeMode
    val translationSpeed = preferencesRepository.translationSpeed
    val targetLanguage = preferencesRepository.targetLanguage
    val batchSize = preferencesRepository.batchSize
    val stringResources = translationRepository.stringResources
    val isTranslating = translationRepository.isTranslating
    val isParsing = translationRepository.isParsing
    val isBackgroundTranslationEnabled = preferencesRepository.isBackgroundTranslationEnabled
    val selectedFileName = translationRepository.selectedFileName
    val logs = logRepository.logs
    
    // Available models from API
    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()
    
    // Loading state for models
    private val _isLoadingModels = MutableStateFlow(false)
    val isLoadingModels: StateFlow<Boolean> = _isLoadingModels.asStateFlow()
    
    /**
     * Set the current navigation tab
     */
    fun setCurrentTab(tab: NavigationTab) {
        _currentTab.value = tab
    }
    
    /**
     * Save API key (Groq)
     */
    fun saveApiKey(apiKey: String) {
        preferencesRepository.saveApiKey(apiKey)
    }
    
    /**
     * Save Gemini API key
     */
    fun saveGeminiApiKey(apiKey: String) {
        preferencesRepository.saveGeminiApiKey(apiKey)
    }
    
    /**
     * Save selected provider
     */
    fun saveSelectedProvider(provider: String) {
        preferencesRepository.saveSelectedProvider(provider)
    }
    
    /**
     * Save selected model
     */
    fun saveSelectedModel(model: String) {
        preferencesRepository.saveSelectedModel(model)
    }
    
    /**
     * Save app language
     */
    fun saveAppLanguage(language: String) {
        preferencesRepository.saveAppLanguage(language)
    }
    
    /**
     * Save dark theme preference
     */
    fun saveDarkTheme(isDark: Boolean) {
        preferencesRepository.saveDarkTheme(isDark)
    }
    
    /**
     * Save theme mode preference
     */
    fun saveThemeMode(mode: ThemeMode) {
        preferencesRepository.saveThemeMode(mode)
    }
    
    /**
     * Save translation speed preference
     */
    fun saveTranslationSpeed(speed: Int) {
        preferencesRepository.saveTranslationSpeed(speed)
    }
    
    /**
     * Save target language preference
     */
    fun saveTargetLanguage(language: String) {
        preferencesRepository.saveTargetLanguage(language)
    }
    
    /**
     * Save background translation state
     */
    fun saveBackgroundTranslationEnabled(isEnabled: Boolean) {
        preferencesRepository.saveBackgroundTranslationEnabled(isEnabled)
    }

    /**
     * Continue translation from where it was stopped
     */
    fun continueTranslation() {
        viewModelScope.launch {
            val targetLang = targetLanguage.value
            val batchSizePref = batchSize.value
            val speed = translationSpeed.value
            val currentIndex = translationRepository.getCurrentTranslationIndex()
            
            // Không có chuỗi cần dịch -> thoát sớm, tránh khởi chạy service và log dư thừa
            if (currentIndex == -1) {
                logRepository.logInfo("Không có chuỗi cần dịch.")
                return@launch
            }
            
            // Start the service if background translation is enabled
            if (isBackgroundTranslationEnabled.value) {
                val serviceIntent = android.content.Intent(application, com.vtu.translate.service.TranslationService::class.java)
                application.startService(serviceIntent)
            }
            
            translationRepository.translateAll(targetLang, currentIndex, speed, batchSizePref)
        }
    }
    
    /**
     * Get current translation progress
     */
    fun getCurrentTranslationIndex(): Int {
        return translationRepository.getCurrentTranslationIndex()
    }
    
    /**
     * Start translation process
     */
    fun startTranslation() {
        viewModelScope.launch {
            val targetLang = targetLanguage.value
            val speed = translationSpeed.value
            val batchSizePref = batchSize.value
            val currentIndex = translationRepository.getCurrentTranslationIndex()
            
            // Không có chuỗi cần dịch -> thoát sớm
            if (currentIndex == -1) {
                logRepository.logInfo("Không có chuỗi cần dịch.")
                return@launch
            }
            
            // Start the service if background translation is enabled
            if (isBackgroundTranslationEnabled.value) {
                val serviceIntent = android.content.Intent(application, com.vtu.translate.service.TranslationService::class.java)
                application.startService(serviceIntent)
            }
            
            translationRepository.translateAll(targetLang, 0, speed, batchSizePref)
        }
    }
    
    /**
     * Save translated file
     */
    fun saveTranslatedFile() {
        viewModelScope.launch {
            val targetLang = targetLanguage.value
            translationRepository.saveTranslatedFile(targetLang)
        }
    }
    
    /**
     * Stop translation process
     */
    fun stopTranslation() {
        translationRepository.stopTranslation()
    }
    
    /**
     * Update a translated string
     */
    fun updateTranslation(index: Int, translatedValue: String) {
        translationRepository.updateStringResource(index, translatedValue)
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        logRepository.clearLogs()
    }
    
    /**
     * Get all logs as text
     */
    fun getLogsAsText(context: android.content.Context): String {
        return logRepository.getLogsAsText(context)
    }
    
    /**
     * Save batch size preference
     */
    fun saveBatchSize(size: Int) {
        preferencesRepository.saveBatchSize(size)
    }
    
    /**
     * Fetch available models from Groq API
     */
    fun fetchAvailableModels() {
        viewModelScope.launch {
            _isLoadingModels.value = true
            groqRepository.fetchAvailableModels()
                .onSuccess { models ->
                    _availableModels.value = models
                }
                .onFailure { _ ->
                    // Keep empty list on error
                    _availableModels.value = emptyList()
                }
            _isLoadingModels.value = false
        }
    }
    
    /**
     * Factory for creating MainViewModel
     */
    class Factory(private val application: VtuTranslateApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(
                    application.preferencesRepository,
                    application.translationRepository,
                    application.logRepository,
                    application.groqRepository,
                    application
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Navigation tabs for the application
 */
enum class NavigationTab {
    TRANSLATE,
    SETTINGS,
    LOG
}