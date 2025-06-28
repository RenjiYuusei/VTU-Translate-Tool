package com.vtu.translate.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.util.Log

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)

class OpenRouterApiService(private val log: (String) -> Unit) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun translateText(model: String, apiKey: String, text: String): String {
        val requestBody = ChatCompletionRequest(
            model = model,
            messages = listOf(Message(role = "user", content = "Translate the following text to Vietnamese: \"\"\"$text\"\"\""))
        )

        try {
            val response = client.post("https://openrouter.ai/api/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                header("HTTP-Referer", "https://vtu-translate-tool.com") // Replace with your actual domain
                header("X-Title", "VTU Translate Tool") // Replace with your actual app name
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                try {
                    val chatCompletionResponse: ChatCompletionResponse = response.body()
                    return chatCompletionResponse.choices.firstOrNull()?.message?.content ?: ""
                } catch (e: Exception) {
                    val errorBody = response.body<String>()
                    log("Error parsing successful API response: ${e.message}, Body: $errorBody")
                    return "Error parsing API response: ${e.message}"
                }
            } else {
                val errorBody = response.body<String>()
                log("API Error: ${response.status}, Body: $errorBody")
                return "Error: ${response.status}"
            }
        } catch (e: Exception) {
            log("Error parsing API response: ${e.message}")
            return "Error parsing API response: ${e.message}"
        }
    }
}