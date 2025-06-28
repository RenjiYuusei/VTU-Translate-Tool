package com.vtu.translate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vtu.translate.VtuTranslateApp
import com.vtu.translate.data.repository.LogRepository
import com.vtu.translate.data.repository.PreferencesRepository
import com.vtu.translate.data.repository.TranslationRepository
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
    private val logRepository: LogRepository
) : ViewModel() {
    
    // Navigation state
    private val _currentTab = MutableStateFlow(NavigationTab.TRANSLATE)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()
    
    // Expose repository flows
    val apiKey = preferencesRepository.apiKey
    val selectedModel = preferencesRepository.selectedModel
    val stringResources = translationRepository.stringResources
    val isTranslating = translationRepository.isTranslating
    val selectedFileName = translationRepository.selectedFileName
    val logs = logRepository.logs
    
    /**
     * Set the current navigation tab
     */
    fun setCurrentTab(tab: NavigationTab) {
        _currentTab.value = tab
    }
    
    /**
     * Save API key
     */
    fun saveApiKey(apiKey: String) {
        preferencesRepository.saveApiKey(apiKey)
    }
    
    /**
     * Save selected model
     */
    fun saveSelectedModel(model: String) {
        preferencesRepository.saveSelectedModel(model)
    }
    
    /**
     * Start translation process
     */
    fun startTranslation() {
        viewModelScope.launch {
            translationRepository.translateAll()
        }
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
    fun getLogsAsText(): String {
        return logRepository.getLogsAsText()
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
                    application.logRepository
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