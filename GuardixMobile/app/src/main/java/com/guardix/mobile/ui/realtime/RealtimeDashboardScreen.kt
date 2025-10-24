package com.guardix.mobile.ui.realtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guardix.mobile.ui.components.CircularProgressIndicatorWithLabel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RealtimeDashboardScreen(
    viewModel: RealtimeDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(key1 = Unit) {
        viewModel.connectToWebSocket()
    }
    
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.disconnectFromWebSocket()
        }
    }
    
    RealtimeDashboardContent(uiState)
}

@Composable
fun RealtimeDashboardContent(state: RealtimeDashboardState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Real-time System Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            LastUpdatedInfo(state.lastUpdated)
        }
        
        item {
            SystemMetricsCard(
                cpuUsage = state.cpuUsage,
                memoryUsage = state.memoryUsage,
                diskUsage = state.diskUsage
            )
        }
        
        item {
            NetworkStatsCard(
                sent = state.networkSent,
                received = state.networkReceived,
                downloadSpeed = state.downloadSpeed,
                uploadSpeed = state.uploadSpeed
            )
        }
        
        item {
            Text(
                text = "Active Processes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(state.processes) { process ->
            ProcessItem(process)
        }
        
        item {
            Text(
                text = "Security Events",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(state.securityEvents) { event ->
            SecurityEventItem(event)
        }
    }
}

@Composable
fun LastUpdatedInfo(timestamp: String) {
    if (timestamp.isNotEmpty()) {
        val formattedTime = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
            outputFormat.format(date)
        } catch (e: Exception) {
            timestamp
        }
        
        Text(
            text = "Last updated: $formattedTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SystemMetricsCard(
    cpuUsage: Float,
    memoryUsage: Float,
    diskUsage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "System Resources",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularProgressIndicatorWithLabel(
                    progress = cpuUsage / 100f,
                    label = "CPU",
                    value = "${cpuUsage.toInt()}%",
                    color = MaterialTheme.colorScheme.primary
                )
                
                CircularProgressIndicatorWithLabel(
                    progress = memoryUsage / 100f,
                    label = "Memory",
                    value = "${memoryUsage.toInt()}%",
                    color = MaterialTheme.colorScheme.secondary
                )
                
                CircularProgressIndicatorWithLabel(
                    progress = diskUsage / 100f,
                    label = "Disk",
                    value = "${diskUsage.toInt()}%",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun NetworkStatsCard(
    sent: String,
    received: String,
    downloadSpeed: Float,
    uploadSpeed: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Network Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Data Sent",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = sent,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Data Received",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = received,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Download Speed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = String.format("%.2f Mbps", downloadSpeed),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Upload Speed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = String.format("%.2f Mbps", uploadSpeed),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessItem(process: ProcessInfoUI) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = process.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "PID: ${process.pid}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "CPU: ${String.format("%.1f%%", process.cpuPercent)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Memory: ${String.format("%.1f%%", process.memoryPercent)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityEventItem(event: SecurityEventUI) {
    val severityColor = when (event.severity.lowercase()) {
        "critical" -> Color.Red
        "high" -> Color(0xFFFF6D00)
        "medium" -> Color(0xFFFFB300)
        else -> Color(0xFF66BB6A)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(severityColor, RoundedCornerShape(6.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.type,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Source: ${event.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = event.severity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = severityColor,
                    fontWeight = FontWeight.Bold
                )
                
                val formattedTime = try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
                    val date = inputFormat.parse(event.timestamp)
                    val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                    outputFormat.format(date)
                } catch (e: Exception) {
                    event.timestamp
                }
                
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}