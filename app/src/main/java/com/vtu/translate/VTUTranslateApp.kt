package com.vtu.translate

import android.app.Application
import com.vtu.translate.di.AppContainer
import com.vtu.translate.di.DefaultAppContainer

class VTUTranslateApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
} 