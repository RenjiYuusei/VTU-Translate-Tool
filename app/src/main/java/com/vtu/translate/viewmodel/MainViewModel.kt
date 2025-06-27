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
import com.vtu.translate.R
import java.io.StringReader
import org.json.JSONObject // Import for JSON parsing

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

    fun onFileSelected(content: String?) {
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
                val stringsToTranslate = mutableMapOf<String, String>()

                var currentStringName: String? = null
                var currentStringValue = StringBuilder()
                var inStringTag = false

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "string") {
                                currentStringName = parser.getAttributeValue(null, "name")
                                inStringTag = true
                                currentStringValue.clear() // Clear for new string
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inStringTag) {
                                currentStringValue.append(parser.text)
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "string" && currentStringName != null) {
                                stringsToTranslate[currentStringName!!] = currentStringValue.toString()
                                inStringTag = false
                                currentStringName = null
                            }
                        }
                    }
                    eventType = parser.next()
                }

                if (stringsToTranslate.isEmpty()) {
                    _errorMessage.value = "No strings found in the selected strings.xml file."
                    return@launch
                }

                // Construct JSON for batch translation
                val jsonForTranslation = JSONObject(stringsToTranslate as Map<*, *>).toString()
                val prompt = "Translate the following JSON object to $targetLanguage. The keys are string names and the values are the texts to translate. Respond with a JSON object in the same format, with the translated values. Ensure the output is a valid JSON object, without any additional text or markdown formatting outside the JSON block:\n$jsonForTranslation"

                val translatedJsonString = openRouterApiService.translateText(
                    model,
                    apiKey,
                    prompt
                )

                // Parse the translated JSON
                val translatedStringsJson = JSONObject(translatedJsonString)
                val translatedStringsMap = mutableMapOf<String, String>()
                translatedStringsJson.keys().forEach { key ->
                    translatedStringsMap[key] = translatedStringsJson.getString(key)
                }

                // Reconstruct the XML with translated strings using regex replacement
                var reconstructedContent = originalContent
                translatedStringsMap.forEach { (name, translatedValue) ->
                    // Regex to find the string tag and its content
                    // This regex is designed to be robust, handling potential whitespace around the text content
                    val regex = Regex("(<string\\s+name=\"$name\">)(.*?)(</string>)")
                    reconstructedContent = reconstructedContent.replaceFirst(regex, "$1$translatedValue$3")
                }

                _translatedFileContent.value = reconstructedContent

            } catch (e: Exception) {
                _errorMessage.value = getApplication<Application>().getString(R.string.error_during_translation) + " ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}
