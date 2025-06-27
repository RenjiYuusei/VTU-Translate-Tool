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
import com.vtu.translate.network.OpenRouterApi
import com.vtu.translate.network.TranslateRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

data class TranslateUiState(
    val originalStrings: Map<String, String> = emptyMap(),
    val translatedStrings: Map<String, String> = emptyMap(),
    val originalContent: String = "",
    val translatedContent: String = "",
    val isTranslating: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null
)

class TranslateViewModel(
    private val context: Context,
    private val openRouterApi: OpenRouterApi,
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslateUiState())
    val uiState = _uiState.asStateFlow()

    fun loadStringsXml(uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    val strings = parseStringsXml(context.contentResolver.openInputStream(uri))
                    _uiState.update {
                        it.copy(
                            originalStrings = strings,
                            originalContent = content,
                            translatedStrings = emptyMap(),
                            translatedContent = ""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                logRepository.addLog("Error loading strings.xml: ${e.message}")
            }
        }
    }

    private fun parseStringsXml(inputStream: InputStream?): Map<String, String> {
        val strings = mutableMapOf<String, String>()
        inputStream?.use {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(it, null)

            var eventType = parser.eventType
            var currentKey: String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "string") {
                            currentKey = parser.getAttributeValue(null, "name")
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (currentKey != null) {
                            strings[currentKey!!] = parser.text
                            currentKey = null
                        }
                    }
                }
                eventType = parser.next()
            }
        }
        return strings
    }

    fun translateAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true, progress = 0f) }
            val apiKey = "Bearer ${settingsRepository.getApiKey.first()}"
            val model = settingsRepository.getSelectedModel.first()
            val originalStrings = _uiState.value.originalStrings
            val translatedStrings = mutableMapOf<String, String>()
            val totalStrings = originalStrings.size
            var completedCount = 0

            logRepository.addLog("Starting translation with model: $model")

            val translationJobs = originalStrings.map { (key, value) ->
                async {
                    try {
                        val request = TranslateRequest(
                            model = model,
                            messages = listOf(
                                mapOf("role" to "user", "content" to "Translate the following text to Vietnamese: $value")
                            )
                        )
                        val response = openRouterApi.translate(apiKey, request)
                        val translatedText = response.choices.first().message.content
                        logRepository.addLog("SUCCESS: '$key' -> '$translatedText'")
                        synchronized(translatedStrings) {
                            translatedStrings[key] = translatedText
                        }
                    } catch (e: Exception) {
                        logRepository.addLog("ERROR translating '$key': ${e.message}")
                        // Keep original if translation fails
                        synchronized(translatedStrings) {
                            translatedStrings[key] = value
                        }
                    } finally {
                        synchronized(this) {
                            completedCount++
                            val currentProgress = completedCount.toFloat() / totalStrings.toFloat()
                            _uiState.update { it.copy(progress = currentProgress) }
                        }
                    }
                }
            }

            translationJobs.awaitAll()

            val translatedXmlContent = createXmlString(translatedStrings)
            _uiState.update {
                it.copy(
                    isTranslating = false,
                    translatedStrings = translatedStrings,
                    translatedContent = translatedXmlContent
                )
            }
            logRepository.addLog("Translation finished.")
        }
    }

    fun getTranslatedXmlContent(): String {
        return createXmlString(_uiState.value.translatedStrings)
    }

    private fun createXmlString(strings: Map<String, String>): String {
        try {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = docBuilder.newDocument()
            val rootElement = doc.createElement("resources")
            doc.appendChild(rootElement)

            strings.forEach { (key, value) ->
                val stringElement = doc.createElement("string")
                stringElement.setAttribute("name", key)
                stringElement.appendChild(doc.createTextNode(value))
                rootElement.appendChild(stringElement)
            }

            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

            val writer = StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(writer))
            return writer.buffer.toString()
        } catch (e: Exception) {
            logRepository.addLog("Error creating XML string: ${e.message}")
            return "Error creating XML: ${e.message}"
        }
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