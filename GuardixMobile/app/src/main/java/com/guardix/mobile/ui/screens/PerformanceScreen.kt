package com.guardix.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.guardix.mobile.data.performance.PerformanceMetrics
import com.guardix.mobile.data.performance.PerformanceMonitor
import com.guardix.mobile.ui.components.NeumorphicCard
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PerformanceCard(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val progress: Float,
    val isGoodPerformance: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    performanceMonitor: PerformanceMonitor = PerformanceMonitor(LocalContext.current)
) {
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var performanceMetrics by remember { mutableStateOf<PerformanceMetrics?>(null) }
    var performanceScore by remember { mutableStateOf(0) }
    var isOptimizing by remember { mutableStateOf(false) }
    
    // Auto-refresh performance data
    LaunchedEffect(Unit) {
        while (true) {
            scope.launch {
                try {
                    val metrics = performanceMonitor.collectMetrics()
                    performanceMetrics = metrics
                    performanceScore = performanceMonitor.calculatePerformanceScore(metrics)
                    isLoading = false
                } catch (e: Exception) {
                    // Handle error gracefully
                    isLoading = false
                }
            }
            delay(5000) // Refresh every 5 seconds
        }
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Performance Monitor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
                Text(
                    text = "Real-time system metrics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayDark
                )
            }
            
            // Refresh indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LightBlue,
                    strokeWidth = 2.dp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Performance Score Card
            item {
                PerformanceScoreCard(
                    score = performanceScore,
                    isLoading = isLoading,
                    onOptimize = {
                        scope.launch {
                            isOptimizing = true
                            delay(3000)
                            isOptimizing = false
                        }
                    },
                    isOptimizing = isOptimizing
                )
            }
            
            // Performance Metrics Grid
            performanceMetrics?.let { metrics ->
                item {
                    Text(
                        text = "System Metrics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GrayText,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    val performanceCards = createPerformanceCards(metrics, performanceMonitor)
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(performanceCards) { card ->
                            PerformanceMetricCard(card = card)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceScoreCard(
    score: Int,
    isLoading: Boolean,
    onOptimize: () -> Unit,
    isOptimizing: Boolean
) {
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Performance Score",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(4.dp),
                        color = LightBlue,
                        trackColor = BackgroundSecondary
                    )
                } else {
                    Text(
                        text = "$score/100",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            score >= 85 -> SuccessGreen
                            score >= 70 -> WarningOrange
                            else -> ErrorRed
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = when {
                            score >= 85 -> "Excellent Performance"
                            score >= 70 -> "Good Performance"
                            score >= 50 -> "Fair Performance"
                            else -> "Needs Optimization"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayDark
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onOptimize,
                    enabled = !isOptimizing && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isOptimizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimizing...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimize")
                    }
                }
            }
            
            // Performance visualization
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = if (isLoading) 0f else (score / 100f),
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp,
                    color = when {
                        score >= 85 -> SuccessGreen
                        score >= 70 -> WarningOrange
                        else -> ErrorRed
                    },
                    trackColor = BackgroundSecondary
                )
                
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = LightBlue,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(card: PerformanceCard) {
    NeumorphicCard(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                tint = card.color,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = card.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GrayText,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = card.title,
                style = MaterialTheme.typography.labelSmall,
                color = GrayDark,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = card.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = card.color,
                trackColor = BackgroundSecondary
            )
        }
    }
}

private fun createPerformanceCards(
    metrics: PerformanceMetrics,
    performanceMonitor: PerformanceMonitor
): List<PerformanceCard> {
    return listOf(
        PerformanceCard(
            title = "Memory",
            value = "${metrics.memoryUsage.usagePercentage.toInt()}%",
            subtitle = performanceMonitor.formatBytes(metrics.memoryUsage.usedRAM),
            icon = Icons.Default.Memory,
            color = LightBlue,
            progress = metrics.memoryUsage.usagePercentage / 100f,
            isGoodPerformance = metrics.memoryUsage.usagePercentage < 70
        ),
        PerformanceCard(
            title = "Storage",
            value = "${metrics.storageInfo.usagePercentage.toInt()}%",
            subtitle = performanceMonitor.formatBytes(metrics.storageInfo.availableStorage) + " free",
            icon = Icons.Default.Storage,
            color = WarningOrange,
            progress = metrics.storageInfo.usagePercentage / 100f,
            isGoodPerformance = metrics.storageInfo.usagePercentage < 80
        ),
        PerformanceCard(
            title = "CPU",
            value = "${metrics.cpuInfo.usage.toInt()}%",
            subtitle = "${metrics.cpuInfo.cores} cores",
            icon = Icons.Default.Speed,
            color = SuccessGreen,
            progress = metrics.cpuInfo.usage / 100f,
            isGoodPerformance = metrics.cpuInfo.usage < 60
        ),
        PerformanceCard(
            title = "Battery",
            value = "${metrics.batteryInfo.level}%",
            subtitle = if (metrics.batteryInfo.isCharging) "Charging" else "Good",
            icon = if (metrics.batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.Battery6Bar,
            color = Cyan,
            progress = metrics.batteryInfo.level / 100f,
            isGoodPerformance = metrics.batteryInfo.level > 20
        )
    )
}