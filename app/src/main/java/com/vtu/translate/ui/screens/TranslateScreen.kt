package com.vtu.translate.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vtu.translate.R
import com.vtu.translate.ui.viewmodel.TranslateUiState
import com.vtu.translate.ui.viewmodel.TranslateViewModel

@Composable
fun TranslateScreen(viewModel: TranslateViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val stringResources = viewModel.stringResources

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.parseFile(context, it)
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TranslateUiState.Success -> {
                if(state.message.isNotEmpty())
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            is TranslateUiState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { filePickerLauncher.launch("text/xml") },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.translate_select_file))
            }
            Button(
                onClick = { viewModel.startTranslation() },
                enabled = stringResources.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.translate_start))
            }
            Button(
                onClick = { viewModel.saveFile(context) },
                enabled = stringResources.isNotEmpty() && stringResources.any { it.translatedValue.isNotEmpty() },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.translate_save_file))
            }
        }

        if (uiState is TranslateUiState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (stringResources.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_file_selected))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(stringResources) { index, resource ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(resource.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = resource.originalValue,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.original_value_header)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = resource.translatedValue,
                                onValueChange = { newValue ->
                                    viewModel.updateTranslatedValue(index, newValue)
                                },
                                label = { Text(stringResource(R.string.translated_value_header)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
} 