package com.vtu.translate.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vtu.translate.data.LogRepository
import com.vtu.translate.data.SettingsRepository
import com.vtu.translate.network.ChatRequest
import com.vtu.translate.network.Message
import com.vtu.translate.network.RetrofitInstance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

data class TranslationState(
    val originalStrings: Map<String, String> = emptyMap(),
    val translatedStrings: Map<String, String> = emptyMap(),
    val isTranslating: Boolean = false,
    val progress: Float = 0f,
    val selectedFileUri: Uri? = null,
    val targetLanguage: String = "vi" // Default to Vietnamese
)

class TranslateViewModel(
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslationState())
    val uiState = _uiState.asStateFlow()

    fun onFileSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedFileUri = uri) }
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
            if (content != null) {
                parseXml(content)
                logRepository.addLog("File loaded: ${uri.path}")
            } else {
                logRepository.addLog("Error: Could not read file content.")
            }
        }
    }

    private fun parseXml(xmlContent: String) {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        val strings = mutableMapOf<String, String>()
        var eventType = parser.eventType
        var currentName: String? = null
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "string") {
                        currentName = parser.getAttributeValue(null, "name")
                    }
                }
                XmlPullParser.TEXT -> {
                    if (currentName != null) {
                        strings[currentName!!] = parser.text
                        currentName = null
                    }
                }
            }
            eventType = parser.next()
        }
        _uiState.update { it.copy(originalStrings = strings) }
    }

    fun translateAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true, progress = 0f) }
            val apiKey = "Bearer ${settingsRepository.getApiKey.first()}"
            // For simplicity, let's use the Gemini model first.
            // A more robust implementation would let the user choose which model to use for the current translation.
            val model = settingsRepository.getGeminiModel.first()

            val originalStrings = _uiState.value.originalStrings
            val translatedStrings = mutableMapOf<String, String>()
            var progress = 0f
            val totalStrings = originalStrings.size

            originalStrings.forEach { (key, value) ->
                try {
                    val request = ChatRequest(
                        model = model,
                        messages = listOf(
                            Message("system", "You are a helpful assistant that translates Android strings.xml files. Translate the given text to ${_uiState.value.targetLanguage}. Only return the translated string, without any extra text or explanations."),
                            Message("user", value)
                        )
                    )
                    val response = RetrofitInstance.api.getChatCompletion(apiKey, request)
                    val translatedText = response.choices.first().message.content
                    translatedStrings[key] = translatedText
                    logRepository.addLog("Translated '$key': '$value' -> '$translatedText'")
                } catch (e: Exception) {
                    logRepository.addLog("Error translating string '$key': ${e.message}")
                    translatedStrings[key] = value // Keep original if translation fails
                }
                progress += 1f / totalStrings
                _uiState.update { it.copy(progress = progress) }
            }

            _uiState.update { it.copy(translatedStrings = translatedStrings, isTranslating = false) }
            logRepository.addLog("Translation finished.")
        }
    }

    fun getTranslatedXmlContent(): String {
        val builder = StringBuilder()
        builder.append("<resources>\n")
        _uiState.value.translatedStrings.forEach { (key, value) ->
            // Basic XML escaping
            val escapedValue = value.replace("&", "&amp;")
                                     .replace("<", "&lt;")
                                     .replace(">", "&gt;")
                                     .replace("'", "\\'")
                                     .replace("\"", "\\\"")
            builder.append("    <string name=\"$key\">$escapedValue</string>\n")
        }
        builder.append("</resources>")
        return builder.toString()
    }
}


class TranslateViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TranslateViewModel(settingsRepository, logRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 