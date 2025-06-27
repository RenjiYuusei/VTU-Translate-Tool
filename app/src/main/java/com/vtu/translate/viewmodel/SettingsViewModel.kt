package com.vtu.translate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vtu.translate.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val apiKey = settingsRepository.getApiKey.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val selectedModel = settingsRepository.getSelectedModel.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "google/gemini-2.0-flash-exp:free"
    )

    fun saveSettings(apiKey: String, selectedModel: String) {
        viewModelScope.launch {
            settingsRepository.saveSettings(apiKey, selectedModel)
        }
    }
}

class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 