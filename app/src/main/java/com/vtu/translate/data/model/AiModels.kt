package com.vtu.translate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Common interface for all AI providers
 */
interface AiApiService {
    suspend fun translateText(text: String, targetLanguage: String): Result<String>
    suspend fun translateBatch(texts: List<String>, targetLanguage: String): Result<List<String>>
    suspend fun getSelectedModel(): String
    suspend fun fetchAvailableModels(): Result<List<String>>
}

/**
 * Generic chat message for all providers
 */
@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

/**
 * Generic translation request
 */
data class TranslationRequest(
    val texts: List<String>,
    val targetLanguage: String,
    val model: String
)

/**
 * Generic translation response
 */
data class TranslationResponse(
    val translatedTexts: List<String>,
    val usage: TokenUsage? = null
)

/**
 * Token usage information
 */
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

// ========== GROQ MODELS ==========

/**
 * Groq API model information
 */
@Serializable
data class GroqModel(
    val id: String,
    @SerialName("object")
    val objectType: String,
    val created: Long,
    @SerialName("owned_by")
    val ownedBy: String,
    val active: Boolean,
    @SerialName("context_window")
    val contextWindow: Int,
    @SerialName("public_apps")
    val publicApps: String? = null
)

/**
 * Groq API models response
 */
@Serializable
data class GroqModelsResponse(
    @SerialName("object")
    val objectType: String,
    val data: List<GroqModel>
)

/**
 * Groq API chat completion request
 */
@Serializable
data class GroqChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 1.0,
)

/**
 * Groq API chat completion response choice
 */
@Serializable
data class GroqChatCompletionChoice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String
)

/**
 * Groq API chat completion response
 */
@Serializable
data class GroqChatCompletionResponse(
    val id: String,
    @SerialName("object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<GroqChatCompletionChoice>,
    val usage: GroqUsage
)

/**
 * Groq API usage information
 */
@Serializable
data class GroqUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

// ========== GEMINI MODELS ==========

/**
 * Gemini AI model information
 */
@Serializable
data class GeminiModel(
    val name: String,
    val displayName: String? = null,
    val description: String? = null,
    val inputTokenLimit: Int? = null,
    val outputTokenLimit: Int? = null,
    val supportedGenerationMethods: List<String>? = null,
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null
)

/**
 * Gemini API models response
 */
@Serializable
data class GeminiModelsResponse(
    val models: List<GeminiModel>
)

/**
 * Gemini API content part
 */
@Serializable
data class GeminiPart(
    val text: String
)

/**
 * Gemini API content
 */
@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

/**
 * Gemini API generation config
 */
@Serializable
data class GeminiGenerationConfig(
    val temperature: Double? = null,
    @SerialName("topK")
    val topK: Int? = null,
    @SerialName("topP")
    val topP: Double? = null,
    val maxOutputTokens: Int? = null,
    val stopSequences: List<String>? = null
)

/**
 * Gemini API safety settings
 */
@Serializable
data class GeminiSafetySetting(
    val category: String,
    val threshold: String
)

/**
 * Gemini API generate content request
 */
@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val safetySettings: List<GeminiSafetySetting>? = null
)

/**
 * Gemini API candidate response
 */
@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null,
    val index: Int? = null,
    val safetyRatings: List<GeminiSafetyRating>? = null
)

/**
 * Gemini API safety rating
 */
@Serializable
data class GeminiSafetyRating(
    val category: String,
    val probability: String
)

/**
 * Gemini API usage metadata
 */
@Serializable
data class GeminiUsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null
)

/**
 * Gemini API generate content response
 */
@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null,
    val usageMetadata: GeminiUsageMetadata? = null,
    val promptFeedback: GeminiPromptFeedback? = null
)

/**
 * Gemini API prompt feedback
 */
@Serializable
data class GeminiPromptFeedback(
    val blockReason: String? = null,
    val safetyRatings: List<GeminiSafetyRating>? = null
)
