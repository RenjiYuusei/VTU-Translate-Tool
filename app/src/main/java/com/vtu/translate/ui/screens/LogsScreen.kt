package com.vtu.translate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vtu.translate.R
import com.vtu.translate.viewmodel.LogsViewModel

@Composable
fun LogsScreen(
    viewModel: LogsViewModel = viewModel()
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.logs_tab), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(logs) { log ->
                Text(text = log, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                viewModel.clearLogs()
                Toast.makeText(context, R.string.log_cleared, Toast.LENGTH_SHORT).show()
            }) {
                Text(text = stringResource(id = R.string.clear_log))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("logs", logs.joinToString("\n"))
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, R.string.log_copied, Toast.LENGTH_SHORT).show()
            }) {
                Text(text = stringResource(id = R.string.copy_log))
            }
        }
    }
} 