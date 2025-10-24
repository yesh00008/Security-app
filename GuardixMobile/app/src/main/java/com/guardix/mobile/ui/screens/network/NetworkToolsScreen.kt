package com.guardix.mobile.ui.screens.network

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.guardix.mobile.ui.theme.*
import com.guardix.mobile.data.managers.*
import com.guardix.mobile.data.managers.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkToolsScreen(
    viewModel: NetworkToolsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeNetworkManager(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Network & Data Tools",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBlue
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
                    )
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Network Status Card
            item {
                NetworkStatusCard(
                    networkInfo = uiState.networkInfo,
                    isLoading = uiState.isLoading
                )
            }
            
            // Data Usage Overview
            item {
                DataUsageOverviewCard(
                    dataUsage = uiState.dataUsage,
                    isLoading = uiState.isLoading
                )
            }
            
            // Network Tools Grid
            item {
                NetworkToolsGrid(
                    onToolClick = { toolType ->
                        viewModel.handleNetworkTool(toolType)
                    }
                )
            }
            
            // WiFi Networks Card
            item {
                WiFiNetworksCard(
                    wifiNetworks = uiState.availableNetworks,
                    onNetworkClick = { network ->
                        viewModel.connectToWiFi(network)
                    }
                )
            }
            
            // Recent Activity
            item {
                RecentNetworkActivityCard(
                    activities = uiState.recentActivity
                )
            }
        }
    }
    
    // Loading Overlay
    if (uiState.isScanning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = LightBlue,
                        strokeWidth = 6.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Scanning Networks...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Please wait",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Speed Test Dialog
    if (uiState.showSpeedTestDialog) {
        SpeedTestDialog(
            speedTestResult = uiState.speedTestResult,
            isRunning = uiState.isSpeedTestRunning,
            onDismiss = { viewModel.dismissSpeedTestDialog() },
            onStartTest = { viewModel.startSpeedTest() }
        )
    }
    
    // Network Details Dialog
    if (uiState.showNetworkDetailsDialog && uiState.selectedNetwork != null) {
        NetworkDetailsDialog(
            network = uiState.selectedNetwork!!,
            onDismiss = { viewModel.dismissNetworkDetailsDialog() },
            onConnect = { viewModel.connectToWiFi(it) },
            onForget = { viewModel.forgetNetwork(it) }
        )
    }
}

