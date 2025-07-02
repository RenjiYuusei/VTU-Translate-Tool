package com.vtu.translate.data.repository

import com.vtu.translate.data.model.LogEntry
import com.vtu.translate.data.model.LogType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing application logs
 */
class LogRepository {
    
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    
    /**
     * Add an INFO log entry
     */
    fun logInfo(message: String) {
        addLogEntry(LogType.INFO, message)
    }
    
    /**
     * Add a SUCCESS log entry
     */
    fun logSuccess(message: String) {
        addLogEntry(LogType.SUCCESS, message)
    }
    
    /**
     * Add an ERROR log entry
     */
    fun logError(message: String) {
        addLogEntry(LogType.ERROR, message)
    }
    
    /**
     * Add a WARNING log entry
     */
    fun logWarning(message: String) {
        addLogEntry(LogType.WARNING, message)
    }
    
    /**
     * Add a log entry to the list
     */
    private fun addLogEntry(type: LogType, message: String) {
        val newEntry = LogEntry(type = type, message = message)
        _logs.value = _logs.value + newEntry
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        _logs.value = emptyList()
    }
    
    /**
     * Get all logs as text for copying
     */
    fun getLogsAsText(context: android.content.Context): String {
        return _logs.value.joinToString("\n") { it.getFormattedEntry(context) }
    }
}