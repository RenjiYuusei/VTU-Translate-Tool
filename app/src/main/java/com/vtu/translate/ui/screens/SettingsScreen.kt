package com.vtu.translate.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vtu.translate.R
import com.vtu.translate.data.SettingsRepository
import com.vtu.translate.viewmodel.SettingsViewModel
import com.vtu.translate.viewmodel.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    var apiKeyVisible by remember { mutableStateOf(false) }

    val allModels = listOf(
        "google/gemini-2.0-flash-exp:free",
        "google/gemma-3-27b-it:free",
        "deepseek/deepseek-r1-0528:free",
        "deepseek/deepseek-chat-v3-0324:free"
    )
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()

    var currentApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var currentSelectedModel by remember(selectedModel) { mutableStateOf(selectedModel) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.api_key), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = currentApiKey,
            onValueChange = { currentApiKey = it },
            label = { Text(stringResource(id = R.string.api_key)) },
            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (apiKeyVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
            context.startActivity(intent)
        }) {
            Text(text = stringResource(id = R.string.get_api_key))
        }

        Spacer(modifier = Modifier.height(16.dp))

        ModelSelector(
            label = stringResource(id = R.string.model),
            models = allModels,
            selectedModel = currentSelectedModel,
            onModelSelected = { currentSelectedModel = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            viewModel.saveSettings(currentApiKey, currentSelectedModel)
            Toast.makeText(context, context.getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        }) {
            Text(text = stringResource(id = R.string.save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelector(
    label: String,
    models: List<String>,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedModel,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onModelSelected(model)
                        expanded = false
                    }
                )
            }
        }
    }
} 