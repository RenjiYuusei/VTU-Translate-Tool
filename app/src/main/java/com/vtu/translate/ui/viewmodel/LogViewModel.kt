package com.vtu.translate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vtu.translate.data.repository.LogRepository
import kotlinx.coroutines.flow.StateFlow

class LogViewModel(private val logRepository: LogRepository) : ViewModel() {

    val logs: StateFlow<List<String>> = logRepository.logs

    fun clearLogs() {
        logRepository.clearLogs()
    }

    companion object {
        fun provideFactory(
            logRepository: LogRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LogViewModel(logRepository) as T
            }
        }
    }
} 