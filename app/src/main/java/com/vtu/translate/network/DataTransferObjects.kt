package com.vtu.translate.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String,
    val messages: List<Message>
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val id: String,
    val choices: List<Choice>
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: Message
) 