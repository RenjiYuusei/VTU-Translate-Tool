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
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import android.util.Log // Import for logging
import android.util.Xml
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val openRouterApiService = OpenRouterApiService(::addLog)

    private val _apiKey = MutableStateFlow<String?>("")
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow("google/gemini-2.0-flash-exp:free")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _selectedFileContent = MutableStateFlow<String?>("")
    val selectedFileContent: StateFlow<String?> = _selectedFileContent.asStateFlow()

    private val _translatedFileContent = MutableStateFlow<String?>("")
    val translatedFileContent: StateFlow<String?> = _translatedFileContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedTargetLanguage = MutableStateFlow("Vietnamese")
    val selectedTargetLanguage: StateFlow<String> = _selectedTargetLanguage.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _logs.value = _logs.value + "[$timestamp] $message"
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    init {
        viewModelScope.launch {
            dataStoreManager.getApiKey.collect { key ->
                _apiKey.value = key
            }
        }
        viewModelScope.launch {
            dataStoreManager.getSelectedModel.collect { model ->
                model?.let { _selectedModel.value = it }
            }
        }
        viewModelScope.launch {
            dataStoreManager.getSelectedTargetLanguage.collect { language ->
                language?.let { _selectedTargetLanguage.value = it }
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
        viewModelScope.launch {
            dataStoreManager.saveSelectedModel(model)
        }
    }

    fun onTargetLanguageSelected(language: String) {
        _selectedTargetLanguage.value = language
        viewModelScope.launch {
            dataStoreManager.saveSelectedTargetLanguage(language)
        }
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
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            try {
                val originalContent = _selectedFileContent.value
                val apiKey = _apiKey.value
                val model = _selectedModel.value
                val targetLanguage = _selectedTargetLanguage.value

                addLog("Starting translation with model: $model")

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

                val batchSize = 20 // Define your desired batch size
                val allTranslatedStrings = mutableMapOf<String, String>()
                var batchError = false

                stringsToTranslate.entries.chunked(batchSize).forEach { batch ->
                    if (batchError) return@forEach

                    val batchMap = batch.associate { it.key to it.value }
                    val jsonForTranslation = json.encodeToString(batchMap)
                    val prompt = """Translate the following JSON object to $targetLanguage. The keys are string names and the values are the texts to translate. Respond with a JSON object in the same format, with the translated values. Ensure the output is a valid JSON object, without any additional text or markdown formatting outside the JSON block:
$jsonForTranslation"""

                    val translatedJsonString = openRouterApiService.translateText(
                        model,
                        apiKey,
                        prompt
                    )

                    if (translatedJsonString.isNullOrBlank()) {
                        _errorMessage.value = "Error: API returned empty or null translation response for a batch."
                        batchError = true
                        return@forEach
                    }

                    addLog("Raw API Response: $translatedJsonString")
                    val cleanedJsonString = translatedJsonString.substringAfter("```json", "").substringBeforeLast("```", "").trim()
                    addLog("Cleaned JSON String: $cleanedJsonString")

                    val jsonToParse = if (cleanedJsonString.isNotEmpty() && cleanedJsonString.startsWith("{") && cleanedJsonString.endsWith("}")) {
                        cleanedJsonString
                    } else {
                        translatedJsonString
                    }

                    try {
                        val translatedStringsJson = json.decodeFromString<Map<String, String>>(jsonToParse)
                        translatedStringsJson.forEach { (key, value) ->
                            allTranslatedStrings[key] = value
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "Error parsing API response for a batch: Invalid JSON format. Details: ${e.message}"
                        e.printStackTrace()
                        batchError = true
                        return@forEach
                    }
                }

                if(batchError) {
                    _isLoading.value = false
                    return@launch
                }


                // Second pass: Reconstruct the XML with translated strings using XmlSerializer
                val serializer: XmlSerializer = Xml.newSerializer()
                val writer = StringWriter()
                serializer.setOutput(writer)
                serializer.startDocument("UTF-8", true)
                serializer.startTag(null, "resources")

                val parserForReconstruction = factory.newPullParser()
                parserForReconstruction.setInput(StringReader(originalContent))
                var event = parserForReconstruction.eventType
                while(event != XmlPullParser.END_DOCUMENT) {
                    when(event) {
                        XmlPullParser.START_TAG -> {
                            if (parserForReconstruction.name == "string") {
                                val name = parserForReconstruction.getAttributeValue(null, "name")
                                val translatable = parserForReconstruction.getAttributeValue(null, "translatable")
                                val translatedText = allTranslatedStrings[name]

                                serializer.startTag(null, "string")
                                serializer.attribute(null, "name", name)
                                if (translatable != null) {
                                     serializer.attribute(null, "translatable", translatable)
                                }

                                if (translatedText != null) {
                                    serializer.text(translatedText)
                                } else {
                                    // Advance parser to get original text if no translation
                                    if(parserForReconstruction.next() == XmlPullParser.TEXT) {
                                        serializer.text(parserForReconstruction.text)
                                    }
                                }
                                serializer.endTag(null, "string")
                            }
                        }
                    }
                    event = parserForReconstruction.next()
                }


                serializer.endTag(null, "resources")
                serializer.endDocument()
                _translatedFileContent.value = writer.toString()

            } catch (e: Exception) {
                _errorMessage.value = "An unexpected error occurred: ${e.message}"
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