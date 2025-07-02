package com.vtu.translate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vtu.translate.R
import com.vtu.translate.data.model.LogEntry
import com.vtu.translate.data.model.LogType
import com.vtu.translate.ui.viewmodel.MainViewModel

@Composable
fun LogScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Log actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Copy logs button
            Button(
                onClick = {
                    val logsText = viewModel.getLogsAsText(context)
                    copyToClipboard(context, logsText)
                    Toast.makeText(
                        context,
                        context.getString(R.string.log_copied),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.weight(1f),
                enabled = logs.isNotEmpty()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_copy),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.copy_log))
            }
            
            // Clear logs button
            Button(
                onClick = {
                    viewModel.clearLogs()
                    Toast.makeText(
                        context,
                        context.getString(R.string.log_cleared),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.weight(1f),
                enabled = logs.isNotEmpty()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.clear_log))
            }
        }
        
        // Log entries
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.log_yet))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { logEntry ->
                    LogEntryItem(logEntry)
                }
            }
        }
    }
}

@Composable
fun LogEntryItem(logEntry: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Log timestamp and type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = logEntry.getFormattedTimestamp(),
                    style = MaterialTheme.typography.bodySmall
                )
                
                Box(
                    modifier = Modifier
                        .background(
                            color = getLogTypeColor(logEntry.type),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(logEntry.type.stringResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
            
            // Log message
            Text(
                text = logEntry.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Get color for log type
 */
@Composable
fun getLogTypeColor(logType: LogType): Color {
    return when (logType) {
        LogType.INFO -> MaterialTheme.colorScheme.primary
        LogType.SUCCESS -> Color.Green
        LogType.ERROR -> Color.Red
        LogType.WARNING -> Color(0xFFFFA500) // Orange
    }
}

/**
 * Copy text to clipboard
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Log Entries", text)
    clipboard.setPrimaryClip(clip)
}