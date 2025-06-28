package com.vtu.translate.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecureStorage(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "encrypted_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun saveApiKey(apiKey: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
        }
    }

    suspend fun getApiKey(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(KEY_API_KEY, null)
        }
    }

    suspend fun saveSelectedModel(model: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_SELECTED_MODEL, model).apply()
        }
    }

    suspend fun getSelectedModel(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(KEY_SELECTED_MODEL, null)
        }
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
    }
} 