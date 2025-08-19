package com.vtu.translate.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.vtu.translate.service.TranslationService
import com.vtu.translate.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Kiểm tra xem một chuỗi có phải là chuỗi đặc biệt không cần dịch hay không
 * 
 * @param value Chuỗi cần kiểm tra
 * @return True nếu chuỗi là chuỗi đặc biệt không cần dịch
 */
private fun isSpecialNonTranslatableString(value: String): Boolean {
    // Cải tiến quy tắc để tránh false positive
    return when {
        value.trim().isEmpty() -> true // Chuỗi rỗng
        value.matches(Regex("^[0-9]+$")) -> true // Số thuần túy
        value.matches(Regex("^[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+){2,}$")) -> true // Package names có ít nhất 3 phần như androidx.startup.provider
        value.matches(Regex("^(http|https)://[a-zA-Z0-9./?_=-]+$")) -> true // URLs hoàn chỉnh
        value.startsWith("androidx.") && value.indexOf(".", 9) != -1 -> true // Androidx packages cụ thể
        value.startsWith("android.") && value.indexOf(".", 8) != -1 -> true // Android packages cụ thể
        value.startsWith("java.") && value.indexOf(".", 5) != -1 -> true // Java packages cụ thể
        value.startsWith("kotlin.") && value.indexOf(".", 7) != -1 -> true // Kotlin packages cụ thể
        value.contains(Regex("\\{\\w*\\}")) -> true // Placeholders như {0}, {name}
        value.contains(Regex("%[sdfx]")) -> true // Format specifiers như %s, %d
        value.matches(Regex("^[A-Z][A-Z_0-9]*$")) -> true // Constants như ACTION_MAIN
        value.matches(Regex("^[a-z][a-z0-9_]*$")) && value.length <= 6 -> true // Short technical identifiers
        else -> false
    }
}

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
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val isBackgroundEnabled by viewModel.isBackgroundTranslationEnabled.collectAsState()
    
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
        // Compact file selection button
        Button(
            onClick = { filePickerLauncher.launch("text/xml") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_file),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = stringResource(R.string.select_file),
                style = MaterialTheme.typography.bodyMedium
            )
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
        
        // Cleaning of unnecessary strings is done automatically during parsing and saving.
        
        // Background translation toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.translation_service_toggle))
            Switch(
                checked = isBackgroundEnabled,
                onCheckedChange = { isEnabled ->
                    // Check notification permission for Android 13+
                    if (isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.notification_permission_required),
                                Toast.LENGTH_LONG
                            ).show()
                            return@Switch
                        }
                    }
                    
                    viewModel.saveBackgroundTranslationEnabled(isEnabled)
                    if (isEnabled) {
                        // Start service immediately to show notification
                        context.startService(Intent(context, TranslationService::class.java))
                    } else {
                        context.stopService(Intent(context, TranslationService::class.java))
                    }
                }
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
                // Start translation button with animation
                val startButtonElevation by animateDpAsState(
                    targetValue = if (!isTranslating && stringResources.isNotEmpty()) 6.dp else 2.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
                
                ElevatedButton(
                    onClick = {
                        val currentApiKey = if (selectedProvider.lowercase() == "gemini") geminiApiKey else apiKey
                        if (currentApiKey.isBlank()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_no_api_key),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@ElevatedButton
                        }
                        
                        if (selectedModel.isBlank()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_no_model_selected),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@ElevatedButton
                        }
                        
                        if (stringResources.isEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_no_file_selected),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@ElevatedButton
                        }
                        
                        viewModel.startTranslation()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTranslating && stringResources.isNotEmpty(),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = startButtonElevation,
                        pressedElevation = 8.dp
                    ),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isTranslating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        contentColor = if (isTranslating) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    AnimatedContent(
                        targetState = isTranslating,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        }
                    ) { translating ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (translating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_start_translate),
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = stringResource(if (translating) R.string.translating else R.string.start_translate),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                
                // Stop translation button with animation
                val stopButtonElevation by animateDpAsState(
                    targetValue = if (isTranslating) 6.dp else 2.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
                
                ElevatedButton(
                    onClick = {
                        viewModel.stopTranslation()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isTranslating,
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = stopButtonElevation,
                        pressedElevation = 8.dp
                    ),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop_translate),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.stop_translation),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            // Continue translation button (only show if there's partially translated content)
            val translatedOrErrorCount = stringResources.count { it.translatedValue.isNotBlank() || it.hasError }
            val hasUntranslatedItems = stringResources.any { it.translatedValue.isBlank() && !it.hasError }
            
            if (hasUntranslatedItems && translatedOrErrorCount > 0 && !isTranslating) {
                Button(
                    onClick = {
                        val currentApiKey = if (selectedProvider.lowercase() == "gemini") geminiApiKey else apiKey
                        if (currentApiKey.isBlank()) {
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
                        
                        viewModel.continueTranslation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_start_translate),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "${stringResource(R.string.continue_translation)} ($translatedOrErrorCount/${stringResources.size})",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            // Save file button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val app = context.applicationContext as com.vtu.translate.VtuTranslateApp
                        val result = app.translationRepository.saveTranslatedFile(targetLanguage)
                        
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
        if (isTranslating || stringResources.any { it.translatedValue.isNotBlank() || it.hasError }) {
            // Count both translated and error items for consistency with notification
            val translatedCount = stringResources.count { it.translatedValue.isNotBlank() || it.hasError }
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
                        text = "$translatedCount/$totalCount ($progressPercent%)",
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
                        targetLanguage = targetLanguage,
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
    onTranslatedValueChange: (String) -> Unit,
    targetLanguage: String = "vi"
) {
    // Kiểm tra xem chuỗi có phải là chuỗi đặc biệt thực sự hay không
    // Chỉ đánh dấu là chuỗi đặc biệt nếu nó thỏa mãn các điều kiện của isSpecialNonTranslatableString
    val isSpecialCase = isSpecialNonTranslatableString(resource.value) && 
                       resource.translatedValue.isNotBlank() && 
                       !resource.isTranslating && 
                       !resource.hasError
    
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
                            text = "Đặc biệt",
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
            
            // Translated value with dynamic label based on target language
            val translatedValueLabelId = when (targetLanguage) {
                "vi" -> R.string.translated_value_vi
                "en" -> R.string.translated_value_en
                "zh" -> R.string.translated_value_zh
                "ru" -> R.string.translated_value_ru
                "ko" -> R.string.translated_value_ko
                "es" -> R.string.translated_value_es
                "fr" -> R.string.translated_value_fr
                "de" -> R.string.translated_value_de
                "ja" -> R.string.translated_value_ja
                else -> R.string.translated_value_default
            }
            
            OutlinedTextField(
                value = resource.translatedValue,
                onValueChange = onTranslatedValueChange,
                label = { Text(stringResource(translatedValueLabelId)) },
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
                    text = "Chuỗi này được giữ nguyên do là định danh kỹ thuật hoặc chuỗi đặc biệt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}