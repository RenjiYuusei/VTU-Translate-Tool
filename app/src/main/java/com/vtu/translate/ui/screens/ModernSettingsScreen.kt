package com.vtu.translate.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.vtu.translate.R
import com.vtu.translate.ui.viewmodel.MainViewModel
import com.vtu.translate.data.model.ThemeMode
import com.vtu.translate.data.model.AiProvider
import com.vtu.translate.ui.components.*

/**
 * Modern Settings Screen with improved UI/UX
 */
@Composable
fun ModernSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Collect all state values
    val aiProvider by viewModel.aiProvider.collectAsState()
    val groqApiKey by viewModel.apiKey.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val selectedGeminiModel by viewModel.selectedGeminiModel.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val translationSpeed by viewModel.translationSpeed.collectAsState()
    val batchSize by viewModel.batchSize.collectAsState()
    
    // Expandable sections state
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    
    fun toggleSection(sectionId: String) {
        expandedSections = if (sectionId in expandedSections) {
            expandedSections - sectionId
        } else {
            expandedSections + sectionId
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(VTUSpacing.large)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
    ) {
        // Header
        VTUSectionHeader(
            title = stringResource(R.string.settings_title),
            subtitle = "Cấu hình ứng dụng dịch thuật"
        )
        
        Spacer(modifier = Modifier.height(VTUSpacing.small))
        
        // AI Provider & API Settings
        ModernSettingCategory(
            icon = { 
                Icon(
                    Icons.Outlined.Api, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) 
            },
            title = "API & AI Configuration",
            subtitle = "Cấu hình nhà cung cấp AI và API keys",
            expanded = "api" in expandedSections,
            onToggle = { toggleSection("api") }
        ) {
            // AI Provider Selection
            AIProviderCard(
                selectedProvider = aiProvider,
                onProviderChange = { viewModel.saveAiProvider(it) }
            )
            
            Spacer(modifier = Modifier.height(VTUSpacing.medium))
            
            // API Key Section based on selected provider
            when (aiProvider) {
                AiProvider.GROQ -> {
                    GroqAPIKeyCard(
                        apiKey = groqApiKey,
                        onApiKeyChange = { viewModel.saveApiKey(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(VTUSpacing.medium))
                    
                    ModelSelectionCard(
                        viewModel = viewModel,
                        selectedModel = selectedModel,
                        onModelChange = { viewModel.saveSelectedModel(it) }
                    )
                }
                AiProvider.GEMINI -> {
                    GeminiAPIKeyCard(
                        apiKey = geminiApiKey,
                        onApiKeyChange = { viewModel.saveGeminiApiKey(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(VTUSpacing.medium))
                    
                    GeminiModelSelectionCard(
                        selectedModel = selectedGeminiModel,
                        onModelChange = { viewModel.saveSelectedGeminiModel(it) }
                    )
                }
            }
        }
        
        // Interface Settings
        ModernSettingCategory(
            icon = { 
                Icon(
                    Icons.Outlined.Palette, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                ) 
            },
            title = "Giao diện",
            subtitle = "Cấu hình theme và ngôn ngữ ứng dụng",
            expanded = "interface" in expandedSections,
            onToggle = { toggleSection("interface") }
        ) {
            ThemeSelectionCard(
                selectedTheme = themeMode,
                onThemeChange = { viewModel.saveThemeMode(it) }
            )
            
            Spacer(modifier = Modifier.height(VTUSpacing.medium))
            
            AppLanguageCard(
                selectedLanguage = appLanguage,
                onLanguageChange = { viewModel.saveAppLanguage(it) }
            )
        }
        
        // Translation Settings
        ModernSettingCategory(
            icon = { 
                Icon(
                    Icons.Outlined.Translate, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                ) 
            },
            title = "Cài đặt dịch thuật",
            subtitle = "Tốc độ dịch, ngôn ngữ đích và batch size",
            expanded = "translation" in expandedSections,
            onToggle = { toggleSection("translation") }
        ) {
            TargetLanguageCard(
                selectedLanguage = targetLanguage,
                onLanguageChange = { viewModel.saveTargetLanguage(it) }
            )
            
            Spacer(modifier = Modifier.height(VTUSpacing.medium))
            
            TranslationSpeedCard(
                speed = translationSpeed,
                onSpeedChange = { viewModel.saveTranslationSpeed(it) }
            )
            
            Spacer(modifier = Modifier.height(VTUSpacing.medium))
            
            BatchSizeCard(
                batchSize = batchSize,
                onBatchSizeChange = { viewModel.saveBatchSize(it) }
            )
        }
        
        // About & Info
        ModernSettingCategory(
            icon = { 
                Icon(
                    Icons.Outlined.Info, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                ) 
            },
            title = "Về ứng dụng",
            subtitle = "Thông tin phiên bản và liên kết hữu ích",
            expanded = "about" in expandedSections,
            onToggle = { toggleSection("about") }
        ) {
            AboutCard()
        }
    }
}

@Composable
private fun ModernSettingCategory(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )
    
    VTUCard {
        Column {
            // Category header
            VTUInfoCard(
                title = title,
                subtitle = subtitle,
                icon = icon,
                onClick = onToggle,
                trailing = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_down),
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(VTUSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun AIProviderCard(
    selectedProvider: AiProvider,
    onProviderChange: (AiProvider) -> Unit
) {
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "AI Provider",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Provider options
            AiProvider.values().forEach { provider ->
                VTUInfoCard(
                    title = provider.displayName,
                    subtitle = provider.description,
                    icon = {
                        Icon(
                            painter = painterResource(
                                when (provider) {
                                    AiProvider.GROQ -> R.drawable.ic_groq
                                    AiProvider.GEMINI -> R.drawable.ic_gemini
                                }
                            ),
                            contentDescription = null,
                            tint = if (selectedProvider == provider) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    onClick = { onProviderChange(provider) },
                    trailing = {
                        if (selectedProvider == provider) {
                            VTUStatusBadge("Đang sử dụng", StatusType.SUCCESS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GroqAPIKeyCard(
    apiKey: String,
    onApiKeyChange: (String) -> Unit
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf(apiKey) }
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Groq API Key",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            VTUTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = "Nhập Groq API Key",
                supportingText = "API key phải bắt đầu với 'gsk_'",
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_key),
                        contentDescription = null
                    )
                },
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VTUSpacing.small)
            ) {
                VTUPrimaryButton(
                    onClick = {
                        if (apiKeyInput.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập API key", Toast.LENGTH_SHORT).show()
                            return@VTUPrimaryButton
                        }
                        
                        if (!apiKeyInput.startsWith("gsk_")) {
                            Toast.makeText(context, "API key phải bắt đầu với 'gsk_'", Toast.LENGTH_SHORT).show()
                            return@VTUPrimaryButton
                        }
                        
                        onApiKeyChange(apiKeyInput)
                        Toast.makeText(context, "API key đã được lưu", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("Lưu")
                }
                
                VTUSecondaryButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_external_link),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("Lấy API Key")
                }
            }
        }
    }
}

@Composable
private fun GeminiAPIKeyCard(
    apiKey: String,
    onApiKeyChange: (String) -> Unit
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf(apiKey) }
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Google Gemini API Key",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            VTUTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = "Nhập Gemini API Key",
                supportingText = "API key phải bắt đầu với 'AIza'",
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_key),
                        contentDescription = null
                    )
                },
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VTUSpacing.small)
            ) {
                VTUPrimaryButton(
                    onClick = {
                        if (apiKeyInput.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập API key", Toast.LENGTH_SHORT).show()
                            return@VTUPrimaryButton
                        }
                        
                        if (!apiKeyInput.startsWith("AIza")) {
                            Toast.makeText(context, "API key phải bắt đầu với 'AIza'", Toast.LENGTH_SHORT).show()
                            return@VTUPrimaryButton
                        }
                        
                        onApiKeyChange(apiKeyInput)
                        Toast.makeText(context, "API key đã được lưu", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("Lưu")
                }
                
                VTUSecondaryButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_external_link),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("Lấy API Key")
                }
            }
        }
    }
}

