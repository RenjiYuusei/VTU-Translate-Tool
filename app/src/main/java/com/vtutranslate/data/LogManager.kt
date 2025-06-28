package com.vtutranslate.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogManager {
    private val _logs = MutableLiveData<String>("")
    val logs: LiveData<String> = _logs

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun log(message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message"
        val currentLogs = _logs.value ?: ""
        val updatedLogs = if (currentLogs.isEmpty()) logEntry else "$currentLogs\n$logEntry"
        _logs.postValue(updatedLogs)
        
        // Also log to Android's system log for debugging
        android.util.Log.d("VTUTranslate", message)
    }

    fun clear() {
        _logs.postValue("")
    }

    fun getLogs(): String {
        return _logs.value ?: ""
    }
} 