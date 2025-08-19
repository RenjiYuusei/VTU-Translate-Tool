package com.vtu.translate.data.repository

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vtu.translate.data.model.ChatCompletionRequest
import com.vtu.translate.data.model.ChatCompletionResponse
import com.vtu.translate.data.model.ChatMessage
import com.vtu.translate.data.model.GroqModelsResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Repository for interacting with the Groq API
 */
@OptIn(ExperimentalSerializationApi::class)
class GroqRepository(private val preferencesRepository: PreferencesRepository) {
    
    companion object {
        private const val BASE_URL = "https://api.groq.com/openai/v1/"
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private val contentType = "application/json".toMediaType()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
    
    private val groqService = retrofit.create(GroqService::class.java)
    
    /**
     * Get available models from Groq API
     */
    suspend fun getModels(): Result<GroqModelsResponse> {
        return try {
            val apiKey = preferencesRepository.apiKey.first()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("API Key is not set"))
            }
            
            val response = groqService.getModels("Bearer $apiKey")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Translate text using Groq API with target language
     */
    suspend fun translateText(text: String, targetLanguage: String = "vi"): Result<String> {
        return translateBatch(listOf(text), targetLanguage).map { it.first() }
    }
    
    /**
     * Translate multiple texts in batch using Groq API
     */
    suspend fun translateBatch(texts: List<String>, targetLanguage: String = "vi"): Result<List<String>> {
        return try {
            val apiKey = preferencesRepository.apiKey.first()
            Log.d("GroqRepository", "API Key length: ${apiKey.length}")
            if (apiKey.isBlank()) {
                Log.e("GroqRepository", "API Key is blank or empty")
                return Result.failure(Exception("API Key không được thiết lập. Vui lòng nhập API key trong Settings."))
            }
            
            val model = preferencesRepository.selectedModel.first()
            if (model.isBlank()) {
                return Result.failure(Exception("Model is not selected"))
            }
            
            // Map language codes to language names
            val languageNames = mapOf(
                "vi" to "Vietnamese",
                "en" to "English", 
                "zh" to "Chinese",
                "ru" to "Russian",
                "ko" to "Korean",
                "es" to "Spanish",
                "fr" to "French",
                "de" to "German",
                "ja" to "Japanese"
            )
            
            val targetLanguageName = languageNames[targetLanguage] ?: "Vietnamese"
            
            // Build batch prompt
            val batchPrompt = if (texts.size == 1) {
                "Translate the following Android string resource value into $targetLanguageName. Return ONLY the translated text without any quotes, explanations, or additional formatting. IMPORTANT: Do NOT translate technical identifiers, package names (like androidx.startup), class names, URLs, placeholders, or format specifiers (like %s, %d). Keep those exactly as they are in the original text.\n\nOriginal text: ${texts[0]}"
            } else {
                val numberedTexts = texts.mapIndexed { index, text -> 
                    "${index + 1}. $text"
                }.joinToString("\n")
                
                """Translate the following Android string resource values into $targetLanguageName. 
                |Return ONLY the translated texts, one per line, in the same order.
                |Format: [number]. [translated text]
                |Do not add quotes around the translated text.
                |IMPORTANT: Do NOT translate technical identifiers, package names (like androidx.startup), class names, URLs, placeholders, or format specifiers (like %s, %d).
                |
                |Original texts:
                |$numberedTexts""".trimMargin()
            }
            
            val request = ChatCompletionRequest(
                model = model,
                messages = listOf(ChatMessage(role = "user", content = batchPrompt)),
                temperature = 0.7
            )
            
            // Implement retry with exponential backoff for HTTP 429 errors
            val maxRetries = 3
            var retryCount = 0
            var lastException: Exception? = null
            
            while (retryCount < maxRetries) {
                try {
                    val response = groqService.createChatCompletion("Bearer $apiKey", request)
                    
                    if (response.choices.isNotEmpty()) {
                        val content = response.choices[0].message.content.trim()
                        
                        return if (texts.size == 1) {
                            // Remove surrounding quotes if present
                            val cleanedContent = content.trim().removeSurrounding("\"").removeSurrounding("'")
                            Result.success(listOf(cleanedContent))
                        } else {
                            // Parse batch response
                            val translatedTexts = content.lines()
                                .filter { it.isNotBlank() }
                                .mapNotNull { line ->
                                    // Extract text after "[number]. " and remove quotes
                                    val match = Regex("""^\d+\.\s*(.+)$""").find(line.trim())
                                    match?.groupValues?.get(1)?.trim()?.removeSurrounding("\"")?.removeSurrounding("'")
                                }
                            
                            if (translatedTexts.size == texts.size) {
                                Result.success(translatedTexts)
                            } else {
                                // Fallback: return original response split by lines
                                Result.success(content.lines().filter { it.isNotBlank() }.take(texts.size))
                            }
                        }
                    } else {
                        return Result.failure(Exception("No response from API"))
                    }
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("GroqRepository", "HTTP ${e.code()}: $errorBody")
                    
                    // Handle specific HTTP errors
                    when (e.code()) {
                        401 -> {
                            return Result.failure(Exception("API key không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại API key trong Settings."))
                        }
                        429 -> {
                            lastException = Exception("Đạt giới hạn tốc độ API (HTTP 429). Đang thử lại...")
                            Log.w("GroqRepository", "HTTP 429 received, retrying after delay. Attempt ${retryCount + 1}/$maxRetries")
                            
                            // Exponential backoff: 1s, 2s, 4s
                            val delayMs = (1000L * Math.pow(2.0, retryCount.toDouble())).toLong()
                            delay(delayMs)
                            retryCount++
                        }
                        403 -> {
                            return Result.failure(Exception("Không có quyền truy cập API. Vui lòng kiểm tra API key."))
                        }
                        404 -> {
                            return Result.failure(Exception("Model không tồn tại hoặc không khả dụng."))
                        }
                        500, 502, 503, 504 -> {
                            return Result.failure(Exception("Lỗi máy chủ Groq (HTTP ${e.code()}). Vui lòng thử lại sau."))
                        }
                        else -> {
                            return Result.failure(Exception("Lỗi HTTP ${e.code()}: ${errorBody ?: e.message()}"))
                        }
                    }
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }
            
            // If we've exhausted all retries
            Result.failure(lastException ?: Exception("Failed after $maxRetries retries"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the currently selected model
     */
    suspend fun getSelectedModel(): String {
        return preferencesRepository.selectedModel.first()
    }
    
    /**
     * Fetch available text generation models from Groq API
     */
    suspend fun fetchAvailableModels(): Result<List<String>> {
        return try {
            val apiKey = preferencesRepository.apiKey.first()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("API Key không được thiết lập"))
            }
            
            val response = groqService.getModels("Bearer $apiKey")
            
            // Filter only text generation models
            val textModels = response.data
                .filter { model ->
                    val modelId = model.id.lowercase()
                    // Exclude whisper, TTS, and guard models
                    !modelId.contains("whisper") && 
                    !modelId.contains("tts") && 
                    !modelId.contains("guard")
                }
                .map { it.id }
                .sorted()
            
            Result.success(textModels)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("GroqRepository", "Error fetching models - HTTP ${e.code()}: $errorBody")
            when (e.code()) {
                401 -> Result.failure(Exception("API key không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại API key (phải bắt đầu với gsk_)"))
                403 -> Result.failure(Exception("Không có quyền truy cập API models. Vui lòng kiểm tra API key"))
                429 -> Result.failure(Exception("Đã vượt quá giới hạn request. Vui lòng thử lại sau"))
                else -> Result.failure(Exception("Lỗi khi tải danh sách model (HTTP ${e.code()}): ${errorBody ?: e.message()}"))
            }
        } catch (e: Exception) {
            Log.e("GroqRepository", "Error fetching models: ${e.message}", e)
            Result.failure(Exception("Lỗi kết nối: ${e.message}"))
        }
    }
    
    /**
     * Groq API service interface
     */
    private interface GroqService {
        @GET("models")
        suspend fun getModels(@Header("Authorization") authorization: String): GroqModelsResponse
        
        @POST("chat/completions")
        suspend fun createChatCompletion(
            @Header("Authorization") authorization: String,
            @Body request: ChatCompletionRequest
        ): ChatCompletionResponse
    }
}