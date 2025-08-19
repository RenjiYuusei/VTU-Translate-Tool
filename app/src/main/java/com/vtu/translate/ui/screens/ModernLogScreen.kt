package com.vtu.translate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vtu.translate.R
import com.vtu.translate.data.model.LogEntry
import com.vtu.translate.data.model.LogType
import com.vtu.translate.ui.viewmodel.MainViewModel
import com.vtu.translate.ui.components.*
import com.vtu.translate.ui.theme.AccentBlue
import com.vtu.translate.ui.theme.AccentGreen
import com.vtu.translate.ui.theme.AccentRed
import com.vtu.translate.ui.theme.AccentYellow
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Modern Log Screen with filtering, search, and better UX
 */
@Composable
fun ModernLogScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    val lazyListState = rememberLazyListState()
    
    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedLogTypes by remember { mutableStateOf(LogType.values().toSet()) }
    var showFilters by remember { mutableStateOf(false) }
    
    // Filter logs based on search and type
    val filteredLogs by remember {
        derivedStateOf {
            logs.filter { log ->
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    log.message.contains(searchQuery, ignoreCase = true)
                }
                val matchesType = log.type in selectedLogTypes
                matchesSearch && matchesType
            }
        }
    }
    
    // Stats
    val logStats by remember {
        derivedStateOf {
            LogStats(
                total = logs.size,
                info = logs.count { it.type == LogType.INFO },
                success = logs.count { it.type == LogType.SUCCESS },
                warning = logs.count { it.type == LogType.WARNING },
                error = logs.count { it.type == LogType.ERROR }
            )
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(VTUSpacing.large),
        verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
    ) {
        // Header with stats
        LogHeader(
            stats = logStats,
            onClearLogs = {
                viewModel.clearLogs()
                Toast.makeText(context, "Đã xóa tất cả logs", Toast.LENGTH_SHORT).show()
            },
            onCopyLogs = {
                val logsText = viewModel.getLogsAsText(context)
                copyToClipboard(context, logsText)
                Toast.makeText(context, "Đã sao chép logs", Toast.LENGTH_SHORT).show()
            },
            hasLogs = logs.isNotEmpty()
        )
        
        // Search and filter section
        LogSearchAndFilter(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            showFilters = showFilters,
            onToggleFilters = { showFilters = !showFilters },
            selectedLogTypes = selectedLogTypes,
            onLogTypeToggle = { logType ->
                selectedLogTypes = if (logType in selectedLogTypes) {
                    selectedLogTypes - logType
                } else {
                    selectedLogTypes + logType
                }
            },
            filteredCount = filteredLogs.size,
            totalCount = logs.size
        )
        
        // Logs list or empty state
        Box(modifier = Modifier.weight(1f)) {
            when {
                logs.isEmpty() -> {
                    LogEmptyState()
                }
                filteredLogs.isEmpty() -> {
                    NoResultsState(
                        searchQuery = searchQuery,
                        onClearSearch = { 
                            searchQuery = ""
                            selectedLogTypes = LogType.values().toSet()
                        }
                    )
                }
                else -> {
                    LogsList(
                        logs = filteredLogs,
                        lazyListState = lazyListState
                    )
                }
            }
        }
    }
}

@Composable
private fun LogHeader(
    stats: LogStats,
    onClearLogs: () -> Unit,
    onCopyLogs: () -> Unit,
    hasLogs: Boolean
) {
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VTUSpacing.medium)
        ) {
            // Header title
            VTUSectionHeader(
                title = "Translation Logs",
                subtitle = "Lịch sử và trạng thái quá trình dịch"
            )
            
            // Stats overview
            if (hasLogs) {
                LogStatsRow(stats = stats)
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VTUSpacing.small)
            ) {
                VTUPrimaryButton(
                    onClick = onCopyLogs,
                    modifier = Modifier.weight(1f),
                    enabled = hasLogs,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("Sao chép")
                }
                
                VTUDangerButton(
                    onClick = onClearLogs,
                    modifier = Modifier.weight(1f),
                    enabled = hasLogs,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text("Xóa tất cả")
                }
            }
        }
    }
}

