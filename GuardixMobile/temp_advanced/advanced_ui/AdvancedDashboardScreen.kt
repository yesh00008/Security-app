package com.guardix.mobile.ui.screens.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.guardix.mobile.data.advanced.*
import com.guardix.mobile.ui.components.*
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.launch

data class AdvancedFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: FeatureCategory,
    val onClick: () -> Unit
)

enum class FeatureCategory {
    SECURITY, PERFORMANCE, NETWORK, STORAGE, UTILITIES, MONITORING
}

data class SystemMetric(
    val label: String,
    val value: String,
    val percentage: Float,
    val trend: String,
    val color: androidx.compose.ui.graphics.Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize advanced managers
    val advancedSecurityManager = remember { AdvancedSecurityManager(context) }
    val advancedPerformanceManager = remember { AdvancedPerformanceManager(context) }
    val advancedNetworkManager = remember { AdvancedNetworkManager(context) }
    val advancedStorageManager = remember { AdvancedStorageManager(context) }
    val advancedSystemUtilities = remember { AdvancedSystemUtilitiesManager(context) }
    
    // State
    var selectedCategory by remember { mutableStateOf(FeatureCategory.SECURITY) }
    var showFeatureDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var systemHealth by remember { mutableStateOf<SystemHealthReport?>(null) }
    
    // System metrics
    val systemMetrics = listOf(
        SystemMetric("Security", "85%", 0.85f, "+2%", SuccessGreen),
        SystemMetric("Performance", "92%", 0.92f, "+5%", LightBlue),
        SystemMetric("Storage", "67%", 0.67f, "-3%", WarningOrange),
        SystemMetric("Network", "78%", 0.78f, "stable", Cyan)
    )
    
    // Advanced features by category
    val securityFeatures = listOf(
        AdvancedFeature("Advanced Virus Scan", "Deep malware detection", Icons.Default.Security, FeatureCategory.SECURITY) {
            scope.launch {
                isProcessing = true
                val result = advancedSecurityManager.performAdvancedVirusScan()
                isProcessing = false
                dialogTitle = "Advanced Scan Complete"
                dialogMessage = "Security Score: ${(result.securityScore * 100).toInt()}%\n" +
                        "Apps Scanned: ${result.appsScanned}\n" +
                        "Threats Found: ${result.threatsFound.size}\n" +
                        "System Vulnerabilities: ${result.systemVulnerabilities.size}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Real-time Protection", "Live threat monitoring", Icons.Default.Shield, FeatureCategory.SECURITY) {
            scope.launch {
                advancedSecurityManager.enableRealTimeScanning()
                dialogTitle = "Real-time Protection"
                dialogMessage = "Real-time scanning enabled\nAll apps will be monitored for threats\nBackground protection active"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Payment Protection", "Secure financial apps", Icons.Default.CreditCard, FeatureCategory.SECURITY) {
            advancedSecurityManager.enablePaymentProtection()
            dialogTitle = "Payment Protection"
            dialogMessage = "Payment apps secured\nFinancial transactions protected\nSecure payment environment enabled"
            showFeatureDialog = true
        },
        AdvancedFeature("Privacy Guard", "Advanced privacy controls", Icons.Default.PrivacyTip, FeatureCategory.SECURITY) {
            val packages = listOf("com.facebook.katana", "com.instagram.android", "com.whatsapp")
            advancedSecurityManager.enablePrivacyGuard(packages)
            dialogTitle = "Privacy Guard"
            dialogMessage = "Privacy protection enabled for social apps\nData access restricted\nLocation tracking limited"
            showFeatureDialog = true
        }
    )
    
    val performanceFeatures = listOf(
        AdvancedFeature("One-Tap Optimization", "Instant performance boost", Icons.Default.Speed, FeatureCategory.PERFORMANCE) {
            scope.launch {
                isProcessing = true
                val result = advancedPerformanceManager.performOneTapOptimization()
                isProcessing = false
                dialogTitle = "Optimization Complete"
                dialogMessage = "Performance improved by ${result.performanceGain}\n" +
                        "${result.actions.size} optimizations applied\n" +
                        "Overall improvement: ${result.overallImprovement}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Phone Boost", "Maximum performance mode", Icons.Default.Rocket, FeatureCategory.PERFORMANCE) {
            scope.launch {
                isProcessing = true
                val result = advancedPerformanceManager.boostPhonePerformance()
                isProcessing = false
                dialogTitle = "Phone Boost Complete"
                dialogMessage = "Performance increased by ${result.performanceIncrease}\n" +
                        "${result.appsKilled} background apps closed\n" +
                        "Memory freed: ${formatFileSize(result.memoryFreed)}\n" +
                        "Battery life extended: ${result.batteryLifeExtension}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Game Boost", "Gaming optimization", Icons.Default.SportsEsports, FeatureCategory.PERFORMANCE) {
            scope.launch {
                isProcessing = true
                val result = advancedPerformanceManager.enableGameBoost()
                isProcessing = false
                dialogTitle = "Game Boost Enabled"
                dialogMessage = "Gaming performance optimized\n" +
                        "Expected performance gain: ${result.expectedPerformanceGain}\n" +
                        "Optimizations:\n${result.optimizations.joinToString("\n• ", "• ")}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Thermal Control", "CPU cooling management", Icons.Default.AcUnit, FeatureCategory.PERFORMANCE) {
            val thermalData = advancedPerformanceManager.getThermalData()
            dialogTitle = "Thermal Status"
            dialogMessage = "Temperature: ${thermalData.currentTemperature.toInt()}°C\n" +
                    "Status: ${thermalData.thermalState}\n" +
                    "Recommendations:\n${thermalData.recommendations.joinToString("\n• ", "• ")}"
            showFeatureDialog = true
        }
    )
    
    val networkFeatures = listOf(
        AdvancedFeature("Network Diagnostics", "Connection analysis", Icons.Default.NetworkCheck, FeatureCategory.NETWORK) {
            scope.launch {
                isProcessing = true
                val result = advancedNetworkManager.performNetworkDiagnostics()
                isProcessing = false
                dialogTitle = "Network Diagnostics"
                dialogMessage = "Connection: ${result.connectionType}\n" +
                        "Speed: ${result.downloadSpeed.toInt()} Mbps down / ${result.uploadSpeed.toInt()} Mbps up\n" +
                        "Latency: ${result.latency}ms\n" +
                        "Quality: ${result.networkQuality}\n" +
                        "Issues: ${result.issues.size}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Data Saver", "Smart data management", Icons.Default.DataSaver, FeatureCategory.NETWORK) {
            val result = advancedNetworkManager.enableDataSaver(DataSavingMode.MODERATE)
            dialogTitle = "Data Saver Enabled"
            dialogMessage = "Mode: ${result.mode}\n" +
                    "Apps restricted: ${result.restrictedAppsCount}\n" +
                    "Estimated savings: ${result.estimatedDataSavings}\n" +
                    "Features:\n${result.features.joinToString("\n• ", "• ")}"
            showFeatureDialog = true
        },
        AdvancedFeature("Network Security", "Connection protection", Icons.Default.VpnLock, FeatureCategory.NETWORK) {
            val analysis = advancedNetworkManager.analyzeNetworkSecurity()
            dialogTitle = "Network Security"
            dialogMessage = "Connection: ${if (analysis.isSecureConnection) "Secure" else "Unsecured"}\n" +
                    "Security Type: ${analysis.securityType}\n" +
                    "Encryption: ${analysis.encryptionStrength}\n" +
                    "Risk Level: ${analysis.riskLevel}\n" +
                    "Vulnerabilities: ${analysis.vulnerabilities.size}"
            showFeatureDialog = true
        },
        AdvancedFeature("Data Monitor", "Usage tracking", Icons.Default.DataUsage, FeatureCategory.NETWORK) {
            val analysis = advancedNetworkManager.analyzeDataUsage()
            dialogTitle = "Data Usage Analysis"
            dialogMessage = "Total Usage: ${formatFileSize(analysis.totalUsage)}\n" +
                    "WiFi: ${formatFileSize(analysis.wifiUsage)}\n" +
                    "Mobile: ${formatFileSize(analysis.mobileUsage)}\n" +
                    "Warning Level: ${analysis.warningLevel}\n" +
                    "Top consumers: ${analysis.topDataConsumers.size}"
            showFeatureDialog = true
        }
    )
    
    val storageFeatures = listOf(
        AdvancedFeature("Storage Analysis", "Detailed space usage", Icons.Default.Storage, FeatureCategory.STORAGE) {
            scope.launch {
                isProcessing = true
                val result = advancedStorageManager.performStorageAnalysis()
                isProcessing = false
                dialogTitle = "Storage Analysis"
                dialogMessage = "Used: ${formatFileSize(result.usedStorage)} / ${formatFileSize(result.totalStorage)}\n" +
                        "Health: ${result.storageHealth}\n" +
                        "Largest files: ${result.largestFiles.size}\n" +
                        "Duplicates: ${result.duplicateFiles.size} groups\n" +
                        "Cleanup recommendations: ${result.cleanupRecommendations.size}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Duplicate Cleaner", "Remove duplicate files", Icons.Default.ContentCopy, FeatureCategory.STORAGE) {
            scope.launch {
                isProcessing = true
                val result = advancedStorageManager.findAndRemoveDuplicates()
                isProcessing = false
                dialogTitle = "Duplicate Cleanup"
                dialogMessage = "Groups found: ${result.duplicateGroupsFound}\n" +
                        "Files removed: ${result.totalDuplicatesRemoved}\n" +
                        "Space freed: ${formatFileSize(result.spaceFreed)}\n" +
                        "Categories cleaned:\n${result.categories.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Media Organizer", "Sort photos & videos", Icons.Default.PhotoLibrary, FeatureCategory.STORAGE) {
            scope.launch {
                isProcessing = true
                val result = advancedStorageManager.organizeMediaFiles()
                isProcessing = false
                dialogTitle = "Media Organization"
                dialogMessage = "Photos organized: ${result.photosOrganized}\n" +
                        "Videos organized: ${result.videosOrganized}\n" +
                        "Audio organized: ${result.audioOrganized}\n" +
                        "Folders created: ${result.foldersCreated}\n" +
                        "Space freed: ${formatFileSize(result.spaceFreed)}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("App Manager", "Analyze installed apps", Icons.Default.Apps, FeatureCategory.STORAGE) {
            val analysis = advancedStorageManager.analyzeInstalledApps()
            dialogTitle = "App Management"
            dialogMessage = "Total apps: ${analysis.totalApps}\n" +
                    "Rarely used: ${analysis.rarelyUsedApps.size}\n" +
                    "Large apps: ${analysis.largeApps.size}\n" +
                    "Need updates: ${analysis.outdatedApps.size}\n" +
                    "Potential savings: ${formatFileSize(analysis.potentialSpaceSavings)}"
            showFeatureDialog = true
        }
    )
    
    val utilityFeatures = listOf(
        AdvancedFeature("System Health", "Comprehensive diagnostics", Icons.Default.Health, FeatureCategory.UTILITIES) {
            scope.launch {
                isProcessing = true
                val health = advancedSystemUtilities.performSystemHealthCheck()
                systemHealth = health
                isProcessing = false
                dialogTitle = "System Health Report"
                dialogMessage = "Overall Health: ${health.overallHealth}\n" +
                        "CPU: ${health.cpuHealth.status}\n" +
                        "Memory: ${health.memoryHealth.status}\n" +
                        "Storage: ${health.storageHealth.status}\n" +
                        "Battery: ${health.batteryHealth.status}\n" +
                        "Issues found: ${health.issues.size}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("App Clone", "Dual app instances", Icons.Default.CopyAll, FeatureCategory.UTILITIES) {
            scope.launch {
                isProcessing = true
                val result = advancedSystemUtilities.createAppClone("com.whatsapp")
                isProcessing = false
                dialogTitle = "App Clone"
                if (result.success) {
                    dialogMessage = "Clone created successfully!\n" +
                            "Original: ${result.cloneInfo?.originalAppName}\n" +
                            "Clone: ${result.cloneInfo?.cloneAppName}\n" +
                            "Isolated data: ${result.cloneInfo?.isolatedData}"
                } else {
                    dialogMessage = result.message
                }
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Safe Box", "Encrypted file vault", Icons.Default.Lock, FeatureCategory.UTILITIES) {
            scope.launch {
                isProcessing = true
                val result = advancedSystemUtilities.createSafeBox()
                isProcessing = false
                dialogTitle = "Safe Box Created"
                dialogMessage = "Secure vault created with:\n" +
                        "Encryption: ${result.encryptionLevel}\n" +
                        "Features:\n${result.features.joinToString("\n• ", "• ")}"
                showFeatureDialog = true
            }
        },
        AdvancedFeature("Notification Manager", "Control app notifications", Icons.Default.Notifications, FeatureCategory.UTILITIES) {
            val analysis = advancedSystemUtilities.analyzeNotifications()
            dialogTitle = "Notification Analysis"
            dialogMessage = "Daily notifications: ${analysis.totalNotificationsPerDay}\n" +
                    "Blocked apps: ${analysis.blockedApps}\n" +
                    "Top apps: ${analysis.topNotificationApps.take(3).joinToString { it.appName }}\n" +
                    "Recommendations: ${analysis.recommendations.size}"
            showFeatureDialog = true
        }
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
        // Header
        Text(
            text = "Advanced Security Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = GrayText,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // System Health Overview
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "System Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(systemMetrics) { metric ->
                        MetricCard(metric = metric)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = FeatureCategory.values().indexOf(selectedCategory),
            containerColor = BackgroundSecondary,
            contentColor = LightBlue,
            divider = {}
        ) {
            FeatureCategory.values().forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedCategory == category) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedCategory == category) LightBlue else GrayDark,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feature Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(600.dp)
        ) {
            val currentFeatures = when (selectedCategory) {
                FeatureCategory.SECURITY -> securityFeatures
                FeatureCategory.PERFORMANCE -> performanceFeatures
                FeatureCategory.NETWORK -> networkFeatures
                FeatureCategory.STORAGE -> storageFeatures
                FeatureCategory.UTILITIES -> utilityFeatures
                else -> securityFeatures
            }
            
            items(currentFeatures) { feature ->
                AdvancedFeatureCard(
                    feature = feature,
                    onClick = feature.onClick
                )
            }
        }
    }
    
    // Processing Dialog
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
                        "Please wait while processing...",
                        color = GrayText
                    )
                }
            },
            confirmButton = { },
            containerColor = BackgroundSecondary
        )
    }
    
    // Feature Result Dialog
    if (showFeatureDialog && !isProcessing) {
        AlertDialog(
            onDismissRequest = { showFeatureDialog = false },
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
                TextButton(onClick = { showFeatureDialog = false }) {
                    Text("OK", color = LightBlue)
                }
            },
            containerColor = BackgroundSecondary
        )
    }
}

@Composable
private fun MetricCard(metric: SystemMetric) {
    NeumorphicCard(
        modifier = Modifier.width(120.dp),
        elevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = metric.color
            )
            Text(
                text = metric.label,
                style = MaterialTheme.typography.bodySmall,
                color = GrayDark
            )
            Text(
                text = metric.trend,
                style = MaterialTheme.typography.labelSmall,
                color = if (metric.trend.startsWith("+")) SuccessGreen else 
                       if (metric.trend.startsWith("-")) WarningOrange else GrayDark
            )
        }
    }
}

@Composable
private fun AdvancedFeatureCard(
    feature: AdvancedFeature,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = LightBlue,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = GrayText
            )
            
            Text(
                text = feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GrayDark
            )
        }
    }
}

// Helper function
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