package com.vtu.translate.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import com.vtu.translate.R
import com.vtu.translate.ui.viewmodel.MainViewModel
import com.vtu.translate.data.model.ThemeMode

/**
 * Validate if the API key is a valid Groq API key format
 */
fun isValidGroqApiKey(apiKey: String): Boolean {
    // Groq API keys start with "gsk_" and have a specific length
    return apiKey.startsWith("gsk_") && apiKey.length >= 20
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val translationSpeed by viewModel.translationSpeed.collectAsState()
    val context = LocalContext.current
    
    // State for expanded sections
    var expandedApiSettings by remember { mutableStateOf(false) }
    var expandedInterfaceSettings by remember { mutableStateOf(false) }
    var expandedTranslationSettings by remember { mutableStateOf(false) }
    var expandedAbout by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        SettingsHeader()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // API Settings
        SettingsCategoryItem(
            icon = R.drawable.ic_key,
            title = stringResource(R.string.api_settings_title),
            subtitle = stringResource(R.string.api_settings_subtitle),
            onClick = { expandedApiSettings = !expandedApiSettings }
        )
        
            AnimatedVisibility(
                visible = expandedApiSettings,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ApiKeySection(viewModel, apiKey)
                    ModelSelectionSection(viewModel, selectedModel)
                }
            }
        
        // Interface Settings
        SettingsCategoryItem(
            icon = R.drawable.ic_interface,
            title = stringResource(R.string.interface_title),
            subtitle = stringResource(R.string.interface_subtitle),
            onClick = { expandedInterfaceSettings = !expandedInterfaceSettings }
        )
        
        AnimatedVisibility(
            visible = expandedInterfaceSettings,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            InterfaceSettingsSection(viewModel, themeMode)
        }
        
        // Translation Settings
        SettingsCategoryItem(
            icon = R.drawable.ic_translate,
            title = stringResource(R.string.translation_settings_title),
            subtitle = stringResource(R.string.translation_settings_subtitle),
            onClick = { expandedTranslationSettings = !expandedTranslationSettings }
        )
        
        AnimatedVisibility(
            visible = expandedTranslationSettings,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            TranslationSettingsSection(viewModel, translationSpeed)
        }
        
        // About
        val versionName = getAppVersion(context)
        SettingsCategoryItem(
            icon = R.drawable.ic_info,
            title = stringResource(R.string.about_title),
            subtitle = "${stringResource(R.string.app_version)}: $versionName",
            onClick = { expandedAbout = !expandedAbout }
        )
        
        AnimatedVisibility(
            visible = expandedAbout,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CreditsSection()
        }
    }
}

@Composable
fun getAppVersion(context: android.content.Context): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName ?: "Unknown"
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName ?: "Unknown"
        }
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }
}

@Composable
fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
    }
}

@Composable
fun SettingsCategoryItem(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ApiKeySection(
    viewModel: MainViewModel,
    apiKey: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf(apiKey) }
    
    SettingsSectionCard(
        title = stringResource(R.string.api_key_title),
        icon = R.drawable.ic_key,
        modifier = modifier
    ) {
        // API Key input field
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text(stringResource(R.string.api_key_title)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save API Key button
            ElevatedButton(
                onClick = {
                    if (apiKeyInput.isBlank()) {
                        Toast.makeText(
                            context,
                            "Vui lòng nhập API key",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@ElevatedButton
                    }
                    
                    if (!isValidGroqApiKey(apiKeyInput)) {
                        Toast.makeText(
                            context,
                            "API key không đúng định dạng Groq (phải bắt đầu với gsk_)",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@ElevatedButton
                    }
                    
                    viewModel.saveApiKey(apiKeyInput)
                    Toast.makeText(
                        context,
                        context.getString(R.string.api_key_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.api_key_saved))
            }
            
            // Get API Key button
            ElevatedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"))
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_key),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
                Text(stringResource(R.string.get_api_key))
            }
        }
    }
}

@Composable
fun ModelSelectionSection(
    viewModel: MainViewModel,
    selectedModel: String,
    modifier: Modifier = Modifier
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val context = LocalContext.current
    
    SettingsSectionCard(
        title = stringResource(R.string.select_model),
        icon = R.drawable.ic_model,
        modifier = modifier
    ) {
        // Auto-fetch models when API key changes
        LaunchedEffect(apiKey) {
            if (apiKey.isNotBlank()) {
                viewModel.fetchAvailableModels()
            }
        }
        
        // Model selection dropdown with animation
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Model dropdown for Groq API
                ModelSelectionDropdown(
                    viewModel = viewModel,
                    selectedModel = selectedModel,
                    onModelSelected = { viewModel.saveSelectedModel(it) }
                )
                
                // Refresh models button
                ElevatedButton(
                    onClick = {
                        if (apiKey.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please enter API Key first",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.fetchAvailableModels()
                            Toast.makeText(
                                context,
                                context.getString(R.string.refreshing_models),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.refresh_models))
                }
            }
        }
    }
}


