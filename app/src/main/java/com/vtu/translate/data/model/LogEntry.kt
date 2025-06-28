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
    
    fun getFormattedEntry(): String {
        return "[${getFormattedTimestamp()}] ${type.prefix}: $message"
    }
}

/**
 * Types of log entries
 */
enum class LogType(val prefix: String) {
    INFO("INFO"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    WARNING("WARNING")
}