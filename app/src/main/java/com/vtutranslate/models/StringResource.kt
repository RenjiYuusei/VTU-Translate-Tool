package com.vtutranslate.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StringResource(
    val name: String,
    val value: String,
    var translatedValue: String = ""
) : Parcelable 