package com.vtu.translate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
 * Groq API chat request message
 */
@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

/**
 * Groq API chat completion request
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7
)

/**
 * Groq API chat completion response choice
 */
@Serializable
data class ChatCompletionChoice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String
)

/**
 * Groq API chat completion response
 */
@Serializable
data class ChatCompletionResponse(
    val id: String,
    @SerialName("object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<ChatCompletionChoice>,
    val usage: Usage
)

/**
 * Groq API usage information
 */
@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)