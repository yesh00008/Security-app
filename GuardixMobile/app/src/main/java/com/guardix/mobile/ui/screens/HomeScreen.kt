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
import com.guardix.mobile.data.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize managers
    val securityManager = remember { SecurityManager(context) }
    val performanceManager = remember { PerformanceManager(context) }
    val privacyManager = remember { PrivacyManager(context) }
    
    // State
    var showScanDialog by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<com.guardix.mobile.data.ScanResult?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var actionMessage by remember { mutableStateOf("") }
    var actionTitle by remember { mutableStateOf("") }
    
    // Refresh data when navigating back to this screen
    LaunchedEffect(Unit) {
        // Refresh all data to ensure real-time information
        securityManager.refreshSecurityData()
        performanceManager.refreshPerformanceData()
        privacyManager.refreshPrivacyData()
    }
    
    // Observe security manager state
    val isScanning by securityManager.isScanning
    val scanProgress by securityManager.scanProgress
    val securityScore by securityManager.securityScore
    val lastScanTime by securityManager.lastScanTime
    val threatsBlocked by securityManager.threatsBlocked
    val appsScanned by securityManager.appsScanned
    
    val quickActions = listOf(
        QuickAction(
            title = "Virus Scan",
            subtitle = "Full system scan",
            icon = Icons.Default.Security,
            onClick = {
                if (!isScanning) {
                    scope.launch {
                        val result = securityManager.performSecurityScan()
                        scanResult = result
                        showScanDialog = true
                        // Refresh security data after scan
                        securityManager.refreshSecurityData()
                    }
                }
            }
        ),
        QuickAction(
            title = "Phone Boost",
            subtitle = "Optimize performance",
            icon = Icons.Default.Speed,
            onClick = {
                scope.launch {
                    val cleaned = performanceManager.cleanMemory()
                    actionTitle = "Phone Boost Complete"
                    actionMessage = "Cleaned ${formatFileSize(cleaned)} of memory\nDevice performance optimized"
                    showActionDialog = true
                    // Refresh performance data after cleanup
                    performanceManager.refreshPerformanceData()
                }
            }
        ),
        QuickAction(
            title = "Data Monitor",
            subtitle = "Track usage",
            icon = Icons.Default.DataUsage,
            onClick = {
                val systemInfo = securityManager.getSystemInfo()
                actionTitle = "Data Usage"
                actionMessage = "Network usage: ${formatFileSize(systemInfo.networkUsage)}\n" +
                        "Storage used: ${formatFileSize(systemInfo.totalStorage - systemInfo.availableStorage)}"
                showActionDialog = true
            }
        ),
        QuickAction(
            title = "App Lock",
            subtitle = "Secure apps",
            icon = Icons.Default.Lock,
            onClick = {
                val lockedCount = privacyManager.getLockedAppsCount()
                actionTitle = "App Lock"
                actionMessage = "Currently $lockedCount apps are protected\nTap Settings > App Lock to manage"
                showActionDialog = true
            }
        ),
        QuickAction(
            title = "Privacy",
            subtitle = "Permissions",
            icon = Icons.Default.PrivacyTip,
            onClick = {
                scope.launch {
                    val cleared = privacyManager.clearPrivacyData()
                    val total = cleared.values.sum()
                    actionTitle = "Privacy Cleaner"
                    actionMessage = "Cleared $total privacy traces:\n" +
                            cleared.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }
                    showActionDialog = true
                    // Refresh privacy data after clearing
                    privacyManager.refreshPrivacyData()
                }
            }
        )
    )
    
    val securityStats = listOf(
        SecurityStat("Threats Blocked", "$threatsBlocked", "+12 today", true),
        SecurityStat("Apps Scanned", "$appsScanned", "All clean", true),
        SecurityStat("Last Scan", lastScanTime, if (lastScanTime == "Just now") "completed" else "ago", lastScanTime == "Just now")
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
        // Top Navigation Bar with Logo and Notification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Guardix Logo",
                    tint = LightBlue,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardix",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
            }
            
            // Single Notification Icon
            IconButton(onClick = { /* TODO: Show notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = LightBlue
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Welcome Message
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.bodyLarge,
            color = GrayDark
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Security Status Card
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
                Text(
                    text = "Device Security Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CircularSecurityIndicator(
                    progress = if (isScanning) scanProgress else securityScore,
                    onClick = {
                        if (!isScanning) {
                            scope.launch {
                                val result = securityManager.performSecurityScan()
                                scanResult = result
                                showScanDialog = true
                                // Refresh security data after scan
                                securityManager.refreshSecurityData()
                            }
                        }
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
        
        // Quick Actions Section
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = GrayText,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quickActions) { action ->
                QuickActionCard(
                    onClick = action.onClick
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = LightBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GrayText
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
        
        // Recent Activity Section
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
                    onClick = { /* TODO: View all activity */ }
                ) {
                    Text(
                        text = "View All",
                        color = LightBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Activity items
            repeat(3) { index ->
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
                                    1 -> WarningOrange.copy(alpha = 0.1f)
                                    else -> LightBlue.copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.Shield
                                1 -> Icons.Default.Warning
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when (index) {
                                0 -> SuccessGreen
                                1 -> WarningOrange
                                else -> LightBlue
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (index) {
                                0 -> "Malware scan completed"
                                1 -> "Suspicious app detected"
                                else -> "System optimized"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = GrayText
                        )
                        Text(
                            text = when (index) {
                                0 -> "2 hours ago"
                                1 -> "4 hours ago"
                                else -> "6 hours ago"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayDark
                        )
                    }
                }
                
                if (index < 2) {
                    HorizontalDivider(
                        color = GrayLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Scan Result Dialog
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
    
    // Action Result Dialog
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
            containerColor = BackgroundSecondary
        )
    }
}
