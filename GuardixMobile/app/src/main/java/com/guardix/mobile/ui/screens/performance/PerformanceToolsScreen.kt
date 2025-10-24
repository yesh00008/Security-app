package com.guardix.mobile.ui.screens.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardix.mobile.data.managers.*
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceToolsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PerformanceToolsViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(context) {
        viewModel.initializePerformanceManager(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Performance & Optimization",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = LightBlue
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Performance Status Overview
            item {
                PerformanceStatusCard(
                    isOptimizing = uiState.isOptimizing,
                    optimizationProgress = uiState.optimizationProgress,
                    lastOptimizationResult = uiState.lastOptimizationResult,
                    memoryInfo = uiState.memoryInfo,
                    thermalInfo = uiState.thermalInfo
                )
            }
            
            // Performance Tools Grid
            items(getPerformanceTools()) { tool ->
                PerformanceToolCard(
                    tool = tool,
                    onToolClick = { toolType ->
                        coroutineScope.launch {
                            viewModel.handleToolAction(toolType)
                        }
                    }
                )
            }
            
            // Recent Performance Activity
            if (uiState.recentActivity.isNotEmpty()) {
                item {
                    RecentPerformanceActivity(
                        activities = uiState.recentActivity
                    )
                }
            }
        }
    }
    
    // Show optimization results dialog
    if (uiState.showOptimizationResults && uiState.lastOptimizationResult != null) {
        OptimizationResultsDialog(
            result = uiState.lastOptimizationResult!!,
            onDismiss = { viewModel.dismissOptimizationResults() }
        )
    }
    
    // Show battery optimization dialog
    if (uiState.showBatteryDialog && uiState.batteryInfo != null) {
        BatteryOptimizationDialog(
            batteryInfo = uiState.batteryInfo!!,
            onOptimize = { 
                coroutineScope.launch {
                    viewModel.optimizeBattery()
                }
            },
            onDismiss = { viewModel.dismissBatteryDialog() }
        )
    }
    
    // Show thermal cooling dialog
    if (uiState.showThermalDialog && uiState.thermalInfo != null) {
        ThermalCoolingDialog(
            thermalInfo = uiState.thermalInfo!!,
            onCool = {
                coroutineScope.launch {
                    viewModel.performThermalCooling()
                }
            },
            onDismiss = { viewModel.dismissThermalDialog() }
        )
    }
}

