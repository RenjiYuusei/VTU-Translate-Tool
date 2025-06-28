package com.vtu.translate.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.vtu.translate.R
import com.vtu.translate.ui.viewmodel.LogViewModel

@Composable
fun LogScreen(viewModel: LogViewModel) {
    val logs by viewModel.logs.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val logText = logs.joinToString("\n")
                    clipboardManager.setText(AnnotatedString(logText))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.log_copy))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { viewModel.clearLogs() },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.log_clear))
            }
        }
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.log_empty))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(logs) { log ->
                    Text(log, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
} 