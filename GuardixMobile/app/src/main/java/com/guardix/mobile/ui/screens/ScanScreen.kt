package com.guardix.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardix.mobile.data.*
import com.guardix.mobile.ui.components.NeumorphicCard
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ScanType(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val duration: String,
    val color: Color,
    val isMLPowered: Boolean = false
)

data class ScanResult(
    val scanType: String,
    val status: ScanStatus,
    val itemsScanned: Int,
    val threatsFound: Int,
    val duration: Long,
    val threats: List<ThreatInfo>,
    val recommendations: List<String>
)

data class ThreatInfo(
    val name: String,
    val type: String,
    val severity: String,
    val location: String,
    val action: String
)

enum class ScanStatus {
    IDLE, SCANNING, COMPLETED, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize managers
    val securityManager = remember { SecurityManager(context) }
    
    var currentScanStatus by remember { mutableStateOf(ScanStatus.IDLE) }
    var scanProgress by remember { mutableStateOf(0f) }
    var currentScanType by remember { mutableStateOf<ScanType?>(null) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var autoScanEnabled by remember { mutableStateOf(true) }
    var anomalyDetectionEnabled by remember { mutableStateOf(true) }
    var selectedScanType by remember { mutableStateOf<ScanType?>(null) }
    var showScanOptions by remember { mutableStateOf(false) }
    
    // Animation for scanning
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scanTypes = listOf(
        ScanType(
            id = "quick",
            name = "Quick Scan",
            description = "Fast scan of critical system areas with ML threat detection",
            icon = Icons.Default.Speed,
            duration = "2-5 min",
            color = LightBlue,
            isMLPowered = true
        ),
        ScanType(
            id = "full",
            name = "Full System Scan",
            description = "Comprehensive scan of entire device with deep ML analysis",
            icon = Icons.Default.Security,
            duration = "15-30 min",
            color = SuccessGreen,
            isMLPowered = true
        ),
        ScanType(
            id = "custom",
            name = "Custom Scan",
            description = "Scan selected folders and file types",
            icon = Icons.Default.FolderSpecial,
            duration = "Variable",
            color = WarningOrange
        ),
        ScanType(
            id = "anomaly",
            name = "Anomaly Detection",
            description = "AI-powered behavioral analysis for suspicious activities",
            icon = Icons.Default.Psychology,
            duration = "5-10 min",
            color = Purple,
            isMLPowered = true
        ),
        ScanType(
            id = "realtime",
            name = "Real-time Protection",
            description = "Continuous ML-powered monitoring and threat detection",
            icon = Icons.Default.Shield,
            duration = "Always on",
            color = Cyan,
            isMLPowered = true
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
        // Security Status Overview
        SecurityStatusOverview(
            autoScanEnabled = autoScanEnabled,
            anomalyDetectionEnabled = anomalyDetectionEnabled,
            onAutoScanToggle = { autoScanEnabled = it },
            onAnomalyToggle = { anomalyDetectionEnabled = it }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Scan Progress or Results
        when (currentScanStatus) {
            ScanStatus.SCANNING -> {
                ScanProgressCard(
                    scanType = currentScanType!!,
                    progress = scanProgress,
                    rotationAngle = rotationAngle
                )
            }
            ScanStatus.COMPLETED -> {
                scanResult?.let { result ->
                    ScanResultCard(result) {
                        currentScanStatus = ScanStatus.IDLE
                        scanResult = null
                    }
                }
            }
            else -> {
                // Scan Types Grid
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Choose Scan Type",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = GrayText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(scanTypes) { scanType ->
                        ScanTypeCard(
                            scanType = scanType,
                            onClick = {
                                selectedScanType = scanType
                                startScan(
                                    scanType = scanType,
                                    securityManager = securityManager,
                                    scope = scope,
                                    onStatusChange = { status -> currentScanStatus = status },
                                    onProgressUpdate = { progress -> scanProgress = progress },
                                    onScanStart = { type -> currentScanType = type },
                                    onScanComplete = { result -> scanResult = result }
                                )
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Scan Settings Card
                        ScanSettingsCard(
                            autoScanEnabled = autoScanEnabled,
                            anomalyDetectionEnabled = anomalyDetectionEnabled,
                            onAutoScanToggle = { autoScanEnabled = it },
                            onAnomalyToggle = { anomalyDetectionEnabled = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityStatusOverview(
    autoScanEnabled: Boolean,
    anomalyDetectionEnabled: Boolean,
    onAutoScanToggle: (Boolean) -> Unit,
    onAnomalyToggle: (Boolean) -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Security Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GrayText
                    )
                    
                    Text(
                        text = "Last scan: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayDark
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SuccessGreen.copy(alpha = 0.2f),
                                    SuccessGreen.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Protected",
                        tint = SuccessGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Protection Features
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProtectionFeature(
                    icon = Icons.Default.AutoMode,
                    name = "Auto Scan",
                    enabled = autoScanEnabled,
                    onToggle = onAutoScanToggle
                )
                
                ProtectionFeature(
                    icon = Icons.Default.Psychology,
                    name = "AI Detection",
                    enabled = anomalyDetectionEnabled,
                    onToggle = onAnomalyToggle
                )
                
                ProtectionFeature(
                    icon = Icons.Default.Update,
                    name = "Real-time",
                    enabled = true,
                    onToggle = { }
                )
            }
        }
    }
}

@Composable
private fun ProtectionFeature(
    icon: ImageVector,
    name: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) LightBlue.copy(alpha = 0.2f) else GrayDark.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (enabled) LightBlue else GrayDark,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) GrayText else GrayDark,
            fontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal
        )
        
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LightBlue,
                checkedTrackColor = LightBlue.copy(alpha = 0.3f),
                uncheckedThumbColor = GrayDark,
                uncheckedTrackColor = GrayDark.copy(alpha = 0.3f)
            ),
            modifier = Modifier.size(width = 32.dp, height = 20.dp)
        )
    }
}

@Composable
private fun ScanTypeCard(
    scanType: ScanType,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                scanType.color.copy(alpha = 0.2f),
                                scanType.color.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = scanType.icon,
                    contentDescription = scanType.name,
                    tint = scanType.color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scanType.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GrayText
                    )
                    
                    if (scanType.isMLPowered) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Purple.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = Purple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = scanType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Duration",
                        tint = scanType.color,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = scanType.duration,
                        style = MaterialTheme.typography.labelMedium,
                        color = scanType.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Start scan",
                tint = GrayDark,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ScanProgressCard(
    scanType: ScanType,
    progress: Float,
    rotationAngle: Float
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scanning in Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Text(
                text = scanType.name,
                style = MaterialTheme.typography.titleMedium,
                color = scanType.color,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    color = scanType.color,
                    strokeWidth = 8.dp,
                    trackColor = scanType.color.copy(alpha = 0.2f)
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(rotationAngle)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    scanType.color.copy(alpha = 0.3f),
                                    scanType.color.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = scanType.icon,
                        contentDescription = "Scanning",
                        tint = scanType.color,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "${(progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Analyzing system files and detecting threats...",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScanResultCard(
    result: ScanResult,
    onDismiss: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Complete",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GrayDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Icon and Message
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (result.threatsFound == 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "Status",
                    tint = if (result.threatsFound == 0) SuccessGreen else WarningOrange,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = if (result.threatsFound == 0) "No threats found" else "${result.threatsFound} threats detected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (result.threatsFound == 0) SuccessGreen else WarningOrange
                    )
                    
                    Text(
                        text = "${result.itemsScanned} items scanned in ${result.duration}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayDark
                    )
                }
            }
            
            if (result.threats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Threats Found:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                result.threats.forEach { threat ->
                    ThreatItem(threat)
                }
            }
            
            if (result.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Recommendations:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                result.recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Recommendation",
                            tint = LightBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreatItem(threat: ThreatInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ErrorRed.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = "Threat",
            tint = ErrorRed,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = threat.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = GrayText
            )
            
            Text(
                text = "${threat.type} • ${threat.severity}",
                style = MaterialTheme.typography.bodySmall,
                color = GrayDark
            )
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SuccessGreen.copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = threat.action,
                style = MaterialTheme.typography.labelSmall,
                color = SuccessGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ScanSettingsCard(
    autoScanEnabled: Boolean,
    anomalyDetectionEnabled: Boolean,
    onAutoScanToggle: (Boolean) -> Unit,
    onAnomalyToggle: (Boolean) -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Scan Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingItem(
                title = "Automatic Scanning",
                description = "Run daily security scans automatically",
                enabled = autoScanEnabled,
                onToggle = onAutoScanToggle,
                icon = Icons.Default.AutoMode
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingItem(
                title = "ML Anomaly Detection",
                description = "Use AI to detect unusual behavior patterns",
                enabled = anomalyDetectionEnabled,
                onToggle = onAnomalyToggle,
                icon = Icons.Default.Psychology
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingItem(
                title = "Real-time Protection",
                description = "Continuous monitoring and threat blocking",
                enabled = true,
                onToggle = { },
                icon = Icons.Default.Shield
            )
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (enabled) LightBlue else GrayDark,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = GrayText
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = GrayDark
            )
        }
        
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LightBlue,
                checkedTrackColor = LightBlue.copy(alpha = 0.3f)
            )
        )
    }
}

private fun startScan(
    scanType: ScanType,
    securityManager: SecurityManager,
    scope: kotlinx.coroutines.CoroutineScope,
    onStatusChange: (ScanStatus) -> Unit,
    onProgressUpdate: (Float) -> Unit,
    onScanStart: (ScanType) -> Unit,
    onScanComplete: (ScanResult) -> Unit
) {
    scope.launch {
        onStatusChange(ScanStatus.SCANNING)
        onScanStart(scanType)

        // Run actual scan and update progress in real time
        val deferred = async { securityManager.performSecurityScan() }
        while (!deferred.isCompleted) {
            onProgressUpdate(securityManager.scanProgress.value)
            delay(60)
        }
        val scanResult = deferred.await()

        val uiResult = ScanResult(
            scanType = scanType.name,
            status = ScanStatus.COMPLETED,
            itemsScanned = scanResult.appsScanned,
            threatsFound = scanResult.threatsFound.size,
            duration = scanResult.scanDuration,
            threats = scanResult.threatsFound.map { threat ->
                ThreatInfo(
                    name = threat.name,
                    type = threat.type,
                    severity = threat.severity,
                    location = threat.appPackage ?: "Unknown",
                    action = "Quarantined"
                )
            },
            recommendations = listOf(
                "Keep your device updated",
                "Avoid downloading apps from unknown sources",
                "Review app permissions periodically"
            )
        )

        onStatusChange(ScanStatus.COMPLETED)
        onScanComplete(uiResult)

        // Proactively clear memory used during the scan
        securityManager.clearScanMemory()
    }
}

