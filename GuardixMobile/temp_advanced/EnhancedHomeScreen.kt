package com.guardix.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.guardix.mobile.data.*
import com.guardix.mobile.data.advanced.*
import com.guardix.mobile.ui.components.*
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.launch

data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

data class SecurityStat(
    val title: String,
    val value: String,
    val trend: String,
    val isPositive: Boolean
)

data class AdvancedFeaturePreview(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize managers
    val securityManager = remember { SecurityManager(context) }
    val performanceManager = remember { PerformanceManager(context) }
    val privacyManager = remember { PrivacyManager(context) }
    
    // Initialize advanced managers
    val advancedSecurityManager = remember { AdvancedSecurityManager(context) }
    val advancedPerformanceManager = remember { AdvancedPerformanceManager(context) }
    val advancedNetworkManager = remember { AdvancedNetworkManager(context) }
    val advancedStorageManager = remember { AdvancedStorageManager(context) }
    val advancedSystemUtilities = remember { AdvancedSystemUtilitiesManager(context) }
    
    // State
    var showScanDialog by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var actionMessage by remember { mutableStateOf("") }
    var actionTitle by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var lastAdvancedScan by remember { mutableStateOf("Never") }
    var systemHealth by remember { mutableStateOf<SystemHealthReport?>(null) }
    
    // Observe security manager state
    val isScanning by securityManager.isScanning
    val scanProgress by securityManager.scanProgress
    val securityScore by securityManager.securityScore
    val lastScanTime by securityManager.lastScanTime
    val threatsBlocked by securityManager.threatsBlocked
    val appsScanned by securityManager.appsScanned
    
    // Initialize system health
    LaunchedEffect(Unit) {
        systemHealth = advancedSystemUtilities.performSystemHealthCheck()
        lastAdvancedScan = "2 hours ago" // Would be retrieved from preferences in real app
    }
    
    // Enhanced quick actions with advanced features
    val quickActions = listOf(
        QuickAction(
            title = "Advanced Scan",
            subtitle = "Deep security analysis",
            icon = Icons.Default.Security,
            onClick = {
                if (!isProcessing) {
                    scope.launch {
                        isProcessing = true
                        val result = advancedSecurityManager.performAdvancedVirusScan()
                        isProcessing = false
                        lastAdvancedScan = "Just now"
                        actionTitle = "Advanced Scan Complete"
                        actionMessage = "Security Score: ${(result.securityScore * 100).toInt()}%\n" +
                                "Apps Scanned: ${result.appsScanned}\n" +
                                "Threats Found: ${result.threatsFound.size}\n" +
                                "System Vulnerabilities: ${result.systemVulnerabilities.size}\n" +
                                "Privacy Risks: ${result.privacyRisks.size}"
                        showActionDialog = true
                    }
                }
            }
        ),
        QuickAction(
            title = "One-Tap Boost",
            subtitle = "Ultimate optimization",
            icon = Icons.Default.Speed,
            onClick = {
                scope.launch {
                    isProcessing = true
                    val result = advancedPerformanceManager.performOneTapOptimization()
                    isProcessing = false
                    actionTitle = "One-Tap Optimization Complete"
                    actionMessage = "Performance improved by ${result.performanceGain}\n" +
                            "${result.actions.size} optimizations applied\n" +
                            "Memory boost: ${result.memoryImprovement}\n" +
                            "Overall improvement: ${result.overallImprovement}"
                    showActionDialog = true
                }
            }
        ),
        QuickAction(
            title = "Network Guard",
            subtitle = "Connection protection",
            icon = Icons.Default.NetworkCheck,
            onClick = {
                scope.launch {
                    isProcessing = true
                    val result = advancedNetworkManager.performNetworkDiagnostics()
                    isProcessing = false
                    actionTitle = "Network Diagnostics Complete"
                    actionMessage = "Connection: ${result.connectionType}\n" +
                            "Speed: ${result.downloadSpeed.toInt()} Mbps down\n" +
                            "Quality: ${result.networkQuality}\n" +
                            "Security: ${if (result.isSecure) "Secure" else "Vulnerable"}\n" +
                            "Issues found: ${result.issues.size}"
                    showActionDialog = true
                }
            }
        ),
        QuickAction(
            title = "Storage Pro",
            subtitle = "Advanced cleanup",
            icon = Icons.Default.Storage,
            onClick = {
                scope.launch {
                    isProcessing = true
                    val result = advancedStorageManager.performStorageAnalysis()
                    isProcessing = false
                    actionTitle = "Storage Analysis Complete"
                    actionMessage = "Used: ${formatFileSize(result.usedStorage)} / ${formatFileSize(result.totalStorage)}\n" +
                            "Health: ${result.storageHealth}\n" +
                            "Duplicates: ${result.duplicateFiles.size} groups\n" +
                            "Large files: ${result.largestFiles.size}\n" +
                            "Cleanup potential: ${formatFileSize(result.cleanupPotential)}"
                    showActionDialog = true
                }
            }
        ),
        QuickAction(
            title = "Privacy Pro",
            subtitle = "Advanced protection",
            icon = Icons.Default.PrivacyTip,
            onClick = {
                val packages = listOf("com.facebook.katana", "com.instagram.android", "com.whatsapp")
                advancedSecurityManager.enablePrivacyGuard(packages)
                actionTitle = "Privacy Guard Enabled"
                actionMessage = "Advanced privacy protection activated:\n" +
                        "• Social media apps secured\n" +
                        "• Location tracking limited\n" +
                        "• Data access restricted\n" +
                        "• Background monitoring active"
                showActionDialog = true
            }
        ),
        QuickAction(
            title = "System Health",
            subtitle = "Comprehensive check",
            icon = Icons.Default.Health,
            onClick = {
                scope.launch {
                    isProcessing = true
                    val health = advancedSystemUtilities.performSystemHealthCheck()
                    isProcessing = false
                    systemHealth = health
                    actionTitle = "System Health Report"
                    actionMessage = "Overall Health: ${health.overallHealth}\n" +
                            "CPU: ${health.cpuHealth.status}\n" +
                            "Memory: ${health.memoryHealth.status}\n" +
                            "Storage: ${health.storageHealth.status}\n" +
                            "Battery: ${health.batteryHealth.status}\n" +
                            "Thermal: ${health.thermalHealth.status}\n\n" +
                            "Issues found: ${health.issues.size}"
                    showActionDialog = true
                }
            }
        )
    )
    
    // Enhanced security stats with advanced data
    val securityStats = listOf(
        SecurityStat("Threats Blocked", "$threatsBlocked", "+12 today", true),
        SecurityStat("System Health", systemHealth?.overallHealth ?: "Checking...", "Excellent", true),
        SecurityStat("Last Advanced Scan", lastAdvancedScan, if (lastAdvancedScan == "Just now") "completed" else "ago", lastAdvancedScan == "Just now")
    )
    
    // Advanced features preview
    val advancedFeatures = listOf(
        AdvancedFeaturePreview("Real-time Protection", "Live threat monitoring", Icons.Default.Shield, SuccessGreen),
        AdvancedFeaturePreview("Game Boost", "Gaming optimization", Icons.Default.SportsEsports, LightBlue),
        AdvancedFeaturePreview("App Clone", "Dual app instances", Icons.Default.CopyAll, Cyan),
        AdvancedFeaturePreview("Safe Box", "Encrypted vault", Icons.Default.Lock, WarningOrange)
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Enhanced Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayDark
                )
                Text(
                    text = "Guardix Mobile Pro",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
                if (systemHealth != null) {
                    Text(
                        text = "System Health: ${systemHealth!!.overallHealth}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Row {
                IconButton(
                    onClick = { navController.navigate("advanced") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "Advanced Dashboard",
                        tint = LightBlue
                    )
                }
                IconButton(
                    onClick = { /* TODO: Implement notifications */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = LightBlue
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Enhanced Security Status Card
        GradientCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    LightBlue.copy(alpha = 0.1f),
                    BackgroundSecondary
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced Security Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GrayText
                    )
                    
                    if (isProcessing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = LightBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Processing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayDark
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CircularSecurityIndicator(
                    progress = if (isScanning) scanProgress else securityScore,
                    onClick = {
                        navController.navigate("advanced")
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    securityStats.forEach { stat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (stat.isPositive) SuccessGreen else GrayText
                            )
                            Text(
                                text = stat.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayDark
                            )
                            Text(
                                text = stat.trend,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (stat.isPositive) SuccessGreen else GrayDark
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Enhanced Quick Actions Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Advanced Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = GrayText
            )
            
            TextButton(
                onClick = { navController.navigate("advanced") }
            ) {
                Text("View All", color = LightBlue)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(450.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quickActions) { action ->
                QuickActionCard(
                    onClick = action.onClick,
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = if (isProcessing) GrayDark else LightBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isProcessing) GrayDark else GrayText
                    )
                    
                    Text(
                        text = action.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayDark
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Advanced Features Preview
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Features",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText
                )
                
                TextButton(
                    onClick = { navController.navigate("advanced") }
                ) {
                    Text("Explore All", color = LightBlue)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(advancedFeatures) { feature ->
                    NeumorphicCard(
                        modifier = Modifier.width(140.dp),
                        onClick = { navController.navigate("advanced") }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = feature.title,
                                tint = feature.color,
                                modifier = Modifier.size(28.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = GrayText
                            )
                            
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayDark
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Enhanced Recent Activity Section
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText
                )
                
                TextButton(
                    onClick = { navController.navigate("reports") }
                ) {
                    Text(
                        text = "View All",
                        color = LightBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Enhanced Activity items
            val activities = listOf(
                Triple("Advanced scan completed", "System secure - no threats found", Icons.Default.Shield),
                Triple("One-tap optimization ran", "Performance improved by 15%", Icons.Default.Speed),
                Triple("Storage analysis done", "2.3 GB duplicates removed", Icons.Default.Storage),
                Triple("Network diagnostics", "Connection optimal and secure", Icons.Default.NetworkCheck),
                Triple("System health check", "All components healthy", Icons.Default.Health)
            )
            
            activities.take(4).forEachIndexed { index, (title, description, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (index) {
                                    0 -> SuccessGreen.copy(alpha = 0.1f)
                                    1 -> LightBlue.copy(alpha = 0.1f)
                                    2 -> WarningOrange.copy(alpha = 0.1f)
                                    else -> Cyan.copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = when (index) {
                                0 -> SuccessGreen
                                1 -> LightBlue
                                2 -> WarningOrange
                                else -> Cyan
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = GrayText
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayDark
                        )
                    }
                    
                    Text(
                        text = "${(index + 1) * 2} hrs ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayDark
                    )
                }
                
                if (index < 3) {
                    Divider(
                        color = GrayLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Scan Result Dialog (keeping existing functionality)
    if (showScanDialog && scanResult != null) {
        AlertDialog(
            onDismissRequest = { 
                showScanDialog = false
                scanResult = null
            },
            title = { 
                Text(
                    "Scan Complete",
                    color = GrayText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Security Score: ${(scanResult!!.securityScore * 100).toInt()}%",
                        color = GrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Apps Scanned: ${scanResult!!.appsScanned}",
                        color = GrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Threats Found: ${scanResult!!.threatsFound.size}",
                        color = if (scanResult!!.threatsFound.isEmpty()) SuccessGreen else WarningOrange,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (scanResult!!.threatsFound.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Threats:",
                            fontWeight = FontWeight.Bold,
                            color = GrayText
                        )
                        scanResult!!.threatsFound.forEach { threat ->
                            Text(
                                "• ${threat.name} (${threat.severity})",
                                color = WarningOrange,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showScanDialog = false
                        scanResult = null
                    }
                ) {
                    Text("OK", color = LightBlue)
                }
            },
            containerColor = BackgroundSecondary
        )
    }
    
    // Enhanced Action Result Dialog
    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { 
                Text(
                    actionTitle,
                    color = GrayText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Text(
                    actionMessage,
                    color = GrayText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showActionDialog = false }) {
                    Text("OK", color = LightBlue)
                }
            },
            dismissButton = {
                if (actionTitle.contains("Advanced")) {
                    TextButton(onClick = { 
                        showActionDialog = false
                        navController.navigate("advanced")
                    }) {
                        Text("View Details", color = LightBlue)
                    }
                }
            },
            containerColor = BackgroundSecondary
        )
    }
}

// Helper function for file size formatting
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