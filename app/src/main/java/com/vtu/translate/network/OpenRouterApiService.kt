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
    val max_tokens: Int? = null
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
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun translateText(
        model: String,
        apiKey: String,
        prompt: String
    ): String? {
        val requestBody = ChatCompletionRequest(
            model = model,
            messages = listOf(Message(role = "user", content = prompt)),
            max_tokens = 4096
        )
        log("Sending translation request to model: $model")

        return try {
            val response = client.post("https://openrouter.ai/api/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                header("HTTP-Referer", "https://vtu-translate-tool.com")
                header("X-Title", "VTU Translate Tool")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseBody = response.body<String>()
            log("Raw API Response: $responseBody")

            if (response.status.isSuccess()) {
                if (responseBody.isBlank()) {
                    log("API Error: Received empty response body with success status ${response.status}.")
                    return null
                }
                val chatResponse = Json{ignoreUnknownKeys=true}.decodeFromString<ChatCompletionResponse>(responseBody)
                chatResponse.choices.firstOrNull()?.message?.content
            } else {
                log("API Error ${response.status}: $responseBody")
                null
            }
        } catch (e: Exception) {
            log("Exception during API call: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}