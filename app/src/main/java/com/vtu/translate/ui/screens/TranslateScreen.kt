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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Translation buttons row
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
                
                // Stop translation button
                Button(
                    onClick = {
                        viewModel.stopTranslation()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isTranslating
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop_translate),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.stop_translation))
                }
            }
            
            // Save file button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val app = context.applicationContext as com.vtu.translate.VtuTranslateApp
                        val result = app.translationRepository.saveTranslatedFile()
                        
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
                modifier = Modifier.fillMaxWidth(),
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
        
        // Show translation progress
        if (isTranslating || stringResources.any { it.translatedValue.isNotBlank() }) {
            val translatedCount = stringResources.count { it.translatedValue.isNotBlank() }
            val totalCount = stringResources.size
            val progressPercent = if (totalCount > 0) (translatedCount * 100 / totalCount) else 0
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đã dịch: $translatedCount/$totalCount chuỗi ($progressPercent%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                // Progress bar
                LinearProgressIndicator(
                    progress = translatedCount.toFloat() / totalCount.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
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
    val isSpecialCase = resource.translatedValue.isNotBlank() && !resource.isTranslating && !resource.hasError
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpecialCase) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Resource name with badge for special cases
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = resource.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (isSpecialCase) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Tự động",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (resource.hasError) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Lỗi",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
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
                enabled = !resource.isTranslating,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface, // Keep text visible when disabled
                    disabledBorderColor = if (isSpecialCase) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledLabelColor = if (isSpecialCase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            
            // Show info message for special cases
            if (isSpecialCase) {
                Text(
                    text = "Chuỗi này được dịch tự động dựa trên quy tắc đặc biệt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}