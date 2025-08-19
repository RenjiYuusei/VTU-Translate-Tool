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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter

/**
 * Repository for handling XML translation operations
 */
class TranslationRepository(
    private val groqRepository: GroqRepository,
    private val logRepository: LogRepository,
    private val context: Context
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
                        // Check if the string contains technical parts that should be preserved
                        if (containsTechnicalParts(value)) {
                            // Add with pre-processed value that marks technical parts
                            val preprocessedValue = preprocessStringWithTechnicalParts(value)
                            stringResources.add(StringResource(name, preprocessedValue))
                        } else {
                            stringResources.add(StringResource(name, value))
                        }
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
        val trimmedValue = value.trim()
        
        // Empty strings should not be translated
        if (trimmedValue.isEmpty()) {
            return true
        }
        
        // Check if the entire string is purely technical (no natural language)
        val purelyTechnicalPatterns = listOf(
            "^[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)+$", // Package names like androidx.startup
            "^[A-Z][a-zA-Z0-9]*$", // Class names like MainActivity (but only if it's the entire string)
            "^[a-zA-Z0-9_-]+$", // Simple technical identifiers (underscore/hyphen only)
            "^(http|https)://[a-zA-Z0-9./?_=-]+$", // Complete URLs
            "^[0-9]+$", // Pure numbers
            "^[0-9]+\\.[0-9]+(\\.[0-9]+)*$", // Version numbers like 1.0.0
            "^#[0-9a-fA-F]{6}$", // Hex color codes
            "^[a-zA-Z_][a-zA-Z0-9_]*$" // Variable names (but only if short and no spaces)
        )
        
        // Check if the string matches purely technical patterns AND is relatively short
        // This prevents longer descriptive text from being marked as non-translatable
        val isPurelyTechnical = purelyTechnicalPatterns.any { trimmedValue.matches(Regex(it)) } && 
                                trimmedValue.length <= 50 && 
                                !trimmedValue.contains(" ") // No spaces in purely technical strings
        
        // Check for strings that are only placeholders or format specifiers
        val isOnlyPlaceholders = trimmedValue.matches(Regex("^[%{}\\s\\d\\w]*$")) &&
                                (trimmedValue.contains(Regex("\\{\\w*\\}")) || 
                                 trimmedValue.contains(Regex("%[sdfx]")))
        
        // Special case: if string contains both natural language AND placeholders/format specifiers,
        // it should be translatable (like the canta_description example)
        val hasNaturalLanguage = trimmedValue.split("\\s+").size >= 3 || // Has at least 3 words
                                trimmedValue.contains(Regex("[.!?]")) || // Contains sentence punctuation
                                trimmedValue.length > 50 // Long strings are likely descriptive
        
        return isPurelyTechnical || (isOnlyPlaceholders && !hasNaturalLanguage)
    }
    
    /**
     * Get predefined translation for special case strings
     * 
     * @param value The string value to translate
     * @return The predefined translation for the special case string
     */
    private fun getSpecialCaseTranslation(value: String): String {
        // For technical strings, we keep them as-is without translation
        return when {
            value.startsWith("androidx.") -> value // Keep package names as-is
            value.startsWith("android.") -> value
            value.startsWith("java.") -> value
            value.startsWith("kotlin.") -> value
            value.startsWith("http") -> value // Keep URLs as is
            else -> value // Keep as is if no special handling defined
        }
    }
    
    /**
     * Boolean to control whether to stop the translation process
     */
    private var shouldStopTranslation = false
    
    /**
     * Stop the current translation process
     */
    fun stopTranslation() {
        shouldStopTranslation = true
        logRepository.logWarning("Đã yêu cầu dừng quá trình dịch.")
    }
    
    /**
     * Get the current translation progress (for continuing translation)
     */
    fun getCurrentTranslationIndex(): Int {
        val resources = _stringResources.value
        return resources.indexOfFirst { it.translatedValue.isBlank() && !it.hasError }
    }
    
    /**
     * Continue translation from where it was stopped
     */
    suspend fun continueTranslation(targetLanguage: String = "vi"): Result<Unit> {
        return translateAll(targetLanguage, continueFromIndex = getCurrentTranslationIndex())
    }
    
    /**
     * Translate all string resources
     */
    suspend fun translateAll(targetLanguage: String = "vi", continueFromIndex: Int = 0, translationSpeed: Int = 3, batchSize: Int = 1): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                _isTranslating.value = true
                shouldStopTranslation = false
                
                val resources = _stringResources.value
                if (resources.isEmpty()) {
                    return@withContext Result.failure(Exception("No strings to translate"))
                }
                
                // Determine the starting index based on continueFromIndex parameter
                val startIndex = maxOf(0, continueFromIndex)
                val remainingCount = resources.size - startIndex
                
                logRepository.logInfo("Bắt đầu dịch $remainingCount chuỗi (từ index $startIndex) với model [${groqRepository.getSelectedModel()}] sang ngôn ngữ '$targetLanguage' với tốc độ $translationSpeed.")
                
                // Create a mutable copy of the resources
                val updatedResources = resources.toMutableList()
                
                // Calculate delay based on translation speed and batch size
                // Batch size now comes from user preference
                val delayBetweenBatchesMs = when (translationSpeed) {
                    1 -> 3000L // Slow
                    2 -> 2000L // Medium-slow
                    3 -> 1000L // Normal (default)
                    4 -> 500L  // Fast
                    5 -> 200L  // Very fast
                    else -> 1000L // Default to normal
                }
                
                // Process in batches, starting from the specified index
                for (batchStart in startIndex until resources.size step batchSize) {
                    // Kiểm tra nếu người dùng đã yêu cầu dừng
                    if (shouldStopTranslation) {
                        logRepository.logInfo("Đã dừng quá trình dịch theo yêu cầu.")
                        break
                    }
                    
                    val batchEnd = minOf(batchStart + batchSize, resources.size)
                    var rateLimitHit = false
                    
                    // Collect resources for batch translation
                    val batchResources = mutableListOf<Pair<Int, StringResource>>()
                    for (i in batchStart until batchEnd) {
                        val resource = resources[i]
                        // Skip if already has a translation (for special cases)
                        if (resource.translatedValue.isBlank() || resource.hasError) {
                            batchResources.add(Pair(i, resource))
                            // Update status to translating
                            updatedResources[i] = resource.copy(isTranslating = true)
                        }
                    }
                    
                    if (batchResources.isNotEmpty()) {
                        _stringResources.value = updatedResources.toList()
                        
                        // Extract texts for batch translation
                        val textsToTranslate = batchResources.map { it.second.value }
                        
                        // Translate batch
                        val result = if (textsToTranslate.size == 1) {
                            // Single text, use regular translation
                            translateTextWithLanguages(textsToTranslate[0], targetLanguage)
                                .map { listOf(it) }
                        } else {
                            // Multiple texts, use batch translation
                            groqRepository.translateBatch(textsToTranslate, targetLanguage)
                        }
                        
                        if (result.isSuccess) {
                            val translatedTexts = result.getOrNull() ?: emptyList()
                            
                            // Update all resources in the batch
                            batchResources.forEachIndexed { index, (originalIndex, resource) ->
                                if (index < translatedTexts.size) {
                                    updatedResources[originalIndex] = resource.copy(
                                        translatedValue = translatedTexts[index],
                                        isTranslating = false,
                                        hasError = false
                                    )
                                    logRepository.logSuccess("Dịch thành công key '${resource.name}'.")
                                }
                            }
                        } else {
                            val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                            
                            // Check if rate limit error
                            if (errorMessage.contains("429") || errorMessage.contains("rate limit") || 
                                errorMessage.contains("Rate limit")) {
                                rateLimitHit = true
                                // Mark all batch resources as error
                                batchResources.forEach { (originalIndex, resource) ->
                                    updatedResources[originalIndex] = resource.copy(
                                        isTranslating = false,
                                        hasError = true
                                    )
                                }
                                logRepository.logWarning("Đạt giới hạn tốc độ API (HTTP 429) khi dịch batch. Sẽ thử lại sau.")
                            } else {
                                // Mark all batch resources as error
                                batchResources.forEach { (originalIndex, resource) ->
                                    updatedResources[originalIndex] = resource.copy(
                                        isTranslating = false,
                                        hasError = true
                                    )
                                    logRepository.logError("Dịch thất bại key '${resource.name}'. Lỗi: $errorMessage.")
                                }
                            }
                        }
                        
                        // Update the list
                        _stringResources.value = updatedResources.toList()
                    }
                    
                    // Check again if user requested stop after processing each batch
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
    suspend fun saveTranslatedFile(targetLanguage: String = "vi"): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val resources = _stringResources.value
                if (resources.isEmpty()) {
                    return@withContext Result.failure(Exception("No strings to save"))
                }
                
                // Map language codes to folder suffixes
                val languageFolderMap = mapOf(
                    "vi" to "values-vi",
                    "en" to "values", // Default folder for English
                    "zh" to "values-zh",
                    "ru" to "values-ru", 
                    "ko" to "values-ko",
                    "es" to "values-es",
                    "fr" to "values-fr",
                    "de" to "values-de",
                    "ja" to "values-ja"
                )
                
                val folderName = languageFolderMap[targetLanguage] ?: "values-$targetLanguage"
                
                // Create directory structure in Downloads/VTU-Translate
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val vtuTranslateDir = File(downloadsDir, "VTU-Translate")
                val resDir = File(vtuTranslateDir, "res")
                val valuesDir = File(resDir, folderName)
                
                if (!valuesDir.exists()) {
                    valuesDir.mkdirs()
                }
                
                val outputFile = File(valuesDir, "strings.xml")
                
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
     * Check if a string contains technical parts that should be preserved during translation
     * 
     * @param value The string value to check
     * @return True if the string contains technical parts
     */
    private fun containsTechnicalParts(value: String): Boolean {
        // Check for strings that contain a mix of translatable text and technical identifiers
        val technicalPatterns = listOf(
            Regex("androidx\\.[a-zA-Z0-9_.]+"),  // androidx package references
            Regex("android\\.[a-zA-Z0-9_.]+"),  // android package references
            Regex("java\\.[a-zA-Z0-9_.]+"),     // java package references
            Regex("kotlin\\.[a-zA-Z0-9_.]+"),   // kotlin package references
            Regex("%[sdfx]"),                    // Format specifiers
            Regex("\\{[^}]*\\}")                // Placeholders in curly braces
        )
        
        return technicalPatterns.any { pattern -> pattern.containsMatchIn(value) }
    }
    
    /**
     * Preprocess a string to mark technical parts that should be preserved during translation
     * 
     * @param value The string value to preprocess
     * @return The preprocessed string with technical parts marked
     */
    private fun preprocessStringWithTechnicalParts(value: String): String {
        // This is a simple implementation that doesn't modify the string
        // The actual preprocessing is handled by the improved prompt in GroqRepository
        return value
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
     * Translate text to specified target language using selected API provider
     */
    private suspend fun translateTextWithLanguages(text: String, targetLanguage: String): Result<String> {
        // Use the selected API provider from preferences
return groqRepository.translateText(text, targetLanguage)
    }
    
}