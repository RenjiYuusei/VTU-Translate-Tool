package com.vtu.translate.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vtu.translate.R
import com.vtu.translate.viewmodel.MainViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.vtu.translate.ui.components.XmlHighlighter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    val apiKey by mainViewModel.apiKey.collectAsState()
    val selectedModel by mainViewModel.selectedModel.collectAsState()
    val selectedFileContent by mainViewModel.selectedFileContent.collectAsState()
    val translatedFileContent by mainViewModel.translatedFileContent.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val selectedLanguage by mainViewModel.selectedLanguage.collectAsState()

    val context = LocalContext.current

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val fileName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }

            if (fileName != "strings.xml") {
                mainViewModel.onFileSelected(null)
                mainViewModel.clearErrorMessage()
                mainViewModel.setErrorMessage(context.getString(R.string.please_select_strings_xml))
                return@let
            }

            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { reader -> reader.readText() }
                    mainViewModel.onFileSelected(content)
                }
            } catch (e: Exception) {
                mainViewModel.onFileSelected(null)
                mainViewModel.clearErrorMessage()
                mainViewModel.setErrorMessage(context.getString(R.string.error_reading_file, e.message))
                e.printStackTrace()
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/xml")) {
        uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use {
                    outputStream ->
                    translatedFileContent?.let { content ->
                        outputStream.write(content.toByteArray())
                    }
                }
            } catch (e: Exception) {
                                mainViewModel.setErrorMessage(context.getString(R.string.error_saving_file, e.message))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.app_name)) })
        }
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    val models = listOf("google/gemma-3-27b-it:free", "deepseek/deepseek-r1-0528:free")
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
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Language Selection
                    Text(stringResource(id = R.string.select_target_language_label))
                    val languages = listOf(stringResource(id = R.string.language_vietnamese), stringResource(id = R.string.language_japanese), stringResource(id = R.string.language_french), stringResource(id = R.string.language_german), stringResource(id = R.string.language_spanish), stringResource(id = R.string.language_korean), stringResource(id = R.string.language_chinese))
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLanguage,
                            onValueChange = { /* Read-only */ },
                            readOnly = true,
                            label = { Text(stringResource(id = R.string.target_language_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded, contentDescription = stringResource(id = R.string.target_language_label)) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languages.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        mainViewModel.onLanguageSelected(language)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { pickFileLauncher.launch(arrayOf("application/xml", "text/xml", "text/plain")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.select_strings_xml))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            selectedFileContent?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.original_content_label))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            XmlHighlighter(xmlContent = it, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { mainViewModel.translateStringsXml() },
                enabled = !isLoading && !selectedFileContent.isNullOrEmpty() && !apiKey.isNullOrEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) stringResource(id = R.string.translating) else stringResource(id = R.string.translate))
            }
            Spacer(modifier = Modifier.height(16.dp))

            translatedFileContent?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.translated_content_label))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            XmlHighlighter(xmlContent = it, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { saveFileLauncher.launch("translated_strings.xml") },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.save_translated_file))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
