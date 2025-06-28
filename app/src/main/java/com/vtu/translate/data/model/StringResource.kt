package com.vtu.translate.data.model

import androidx.compose.runtime.Stable

@Stable
data class StringResource(
    val name: String,
    val originalValue: String,
    var translatedValue: String = ""
) 