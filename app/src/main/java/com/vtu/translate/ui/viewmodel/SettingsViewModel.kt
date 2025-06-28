package com.vtu.translate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vtu.translate.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val apiKey: StateFlow<String?> = settingsRepository.getApiKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedModel: StateFlow<String?> = settingsRepository.getSelectedModel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableModels = settingsRepository.getAvailableModels()

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsRepository.saveApiKey(apiKey)
        }
    }

    fun saveSelectedModel(model: String) {
        viewModelScope.launch {
            settingsRepository.saveSelectedModel(model)
        }
    }

    companion object {
        fun provideFactory(
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository) as T
            }
        }
    }
} 