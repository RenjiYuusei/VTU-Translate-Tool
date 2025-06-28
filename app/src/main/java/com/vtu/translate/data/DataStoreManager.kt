package com.vtu.translate.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "api_key_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val SELECTED_TARGET_LANGUAGE = stringPreferencesKey("selected_target_language")
    }

    val getApiKey: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY]
        }

    val getSelectedModel: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_MODEL]
        }

    val getSelectedTargetLanguage: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_TARGET_LANGUAGE]
        }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit {
            it[API_KEY] = apiKey
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit {
            it[SELECTED_MODEL] = model
        }
    }

    suspend fun saveSelectedTargetLanguage(language: String) {
        context.dataStore.edit {
            it[SELECTED_TARGET_LANGUAGE] = language
        }
    }
}