@Composable
private fun ModelSelectionCard(
    viewModel: MainViewModel,
    selectedModel: String,
    onModelChange: (String) -> Unit
) {
    val context = LocalContext.current
    val availableModels by viewModel.availableModels.collectAsState()
    val isLoading by viewModel.isLoadingModels.collectAsState()
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Model Selection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            if (availableModels.isNotEmpty()) {
                Text(
                    text = "Mô hình hiện tại: $selectedModel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                availableModels.take(3).forEach { model ->
                    VTUInfoCard(
                        title = model,
                        subtitle = if (model == selectedModel) "Đang sử dụng" else "Nhấn để chọn",
                        onClick = { onModelChange(model) },
                        trailing = {
                            if (model == selectedModel) {
                                VTUStatusBadge("Đang dùng", StatusType.SUCCESS)
                            }
                        }
                    )
                }
            }
            
            VTUSecondaryButton(
                onClick = {
                    viewModel.fetchAvailableModels()
                    Toast.makeText(context, "Đang tải danh sách model...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            ) {
                Text("Tải lại danh sách Model")
            }
        }
    }
}

@Composable
private fun GeminiModelSelectionCard(
    selectedModel: String,
    onModelChange: (String) -> Unit
) {
    val models = listOf(
        "models/gemini-2.0-flash-exp" to "Gemini 2.0 Flash Exp (Experimental)",
        "models/gemini-1.5-flash" to "Gemini 1.5 Flash (Recommended)",
        "models/gemini-1.5-pro" to "Gemini 1.5 Pro (Advanced)"
    )
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Gemini Models",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            models.forEach { (modelId, displayName) ->
                VTUInfoCard(
                    title = displayName,
                    subtitle = if (modelId == selectedModel) "Đang sử dụng" else "Nhấn để chọn",
                    onClick = { onModelChange(modelId) },
                    trailing = {
                        if (modelId == selectedModel) {
                            VTUStatusBadge("Đang dùng", StatusType.SUCCESS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionCard(
    selectedTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            ThemeMode.values().forEach { theme ->
                val context = LocalContext.current
                VTUInfoCard(
                    title = context.getString(theme.displayNameRes),
                    subtitle = when (theme) {
                        ThemeMode.LIGHT -> "Giao diện sáng"
                        ThemeMode.DARK -> "Giao diện tối"
                        ThemeMode.SYSTEM -> "Theo hệ thống"
                    },
                    onClick = { onThemeChange(theme) },
                    trailing = {
                        if (selectedTheme == theme) {
                            VTUStatusBadge("Đang dùng", StatusType.SUCCESS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppLanguageCard(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val languages = mapOf(
        "system" to "Theo hệ thống",
        "vi" to "Tiếng Việt",
        "en" to "English"
    )
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Ngôn ngữ ứng dụng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            languages.forEach { (code, name) ->
                VTUInfoCard(
                    title = name,
                    subtitle = if (code == selectedLanguage) "Đang sử dụng" else "Nhấn để chọn",
                    onClick = { onLanguageChange(code) },
                    trailing = {
                        if (selectedLanguage == code) {
                            VTUStatusBadge("Đang dùng", StatusType.SUCCESS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TargetLanguageCard(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val languages = mapOf(
        "vi" to "Tiếng Việt",
        "en" to "English", 
        "zh" to "中文",
        "ja" to "日本語",
        "ko" to "한국어",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "ru" to "Русский"
    )
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Ngôn ngữ đích",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Chọn ngôn ngữ muốn dịch sang",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            languages.entries.chunked(3).forEach { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VTUSpacing.small)
                ) {
                    chunk.forEach { (code, name) ->
                        VTUCard(
                            modifier = Modifier.weight(1f),
                            onClick = { onLanguageChange(code) },
                            elevation = if (selectedLanguage == code) VTUElevation.high else VTUElevation.low
                        ) {
                            Column(
                                modifier = Modifier.padding(VTUSpacing.medium),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedLanguage == code) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (selectedLanguage == code) FontWeight.Bold else FontWeight.Normal
                                )
                                if (selectedLanguage == code) {
                                    VTUStatusBadge("✓", StatusType.SUCCESS)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TranslationSpeedCard(
    speed: Int,
    onSpeedChange: (Int) -> Unit
) {
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Tốc độ dịch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Mức ${speed}/5 - ${
                    when (speed) {
                        1 -> "Rất chậm (3 giây giữa các lần dịch)"
                        2 -> "Chậm (2 giây giữa các lần dịch)"
                        3 -> "Bình thường (1 giây giữa các lần dịch)"
                        4 -> "Nhanh (0.5 giây giữa các lần dịch)"
                        5 -> "Rất nhanh (0.2 giây giữa các lần dịch)"
                        else -> "Không xác định"
                    }
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Slider(
                value = speed.toFloat(),
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Chậm", style = MaterialTheme.typography.labelSmall)
                Text("Nhanh", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun BatchSizeCard(
    batchSize: Int,
    onBatchSizeChange: (Int) -> Unit
) {
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "Kích thước batch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Số lượng chuỗi dịch cùng lúc: $batchSize",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Slider(
                value = batchSize.toFloat(),
                onValueChange = { onBatchSizeChange(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1", style = MaterialTheme.typography.labelSmall)
                Text("10", style = MaterialTheme.typography.labelSmall)
            }
            
            Text(
                text = "Batch size cao hơn = nhanh hơn nhưng dễ bị rate limit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutCard() {
    val context = LocalContext.current
    
    val versionName = try {
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
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            Text(
                text = "VTU Translate Tool",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Phiên bản: $versionName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Được phát triển bởi RenjiYuusei",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VTUSpacing.small)
            ) {
                VTUSecondaryButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RenjiYuusei/VTU-Translate-Tool"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("GitHub")
                }
                
                VTUSecondaryButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/hVQm9fNV"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_discord),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("Discord")
                }
            }
        }
    }
}
