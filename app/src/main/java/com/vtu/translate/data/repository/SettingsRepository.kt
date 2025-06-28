package com.vtu.translate.data.repository

import com.vtu.translate.data.local.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SettingsRepository(private val secureStorage: SecureStorage) {

    suspend fun saveApiKey(apiKey: String) {
        secureStorage.saveApiKey(apiKey)
    }

    fun getApiKey(): Flow<String?> = flow {
        emit(secureStorage.getApiKey())
    }

    suspend fun saveSelectedModel(model: String) {
        secureStorage.saveSelectedModel(model)
    }

    fun getSelectedModel(): Flow<String?> = flow {
        emit(secureStorage.getSelectedModel())
    }

    fun getAvailableModels(): List<Pair<String, List<String>>> {
        return listOf(
            "Google" to listOf(
                "google/gemma-2-27b-it:free",
                "google/gemini-2.0-flash-exp:free"
            ),
            "DeepSeek" to listOf(
                "deepseek/deepseek-r1-0528:free",
                "deepseek/deepseek-chat-v3-0324:free"
            )
        )
    }
} 