package com.vtu.translate.util

import android.content.Context
import android.net.Uri
import com.vtu.translate.data.model.StringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class XmlParser {
    suspend fun parse(context: Context, uri: Uri): Result<List<StringResource>> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.let {
                    Result.success(parseXml(it))
                } ?: Result.failure(Exception("Failed to open input stream from URI."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    @Throws(XmlPullParserException::class, java.io.IOException::class)
    private fun parseXml(inputStream: InputStream): List<StringResource> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        val stringResources = mutableListOf<StringResource>()
        var eventType = parser.eventType
        var currentName: String? = null
        var isTranslatable = true

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "string") {
                        currentName = parser.getAttributeValue(null, "name")
                        val translatableAttr = parser.getAttributeValue(null, "translatable")
                        isTranslatable = translatableAttr?.equals("false") != true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (currentName != null && isTranslatable) {
                        val value = parser.text.trim()
                        if (value.isNotEmpty()) {
                            stringResources.add(StringResource(currentName!!, value))
                            currentName = null // Reset for next tag
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        inputStream.close()
        return stringResources
    }
} 