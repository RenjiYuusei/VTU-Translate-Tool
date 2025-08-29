@file:Suppress("DEPRECATION")
package com.vtu.translate.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.vtu.translate.data.model.ThemeMode

/**
 * Repository for managing encrypted preferences
 */
@Suppress("DEPRECATION")
class PreferencesRepository(context: Context) {
    
    companion object {
        private const val PREFERENCES_FILE = "encrypted_prefs.xml"
        private const val KEY_API_KEY = "groq_api_key"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_SELECTED_PROVIDER = "selected_provider"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_TARGET_LANGUAGE = "target_language"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_TRANSLATION_SPEED = "translation_speed"
        private const val KEY_INVERTED_TRANSLATE = "inverted_translate"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_BATCH_SIZE = "batch_size"
        private const val KEY_BACKGROUND_TRANSLATION = "background_translation"
        
        // Default selections
        private const val DEFAULT_PROVIDER = "groq"
        private const val DEFAULT_MODEL = "meta-llama/llama-4-maverick-17b-128e-instruct"
        
        // Default language (Vietnamese)
        private const val DEFAULT_LANGUAGE = "vi"
        
        // Default dark theme (true for dark mode)
        private const val DEFAULT_DARK_THEME = true
        
        // Default translation speed (3 = normal speed)
        private const val DEFAULT_TRANSLATION_SPEED = 3
        
        // Default theme mode
        private const val DEFAULT_THEME_MODE = "dark"
        
        // Default batch size (1 = single translation)
        private const val DEFAULT_BATCH_SIZE = 1
    }
    
    private val _themeMode = MutableStateFlow(ThemeMode.fromId(DEFAULT_THEME_MODE))
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private val _apiKey = MutableStateFlow<String>("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    
    private val _geminiApiKey = MutableStateFlow<String>("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()
    
    private val _selectedProvider = MutableStateFlow<String>(DEFAULT_PROVIDER)
    val selectedProvider: StateFlow<String> = _selectedProvider.asStateFlow()
    
    private val _selectedModel = MutableStateFlow<String>(DEFAULT_MODEL)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()
    
    private val _appLanguage = MutableStateFlow<String>(DEFAULT_LANGUAGE)
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()
    
    private val _isDarkTheme = MutableStateFlow<Boolean>(DEFAULT_DARK_THEME)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    private val _translationSpeed = MutableStateFlow<Int>(DEFAULT_TRANSLATION_SPEED)
    val translationSpeed: StateFlow<Int> = _translationSpeed.asStateFlow()
    
    private val _targetLanguage = MutableStateFlow<String>("vi")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()
    
    private val _isInvertedTranslate = MutableStateFlow<Boolean>(false)
    val isInvertedTranslate: StateFlow<Boolean> = _isInvertedTranslate.asStateFlow()
    
    private val _batchSize = MutableStateFlow<Int>(DEFAULT_BATCH_SIZE)
    private val _isBackgroundTranslationEnabled = MutableStateFlow<Boolean>(false)
    val isBackgroundTranslationEnabled: StateFlow<Boolean> = _isBackgroundTranslationEnabled.asStateFlow()
    val batchSize: StateFlow<Int> = _batchSize.asStateFlow()
    
    private val encryptedPrefs: SharedPreferences
    
    init {
        // Initialize EncryptedSharedPreferences with error handling
        encryptedPrefs = try {
            // Create or retrieve the Master Key for encryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            // Initialize EncryptedSharedPreferences
            EncryptedSharedPreferences.create(
                context,
                PREFERENCES_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if EncryptedSharedPreferences fails
            android.util.Log.w("PreferencesRepository", "Failed to create EncryptedSharedPreferences, falling back to regular SharedPreferences", e)
            context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        }
        
        // Load saved values
        loadSavedValues()
    }

    private fun loadSavedValues() {
        _apiKey.value = encryptedPrefs.getString(KEY_API_KEY, "") ?: ""
        _geminiApiKey.value = encryptedPrefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        _selectedProvider.value = encryptedPrefs.getString(KEY_SELECTED_PROVIDER, DEFAULT_PROVIDER) ?: DEFAULT_PROVIDER
        _selectedModel.value = encryptedPrefs.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        _appLanguage.value = encryptedPrefs.getString(KEY_APP_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        _isDarkTheme.value = encryptedPrefs.getBoolean(KEY_DARK_THEME, DEFAULT_DARK_THEME)
        _translationSpeed.value = encryptedPrefs.getInt(KEY_TRANSLATION_SPEED, DEFAULT_TRANSLATION_SPEED)
        _targetLanguage.value = encryptedPrefs.getString(KEY_TARGET_LANGUAGE, "vi") ?: "vi"
        _isInvertedTranslate.value = encryptedPrefs.getBoolean(KEY_INVERTED_TRANSLATE, false)
        _themeMode.value = ThemeMode.fromId(encryptedPrefs.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE) ?: DEFAULT_THEME_MODE)
        _batchSize.value = encryptedPrefs.getInt(KEY_BATCH_SIZE, DEFAULT_BATCH_SIZE)
        _isBackgroundTranslationEnabled.value = encryptedPrefs.getBoolean(KEY_BACKGROUND_TRANSLATION, false)
    }
    
    /**
     * Save API key to encrypted preferences (Groq)
     */
    fun saveApiKey(apiKey: String) {
        encryptedPrefs.edit().putString(KEY_API_KEY, apiKey).apply()
        _apiKey.value = apiKey
    }
    
    /**
     * Save Gemini API key to encrypted preferences
     */
    fun saveGeminiApiKey(apiKey: String) {
        encryptedPrefs.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
        _geminiApiKey.value = apiKey
    }
    
    /**
     * Save selected provider (groq or gemini)
     */
    fun saveSelectedProvider(provider: String) {
        encryptedPrefs.edit().putString(KEY_SELECTED_PROVIDER, provider).apply()
        _selectedProvider.value = provider
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
    
    /**
     * Save dark theme preference
     */
    fun saveDarkTheme(isDark: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
        _isDarkTheme.value = isDark
    }
    
    /**
     * Save translation speed preference
     */
    fun saveTranslationSpeed(speed: Int) {
        encryptedPrefs.edit().putInt(KEY_TRANSLATION_SPEED, speed).apply()
        _translationSpeed.value = speed
    }
    
    /**
     * Save target language preference
     */
    fun saveTargetLanguage(language: String) {
        encryptedPrefs.edit().putString(KEY_TARGET_LANGUAGE, language).apply()
        _targetLanguage.value = language
    }
    
    /**
     * Save inverted translate preference
     */
    fun saveInvertedTranslate(isInverted: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_INVERTED_TRANSLATE, isInverted).apply()
        _isInvertedTranslate.value = isInverted
    }
    
    /**
     * Save theme mode preference
     */
    fun saveThemeMode(mode: ThemeMode) {
        encryptedPrefs.edit().putString(KEY_THEME_MODE, mode.id).apply()
        _themeMode.value = mode
    }
    
    /**
     * Save background translation preference
     */
    fun saveBackgroundTranslationEnabled(isEnabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BACKGROUND_TRANSLATION, isEnabled).apply()
        _isBackgroundTranslationEnabled.value = isEnabled
    }

    /**
     * Save batch size preference
     */
    fun saveBatchSize(size: Int) {
        encryptedPrefs.edit().putInt(KEY_BATCH_SIZE, size).apply()
        _batchSize.value = size
    }
}
