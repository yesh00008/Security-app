package com.guardix.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ScanReport(
    val id: String,
    val type: String,
    val timestamp: Long,
    val status: String,
    val threatsFound: Int,
    val itemsScanned: Int,
    val duration: Long,
    val details: List<String>
)

data class DeviceHealthMetric(
    val name: String,
    val value: String,
    val percentage: Float,
    val status: String,
    val icon: ImageVector,
    val color: Color
)

data class PerformanceMetric(
    val name: String,
    val current: String,
    val trend: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize managers
    val securityManager = remember { SecurityManager(context) }
    val performanceManager = remember { PerformanceManager(context) }
    
    var selectedTab by remember { mutableStateOf(0) }
    var scanReports by remember { mutableStateOf<List<ScanReport>>(emptyList()) }
    var deviceMetrics by remember { mutableStateOf<List<DeviceHealthMetric>>(emptyList()) }
    var performanceMetrics by remember { mutableStateOf<List<PerformanceMetric>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val tabs = listOf("Scan Reports", "Device Health", "Performance", "Security Status")
    
    LaunchedEffect(Unit) {
        isLoading = true
        
        // Generate sample scan reports
        scanReports = generateSampleScanReports()
        
        // Get device health metrics
        val systemInfo = securityManager.getSystemInfo()
        deviceMetrics = listOf(
            DeviceHealthMetric(
                name = "CPU Usage",
                value = "${systemInfo.cpuUsage.toInt()}%",
                percentage = systemInfo.cpuUsage / 100f,
                status = if (systemInfo.cpuUsage < 70) "Good" else "High",
                icon = Icons.Default.Memory,
                color = if (systemInfo.cpuUsage < 70) SuccessGreen else WarningOrange
            ),
            DeviceHealthMetric(
                name = "Memory Usage", 
                value = "${((systemInfo.totalRAM - systemInfo.availableRAM) * 100 / systemInfo.totalRAM).toInt()}%",
                percentage = (systemInfo.totalRAM - systemInfo.availableRAM).toFloat() / systemInfo.totalRAM.toFloat(),
                status = "Normal",
                icon = Icons.Default.Storage,
                color = LightBlue
            ),
            DeviceHealthMetric(
                name = "Battery Health",
                value = "${systemInfo.batteryLevel}%",
                percentage = systemInfo.batteryLevel / 100f,
                status = if (systemInfo.batteryLevel > 50) "Good" else "Low",
                icon = Icons.Default.BatteryChargingFull,
                color = if (systemInfo.batteryLevel > 50) SuccessGreen else ErrorRed
            ),
            DeviceHealthMetric(
                name = "Storage Space",
                value = "68%",
                percentage = 0.68f,
                status = "Available",
                icon = Icons.Default.Folder,
                color = Cyan
            )
        )
        
        // Get performance metrics
        performanceMetrics = listOf(
            PerformanceMetric(
                name = "Boot Time",
                current = "12.3s",
                trend = "-2.1s ↓",
                icon = Icons.Default.Speed,
                color = SuccessGreen
            ),
            PerformanceMetric(
                name = "App Load Time",
                current = "1.2s",
                trend = "+0.3s ↑",
                icon = Icons.Default.Apps,
                color = WarningOrange
            ),
            PerformanceMetric(
                name = "Network Speed",
                current = "85 Mbps",
                trend = "+5 Mbps ↑",
                icon = Icons.Default.NetworkCheck,
                color = LightBlue
            ),
            PerformanceMetric(
                name = "Temperature",
                current = "36°C",
                trend = "Normal",
                icon = Icons.Default.Thermostat,
                color = SuccessGreen
            )
        )
        
        isLoading = false
    }
    
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
        // Header with overall status
        OverallStatusCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundSecondary,
            contentColor = LightBlue,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                    color = LightBlue,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == index) LightBlue else GrayDark,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = LightBlue,
                    strokeWidth = 3.dp
                )
            }
        } else {
            when (selectedTab) {
                0 -> ScanReportsContent(scanReports)
                1 -> DeviceHealthContent(deviceMetrics)
                2 -> PerformanceContent(performanceMetrics)
                3 -> SecurityStatusContent()
            }
        }
    }
}

