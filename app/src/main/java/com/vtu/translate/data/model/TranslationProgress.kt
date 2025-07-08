package com.vtu.translate.data.model

data class TranslationProgress(
    val current: Int,
    val total: Int,
    val currentName: String? = null,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)