@Composable
fun InterfaceSettingsSection(
    viewModel: MainViewModel,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val appLanguage by viewModel.appLanguage.collectAsState()
    
    SettingsSectionCard(
        title = stringResource(R.string.interface_title),
        icon = R.drawable.ic_interface,
        modifier = modifier
    ) {
        // Theme mode dropdown
        ThemeModeDropdown(
            selectedThemeMode = themeMode,
            onThemeModeSelected = { viewModel.saveThemeMode(it) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        // App language dropdown
        AppLanguageDropdown(
            selectedLanguage = appLanguage,
            onLanguageSelected = { viewModel.saveAppLanguage(it) }
        )
    }
}

@Composable
fun TranslationSettingsSection(
    viewModel: MainViewModel,
    translationSpeed: Int,
    modifier: Modifier = Modifier
) {
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val batchSize by viewModel.batchSize.collectAsState()
    
    SettingsSectionCard(
        title = stringResource(R.string.translation_settings_title),
        icon = R.drawable.ic_translate,
        modifier = modifier
    ) {
        // Target language selection dropdown
        TargetLanguageDropdown(
            selectedLanguage = targetLanguage,
            onLanguageSelected = { viewModel.saveTargetLanguage(it) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Batch size slider
        Column {
            Text(
                text = stringResource(R.string.batch_translation_size),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.batch_size_description, batchSize),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = batchSize.toFloat(),
                onValueChange = { viewModel.saveBatchSize(it.toInt()) },
                valueRange = 1f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.batch_size_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Translation speed slider
        Column {
            Text(
                text = stringResource(R.string.translation_speed),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Speed: $translationSpeed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = translationSpeed.toFloat(),
                onValueChange = { viewModel.saveTranslationSpeed(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CreditsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    SettingsSectionCard(
        title = stringResource(R.string.credit_title),
        icon = R.drawable.ic_info,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.credit_author),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // GitHub link button
        ElevatedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RenjiYuusei/VTU-Translate-Tool"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_github),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("GitHub Repository")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Discord link button
        ElevatedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/hVQm9fNV"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_discord),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.credit_discord))
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Section content
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLanguageDropdown(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Define available app languages
    val appLanguages = mapOf(
        "system" to "Theo hệ thống (System)",
        "vi" to "Tiếng Việt (Vietnamese)",
        "en" to "Tiếng Anh (English)"
    )
    
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.app_language),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = appLanguages[selectedLanguage] ?: selectedLanguage,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                appLanguages.forEach { (code, name) ->
                    val isSelected = code == selectedLanguage
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onLanguageSelected(code)
                            expanded = false
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetLanguageDropdown(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Define available target languages
    val languages = mapOf(
        "vi" to "Tiếng Việt (Vietnamese)",
        "en" to "Tiếng Anh (English)", 
        "zh" to "Tiếng Trung (Chinese)",
        "ru" to "Tiếng Nga (Russian)",
        "ko" to "Tiếng Hàn (Korean)",
        "es" to "Tiếng Tây Ban Nha (Spanish)",
        "fr" to "Tiếng Pháp (French)",
        "de" to "Tiếng Đức (German)",
        "ja" to "Tiếng Nhật (Japanese)"
    )
    
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.target_language),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = languages[selectedLanguage] ?: selectedLanguage,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                languages.forEach { (code, name) ->
                    val isSelected = code == selectedLanguage
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onLanguageSelected(code)
                            expanded = false
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeDropdown(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.theme_mode),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            val context = LocalContext.current
            TextField(
                value = context.getString(selectedThemeMode.displayNameRes),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemeMode.values().forEach { themeMode ->
                    val isSelected = themeMode == selectedThemeMode
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = context.getString(themeMode.displayNameRes),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onThemeModeSelected(themeMode)
                            expanded = false
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectionDropdown(
    viewModel: MainViewModel,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val availableModels by viewModel.availableModels.collectAsState()
    val isLoadingModels by viewModel.isLoadingModels.collectAsState()
    
    // Fetch models when dropdown is expanded
    LaunchedEffect(expanded) {
        if (expanded && availableModels.isEmpty()) {
            viewModel.fetchAvailableModels()
        }
    }
    
    // Animation for dropdown expansion
    val elevation by animateDpAsState(
        targetValue = if (expanded) 8.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        // Animated TextField
        TextField(
            value = selectedModel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { 
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .shadow(elevation = elevation, shape = RoundedCornerShape(12.dp))
        )
        
        // Animated Dropdown Menu
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (isLoadingModels) {
                    // Show loading indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Show models
                    if (availableModels.isNotEmpty()) {
                        // Dynamic models from API
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = stringResource(R.string.available_models),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                ) 
                            },
                            onClick = { },
                            enabled = false,
                            colors = MenuDefaults.itemColors(
                                disabledTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        availableModels.forEach { model ->
                        val isSelected = model == selectedModel
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = model,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                onModelSelected(model)
                                expanded = false
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null,
                            colors = MenuDefaults.itemColors(
                                textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    } else {
                        // No models available message
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = "Không có model nào. Vui lòng:\n1. Kiểm tra API key (phải bắt đầu với gsk_)\n2. Kiểm tra kết nối internet\n3. Nhấn nút Refresh Models",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                ) 
                            },
                            onClick = { },
                            enabled = false
                        )
                    }
                }
            }
        }
    }
}

