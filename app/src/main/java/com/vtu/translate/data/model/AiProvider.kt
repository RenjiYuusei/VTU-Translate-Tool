package com.vtu.translate.data.model

/**
 * Enum representing different AI providers
 */
enum class AiProvider(
    val id: String,
    val displayName: String,
    val description: String,
    val requiresApiKey: Boolean = true
) {
    GROQ("groq", "Groq", "Dịch nhanh với các mô hình Llama", true),
    GEMINI("gemini", "Google Gemini", "Các mô hình tiên tiến của Google", true);

    companion object {
        fun fromId(id: String): AiProvider {
            return values().find { it.id == id } ?: GROQ
        }
    }
}
