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
                mainViewModel.setErrorMessage("Please select a file named strings.xml.")
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
                mainViewModel.setErrorMessage("Error reading file: ${e.message}")
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
                        OutlinedTextField(
                            value = apiKey ?: "",
                            onValueChange = { mainViewModel.onApiKeyChange(it) },
                            label = { Text(stringResource(id = R.string.openrouter_api_key)) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
                            context.startActivity(intent)
                        }) {
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
                            label = { Text("Select AI Model") },
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

                    Button(
                        onClick = { pickFileLauncher.launch(arrayOf("application/xml", "text/xml", "text/plain")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select strings.xml")
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
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { mainViewModel.translateStringsXml() },
                enabled = !isLoading && !selectedFileContent.isNullOrEmpty() && !apiKey.isNullOrEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Translating..." else "Translate")
            }
            Spacer(modifier = Modifier.height(16.dp))

            translatedFileContent?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Translated File")
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
