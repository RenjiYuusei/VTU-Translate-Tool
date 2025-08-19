package com.vtu.translate.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Repository for interacting with Google Gemini Generative Language API
 */
@OptIn(ExperimentalSerializationApi::class)
class GeminiRepository(private val preferencesRepository: PreferencesRepository) {

    companion object {
        // v1beta models endpoint
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"
        private const val DEFAULT_MODEL = "models/gemini-2.5-flash"
    }

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
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

    private val service = retrofit.create(GeminiService::class.java)

    suspend fun translateText(text: String, targetLanguage: String = "vi"): Result<String> {
        return translateBatch(listOf(text), targetLanguage).map { it.first() }
    }

    suspend fun translateBatch(texts: List<String>, targetLanguage: String = "vi"): Result<List<String>> {
        return try {
            val apiKey = preferencesRepository.geminiApiKey.first()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API key chưa được thiết lập"))
            }

            val model = DEFAULT_MODEL // could be extended later to selectable

            val prompt = if (texts.size == 1) {
                "Translate the following Android string resource value into $targetLanguage. Return ONLY the translated text without quotes or extra text. Do NOT translate technical identifiers, package names, URLs, placeholders or format specifiers (%s, %d).\nOriginal: ${texts[0]}"
            } else {
                buildString {
                    append("Translate the following Android string resource values into $targetLanguage. Return one translated line per input, in order. Do NOT translate technical identifiers, package names, URLs, placeholders or format specifiers (%s, %d).\n")
                    texts.forEachIndexed { i, s -> append("${i + 1}. ").append(s).append('\n') }
                }
            }

            val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
            val response = service.generateContent("$model:generateContent?key=$apiKey", request)
            val textOut = response.candidates?.firstOrNull()?.content?.parts?.joinToString("") { it.text ?: "" }?.trim()
                ?: return Result.failure(Exception("No response from Gemini"))

            val outputs = if (texts.size == 1) listOf(textOut) else textOut.lines().filter { it.isNotBlank() }.take(texts.size)
            Result.success(outputs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// --- Retrofit API + DTOs ---
private interface GeminiService {
    @POST("v1beta/{modelPath}")
    suspend fun generateContent(
        @retrofit2.http.Path(value = "modelPath", encoded = true) modelPath: String,
        @Body request: GeminiRequest,
        @Header("Content-Type") contentType: String = "application/json"
    ): GeminiResponse
}

@kotlinx.serialization.Serializable
private data class GeminiRequest(val contents: List<Content>)

@kotlinx.serialization.Serializable
private data class Content(val parts: List<Part>)

@kotlinx.serialization.Serializable
private data class Part(val text: String? = null)

@kotlinx.serialization.Serializable
private data class GeminiResponse(val candidates: List<Candidate>? = null)

@kotlinx.serialization.Serializable
private data class Candidate(val content: Content)

