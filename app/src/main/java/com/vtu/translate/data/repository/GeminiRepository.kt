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
import retrofit2.http.Path

/**
 * Repository for interacting with Google Gemini Generative Language API
 */
@OptIn(ExperimentalSerializationApi::class)
class GeminiRepository(private val preferencesRepository: PreferencesRepository) {

    companion object {
        // v1beta models endpoint
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
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

            // Ensure we always use a valid Gemini model
            val selected = preferencesRepository.selectedModel.first()
            val model = selected.takeIf { it.isNotBlank() && it.startsWith("gemini") } ?: DEFAULT_MODEL

            val prompt = if (texts.size == 1) {
                "Translate the following Android string resource value into $targetLanguage. Return ONLY the translated text without quotes, numbering, bullets, or extra text. Do NOT translate technical identifiers, package names, URLs, placeholders or format specifiers (%s, %d).\nOriginal: ${texts[0]}"
            } else {
                buildString {
                    append("Translate the following Android string resource values into $targetLanguage. Return ONLY the translated texts, one per line, in the same order. Do NOT include numbers, bullets, brackets, or any extra characters. Output plain text lines only. Do NOT translate technical identifiers, package names, URLs, placeholders or format specifiers (%s, %d).\n")
                    append("Original texts:\n")
                    texts.forEach { s -> append(s).append('\n') }
                }
            }

            val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
            val response = service.generateContent(model, request, apiKey)
            val textOut = response.candidates?.firstOrNull()?.content?.parts?.joinToString("") { it.text ?: "" }?.trim()
                ?: return Result.failure(Exception("No response from Gemini"))

            // Clean lines: strip quotes, leading numbering/bullets if any
            fun cleanLine(s: String): String {
                val unquoted = s.trim().removeSurrounding("\"").removeSurrounding("'")
                return unquoted.replace(Regex("^\\s*(?:[0-9]+[.)]|[-•])\\s*"), "").trim()
            }

            val outputs = if (texts.size == 1) {
                listOf(cleanLine(textOut))
            } else {
                textOut.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { cleanLine(it) }
                    .take(texts.size)
            }
            Result.success(outputs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// --- Retrofit API + DTOs ---
private interface GeminiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Path("model") model: String,
        @Body request: GeminiRequest,
        @Header("x-goog-api-key") apiKey: String
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

