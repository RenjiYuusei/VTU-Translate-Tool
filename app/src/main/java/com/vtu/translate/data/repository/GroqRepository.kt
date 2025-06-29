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
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
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
     * Translate text using Groq API
     */
    suspend fun translateText(text: String): Result<String> {
        return try {
            val apiKey = preferencesRepository.apiKey.first()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("API Key is not set"))
            }
            
            val model = preferencesRepository.selectedModel.first()
            if (model.isBlank()) {
                return Result.failure(Exception("Model is not selected"))
            }
            
            val prompt = "Translate the following Android string resource value into Vietnamese. Do not add explanations or surrounding quotes. Return ONLY the translated text. IMPORTANT: Do NOT translate technical identifiers, package names (like androidx.startup), class names, URLs, placeholders, or format specifiers (like %s, %d). Keep those exactly as they are in the original text. Original text: \"$text\""
            
            val request = ChatCompletionRequest(
                model = model,
                messages = listOf(ChatMessage(role = "user", content = prompt)),
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
                        return Result.success(response.choices[0].message.content.trim())
                    } else {
                        return Result.failure(Exception("No response from API"))
                    }
                } catch (e: HttpException) {
                    // Handle HTTP 429 Too Many Requests and HTTP 503 Service Unavailable
                    if (e.code() == 429 || e.code() == 503) {
                        val errorCode = e.code()
                        val errorMessage = if (errorCode == 429) "Rate limit exceeded (HTTP 429)" else "Service unavailable (HTTP 503)"
                        lastException = Exception("$errorMessage. Retrying...")
                        Log.w("GroqRepository", "HTTP $errorCode received, retrying after delay. Attempt ${retryCount + 1}/$maxRetries")
                        
                        // Exponential backoff: 1s, 2s, 4s
                        val delayMs = (1000L * Math.pow(2.0, retryCount.toDouble())).toLong()
                        delay(delayMs)
                        retryCount++
                    } else {
                        return Result.failure(e)
                    }
                } catch (e: Exception) {
                    // Handle timeout and other network errors
                    if (e.message?.contains("timeout", ignoreCase = true) == true ||
                        e.message?.contains("connection", ignoreCase = true) == true) {
                        lastException = Exception("Network timeout or connection error. Retrying...")
                        Log.w("GroqRepository", "Network error received, retrying after delay. Attempt ${retryCount + 1}/$maxRetries: ${e.message}")
                        
                        // Exponential backoff: 1s, 2s, 4s
                        val delayMs = (1000L * Math.pow(2.0, retryCount.toDouble())).toLong()
                        delay(delayMs)
                        retryCount++
                    } else {
                        return Result.failure(e)
                    }
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