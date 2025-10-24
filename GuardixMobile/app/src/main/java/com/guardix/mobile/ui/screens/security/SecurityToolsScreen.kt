package com.guardix.mobile.ui.screens.security

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
import com.guardix.mobile.data.ScanResult
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityToolsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SecurityToolsViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(context) {
        viewModel.initializeSecurityManager(context)
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
                    "Security & Privacy Tools",
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
            // Security Status Overview
            item {
                SecurityStatusCard(
                    isScanning = uiState.isScanning,
                    scanProgress = uiState.scanProgress,
                    lastScanResult = uiState.lastScanResult
                )
            }
            
            // Security Tools Grid
            items(getSecurityTools()) { tool ->
                SecurityToolCard(
                    tool = tool,
                    onToolClick = { toolType ->
                        coroutineScope.launch {
                            viewModel.handleToolAction(toolType)
                        }
                    }
                )
            }
            
            // Recent Security Activity
            if (uiState.recentActivity.isNotEmpty()) {
                item {
                    RecentSecurityActivity(
                        activities = uiState.recentActivity
                    )
                }
            }
        }
    }
    
    // Show scan results dialog
    if (uiState.showScanResults && uiState.lastScanResult != null) {
        val lastScanResult = uiState.lastScanResult!!
        ScanResultsDialog(
            scanResult = lastScanResult,
            onDismiss = { viewModel.dismissScanResults() }
        )
    }
    
    // Show app lock dialog
    if (uiState.showAppLockDialog) {
        AppLockSetupDialog(
            apps = uiState.availableApps,
            onAppLock = { packageName, lockType ->
                coroutineScope.launch {
                    viewModel.lockApp(packageName, lockType)
                }
            },
            onDismiss = { viewModel.dismissAppLockDialog() }
        )
    }
}

@Composable
private fun SecurityStatusCard(
    isScanning: Boolean,
    scanProgress: Float,
    lastScanResult: ScanResult?
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
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = if (lastScanResult?.threatsFound?.isEmpty() == true) SuccessGreen else WarningOrange,
                    modifier = Modifier.size(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Security Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayText
                    )
                    Text(
                        if (lastScanResult == null) "No recent scan" 
                        else if (lastScanResult.threatsFound.isEmpty()) "Device Protected" 
                        else "${lastScanResult.threatsFound.size} threats found",
                        fontSize = 14.sp,
                        color = GrayDark
                    )
                }
                
                if (isScanning) {
                    CircularProgressIndicator(
                        progress = scanProgress,
                        modifier = Modifier.size(24.dp),
                        color = LightBlue
                    )
                }
            }
            
            if (isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = scanProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = LightBlue
                )
                Text(
                    "Scanning... ${(scanProgress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = GrayDark,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            lastScanResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SecurityStat("Apps Scanned", result.appsScanned.toString())
                    SecurityStat("Threats Found", result.threatsFound.size.toString())
                    SecurityStat("Scan Time", "${result.scanDuration / 1000}s")
                }
            }
        }
    }
}

@Composable
private fun SecurityStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = LightBlue
        )
        Text(
            label,
            fontSize = 12.sp,
            color = GrayDark
        )
    }
}

