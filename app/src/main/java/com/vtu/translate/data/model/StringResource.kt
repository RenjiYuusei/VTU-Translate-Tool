package com.vtu.translate.data.model

/**
 * Represents a string resource from an Android strings.xml file
 */
data class StringResource(
    val name: String,
    val value: String,
    var translatedValue: String = "",
    var isTranslating: Boolean = false,
    var hasError: Boolean = false
)