@Composable
fun NetworkStatusCard(
    networkInfo: NetworkInfo?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (networkInfo?.isConnected == true) SuccessGreen else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = LightBlue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Loading network information...")
                }
            } else if (networkInfo != null) {
                NetworkInfoRow("Connection", networkInfo.connectionType)
                NetworkInfoRow("Network Name", networkInfo.networkName)
                NetworkInfoRow("IP Address", networkInfo.ipAddress)
                NetworkInfoRow("Signal Strength", "${networkInfo.signalStrength}%")
                NetworkInfoRow("Speed", "${networkInfo.connectionSpeed} Mbps")
            } else {
                Text(
                    text = "No network information available",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun NetworkInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DataUsageOverviewCard(
    dataUsage: DataUsageInfo?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Data Usage This Month",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = LightBlue
                )
            } else if (dataUsage != null) {
                // Data usage progress
                val usagePercent = (dataUsage.usedData.toFloat() / dataUsage.dataLimit.toFloat()) * 100
                
                LinearProgressIndicator(
                    progress = usagePercent / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        usagePercent > 90 -> ErrorRed
                        usagePercent > 75 -> WarningOrange
                        else -> SuccessGreen
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${formatDataSize(dataUsage.usedData)} used",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${formatDataSize(dataUsage.dataLimit)} limit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${formatDataSize(dataUsage.remainingData)} remaining (${String.format("%.1f", 100 - usagePercent)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NetworkToolsGrid(
    onToolClick: (NetworkToolType) -> Unit
) {
    val tools = listOf(
        NetworkTool("Speed Test", Icons.Default.Speed, LightBlue, NetworkToolType.SPEED_TEST),
        NetworkTool("WiFi Analyzer", Icons.Default.Analytics, SuccessGreen, NetworkToolType.WIFI_ANALYZER),
        NetworkTool("Ping Test", Icons.Default.Timeline, WarningOrange, NetworkToolType.PING_TEST),
        NetworkTool("Port Scanner", Icons.Default.Scanner, Purple, NetworkToolType.PORT_SCANNER),
        NetworkTool("Data Monitor", Icons.Default.DataUsage, Cyan, NetworkToolType.DATA_MONITOR),
        NetworkTool("Network Info", Icons.Default.Info, ErrorRed, NetworkToolType.NETWORK_INFO)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Network Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tools) { tool ->
                    NetworkToolCard(
                        tool = tool,
                        onClick = { onToolClick(tool.type) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetworkToolCard(
    tool: NetworkTool,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .combinedClickable(
                onClickLabel = tool.name,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = tool.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, tool.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        tool.color.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.name,
                    tint = tool.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tool.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun WiFiNetworksCard(
    wifiNetworks: List<WiFiNetwork>,
    onNetworkClick: (WiFiNetwork) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Networks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = LightBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wifiNetworks) { network ->
                    WiFiNetworkItem(
                        network = network,
                        onClick = { onNetworkClick(network) }
                    )
                }
            }
        }
    }
}

@Composable
fun WiFiNetworkItem(
    network: WiFiNetwork,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (network.isSecured) Icons.Default.Lock else Icons.Default.WifiProtectedSetup,
                contentDescription = null,
                tint = if (network.signalStrength > 70) SuccessGreen 
                      else if (network.signalStrength > 40) WarningOrange 
                      else ErrorRed,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = network.ssid,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (network.isConnected) "Connected" else "${network.signalStrength}% • ${network.security}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (network.isConnected) SuccessGreen else Color.Gray
                )
            }
            
            if (network.isConnected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Connected",
                    tint = SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RecentNetworkActivityCard(
    activities: List<NetworkActivity>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.height(150.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { activity ->
                    NetworkActivityItem(activity = activity)
                }
            }
        }
    }
}

@Composable
fun NetworkActivityItem(
    activity: NetworkActivity
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(activity.color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = activity.color,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// Dialog Components
@Composable
fun SpeedTestDialog(
    speedTestResult: SpeedTestResult?,
    isRunning: Boolean,
    onDismiss: () -> Unit,
    onStartTest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Internet Speed Test",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = LightBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Testing speed...")
                } else if (speedTestResult != null) {
                    Text("Download: ${speedTestResult.downloadSpeed} Mbps")
                    Text("Upload: ${speedTestResult.uploadSpeed} Mbps")
                    Text("Ping: ${speedTestResult.ping} ms")
                } else {
                    Text("Test your internet connection speed")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = if (!isRunning && speedTestResult == null) onStartTest else onDismiss
            ) {
                Text(if (!isRunning && speedTestResult == null) "Start Test" else "Close")
            }
        },
        dismissButton = {
            if (!isRunning) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun NetworkDetailsDialog(
    network: WiFiNetwork,
    onDismiss: () -> Unit,
    onConnect: (WiFiNetwork) -> Unit,
    onForget: (WiFiNetwork) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = network.ssid,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Security: ${network.security}")
                Text("Signal Strength: ${network.signalStrength}%")
                Text("Frequency: ${network.frequency} GHz")
                if (network.isConnected) {
                    Text("Status: Connected", color = SuccessGreen)
                }
            }
        },
        confirmButton = {
            if (!network.isConnected) {
                TextButton(onClick = { onConnect(network) }) {
                    Text("Connect")
                }
            }
        },
        dismissButton = {
            Row {
                if (network.isConnected) {
                    TextButton(onClick = { onForget(network) }) {
                        Text("Forget")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

// Data classes and utilities
data class NetworkTool(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: NetworkToolType
)

private fun formatDataSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        else -> String.format("%.0f KB", bytes / 1024.0)
    }
}
