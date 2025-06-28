package com.vtu.translate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val apiKey = stringPreferencesKey("api_key")
    private val geminiModel = stringPreferencesKey("gemini_model")
    private val deepSeekModel = stringPreferencesKey("deepseek_model")

    val getApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[apiKey] ?: ""
    }

    val getGeminiModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[geminiModel] ?: "google/gemini-2.0-flash-exp:free"
    }

    val getDeepSeekModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[deepSeekModel] ?: "deepseek/deepseek-r1-0528:free"
    }

    suspend fun saveSettings(apiKey: String, geminiModel: String, deepSeekModel: String) {
        context.dataStore.edit { settings ->
            settings[this.apiKey] = apiKey
            settings[this.geminiModel] = geminiModel
            settings[this.deepSeekModel] = deepSeekModel
        }
    }
} 