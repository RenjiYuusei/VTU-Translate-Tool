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

    suspend fun translateText(
        model: String,
        apiKey: String,
        prompt: String
    ): String? {
        val requestBody = ChatCompletionRequest(
            model = model,
            messages = listOf(Message(role = "user", content = prompt))
        )

        return try {
            val response = client.post("https://openrouter.ai/api/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                header("Content-Type", "application/json")
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val chatResponse = response.body<ChatCompletionResponse>()
                chatResponse.choices.firstOrNull()?.message?.content
            } else {
                val errorBody = response.body<String>()
                log("API Error ${response.status}: $errorBody")
                null
            }
        } catch (e: Exception) {
            log("Exception during API call: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}