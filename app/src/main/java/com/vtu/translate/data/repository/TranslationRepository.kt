package com.vtu.translate.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.vtu.translate.data.model.LogType
import com.vtu.translate.data.model.StringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter

/**
 * Repository for handling XML translation operations
 */
class TranslationRepository(
    private val groqRepository: GroqRepository,
    private val logRepository: LogRepository
) {
    
    private val _stringResources = MutableStateFlow<List<StringResource>>(emptyList())
    val stringResources: StateFlow<List<StringResource>> = _stringResources.asStateFlow()
    
    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()
    
    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName.asStateFlow()
    
    /**
     * Parse strings.xml file from URI
     */
    suspend fun parseStringsXml(context: Context, uri: Uri): Result<List<StringResource>> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = getFileName(context, uri)
                _selectedFileName.value = fileName
                
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = true
                    val parser = factory.newPullParser()
                    parser.setInput(stream, null)
                    
                    val stringResources = mutableListOf<StringResource>()
                    var eventType = parser.eventType
                    var name: String? = null
                    var translatable: String? = null
                    
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                if (parser.name == "string") {
                                    name = parser.getAttributeValue(null, "name")
                                    translatable = parser.getAttributeValue(null, "translatable")
                                }
                            }
                            XmlPullParser.TEXT -> {
                                if (name != null) {
                                    val value = parser.text
                                    // Check if string is translatable
                                    val isTranslatable = translatable == null || translatable != "false"
                                    
                                    // Special case handling for known non-translatable strings
                                    val isSpecialCase = isSpecialNonTranslatableString(value)
                                    
                                    if (isTranslatable && !isSpecialCase) {
                                        stringResources.add(StringResource(name, value))
                                    } else if (isSpecialCase) {
                                        // Add with pre-defined translation for special cases
                                        val predefinedTranslation = getSpecialCaseTranslation(value)
                                        stringResources.add(StringResource(
                                            name = name,
                                            value = value,
                                            translatedValue = predefinedTranslation,
                                            isTranslating = false,
                                            hasError = false
                                        ))
                                    }
                                }
                            }
                            XmlPullParser.END_TAG -> {
                                if (parser.name == "string") {
                                    name = null
                                    translatable = null
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                    
                    _stringResources.value = stringResources
                    return@withContext Result.success(stringResources)
                } ?: return@withContext Result.failure(Exception("Could not open file"))
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }
    
    /**
     * Check if a string is a special non-translatable string
     * 
     * @param value The string value to check
     * @return True if the string is a special non-translatable string
     */
    private fun isSpecialNonTranslatableString(value: String): Boolean {
        // Check for package names, class names, or other technical strings
        return value.matches(Regex("^[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)+$")) || // Package names like androidx.startup
               value.matches(Regex("^[A-Z][a-zA-Z0-9]*$")) || // Class names like MainActivity
               value.matches(Regex("^[a-zA-Z0-9_]+$")) || // Simple technical identifiers
               value.startsWith("http://") || value.startsWith("https://") // URLs
    }
    
    /**
     * Get predefined translation for special case strings
     * 
     * @param value The string value to translate
     * @return The predefined translation for the special case string
     */
    private fun getSpecialCaseTranslation(value: String): String {
        // Map of known special cases
        return when {
            value.startsWith("androidx.") -> "AndroidX ${value.substringAfter("androidx.")}"
            value.startsWith("android.") -> "Android ${value.substringAfter("android.")}"
            value.startsWith("java.") -> "Java ${value.substringAfter("java.")}"
            value.startsWith("kotlin.") -> "Kotlin ${value.substringAfter("kotlin.")}"
            value.startsWith("http") -> value // Keep URLs as is
            else -> value // Keep as is if no special handling defined
        }
    }
    
    /**
     * Biến để kiểm soát việc dừng quá trình dịch
     */
    private var shouldStopTranslation = false
    
    /**
     * Dừng quá trình dịch
     */
    fun stopTranslation() {
        shouldStopTranslation = true
        logRepository.logWarning("Đã yêu cầu dừng quá trình dịch.")
    }
    
    /**
     * Translate all string resources
     */
    suspend fun translateAll(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                _isTranslating.value = true
                shouldStopTranslation = false
                
                val resources = _stringResources.value
                if (resources.isEmpty()) {
                    return@withContext Result.failure(Exception("No strings to translate"))
                }
                
                logRepository.logInfo("Bắt đầu dịch ${resources.size} chuỗi với model [${groqRepository.getSelectedModel()}].")
                
                // Create a mutable copy of the resources
                val updatedResources = resources.toMutableList()
                
                // Batch size and delay to avoid rate limiting
                val batchSize = 5
                val delayBetweenBatchesMs = 1000L // 1 second delay between batches
                
                // Process in batches
                for (batchStart in resources.indices step batchSize) {
                    // Kiểm tra nếu người dùng đã yêu cầu dừng
                    if (shouldStopTranslation) {
                        logRepository.logInfo("Đã dừng quá trình dịch theo yêu cầu.")
                        break
                    }
                    
                    val batchEnd = minOf(batchStart + batchSize, resources.size)
                    var rateLimitHit = false
                    
                    for (i in batchStart until batchEnd) {
                        // Kiểm tra lại nếu người dùng đã yêu cầu dừng
                        if (shouldStopTranslation) break
                        
                        val resource = resources[i]
                        
                        // Skip if already has a translation (for special cases)
                        if (resource.translatedValue.isNotBlank() && !resource.hasError) {
                            continue
                        }
                        
                        // Update status to translating
                        updatedResources[i] = resource.copy(isTranslating = true)
                        _stringResources.value = updatedResources.toList()
                        
                        // Translate the string
                        val result = groqRepository.translateText(resource.value)
                        
                        if (result.isSuccess) {
                            val translatedText = result.getOrNull() ?: ""
                            updatedResources[i] = resource.copy(
                                translatedValue = translatedText,
                                isTranslating = false,
                                hasError = false
                            )
                            logRepository.logSuccess("Dịch thành công key '${resource.name}'.") 
                        } else {
                            val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                            
                            // Check if rate limit error
                            if (errorMessage.contains("429") || errorMessage.contains("rate limit") || 
                                errorMessage.contains("Rate limit")) {
                                rateLimitHit = true
                                updatedResources[i] = resource.copy(
                                    isTranslating = false,
                                    hasError = true
                                )
                                logRepository.logWarning("Đạt giới hạn tốc độ API (HTTP 429) khi dịch key '${resource.name}'. Sẽ thử lại sau.")
                                break // Break the inner loop to pause and retry
                            } else {
                                updatedResources[i] = resource.copy(
                                    isTranslating = false,
                                    hasError = true
                                )
                                logRepository.logError("Dịch thất bại key '${resource.name}'. Lỗi: $errorMessage.")
                            }
                        }
                        
                        // Update the list
                        _stringResources.value = updatedResources.toList()
                        
                        // Small delay between individual requests
                        delay(200) // 200ms between individual requests
                    }
                    
                    // Kiểm tra lại nếu người dùng đã yêu cầu dừng
                    if (shouldStopTranslation) {
                        logRepository.logInfo("Đã dừng quá trình dịch theo yêu cầu.")
                        break
                    }
                    
                // If rate limit was hit, wait longer before continuing
                    if (rateLimitHit) {
                        logRepository.logInfo("Đợi 5 giây trước khi tiếp tục do đạt giới hạn tốc độ API...")
                        delay(5000) // Wait 5 seconds before continuing
                    } else if (batchEnd < resources.size) {
                        // Only delay between batches if there are more batches to process
                        delay(delayBetweenBatchesMs)
                    }
                }
                
                _isTranslating.value = false
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                _isTranslating.value = false
                return@withContext Result.failure(e)
            }
        }
    }
    
    /**
     * Save translated strings to a new XML file
     */
    suspend fun saveTranslatedFile(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val resources = _stringResources.value
                if (resources.isEmpty()) {
                    return@withContext Result.failure(Exception("No strings to save"))
                }
                
                // Create directory structure in Downloads folder
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val resDir = File(downloadsDir, "res")
                val valuesViDir = File(resDir, "values-vi")
                
                if (!valuesViDir.exists()) {
                    valuesViDir.mkdirs()
                }
                
                val outputFile = File(valuesViDir, "strings.xml")
                
                // Create XML file
                val serializer = XmlPullParserFactory.newInstance().newSerializer()
                val writer = OutputStreamWriter(FileOutputStream(outputFile), "UTF-8")
                
                serializer.setOutput(writer)
                serializer.startDocument("UTF-8", true)
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                
                serializer.startTag("", "resources")
                
                for (resource in resources) {
                    serializer.startTag("", "string")
                    serializer.attribute("", "name", resource.name)
                    
                    // Use translated value if available, otherwise use original
                    val value = if (resource.translatedValue.isNotBlank()) {
                        resource.translatedValue
                    } else {
                        resource.value
                    }
                    
                    serializer.text(value)
                    serializer.endTag("", "string")
                }
                
                serializer.endTag("", "resources")
                serializer.endDocument()
                writer.close()
                
                val filePath = outputFile.absolutePath
                logRepository.logInfo("Đã lưu file vào $filePath.")
                
                return@withContext Result.success(filePath)
            } catch (e: Exception) {
                logRepository.logError("Lỗi khi lưu file: ${e.message}")
                return@withContext Result.failure(e)
            }
        }
    }
    
    /**
     * Update a string resource in the list
     */
    fun updateStringResource(index: Int, translatedValue: String) {
        val currentList = _stringResources.value.toMutableList()
        if (index in currentList.indices) {
            val updatedResource = currentList[index].copy(translatedValue = translatedValue)
            currentList[index] = updatedResource
            _stringResources.value = currentList
        }
    }
    
    /**
     * Clear all string resources
     */
    fun clearResources() {
        _stringResources.value = emptyList()
        _selectedFileName.value = null
    }
    
    /**
     * Get file name from URI
     */
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "Unknown file"
    }
    
    /**
     * Get the currently selected model
     */
    private suspend fun getSelectedModel(): String {
        return groqRepository.getSelectedModel()
    }
}