package com.vtu.translate.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing encrypted preferences
 */
class PreferencesRepository(context: Context) {
    
    companion object {
        private const val PREFERENCES_FILE = "encrypted_prefs.xml"
        private const val KEY_API_KEY = "groq_api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_APP_LANGUAGE = "app_language"
        
        // Default model
        private const val DEFAULT_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"
        
        // Default language (Vietnamese)
        private const val DEFAULT_LANGUAGE = "vi"
    }
    
    private val _apiKey = MutableStateFlow<String>("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    
    private val _selectedModel = MutableStateFlow<String>(DEFAULT_MODEL)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()
    
    private val _appLanguage = MutableStateFlow<String>(DEFAULT_LANGUAGE)
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()
    
    private val encryptedPrefs: SharedPreferences
    
    init {
        // Create or retrieve the Master Key for encryption
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        // Initialize EncryptedSharedPreferences
        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFERENCES_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        // Load saved values
        loadSavedValues()
    }
    
    private fun loadSavedValues() {
        _apiKey.value = encryptedPrefs.getString(KEY_API_KEY, "") ?: ""
        _selectedModel.value = encryptedPrefs.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        _appLanguage.value = encryptedPrefs.getString(KEY_APP_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Save API key to encrypted preferences
     */
    fun saveApiKey(apiKey: String) {
        encryptedPrefs.edit().putString(KEY_API_KEY, apiKey).apply()
        _apiKey.value = apiKey
    }
    
    /**
     * Save selected model to preferences
     */
    fun saveSelectedModel(model: String) {
        encryptedPrefs.edit().putString(KEY_SELECTED_MODEL, model).apply()
        _selectedModel.value = model
    }
    
    /**
     * Save app language to preferences
     */
    fun saveAppLanguage(language: String) {
        encryptedPrefs.edit().putString(KEY_APP_LANGUAGE, language).apply()
        _appLanguage.value = language
    }
}