package com.vtutranslate

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.vtutranslate.data.LogManager
import com.vtutranslate.network.OpenRouterApiClient

class VTUTranslateApp : Application() {

    companion object {
        lateinit var instance: VTUTranslateApp
            private set

        fun getSharedPreferences(): SharedPreferences {
            return instance.getSharedPreferences("vtu_translate_prefs", Context.MODE_PRIVATE)
        }
    }

    lateinit var apiClient: OpenRouterApiClient
    lateinit var logManager: LogManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        logManager = LogManager()
        logManager.log("Application started")
        
        // Initialize API client
        apiClient = OpenRouterApiClient()
    }
} 