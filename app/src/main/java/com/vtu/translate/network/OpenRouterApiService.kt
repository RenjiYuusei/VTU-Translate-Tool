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

class OpenRouterApiService {

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
                val chatCompletionResponse: ChatCompletionResponse = response.body()
                return chatCompletionResponse.choices.firstOrNull()?.message?.content ?: ""
            } else {
                val errorBody = response.body<String>()
                println("API Error: ${response.status}, Body: $errorBody")
                return "Error: ${response.status}"
            }
        } catch (e: Exception) {
            println("Error parsing API response: ${e.message}")
            return "Error parsing API response: ${e.message}"
        }
    }
}