package com.vtu.translate.data.repository

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vtu.translate.data.model.*
import com.vtu.translate.data.model.AiApiService
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
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Repository for interacting with the Gemini API
 */
@OptIn(ExperimentalSerializationApi::class)
class GeminiRepository(private val preferencesRepository: PreferencesRepository) : AiApiService {
    
    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private val contentType = "application/json".toMediaType()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Enable full logging to debug 400 errors
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
    
    private val geminiService = retrofit.create(GeminiService::class.java)
    
    /**
     * Get available models from Gemini API
     */
    suspend fun getModels(): Result<GeminiModelsResponse> {
        return try {
            val apiKey = preferencesRepository.geminiApiKey.first()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API Key is not set"))
            }
            
            val response = geminiService.getModels(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Translate text using Gemini API with target language
     */
    override suspend fun translateText(text: String, targetLanguage: String): Result<String> {
        return translateBatch(listOf(text), targetLanguage).map { it.first() }
    }
    
    /**
     * Translate multiple texts in batch using Gemini API
     */
    override suspend fun translateBatch(texts: List<String>, targetLanguage: String): Result<List<String>> {
        return try {
            val apiKey = preferencesRepository.geminiApiKey.first()
            Log.d("GeminiRepository", "API Key length: ${apiKey.length}")
            if (apiKey.isBlank()) {
                Log.e("GeminiRepository", "API Key is blank or empty")
                return Result.failure(Exception("Gemini API Key không được thiết lập. Vui lòng nhập API key trong Settings."))
            }
            
            val rawModel = preferencesRepository.selectedGeminiModel.first()
            if (rawModel.isBlank()) {
                return Result.failure(Exception("Gemini model is not selected"))
            }
            
            // For Gemini API, we need to pass the model name without 'models/' prefix in the path
            // The 'models/' is already part of the URL path structure
            val model = if (rawModel.startsWith("models/")) {
                rawModel.removePrefix("models/")
            } else {
                rawModel
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
            
            Log.d("GeminiRepository", "Using model: $model (original: $rawModel)")
            Log.d("GeminiRepository", "Target language: $targetLanguageName")
            Log.d("GeminiRepository", "Texts to translate: ${texts.size} items")
            
            // Build simpler prompt similar to GroqRepository
            val batchPrompt = if (texts.size == 1) {
                "Translate the following Android string resource value into $targetLanguageName. Return ONLY the translated text without any quotes, explanations, or additional formatting. IMPORTANT: Do NOT translate technical identifiers, package names (like androidx.startup), class names, URLs, placeholders, or format specifiers (like %s, %d). Keep those exactly as they are in the original text.\n\nOriginal text: ${texts[0]}"
            } else {
                val numberedTexts = texts.mapIndexed { index, text -> 
                    "${index + 1}. $text"
                }.joinToString("\n")
                
                """Translate the following Android string resource values into $targetLanguageName. 
Return ONLY the translated texts, one per line, in the same order.
Format: [number]. [translated text]
Do not add quotes around the translated text.
IMPORTANT: Do NOT translate technical identifiers, package names (like androidx.startup), class names, URLs, placeholders, or format specifiers (like %s, %d).

Original texts:
$numberedTexts""".trimIndent()
            }
            
            val request = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = batchPrompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 1.0
                )
            )
            
            // Implement retry with exponential backoff for HTTP 429 errors
            val maxRetries = 3
            var retryCount = 0
            var lastException: Exception? = null
            
            while (retryCount < maxRetries) {
                try {
                    val response = geminiService.generateContent(model, apiKey, request)
                    
                    if (!response.candidates.isNullOrEmpty() && 
                        !response.candidates[0].content.parts.isNullOrEmpty()) {
                        val content = response.candidates[0].content.parts[0].text.trim()
                        
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
                        return Result.failure(Exception("No response from Gemini API"))
                    }
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("GeminiRepository", "HTTP ${e.code()}: $errorBody")
                    
                    // Handle specific HTTP errors
                    when (e.code()) {
                        400 -> {
                            Log.e("GeminiRepository", "HTTP 400 error body: $errorBody")
                            Log.e("GeminiRepository", "Request model: $model")
                            Log.e("GeminiRepository", "Request prompt length: ${batchPrompt.length}")
                            return Result.failure(Exception("Yêu cầu không hợp lệ (HTTP 400). Chi tiết: ${errorBody ?: "Không có thông tin lỗi"}"))
                        }
                        401 -> {
                            return Result.failure(Exception("Gemini API key không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại API key trong Settings."))
                        }
                        403 -> {
                            return Result.failure(Exception("Không có quyền truy cập Gemini API. Vui lòng kiểm tra API key."))
                        }
                        429 -> {
                            lastException = Exception("Đạt giới hạn tốc độ Gemini API (HTTP 429). Đang thử lại...")
                            Log.w("GeminiRepository", "HTTP 429 received, retrying after delay. Attempt ${retryCount + 1}/$maxRetries")
                            
                            // Exponential backoff: 1s, 2s, 4s
                            val delayMs = (1000L * Math.pow(2.0, retryCount.toDouble())).toLong()
                            delay(delayMs)
                            retryCount++
                        }
                        404 -> {
                            return Result.failure(Exception("Gemini model không tồn tại hoặc không khả dụng."))
                        }
                        500, 502, 503, 504 -> {
                            return Result.failure(Exception("Lỗi máy chủ Gemini (HTTP ${e.code()}). Vui lòng thử lại sau."))
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
     * Get the currently selected Gemini model
     */
    override suspend fun getSelectedModel(): String {
        return preferencesRepository.selectedGeminiModel.first()
    }
    
    /**
     * Fetch available models from Gemini API
     */
    override suspend fun fetchAvailableModels(): Result<List<String>> {
        return try {
            val apiKey = preferencesRepository.geminiApiKey.first()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API Key không được thiết lập"))
            }
            
            val response = geminiService.getModels(apiKey)
            
            // Filter only generative models
            val textModels = response.models
                .filter { model ->
                    val modelName = model.name.lowercase()
                    // Filter for text generation models
                    model.supportedGenerationMethods?.contains("generateContent") == true &&
                    (modelName.contains("gemini") || modelName.contains("text"))
                }
                .map { it.name }
                .sorted()
            
            Result.success(textModels)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("GeminiRepository", "Error fetching models - HTTP ${e.code()}: $errorBody")
            when (e.code()) {
                400 -> Result.failure(Exception("Yêu cầu không hợp lệ khi tải danh sách model"))
                401 -> Result.failure(Exception("Gemini API key không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại API key"))
                403 -> Result.failure(Exception("Không có quyền truy cập Gemini API models. Vui lòng kiểm tra API key"))
                429 -> Result.failure(Exception("Đã vượt quá giới hạn request. Vui lòng thử lại sau"))
                else -> Result.failure(Exception("Lỗi khi tải danh sách Gemini model (HTTP ${e.code()}): ${errorBody ?: e.message()}"))
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error fetching models: ${e.message}", e)
            Result.failure(Exception("Lỗi kết nối Gemini API: ${e.message}"))
        }
    }
    
    /**
     * Gemini API service interface
     */
    private interface GeminiService {
        @GET("models")
        suspend fun getModels(@Query("key") apiKey: String): GeminiModelsResponse
        
        @POST("models/{model}:generateContent")
        suspend fun generateContent(
            @Path("model") model: String,
            @Query("key") apiKey: String,
            @Body request: GeminiGenerateContentRequest
        ): GeminiGenerateContentResponse
    }
}
