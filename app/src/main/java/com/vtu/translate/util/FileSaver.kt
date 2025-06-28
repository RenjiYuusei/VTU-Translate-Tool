package com.vtu.translate.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.vtu.translate.data.model.StringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

class FileSaver {
    suspend fun save(context: Context, stringResources: List<StringResource>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val content = generateXmlContent(stringResources)
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "strings.xml")
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/xml")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/res/values-vi")
                    }
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it).use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(content)
                        }
                    }
                    Result.success(uri.toString())
                } ?: Result.failure(Exception("Failed to create file in Downloads."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun generateXmlContent(stringResources: List<StringResource>): String {
        val builder = StringBuilder()
        builder.appendLine("<resources>")
        stringResources.forEach { resource ->
            // Escape characters for XML
            val value = resource.translatedValue
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "\\'")
                .replace("\"", "\\\"")

            if (value.isNotEmpty()) {
                builder.appendLine("    <string name=\"${resource.name}\">${value}</string>")
            }
        }
        builder.appendLine("</resources>")
        return builder.toString()
    }
} 