package com.vtu.translate.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object LogRepository {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun addLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val newLogs = _logs.value.toMutableList()
        newLogs.add(0, "$timestamp - $message")
        _logs.value = newLogs
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
} 