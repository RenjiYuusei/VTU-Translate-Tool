package com.vtu.translate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<Message>
)

@Serializable
data class Message(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
) 