@Composable
private fun SecurityToolCard(
    tool: SecurityTool,
    onToolClick: (SecurityToolType) -> Unit
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
            }
            
            if (tool.isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SuccessGreen, androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

@Composable
private fun RecentSecurityActivity(
    activities: List<SecurityActivity>
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
                "Recent Security Activity",
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
private fun ScanResultsDialog(
    scanResult: ScanResult,
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
                    if (scanResult.threatsFound.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (scanResult.threatsFound.isEmpty()) SuccessGreen else WarningOrange
                )
                Text("Scan Results")
            }
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        "Scanned ${scanResult.appsScanned} apps in ${scanResult.scanDuration / 1000} seconds",
                        fontSize = 14.sp,
                        color = GrayDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (scanResult.threatsFound.isNotEmpty()) {
                    item {
                        Text(
                            "Threats Found:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(scanResult.threatsFound) { threat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    threat.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${threat.type} - ${threat.severity} Risk",
                                    fontSize = 12.sp,
                                    color = ErrorRed
                                )
                                Text(
                                    threat.description,
                                    fontSize = 12.sp,
                                    color = GrayDark
                                )
                            }
                        }
                    }
                }
                
                if (scanResult.recommendations.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Recommendations:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(scanResult.recommendations) { recommendation ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("•", color = LightBlue)
                            Text(
                                recommendation,
                                fontSize = 14.sp,
                                color = GrayText
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun AppLockSetupDialog(
    apps: List<AppLockInfo>,
    onAppLock: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedApp by remember { mutableStateOf<AppLockInfo?>(null) }
    var selectedLockType by remember { mutableStateOf("FINGERPRINT") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Lock Setup") },
        text = {
            Column {
                Text("Select an app to lock:")
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(apps.filter { !it.isLocked }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedApp == app,
                                onClick = { selectedApp = app }
                            )
                            Text(
                                app.appName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                if (selectedApp != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lock Type:")
                    Row {
                        listOf("FINGERPRINT", "PASSWORD", "PATTERN").forEach { lockType ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedLockType == lockType,
                                    onClick = { selectedLockType = lockType }
                                )
                                Text(lockType, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedApp?.let { app ->
                        onAppLock(app.packageName, selectedLockType)
                        onDismiss()
                    }
                },
                enabled = selectedApp != null
            ) {
                Text("Lock App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data classes for UI
data class SecurityTool(
    val type: SecurityToolType,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isActive: Boolean = false
)

enum class SecurityToolType {
    VIRUS_SCAN,
    REAL_TIME_SCAN,
    APP_LOCK,
    PAYMENT_PROTECTION,
    PERMISSIONS_MANAGER,
    PRIVACY_GUARD,
    SECURITY_REMINDERS,
    FILE_PRIVACY
}

data class SecurityActivity(
    val description: String,
    val timestamp: String,
    val icon: ImageVector,
    val color: Color
)

private fun getSecurityTools(): List<SecurityTool> {
    return listOf(
        SecurityTool(
            type = SecurityToolType.VIRUS_SCAN,
            title = "Virus Scan",
            description = "Comprehensive malware and threat detection",
            icon = Icons.Default.Security,
            color = LightBlue
        ),
        SecurityTool(
            type = SecurityToolType.REAL_TIME_SCAN,
            title = "Real-time Protection",
            description = "Continuous monitoring for new threats",
            icon = Icons.Default.Shield,
            color = SuccessGreen
        ),
        SecurityTool(
            type = SecurityToolType.APP_LOCK,
            title = "App Lock",
            description = "Secure apps with fingerprint or password",
            icon = Icons.Default.Lock,
            color = WarningOrange
        ),
        SecurityTool(
            type = SecurityToolType.PAYMENT_PROTECTION,
            title = "Payment Protection",
            description = "Enhanced security for financial apps",
            icon = Icons.Default.Payment,
            color = ErrorRed
        ),
        SecurityTool(
            type = SecurityToolType.PERMISSIONS_MANAGER,
            title = "App Permissions",
            description = "Review and manage app permissions",
            icon = Icons.Default.AdminPanelSettings,
            color = LightBlue
        ),
        SecurityTool(
            type = SecurityToolType.PRIVACY_GUARD,
            title = "Privacy Guard",
            description = "Control app access to personal data",
            icon = Icons.Default.PrivacyTip,
            color = SuccessGreen
        ),
        SecurityTool(
            type = SecurityToolType.SECURITY_REMINDERS,
            title = "Security Reminders",
            description = "Regular security check notifications",
            icon = Icons.Default.NotificationImportant,
            color = WarningOrange
        ),
        SecurityTool(
            type = SecurityToolType.FILE_PRIVACY,
            title = "File Privacy",
            description = "Encrypt and protect sensitive files",
            icon = Icons.Default.Folder,
            color = ErrorRed
        )
    )
}
