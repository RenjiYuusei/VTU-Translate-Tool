package com.vtu.translate.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") apiKey: String,
        @Body chatRequest: ChatRequest
    ): ChatResponse
} 