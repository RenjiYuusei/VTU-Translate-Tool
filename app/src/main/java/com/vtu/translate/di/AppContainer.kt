package com.vtu.translate.di

import android.content.Context
import com.vtu.translate.data.local.SecureStorage
import com.vtu.translate.data.remote.ApiClient
import com.vtu.translate.data.repository.SettingsRepository
import com.vtu.translate.data.repository.TranslationRepository
import com.vtu.translate.util.FileSaver
import com.vtu.translate.util.XmlParser

interface AppContainer {
    val settingsRepository: SettingsRepository
    val translationRepository: TranslationRepository
    val xmlParser: XmlParser
    val fileSaver: FileSaver
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(SecureStorage(context))
    }

    override val translationRepository: TranslationRepository by lazy {
        TranslationRepository(ApiClient.instance)
    }

    override val xmlParser: XmlParser by lazy {
        XmlParser()
    }

    override val fileSaver: FileSaver by lazy {
        FileSaver()
    }
} 