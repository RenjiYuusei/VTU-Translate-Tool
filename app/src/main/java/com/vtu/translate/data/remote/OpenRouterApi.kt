package com.vtu.translate.data.remote

import com.vtu.translate.data.model.OpenRouterRequest
import com.vtu.translate.data.model.OpenRouterResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun getTranslation(
        @Header("Authorization") apiKey: String,
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
} 