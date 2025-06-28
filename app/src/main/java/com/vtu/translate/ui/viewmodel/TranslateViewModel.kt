package com.vtu.translate.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vtu.translate.data.model.StringResource
import com.vtu.translate.data.repository.LogRepository
import com.vtu.translate.data.repository.SettingsRepository
import com.vtu.translate.data.repository.TranslationRepository
import com.vtu.translate.util.FileSaver
import com.vtu.translate.util.XmlParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TranslateViewModel(
    private val translationRepository: TranslationRepository,
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository,
    private val xmlParser: XmlParser,
    private val fileSaver: FileSaver
) : ViewModel() {

    private val _uiState = MutableStateFlow<TranslateUiState>(TranslateUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val stringResources = mutableStateListOf<StringResource>()

    fun parseFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = TranslateUiState.Loading
            xmlParser.parse(context, uri).onSuccess {
                stringResources.clear()
                stringResources.addAll(it)
                _uiState.value = TranslateUiState.Success("File parsed successfully.")
            }.onFailure {
                _uiState.value = TranslateUiState.Error("Error parsing file: ${it.message}")
            }
        }
    }

    fun startTranslation() {
        viewModelScope.launch {
            val apiKey = settingsRepository.getApiKey().first()
            val model = settingsRepository.getSelectedModel().first()

            if (apiKey.isNullOrEmpty()) {
                _uiState.value = TranslateUiState.Error("API Key is not set.")
                logRepository.addLog("ERROR", "API Key is not set.")
                return@launch
            }
            if (model.isNullOrEmpty()) {
                _uiState.value = TranslateUiState.Error("AI Model is not selected.")
                logRepository.addLog("ERROR", "AI Model is not selected.")
                return@launch
            }

            _uiState.value = TranslateUiState.Loading
            logRepository.addLog("INFO", "Starting translation of ${stringResources.size} strings with model '$model'.")

            stringResources.forEachIndexed { index, resource ->
                translationRepository.translate(apiKey, model, resource.originalValue)
                    .onSuccess { translatedText ->
                        stringResources[index] = resource.copy(translatedValue = translatedText)
                        logRepository.addLog("SUCCESS", "Translated string key '${resource.name}'.")
                    }
                    .onFailure { error ->
                        logRepository.addLog("ERROR", "Failed to translate string key '${resource.name}'. Error: ${error.message}")
                    }
            }
            _uiState.value = TranslateUiState.Success("Translation complete.")
        }
    }

    fun saveFile(context: Context) {
        viewModelScope.launch {
            _uiState.value = TranslateUiState.Loading
            fileSaver.save(context, stringResources).onSuccess { path ->
                _uiState.value = TranslateUiState.Success("File saved successfully to $path")
                logRepository.addLog("INFO", "File saved to $path")
            }.onFailure {
                val errorMessage = "Failed to save file: ${it.message}"
                _uiState.value = TranslateUiState.Error(errorMessage)
                logRepository.addLog("ERROR", errorMessage)
            }
        }
    }
    
    fun updateTranslatedValue(index: Int, newValue: String) {
        if (index >= 0 && index < stringResources.size) {
            stringResources[index] = stringResources[index].copy(translatedValue = newValue)
        }
    }
}

sealed class TranslateUiState {
    object Idle : TranslateUiState()
    object Loading : TranslateUiState()
    data class Success(val message: String) : TranslateUiState()
    data class Error(val message: String) : TranslateUiState()
}

class TranslateViewModelFactory(
    private val translationRepository: TranslationRepository,
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository,
    private val xmlParser: XmlParser,
    private val fileSaver: FileSaver
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TranslateViewModel(
            translationRepository,
            settingsRepository,
            logRepository,
            xmlParser,
            fileSaver
        ) as T
    }
} 