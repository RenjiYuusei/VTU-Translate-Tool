package com.vtu.translate.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vtu.translate.data.DataStoreManager
import com.vtu.translate.network.OpenRouterApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val openRouterApiService = OpenRouterApiService()

    private val _apiKey = MutableStateFlow<String?>("")
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow("google/gemma-3-27b-it:free")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _selectedFileContent = MutableStateFlow<String?>("")
    val selectedFileContent: StateFlow<String?> = _selectedFileContent.asStateFlow()

    private val _translatedFileContent = MutableStateFlow<String?>("")
    val translatedFileContent: StateFlow<String?> = _translatedFileContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Vietnamese")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.getApiKey.collect { key ->
                _apiKey.value = key
            }
        }
    }

    fun onApiKeyChange(newKey: String) {
        _apiKey.value = newKey
        viewModelScope.launch {
            dataStoreManager.saveApiKey(newKey)
        }
    }

    fun onModelSelected(model: String) {
        _selectedModel.value = model
    }

    fun onLanguageSelected(language: String) {
        _selectedLanguage.value = language
    }

    fun onFileSelected(content: String) {
        _selectedFileContent.value = content
        _translatedFileContent.value = null // Clear previous translation
        _errorMessage.value = null // Clear previous error
    }

    fun translateStringsXml() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val originalContent = _selectedFileContent.value
                val apiKey = _apiKey.value
                val model = _selectedModel.value
                val targetLanguage = _selectedLanguage.value

                if (originalContent.isNullOrEmpty()) {
                    _errorMessage.value = "Please select a strings.xml file first."
                    return@launch
                }
                if (apiKey.isNullOrEmpty()) {
                    _errorMessage.value = "Please enter your OpenRouter API Key."
                    return@launch
                }

                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val parser = factory.newPullParser()
                parser.setInput(StringReader(originalContent))

                var eventType = parser.eventType
                val translatedStrings = mutableMapOf<String, String>()
                val stringBuilder = StringBuilder()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "string") {
                                val name = parser.getAttributeValue(null, "name")
                                parser.next()
                                if (parser.eventType == XmlPullParser.TEXT) {
                                    val originalText = parser.text
                                    val translatedText = openRouterApiService.translateText(
                                        model,
                                        apiKey,
                                        "Translate the following text to $targetLanguage: \"\"\"$originalText\"\"\"")
                                    translatedStrings[name] = translatedText
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }

                // Reconstruct the XML with translated strings
                stringBuilder.append("<resources>\n")
                translatedStrings.forEach { (name, value) ->
                    stringBuilder.append("    <string name=\""$name\"">$value</string>\n")
                }
                stringBuilder.append("</resources>")

                _translatedFileContent.value = stringBuilder.toString()

            } catch (e: Exception) {
                _errorMessage.value = "Error during translation: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
