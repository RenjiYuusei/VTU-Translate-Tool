package com.vtu.translate.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vtu.translate.R
import com.vtu.translate.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(mainViewModel: MainViewModel = viewModel()) {
    val apiKey by mainViewModel.apiKey.collectAsState()
    val selectedModel by mainViewModel.selectedModel.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.settings_title)) })
        }
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        var passwordVisible by rememberSaveable { mutableStateOf(false) }
                        OutlinedTextField(
                            value = apiKey ?: "",
                            onValueChange = { mainViewModel.onApiKeyChange(it) },
                            label = { Text(stringResource(id = R.string.api_key_hint)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff
                                val description = if (passwordVisible) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp) // Fixed height for alignment
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.height(56.dp) // Fixed height for alignment
                        ) {
                            Text(stringResource(id = R.string.get_api_key))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Model Selection
                    var modelExpanded by remember { mutableStateOf(false) }
                    val models = listOf("google/gemini-2.0-flash-exp:free", "google/gemma-3-27b-it:free", "deepseek/deepseek-r1-0528:free")
                    ExposedDropdownMenuBox(
                        expanded = modelExpanded,
                        onExpandedChange = { modelExpanded = !modelExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedModel,
                            onValueChange = { /* Read-only */ },
                            readOnly = true,
                            label = { Text(stringResource(id = R.string.select_ai_model_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = modelExpanded,
                            onDismissRequest = { modelExpanded = false }
                        ) {
                            models.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model) },
                                    onClick = {
                                        mainViewModel.onModelSelected(model)
                                        modelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Language Selection
                    var languageExpanded by remember { mutableStateOf(false) }
                    val languages = listOf(stringResource(id = R.string.language_vietnamese), stringResource(id = R.string.language_japanese), stringResource(id = R.string.language_french), stringResource(id = R.string.language_german), stringResource(id = R.string.language_spanish), stringResource(id = R.string.language_korean), stringResource(id = R.string.language_chinese))
                    ExposedDropdownMenuBox(
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = !languageExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTargetLanguage,
                            onValueChange = { /* Read-only */ },
                            readOnly = true,
                            label = { Text(stringResource(id = R.string.select_target_language_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            languages.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        mainViewModel.onTargetLanguageSelected(language)
                                        languageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}