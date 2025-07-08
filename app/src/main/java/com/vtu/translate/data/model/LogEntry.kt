package com.vtu.translate.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a log entry in the application
 */
data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val type: LogType,
    val message: String
) {
    fun getFormattedTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    fun getFormattedEntry(context: android.content.Context): String {
        val typeText = context.getString(type.stringResId)
        return "[${getFormattedTimestamp()}] $typeText: $message"
    }
}

/**
 * Types of log entries
 */
enum class LogType(val stringResId: Int) {
    INFO(com.vtu.translate.R.string.log_type_info),
    SUCCESS(com.vtu.translate.R.string.log_type_success),
    ERROR(com.vtu.translate.R.string.log_type_error),
    WARNING(com.vtu.translate.R.string.log_type_warning)
}
