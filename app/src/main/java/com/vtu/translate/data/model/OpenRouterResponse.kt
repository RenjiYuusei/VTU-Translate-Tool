package com.vtu.translate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterResponse(
    @SerialName("choices") val choices: List<Choice>,
    @SerialName("error") val error: Error? = null
)

@Serializable
data class Choice(
    @SerialName("message") val message: ResponseMessage
)

@Serializable
data class ResponseMessage(
    @SerialName("content") val content: String
)

@Serializable
data class Error(
    @SerialName("message") val message: String
) 