@Composable
private fun PerformanceStatusCard(
    isOptimizing: Boolean,
    optimizationProgress: Float,
    lastOptimizationResult: PerformanceOptimizationResult?,
    memoryInfo: MemoryInfo?,
    thermalInfo: ThermalInfo?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = LightBlue,
                    modifier = Modifier.size(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Performance Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayText
                    )
                    Text(
                        if (lastOptimizationResult == null) "Ready to optimize" 
                        else "Last optimization: ${formatFileSize(lastOptimizationResult.memoryFreed)} freed",
                        fontSize = 14.sp,
                        color = GrayDark
                    )
                }
                
                if (isOptimizing) {
                    CircularProgressIndicator(
                        progress = { optimizationProgress },
                        modifier = Modifier.size(24.dp),
                        color = LightBlue
                    )
                }
            }
            
            if (isOptimizing) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { optimizationProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = LightBlue
                )
                Text(
                    "Optimizing... ${(optimizationProgress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = GrayDark,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Performance Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                memoryInfo?.let { memory ->
                    PerformanceStat(
                        "Memory Usage", 
                        "${memory.memoryUsagePercent.toInt()}%",
                        if (memory.memoryUsagePercent > 80) WarningOrange else SuccessGreen
                    )
                }
                
                thermalInfo?.let { thermal ->
                    PerformanceStat(
                        "Temperature", 
                        "${thermal.currentTemperature.toInt()}°C",
                        when (thermal.thermalState) {
                            ThermalState.NORMAL -> SuccessGreen
                            ThermalState.WARNING -> WarningOrange
                            ThermalState.CRITICAL -> ErrorRed
                        }
                    )
                }
                
                lastOptimizationResult?.let { result ->
                    PerformanceStat(
                        "Apps Optimized", 
                        result.appsOptimized.toString(),
                        LightBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = GrayDark,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PerformanceToolCard(
    tool: PerformanceTool,
    onToolClick: (PerformanceToolType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onToolClick(tool.type) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        tool.color.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    tool.icon,
                    contentDescription = null,
                    tint = tool.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tool.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText
                )
                Text(
                    tool.description,
                    fontSize = 14.sp,
                    color = GrayDark
                )
                if (tool.status.isNotEmpty()) {
                    Text(
                        tool.status,
                        fontSize = 12.sp,
                        color = tool.statusColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open ${tool.title}",
                tint = GrayDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RecentPerformanceActivity(
    activities: List<PerformanceActivity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Recent Performance Activity",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrayText
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            activities.take(5).forEach { activity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        activity.icon,
                        contentDescription = null,
                        tint = activity.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        activity.description,
                        fontSize = 14.sp,
                        color = GrayText,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        activity.timestamp,
                        fontSize = 12.sp,
                        color = GrayDark
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimizationResultsDialog(
    result: PerformanceOptimizationResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen
                )
                Text("Optimization Complete")
            }
        },
        text = {
            Column {
                Text(
                    "Performance optimization completed successfully!",
                    fontSize = 14.sp,
                    color = GrayDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Results
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Memory Freed", fontSize = 12.sp, color = GrayDark)
                        Text(formatFileSize(result.memoryFreed), fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Storage Freed", fontSize = 12.sp, color = GrayDark)
                        Text(formatFileSize(result.storageFreed), fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Apps Optimized", fontSize = 12.sp, color = GrayDark)
                        Text(result.appsOptimized.toString(), fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Battery life improvement: ${result.batteryImprovement}%",
                    fontSize = 14.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (result.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recommendations:", fontWeight = FontWeight.SemiBold)
                    result.details.forEach { recommendation ->
                        Text("• $recommendation", fontSize = 12.sp, color = GrayDark)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Great!")
            }
        }
    )
}

@Composable
private fun BatteryOptimizationDialog(
    batteryInfo: BatteryOptimizationInfo,
    onOptimize: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = WarningOrange
                )
                Text("Battery Optimization")
            }
        },
        text = {
            Column {
                Text("Current battery level: ${batteryInfo.batteryLevel}%")
                Text("Health: ${batteryInfo.batteryHealth}")
                Text("Estimated time remaining: ${batteryInfo.estimatedTimeRemaining}")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (batteryInfo.batteryUsageToday.isNotEmpty()) {
                    Text("Battery usage today:", fontWeight = FontWeight.SemiBold)
                    batteryInfo.batteryUsageToday.take(3).forEach { app ->
                        Text("• ${app.appName} (${app.usagePercent.toInt()}%)", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Optimization recommendations:", fontWeight = FontWeight.SemiBold)
                batteryInfo.optimizationSuggestions.take(3).forEach { recommendation ->
                    Text("• $recommendation", fontSize = 12.sp, color = GrayDark)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOptimize) {
                Text("Optimize Battery")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ThermalCoolingDialog(
    thermalInfo: ThermalInfo,
    onCool: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = when (thermalInfo.thermalState) {
                        ThermalState.NORMAL -> SuccessGreen
                        ThermalState.WARNING -> WarningOrange
                        ThermalState.CRITICAL -> ErrorRed
                    }
                )
                Text("Thermal Management")
            }
        },
        text = {
            Column {
                Text("Current temperature: ${thermalInfo.currentTemperature.toInt()}°C")
                Text("Thermal state: ${thermalInfo.thermalState}")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Cooling recommendations:", fontWeight = FontWeight.SemiBold)
                listOf("Close resource-intensive apps", "Enable power saving mode", "Let device cool down").forEach { recommendation ->
                    Text("• $recommendation", fontSize = 12.sp, color = GrayDark)
                }
            }
        },
        confirmButton = {
            if (thermalInfo.thermalState != ThermalState.NORMAL) {
                TextButton(onClick = onCool) {
                    Text("Cool Down")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (thermalInfo.thermalState != ThermalState.NORMAL) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// Data classes for UI
data class PerformanceTool(
    val type: PerformanceToolType,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val status: String = "",
    val statusColor: Color = GrayDark
)

enum class PerformanceToolType {
    ONE_TAP_OPTIMIZATION,
    PHONE_ACCELERATION,
    JUNK_CLEANER,
    APP_STARTUP_MANAGER,
    POWER_CONSUMPTION_MANAGER,
    THERMAL_COOLING,
    BATTERY_MANAGEMENT,
    GAME_BOOST
}

data class PerformanceActivity(
    val description: String,
    val timestamp: String,
    val icon: ImageVector,
    val color: Color
)

private fun getPerformanceTools(): List<PerformanceTool> {
    return listOf(
        PerformanceTool(
            type = PerformanceToolType.ONE_TAP_OPTIMIZATION,
            title = "One-Tap Optimization",
            description = "Complete system optimization in one tap",
            icon = Icons.Default.TouchApp,
            color = LightBlue,
            status = "Ready to optimize",
            statusColor = SuccessGreen
        ),
        PerformanceTool(
            type = PerformanceToolType.PHONE_ACCELERATION,
            title = "Phone Acceleration",
            description = "Boost device speed and responsiveness",
            icon = Icons.Default.Speed,
            color = SuccessGreen,
            status = "Speed boost available",
            statusColor = SuccessGreen
        ),
        PerformanceTool(
            type = PerformanceToolType.JUNK_CLEANER,
            title = "Junk Cleaner",
            description = "Remove unnecessary files and cache",
            icon = Icons.Default.DeleteSweep,
            color = WarningOrange,
            status = "182 MB to clean",
            statusColor = WarningOrange
        ),
        PerformanceTool(
            type = PerformanceToolType.APP_STARTUP_MANAGER,
            title = "App Startup Manager",
            description = "Control which apps start automatically",
            icon = Icons.Default.PlayArrow,
            color = Cyan,
            status = "12 auto-start apps",
            statusColor = Cyan
        ),
        PerformanceTool(
            type = PerformanceToolType.POWER_CONSUMPTION_MANAGER,
            title = "Power Management",
            description = "Monitor and reduce battery drain",
            icon = Icons.Default.Battery4Bar,
            color = ErrorRed,
            status = "High consumption detected",
            statusColor = ErrorRed
        ),
        PerformanceTool(
            type = PerformanceToolType.THERMAL_COOLING,
            title = "Thermal Cooling",
            description = "Monitor and cool device temperature",
            icon = Icons.Default.Thermostat,
            color = LightBlue,
            status = "Temperature normal",
            statusColor = SuccessGreen
        ),
        PerformanceTool(
            type = PerformanceToolType.BATTERY_MANAGEMENT,
            title = "Battery Management",
            description = "Optimize battery usage and health",
            icon = Icons.Default.BatteryChargingFull,
            color = SuccessGreen,
            status = "Battery health: Good",
            statusColor = SuccessGreen
        ),
        PerformanceTool(
            type = PerformanceToolType.GAME_BOOST,
            title = "Game Boost",
            description = "Enhance gaming performance",
            icon = Icons.Default.Gamepad,
            color = ErrorRed,
            status = "3 games optimized",
            statusColor = LightBlue
        )
    )
}

// Utility function for file size formatting
private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}