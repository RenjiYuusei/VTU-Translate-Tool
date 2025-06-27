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
import org.json.JSONException // Import for JSONException
import android.util.Log // Import for logging

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

                // First pass: Extract strings for translation
                val parserForExtraction = factory.newPullParser()
                parserForExtraction.setInput(StringReader(originalContent))

                val stringsToTranslate = mutableMapOf<String, String>()
                var eventType = parserForExtraction.eventType
                var currentStringName: String? = null
                var inStringTag = false

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parserForExtraction.name == "string") {
                                currentStringName = parserForExtraction.getAttributeValue(null, "name")
                                inStringTag = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inStringTag && currentStringName != null) {
                                stringsToTranslate[currentStringName] = parserForExtraction.text
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parserForExtraction.name == "string") {
                                inStringTag = false
                                currentStringName = null
                            }
                        }
                    }
                    eventType = parserForExtraction.next()
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

                val translatedStringsMap = mutableMapOf<String, String>()
                if (translatedJsonString.isNullOrEmpty()) {
                    _errorMessage.value = "Error: API returned empty or null translation response."
                    return@launch
                }

                val cleanedJsonString = translatedJsonString.substringAfter("```json").substringBeforeLast("```").trim()
                Log.d("MainViewModel", "Cleaned JSON String: $cleanedJsonString")

                try {
                    val translatedStringsJson = JSONObject(cleanedJsonString)
                    translatedStringsJson.keys().forEach { key ->
                        translatedStringsMap[key] = translatedStringsJson.getString(key)
                    }
                } catch (e: JSONException) {
                    _errorMessage.value = "Error parsing API response: Invalid JSON format. Details: ${e.message}"
                    e.printStackTrace()
                    return@launch
                } catch (e: Exception) {
                    _errorMessage.value = "Unexpected error during API response parsing. Details: ${e.message}"
                    e.printStackTrace()
                    return@launch
                }

                // Second pass: Reconstruct the XML with translated strings
                val parserForReconstruction = factory.newPullParser()
                parserForReconstruction.setInput(StringReader(originalContent))
                val stringBuilder = StringBuilder()

                eventType = parserForReconstruction.eventType
                var skipText = false

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_DOCUMENT -> {
                            // Do nothing, handled by START_TAG for <resources>
                        }
                        XmlPullParser.END_DOCUMENT -> {
                            // Do nothing
                        }
                        XmlPullParser.START_TAG -> {
                            stringBuilder.append("<" + parserForReconstruction.name)
                            for (i in 0 until parserForReconstruction.attributeCount) {
                                stringBuilder.append(" " + parserForReconstruction.getAttributeName(i) + "=\"")
                                stringBuilder.append(parserForReconstruction.getAttributeValue(i) + "\"")
                            }
                            stringBuilder.append(">")
                            if (parserForReconstruction.name == "string") {
                                val name = parserForReconstruction.getAttributeValue(null, "name")
                                val translatedText = translatedStringsMap[name]
                                if (translatedText != null) {
                                    stringBuilder.append(translatedText)
                                    skipText = true // Skip the original text event
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (!skipText || parserForReconstruction.name != "string") { // Only append end tag if not skipping text for string
                                stringBuilder.append("</" + parserForReconstruction.name + ">")
                            }
                            skipText = false // Reset skipText after handling the string tag
                        }
                        XmlPullParser.TEXT -> {
                            if (!skipText) {
                                stringBuilder.append(parserForReconstruction.text)
                            }
                        }
                        XmlPullParser.CDSECT -> {
                            stringBuilder.append("<![CDATA[" + parserForReconstruction.text + "]]>")
                        }
                        XmlPullParser.COMMENT -> {
                            stringBuilder.append("<!--" + parserForReconstruction.text + "-->")
                        }
                        XmlPullParser.ENTITY_REF -> {
                            stringBuilder.append("&" + parserForReconstruction.name + ";")
                        }
                        XmlPullParser.IGNORABLE_WHITESPACE -> {
                            stringBuilder.append(parserForReconstruction.text)
                        }
                        XmlPullParser.PROCESSING_INSTRUCTION -> {
                            stringBuilder.append("<?" + parserForReconstruction.text + "?>")
                        }
                    }
                    eventType = parserForReconstruction.next()
                }

                _translatedFileContent.value = stringBuilder.toString()

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