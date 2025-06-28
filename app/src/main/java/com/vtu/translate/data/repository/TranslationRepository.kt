package com.vtu.translate.data.repository

import com.vtu.translate.data.model.Message
import com.vtu.translate.data.model.OpenRouterRequest
import com.vtu.translate.data.remote.OpenRouterApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslationRepository(private val openRouterApi: OpenRouterApi) {

    suspend fun translate(
        apiKey: String,
        model: String,
        text: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "Translate the following Android string resource value into Vietnamese. Do not add explanations or surrounding quotes. Just return the translated text. Original text: \"$text\""
                val request = OpenRouterRequest(
                    model = model,
                    messages = listOf(Message(role = "user", content = prompt))
                )
                val response = openRouterApi.getTranslation("Bearer $apiKey", request)

                if (response.error != null) {
                    Result.failure(Exception(response.error.message))
                } else if (response.choices.isNotEmpty()) {
                    val translatedText = response.choices[0].message.content.trim().removeSurrounding("\"")
                    Result.success(translatedText)
                } else {
                    Result.failure(Exception("Unknown error: Empty response from API."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 