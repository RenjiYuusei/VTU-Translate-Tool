package com.vtu.translate.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vtu.translate.R
import com.vtu.translate.data.model.StringResource
import com.vtu.translate.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun TranslateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val stringResources by viewModel.stringResources.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val selectedFileName by viewModel.selectedFileName.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val app = context.applicationContext as com.vtu.translate.VtuTranslateApp
                val result = app.translationRepository.parseStringsXml(context, uri)
                
                if (result.isFailure) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_invalid_file),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // File selection button
        Button(
            onClick = { filePickerLauncher.launch("text/xml") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_file),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.select_file))
        }
        
        // Show selected file name
        if (selectedFileName != null) {
            Text(
                text = selectedFileName ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = stringResource(R.string.no_file_selected),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Translation actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Start translation button
            Button(
                onClick = {
                    if (apiKey.isBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_no_api_key),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    
                    if (selectedModel.isBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_no_model_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    
                    if (stringResources.isEmpty()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_no_file_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    
                    viewModel.startTranslation()
                },
                modifier = Modifier.weight(1f),
                enabled = !isTranslating && stringResources.isNotEmpty()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_start_translate),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(if (isTranslating) R.string.translating else R.string.start_translate))
            }
            
            // Save file button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val app = context.applicationContext as com.vtu.translate.VtuTranslateApp
                        val result = app.translationRepository.saveTranslatedFile(context)
                        
                        if (result.isSuccess) {
                            val filePath = result.getOrNull()
                            Toast.makeText(
                                context,
                                context.getString(R.string.file_saved, filePath),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val error = result.exceptionOrNull()?.message ?: "Unknown error"
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_saving_file, error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isTranslating && stringResources.isNotEmpty()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.save_translated_file))
            }
        }
        
        // Show loading indicator if translating
        if (isTranslating) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // List of string resources
        if (stringResources.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(stringResources) { index, resource ->
                    StringResourceItem(
                        resource = resource,
                        onTranslatedValueChange = { newValue ->
                            viewModel.updateTranslation(index, newValue)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StringResourceItem(
    resource: StringResource,
    onTranslatedValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Resource name
            Text(
                text = resource.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Original value
            OutlinedTextField(
                value = resource.value,
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.original_value)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Translated value
            OutlinedTextField(
                value = resource.translatedValue,
                onValueChange = onTranslatedValueChange,
                label = { Text(stringResource(R.string.translated_value)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !resource.isTranslating
            )
            
            // Show loading indicator if translating
            if (resource.isTranslating) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}