package com.vtutranslate.data

import com.vtutranslate.VTUTranslateApp
import com.vtutranslate.models.TranslationModel

class SettingsManager {
    
    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val DEFAULT_MODEL = "google/gemini-2.0-flash-exp:free"
    }
    
    private val sharedPreferences = VTUTranslateApp.getSharedPreferences()
    
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }
    
    fun getApiKey(): String {
        return sharedPreferences.getString(KEY_API_KEY, "") ?: ""
    }
    
    fun saveSelectedModel(model: TranslationModel) {
        sharedPreferences.edit().putString(KEY_SELECTED_MODEL, model.modelId).apply()
    }
    
    fun getSelectedModel(): TranslationModel {
        val modelId = sharedPreferences.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        return TranslationModel.values().find { it.modelId == modelId } ?: TranslationModel.GEMINI_2_FLASH
    }
} 