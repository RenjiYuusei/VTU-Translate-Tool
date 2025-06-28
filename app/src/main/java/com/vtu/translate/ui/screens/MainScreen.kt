package com.vtu.translate.ui.screens

import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import com.vtu.translate.ui.components.XmlHighlighter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, mainViewModel: MainViewModel = viewModel()) {
    val apiKey by mainViewModel.apiKey.collectAsState()
    val selectedFileContent by mainViewModel.selectedFileContent.collectAsState()
    val translatedFileContent by mainViewModel.translatedFileContent.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val selectedLanguage by mainViewModel.selectedLanguage.collectAsState()

    val context = LocalContext.current

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
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
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { navController.navigate("settings_screen") }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.settings_title))
                    }
                }
            )
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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

            val logs by mainViewModel.logs.collectAsState()
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(id = R.string.application_logs), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (logs.isEmpty()) {
                            Text("No logs yet.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            logs.forEach { logMessage ->
                                Text(logMessage, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { mainViewModel.clearLogs() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(id = R.string.clear_logs))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val clipboardManager = LocalClipboardManager.current
                        Button(
                            onClick = { clipboardManager.setText(AnnotatedString(logs.joinToString("\n"))) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(id = R.string.copy_logs))
                        }
                    }
                }
            }
        }
    }
}