@Composable
private fun OverallStatusCard() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Status",
                    tint = SuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Device Status: Excellent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Last scan: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    StatusChip("Secure", SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip("Optimized", LightBlue)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ScanReportsContent(reports: List<ScanReport>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reports) { report ->
            ScanReportCard(report)
        }
    }
}

@Composable
private fun ScanReportCard(report: ScanReport) {
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
                        text = "${report.type} Scan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GrayText
                    )
                    
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(report.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayDark
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (report.status) {
                                "Clean" -> SuccessGreen.copy(alpha = 0.1f)
                                "Threats Found" -> ErrorRed.copy(alpha = 0.1f)
                                else -> WarningOrange.copy(alpha = 0.1f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = report.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (report.status) {
                            "Clean" -> SuccessGreen
                            "Threats Found" -> ErrorRed
                            else -> WarningOrange
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Items Scanned", report.itemsScanned.toString())
                InfoItem("Threats Found", report.threatsFound.toString())
                InfoItem("Duration", "${report.duration}s")
            }
            
            if (report.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                report.details.take(3).forEach { detail ->
                    Text(
                        text = "• $detail",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayDark,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GrayDark
        )
    }
}

@Composable
private fun DeviceHealthContent(metrics: List<DeviceHealthMetric>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(metrics) { metric ->
            DeviceHealthCard(metric)
        }
    }
}

@Composable
private fun DeviceHealthCard(metric: DeviceHealthMetric) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(metric.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = metric.name,
                    tint = metric.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = metric.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = GrayText
                    )
                    
                    Text(
                        text = metric.value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = metric.color
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { metric.percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = metric.color,
                    trackColor = metric.color.copy(alpha = 0.2f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Status: ${metric.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayDark
                )
            }
        }
    }
}

@Composable
private fun PerformanceContent(metrics: List<PerformanceMetric>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(metrics) { metric ->
            PerformanceCard(metric)
        }
    }
}

@Composable
private fun PerformanceCard(metric: PerformanceMetric) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(metric.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = metric.name,
                    tint = metric.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = metric.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current: ${metric.current}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayDark
                    )
                    
                    Text(
                        text = metric.trend,
                        style = MaterialTheme.typography.bodyMedium,
                        color = metric.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityStatusContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SecurityOverviewCard()
        }
        
        item {
            ThreatHistoryCard()
        }
        
        item {
            SecurityRecommendationsCard()
        }
    }
}

@Composable
private fun SecurityOverviewCard() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Security Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecurityMetricItem("Apps Secured", "24", Icons.Default.Shield, SuccessGreen)
                SecurityMetricItem("Threats Blocked", "8", Icons.Default.Block, ErrorRed)
                SecurityMetricItem("Last Scan", "2h ago", Icons.Default.Schedule, LightBlue)
            }
        }
    }
}

@Composable
private fun SecurityMetricItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GrayDark,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ThreatHistoryCard() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Recent Threat Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val threats = listOf(
                "Malicious website blocked - chrome.com",
                "Suspicious app permission denied - TikTok",
                "Phishing attempt detected - email link"
            )
            
            threats.forEach { threat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Threat",
                        tint = WarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = threat,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayDark
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityRecommendationsCard() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Security Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val recommendations = listOf(
                "Enable automatic security updates",
                "Review app permissions regularly",
                "Set up two-factor authentication"
            )
            
            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
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

private fun generateSampleScanReports(): List<ScanReport> {
    return listOf(
        ScanReport(
            id = "1",
            type = "Full System",
            timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
            status = "Clean",
            threatsFound = 0,
            itemsScanned = 1247,
            duration = 45,
            details = listOf(
                "All system files verified",
                "No malware detected",
                "Security configuration optimal"
            )
        ),
        ScanReport(
            id = "2", 
            type = "Quick",
            timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
            status = "Threats Found",
            threatsFound = 2,
            itemsScanned = 456,
            duration = 12,
            details = listOf(
                "Suspicious file quarantined",
                "Potentially unwanted program removed",
                "Browser cache cleaned"
            )
        ),
        ScanReport(
            id = "3",
            type = "Custom",
            timestamp = System.currentTimeMillis() - 86400000, // 1 day ago  
            status = "Clean",
            threatsFound = 0,
            itemsScanned = 892,
            duration = 28,
            details = listOf(
                "Selected folders scanned",
                "No issues found",
                "System performance verified"
            )
        )
    )
}