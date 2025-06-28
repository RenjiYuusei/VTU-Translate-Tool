package com.vtu.translate.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
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
import com.vtu.translate.data.LogRepository
import com.vtu.translate.data.SettingsRepository
import com.vtu.translate.viewmodel.TranslateViewModel
import com.vtu.translate.viewmodel.TranslateViewModelFactory

@Composable
fun TranslateScreen(
    viewModel: TranslateViewModel = viewModel(
        factory = TranslateViewModelFactory(
            SettingsRepository(LocalContext.current),
            LogRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onFileSelected(uri, context)
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/xml")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(viewModel.getTranslatedXmlContent().toByteArray())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { filePickerLauncher.launch("application/xml") }) {
            Text(text = stringResource(id = R.string.load_strings_xml))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isTranslating) {
            LinearProgressIndicator(progress = uiState.progress, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Original", style = MaterialTheme.typography.headlineSmall)
                LazyColumn {
                    items(uiState.originalStrings.toList()) { (key, value) ->
                        Text("$key: $value", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Translated", style = MaterialTheme.typography.headlineSmall)
                LazyColumn {
                    items(uiState.translatedStrings.toList()) { (key, value) ->
                        Text("$key: $value", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row {
            Button(
                onClick = { viewModel.translateAll() },
                enabled = uiState.originalStrings.isNotEmpty() && !uiState.isTranslating
            ) {
                Text(text = stringResource(id = R.string.translate_tab))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { saveFileLauncher.launch("strings.xml") },
                enabled = uiState.translatedStrings.isNotEmpty() && !uiState.isTranslating
            ) {
                Text(text = stringResource(id = R.string.save_translated_file))
            }
        }
    }
} 