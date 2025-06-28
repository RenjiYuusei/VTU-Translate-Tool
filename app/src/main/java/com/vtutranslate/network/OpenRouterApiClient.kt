package com.vtutranslate.network

import com.vtutranslate.VTUTranslateApp
import com.vtutranslate.data.SettingsManager
import com.vtutranslate.models.StringResource
import com.vtutranslate.models.TranslationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenRouterApiClient {
    private val settingsManager = SettingsManager()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BASE_URL = "https://openrouter.ai/api/v1/chat/completions"
    }

    suspend fun translateStrings(
        stringResources: List<StringResource>
    ): List<StringResource> = withContext(Dispatchers.IO) {
        val model = settingsManager.getSelectedModel()
        val apiKey = settingsManager.getApiKey()
        val logManager = VTUTranslateApp.instance.logManager
        
        if (apiKey.isBlank()) {
            logManager.log("Error: API key not provided")
            throw IllegalStateException("API key not provided")
        }

        logManager.log("Starting translation using model: ${model.displayName}")
        
        // Create string representation for all resources
        val stringsList = stringResources.joinToString("\\n") { 
            "<string name=\"${it.name}\">${it.value}</string>" 
        }
        
        // Prepare the prompt
        val prompt = "Translate the following strings.xml entries from English to Vietnamese. " +
                "Maintain the same format and keep the structure intact. Only translate the text content, " +
                "not the XML tags or attribute names. Here are the strings:\\n$stringsList"
        
        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val requestJson = JSONObject().apply {
            put("model", model.modelId)
            put("messages", messagesArray)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            
            if (!response.isSuccessful) {
                logManager.log("API Error: ${response.code} - $responseBody")
                throw Exception("API Error: ${response.code}")
            }
            
            logManager.log("Received translation response")
            
            // Parse the response
            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.getJSONArray("choices")
            
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")
                
                // Extract translated strings from the content
                val translatedStrings = parseTranslatedContent(content)
                
                // Match translations with original strings
                stringResources.forEach { resource ->
                    val translatedValue = translatedStrings[resource.name]
                    if (translatedValue != null) {
                        resource.translatedValue = translatedValue
                        logManager.log("Translated: ${resource.name}")
                    } else {
                        logManager.log("Warning: No translation found for ${resource.name}")
                    }
                }
                
                logManager.log("Translation completed")
            } else {
                logManager.log("Error: No choices in response")
                throw Exception("No choices in response")
            }
            
        } catch (e: Exception) {
            logManager.log("Translation error: ${e.message}")
            throw e
        }
        
        return@withContext stringResources
    }
    
    private fun parseTranslatedContent(content: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = "<string\\s+name=[\"']([^\"']+)[\"']>([^<]*)</string>".toRegex()
        val matches = regex.findAll(content)
        
        for (match in matches) {
            val name = match.groupValues[1]
            val value = match.groupValues[2]
            result[name] = value
        }
        
        return result
    }
} 