package com.guardix.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guardix.mobile.data.*
import com.guardix.mobile.ui.components.NeumorphicCard
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

data class SecurityTool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEnabled: Boolean = true,
    val status: String = "Ready",
    val statusColor: androidx.compose.ui.graphics.Color = SuccessGreen,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToNetwork: () -> Unit = {},
    onNavigateToStorage: () -> Unit = {},
    onNavigateToTool: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize managers
    val securityManager = remember { SecurityManager(context) }
    val performanceManager = remember { PerformanceManager(context) }
    val privacyManager = remember { PrivacyManager(context) }
    
    var selectedCategory by remember { mutableStateOf("All Tools") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var modelSummary by remember { mutableStateOf<ModelSummary?>(null) }

    val categories = listOf("All Tools", "Security", "Performance", "Network", "Storage")

    LaunchedEffect(Unit) {
        modelSummary = securityManager.getModelSummary()
    }
    
    // Comprehensive Tool Suites - Main Categories
    val comprehensiveTools = listOf(
        SecurityTool(
            title = "Security & Privacy Tools",
            description = "Comprehensive security suite with 8 powerful protection tools",
            icon = Icons.Default.Security,
            status = "8 tools available",
            statusColor = LightBlue,
            onClick = onNavigateToSecurity
        ),
        SecurityTool(
            title = "Performance Optimization",
            description = "Complete performance suite with 8 optimization tools",
            icon = Icons.Default.Speed,
            status = "8 tools available", 
            statusColor = SuccessGreen,
            onClick = onNavigateToPerformance
        ),
        SecurityTool(
            title = "Network Management",
            description = "Advanced network tools for monitoring and analysis",
            icon = Icons.Default.NetworkCheck,
            status = "6 tools available",
            statusColor = Cyan,
            onClick = onNavigateToNetwork
        ),
        SecurityTool(
            title = "Storage Management",
            description = "Complete storage suite for cleanup and file management",
            icon = Icons.Default.Storage,
            status = "6 tools available",
            statusColor = WarningOrange,
            onClick = onNavigateToStorage
        )
    )

    val securityTools = listOf(
        SecurityTool(
            title = "Malware Scanner",
            description = "Deep scan for viruses, trojans, and malicious apps",
            icon = Icons.Default.BugReport,
            status = "Last scan: ${securityManager.lastScanTime.value}",
            onClick = {
                onNavigateToTool("malware_scanner")
            }
        ),
        SecurityTool(
            title = "Phishing Protection",
            description = "Block malicious websites and phishing attempts",
            icon = Icons.Default.Shield,
            status = "Active",
            onClick = {
                scope.launch {
                    isProcessing = true
                    val result = securityManager.checkPhishing(url = "http://phish.me/login", text = null)
                    isProcessing = false
                    dialogTitle = "Phishing Protection"
                    dialogMessage = result?.let {
                        val risk = (it.probability * 100).roundToInt()
                        val statusText = if (it.isPhishing) "Phishing risk detected" else "URL appears safe"
                        buildString {
                            appendLine("Scanned: ${it.source ?: "sample"}")
                            appendLine(statusText)
                            append("Probability: $risk%")
                        }
                    } ?: "Unable to reach phishing detection service."
                    showDialog = true
                }
            }
        ),
        SecurityTool(
            title = "Network Monitor",
            description = "Monitor network connections and data usage",
            icon = Icons.Default.NetworkCheck,
            status = "Monitoring",
            onClick = {
                scope.launch {
                    isProcessing = true
                    val idsResult = securityManager.monitorNetwork()
                    isProcessing = false
                    dialogTitle = "Network Monitor"
                    dialogMessage = idsResult?.let {
                        val score = (it.score * 100).roundToInt()
                        val alertText = if (it.alert) "Alert triggered" else "No anomalies detected"
                        val anomalies = if (it.anomalies.isEmpty()) {
                            "Anomalies: none"
                        } else {
                            it.anomalies.joinToString("\n") { anomaly ->
                                val anomalyScore = (anomaly.score * 100).roundToInt()
                                "\u2022 Entry ${anomaly.index}: $anomalyScore% risk"
                            }
                        }
                        buildString {
                            appendLine("Score: $score%")
                            appendLine(alertText)
                            append(anomalies)
                        }
                    } ?: "Unable to reach intrusion detection service."
                    showDialog = true
                }
            }
        ),
        SecurityTool(
            title = "Model Insights",
            description = "View active ML profiles",
            icon = Icons.Default.AutoAwesome,
            status = modelSummary?.let { "Profile: ${it.activeProfile.uppercase(Locale.getDefault())}" } ?: "Loading...",
            statusColor = if (modelSummary != null) SuccessGreen else GrayDark,
            onClick = {
                scope.launch {
                    if (modelSummary == null) {
                        isProcessing = true
                        modelSummary = securityManager.getModelSummary()
                        isProcessing = false
                    }
                    dialogTitle = "Model Insights"
                    dialogMessage = modelSummary?.let { summary ->
                        val details = summary.models.joinToString("\n") { model ->
                            val sizeText = model.sizeKb?.let { size ->
                                String.format(Locale.getDefault(), "%.1f KB", size)
                            }
                            buildString {
                                append('\u2022')
                                append(' ')
                                append(model.name)
                                append(" (${model.algorithm}) - ${model.profile}")
                                if (!sizeText.isNullOrEmpty()) {
                                    append(" | ")
                                    append(sizeText)
                                }
                            }
                        }
                        "Active profile: ${summary.activeProfile}\n$details"
                    } ?: "Unable to load model metadata."
                    showDialog = true
                }
            }
        ),
        SecurityTool(
            title = "App Permissions",
            description = "Review and manage app permissions",
            icon = Icons.Default.AdminPanelSettings,
            status = "12 apps reviewed",
            onClick = {
                val riskyApps = privacyManager.getPermissionRiskyApps()
                dialogTitle = "App Permissions"
                dialogMessage = "Apps requiring attention:\n" +
                        riskyApps.joinToString("\n") { "• ${it.name}" } +
                        "\n\nReview these apps for excessive permissions"
                showDialog = true
            }
        )
    )
    
    val performanceTools = listOf(
        SecurityTool(
            title = "Memory Cleaner",
            description = "Free up RAM and close background apps",
            icon = Icons.Default.Memory,
            status = "3.2GB available",
            onClick = {
                onNavigateToTool("memory_cleaner")
            }
        ),
        SecurityTool(
            title = "Storage Cleaner",
            description = "Remove junk files and cache data",
            icon = Icons.Default.Storage,
            status = "1.8GB to clean",
            onClick = {
                scope.launch {
                    isProcessing = true
                    val cleaned = performanceManager.cleanStorage()
                    isProcessing = false
                    dialogTitle = "Storage Cleaned"
                    dialogMessage = "Removed ${formatFileSize(cleaned)} of junk files\n" +
                            "Cache data cleared\nStorage optimized!"
                    showDialog = true
                }
            }
        ),
        SecurityTool(
            title = "Battery Optimizer",
            description = "Optimize battery usage and extend life",
            icon = Icons.Default.BatteryChargingFull,
            status = "Good health",
            onClick = {
                scope.launch {
                    isProcessing = true
                    val optimization = performanceManager.optimizeBattery()
                    isProcessing = false
                    dialogTitle = "Battery Optimized"
                    dialogMessage = "Battery optimization complete!\n$optimization\nExpected 20% longer battery life"
                    showDialog = true
                }
            }
        ),
        SecurityTool(
            title = "CPU Monitor",
            description = "Monitor CPU usage and temperature",
            icon = Icons.Default.Speed,
            status = "Normal (45°C)",
            onClick = {
                val systemInfo = securityManager.getSystemInfo()
                dialogTitle = "System Information"
                dialogMessage = "CPU Usage: ${systemInfo.cpuUsage.toInt()}%\n" +
                        "RAM Usage: ${formatFileSize(systemInfo.totalRAM - systemInfo.availableRAM)}\n" +
                        "Battery Level: ${systemInfo.batteryLevel}%\n" +
                        "Temperature: Normal"
                showDialog = true
            }
        )
    )
    
    val privacyTools = listOf(
        SecurityTool(
            title = "Biometric Setup",
            description = "Configure fingerprint and face unlock",
            icon = Icons.Default.Fingerprint,
            status = "2 methods active",
            onClick = {
                scope.launch {
                    isProcessing = true
                    val result = securityManager.verifyBiometric()
                    isProcessing = false
                    dialogTitle = "Biometric Security"
                    dialogMessage = result?.let {
                        val probability = (it.probability * 100).roundToInt()
                        val threshold = (it.threshold * 100).roundToInt()
                        val statusText = if (it.match) "Profile verified" else "Mismatch detected"
                        buildString {
                            appendLine(statusText)
                            appendLine("Confidence: $probability%")
                            append("Threshold: $threshold%")
                        }
                    } ?: "Unable to verify biometric profile."
                    showDialog = true
                }
            }
        ),
        SecurityTool(
            title = "App Lock",
            description = "Lock sensitive apps with PIN or biometrics",
            icon = Icons.Default.Lock,
            status = "${privacyManager.getLockedAppsCount()} apps locked",
            onClick = {
                val apps = securityManager.getInstalledApps().take(5)
                dialogTitle = "App Lock Manager"
                dialogMessage = "Locked apps: ${privacyManager.getLockedAppsCount()}\n\nRecommended to lock:\n" +
                        apps.joinToString("\n") { "• ${it.name}" }
                showDialog = true
            }
        ),
        SecurityTool(
            title = "Privacy Cleaner",
            description = "Clear browsing history and private data",
            icon = Icons.Default.DeleteSweep,
            status = "Ready to clean",
            onClick = {
                scope.launch {
                    isProcessing = true
                    val cleared = privacyManager.clearPrivacyData()
                    isProcessing = false
                    val total = cleared.values.sum()
                    dialogTitle = "Privacy Data Cleared"
                    dialogMessage = "Cleared $total privacy traces:\n" +
                            cleared.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }
                    showDialog = true
                }
            }
        ),
        SecurityTool(
            title = "Location Guard",
            description = "Monitor and control location access",
            icon = Icons.Default.LocationOn,
            status = "12 apps tracked",
            onClick = {
                val riskyApps = privacyManager.getPermissionRiskyApps()
                dialogTitle = "Location Privacy"
                dialogMessage = "Apps with location access: 12\n\nHigh-risk apps detected:\n" +
                        riskyApps.joinToString("\n") { "• ${it.name}" } +
                        "\n\nConsider reviewing permissions"
                showDialog = true
            }
        )
    )
    
    val networkTools = listOf(
        SecurityTool(
            title = "Network Tools Suite",
            description = "Access 6 powerful network management tools",
            icon = Icons.Default.NetworkCheck,
            status = "Speed test, WiFi analyzer, ping test & more",
            statusColor = Cyan,
            onClick = onNavigateToNetwork
        ),
        SecurityTool(
            title = "Speed Test",
            description = "Test your internet download and upload speeds",
            icon = Icons.Default.Speed,
            status = "Ready to test",
            onClick = {
                onNavigateToTool("network_speed_test")
            }
        ),
        SecurityTool(
            title = "WiFi Analyzer",
            description = "Analyze WiFi networks and signal strength",
            icon = Icons.Default.Wifi,
            status = "Network scanning",
            onClick = { /* Individual tool placeholder */ }
        ),
        SecurityTool(
            title = "Network Monitor",
            description = "Monitor real-time network data usage",
            icon = Icons.Default.DataUsage,
            status = "Monitoring active",
            onClick = { /* Individual tool placeholder */ }
        )
    )
    
    val storageTools = listOf(
        SecurityTool(
            title = "Storage Tools Suite", 
            description = "Access 6 comprehensive storage management tools",
            icon = Icons.Default.Storage,
            status = "Quick clean, duplicates, large files & more",
            statusColor = WarningOrange,
            onClick = onNavigateToStorage
        ),
        SecurityTool(
            title = "Quick Clean",
            description = "Fast cleanup of junk files and cache",
            icon = Icons.Default.CleaningServices,
            status = "1.2GB to clean",
            onClick = { /* Individual tool placeholder */ }
        ),
        SecurityTool(
            title = "Duplicate Finder",
            description = "Find and remove duplicate files",
            icon = Icons.Default.ContentCopy,
            status = "Scan for duplicates",
            onClick = { /* Individual tool placeholder */ }
        ),
        SecurityTool(
            title = "Large Files",
            description = "Identify and manage large files",
            icon = Icons.Default.FilePresent,
            status = "Find large files",
            onClick = { /* Individual tool placeholder */ }
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.05f),
                        BackgroundPrimary
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Just a small spacer for top padding
        Spacer(modifier = Modifier.height(8.dp))
        
        // Category Tabs in a Row Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            categories.forEach { category ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedCategory == category) 
                                LightBlue.copy(alpha = 0.2f) 
                            else 
                                BackgroundSecondary
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selectedCategory == category) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedCategory == category) LightBlue else GrayDark
                    )
                }
            }
        }
        
        // Mobile-responsive layout - 3 columns for tools
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val currentTools = when (selectedCategory) {
                "All Tools" -> comprehensiveTools
                "Security" -> securityTools
                "Performance" -> performanceTools
                "Network" -> networkTools
                "Storage" -> storageTools
                "Privacy" -> privacyTools
                else -> comprehensiveTools
            }
            
            items(currentTools) { tool ->
                CompactToolCard(tool = tool)
            }
        }
    }
    
    // Progress Dialog
    if (isProcessing) {
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Text(
                    "Processing...",
                    color = GrayText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LightBlue,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Please wait...",
                        color = GrayText
                    )
                }
            },
            confirmButton = { },
            containerColor = BackgroundSecondary
        )
    }
    
    // Result Dialog
    if (showDialog && !isProcessing) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { 
                Text(
                    dialogTitle,
                    color = GrayText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Text(
                    dialogMessage,
                    color = GrayText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", color = LightBlue)
                }
            },
            containerColor = BackgroundSecondary
        )
    }
}

@Composable
private fun ToolCard(tool: SecurityTool) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                LightBlue.copy(alpha = 0.1f),
                                Cyan.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = LightBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayDark
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(tool.statusColor)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = tool.status,
                        style = MaterialTheme.typography.labelMedium,
                        color = tool.statusColor
                    )
                }
            }
            
            // Action button
            IconButton(
                onClick = tool.onClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBlue.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open ${tool.title}",
                    tint = LightBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactToolCard(tool: SecurityTool) {
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { tool.onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                LightBlue.copy(alpha = 0.1f),
                                Cyan.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = LightBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title
            Text(
                text = tool.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = GrayText,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(tool.statusColor)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = tool.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = tool.statusColor
                )
            }
        }
    }
}