@Composable
private fun LogStatsRow(stats: LogStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(VTUSpacing.small)
    ) {
        StatCard(
            label = "Tổng",
            value = stats.total,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Thông tin",
            value = stats.info,
            color = AccentBlue,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Thành công",
            value = stats.success,
            color = AccentGreen,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Cảnh báo",
            value = stats.warning,
            color = AccentYellow,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Lỗi",
            value = stats.error,
            color = AccentRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    VTUCard(
        modifier = modifier,
        elevation = VTUElevation.low
    ) {
        Column(
            modifier = Modifier.padding(VTUSpacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogSearchAndFilter(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    selectedLogTypes: Set<LogType>,
    onLogTypeToggle: (LogType) -> Unit,
    filteredCount: Int,
    totalCount: Int
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (showFilters) 180f else 0f
    )
    
    VTUCard {
        Column(
            modifier = Modifier.padding(VTUSpacing.large)
        ) {
            // Search bar
            VTUTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = "Tìm kiếm logs",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = { onSearchQueryChange("") }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                } else null,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(VTUSpacing.medium))
            
            // Filter toggle and results count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hiển thị $filteredCount/$totalCount logs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleFilters() }
                ) {
                    Text(
                        text = "Bộ lọc",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Filter chips
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = VTUSpacing.medium)
                ) {
                    Text(
                        text = "Loại log:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = VTUSpacing.small)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(VTUSpacing.small)
                    ) {
                        LogType.values().forEach { logType ->
                            FilterChip(
                                onClick = { onLogTypeToggle(logType) },
                                label = { 
                                    Text(
                                        text = getLogTypeName(logType),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                selected = logType in selectedLogTypes,
                                leadingIcon = if (logType in selectedLogTypes) {
                                    {
                                        Icon(
                                            imageVector = getLogTypeIcon(logType),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getLogTypeColor(logType).copy(alpha = 0.2f),
                                    selectedLabelColor = getLogTypeColor(logType),
                                    selectedLeadingIconColor = getLogTypeColor(logType)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogsList(
    logs: List<LogEntry>,
    lazyListState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(VTUSpacing.small),
        modifier = Modifier.fillMaxSize()
    ) {
        items(logs, key = { "${it.timestamp}-${it.hashCode()}" }) { logEntry ->
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ModernLogEntryItem(logEntry = logEntry)
            }
        }
    }
}

@Composable
private fun ModernLogEntryItem(logEntry: LogEntry) {
    VTUCard(
        elevation = VTUElevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VTUSpacing.large),
            verticalAlignment = Alignment.Top
        ) {
            // Log type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getLogTypeColor(logEntry.type).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getLogTypeIcon(logEntry.type),
                    contentDescription = null,
                    tint = getLogTypeColor(logEntry.type),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(VTUSpacing.medium))
            
            // Log content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Timestamp and type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(logEntry.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    VTUStatusBadge(
                        text = getLogTypeName(logEntry.type),
                        type = when (logEntry.type) {
                            LogType.SUCCESS -> StatusType.SUCCESS
                            LogType.WARNING -> StatusType.WARNING
                            LogType.ERROR -> StatusType.ERROR
                            LogType.INFO -> StatusType.INFO
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(VTUSpacing.xs))
                
                // Log message
                Text(
                    text = logEntry.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LogEmptyState() {
    VTUEmptyState(
        title = "Chưa có logs",
        subtitle = "Logs sẽ hiển thị khi bạn bắt đầu dịch file",
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_log),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    )
}

@Composable
private fun NoResultsState(
    searchQuery: String,
    onClearSearch: () -> Unit
) {
    VTUEmptyState(
        title = "Không tìm thấy kết quả",
        subtitle = if (searchQuery.isNotEmpty()) {
            "Không có logs nào phù hợp với \"$searchQuery\""
        } else {
            "Không có logs nào phù hợp với bộ lọc hiện tại"
        },
        icon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        },
        action = {
            VTUSecondaryButton(
                onClick = onClearSearch
            ) {
                Text("Xóa bộ lọc")
            }
        }
    )
}

// Helper functions
@Composable
private fun getLogTypeColor(logType: LogType): Color {
    return when (logType) {
        LogType.INFO -> AccentBlue
        LogType.SUCCESS -> AccentGreen
        LogType.WARNING -> AccentYellow
        LogType.ERROR -> AccentRed
    }
}

@Composable
private fun getLogTypeIcon(logType: LogType): ImageVector {
    return when (logType) {
        LogType.INFO -> Icons.Outlined.Info
        LogType.SUCCESS -> Icons.Default.Clear // Using a checkmark-like icon
        LogType.WARNING -> Icons.Outlined.Warning
        LogType.ERROR -> Icons.Outlined.Error
    }
}

private fun getLogTypeName(logType: LogType): String {
    return when (logType) {
        LogType.INFO -> "Thông tin"
        LogType.SUCCESS -> "Thành công"
        LogType.WARNING -> "Cảnh báo"
        LogType.ERROR -> "Lỗi"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(timestamp)
}

/**
 * Copy text to clipboard
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Log Entries", text)
    clipboard.setPrimaryClip(clip)
}

// Data classes
private data class LogStats(
    val total: Int,
    val info: Int,
    val success: Int,
    val warning: Int,
    val error: Int
)
