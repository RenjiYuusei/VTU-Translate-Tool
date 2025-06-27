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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vtu.translate.viewmodel.MainViewModel

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

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use {
                    inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    mainViewModel.onFileSelected(content)
                }
            } catch (e: Exception) {
                mainViewModel.clearErrorMessage()
                mainViewModel.onFileSelected("Error reading file: ${e.message}")
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
                mainViewModel.clearErrorMessage()
                mainViewModel.onFileSelected("Error saving file: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("VTU Translate Tool") })
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
            OutlinedTextField(
                value = apiKey ?: "",
                onValueChange = { mainViewModel.onApiKeyChange(it) },
                label = { Text("OpenRouter API Key") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Model Selection
            Text("Select AI Model:")
            Row {
                val models = listOf("google/gemma-3-27b-it:free", "deepseek/deepseek-r1-0528:free")
                models.forEach { model ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedModel == model),
                            onClick = { mainViewModel.onModelSelected(model) }
                        )
                        Text(model)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Language Selection
            Text("Select Target Language:")
            val languages = listOf("Vietnamese", "Japanese", "French", "German", "Spanish", "Korean", "Chinese")
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = { /* Read-only */ },
                    readOnly = true,
                    label = { Text("Target Language") },
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

            Button(onClick = { pickFileLauncher.launch(arrayOf("application/xml")) }) {
                Text("Select strings.xml")
            }
            Spacer(modifier = Modifier.height(16.dp))

            selectedFileContent?.let {
                Text("Original Content:")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(it, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { mainViewModel.translateStringsXml() },
                enabled = !isLoading && !selectedFileContent.isNullOrEmpty() && !apiKey.isNullOrEmpty()
            ) {
                Text(if (isLoading) "Translating..." else "Translate")
            }
            Spacer(modifier = Modifier.height(16.dp))

            translatedFileContent?.let {
                Text("Translated Content:")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(it, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { saveFileLauncher.launch("translated_strings.xml") },
                    enabled = !isLoading
                ) {
                    Text("Save Translated File")
                }
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
