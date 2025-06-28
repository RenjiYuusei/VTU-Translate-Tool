package com.vtu.translate.viewmodel

import androidx.lifecycle.ViewModel
import com.vtu.translate.data.LogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class LogsViewModel : ViewModel() {
    val logs = LogRepository.logs.stateIn(
        scope = androidx.lifecycle.viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun clearLogs() {
        LogRepository.clearLogs()
    }
} 