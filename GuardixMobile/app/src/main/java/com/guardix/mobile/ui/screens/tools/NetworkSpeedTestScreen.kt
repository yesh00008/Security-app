package com.guardix.mobile.ui.screens.tools

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardix.mobile.data.*
import com.guardix.mobile.ui.components.NeumorphicCard
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.random.Random

data class SpeedTestResult(
    val downloadSpeed: Float, // Mbps
    val uploadSpeed: Float,   // Mbps
    val ping: Int,           // ms
    val jitter: Int,         // ms
    val packetLoss: Float,   // %
    val server: String,
    val location: String,
    val timestamp: String,
    val connectionType: String
)

data class TestHistory(
    val results: List<SpeedTestResult>
)

enum class TestState {
    IDLE, TESTING_PING, TESTING_DOWNLOAD, TESTING_UPLOAD, COMPLETED, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSpeedTestScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var testState by remember { mutableStateOf(TestState.IDLE) }
    var currentTest by remember { mutableStateOf("") }
    var testProgress by remember { mutableStateOf(0f) }
    var currentSpeed by remember { mutableStateOf(0f) }
    var testResult by remember { mutableStateOf<SpeedTestResult?>(null) }
    var testHistory by remember { mutableStateOf<List<SpeedTestResult>>(emptyList()) }
    var autoTestEnabled by remember { mutableStateOf(false) }
    var selectedServer by remember { mutableStateOf("Auto") }
    
    // Animation for the speed gauge
    val infiniteTransition = rememberInfiniteTransition(label = "speed_test")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Load test history
    LaunchedEffect(Unit) {
        testHistory = loadTestHistory()
    }
    
    fun startSpeedTest() {
        scope.launch {
            testState = TestState.TESTING_PING
            currentTest = "Testing Ping..."
            testProgress = 0f
            
            // Simulate ping test
            for (i in 0..100 step 10) {
                delay(100)
                testProgress = i / 100f
            }
            
            testState = TestState.TESTING_DOWNLOAD
            currentTest = "Testing Download Speed..."
            testProgress = 0f
            currentSpeed = 0f
            
            // Simulate download test with speed ramping
            for (i in 0..100 step 2) {
                delay(50)
                testProgress = i / 100f
                currentSpeed = sin(i * 0.02f) * 85f + Random.nextFloat() * 15f
            }
            
            testState = TestState.TESTING_UPLOAD
            currentTest = "Testing Upload Speed..."
            testProgress = 0f
            currentSpeed = 0f
            
            // Simulate upload test
            for (i in 0..100 step 3) {
                delay(60)
                testProgress = i / 100f
                currentSpeed = sin(i * 0.015f) * 45f + Random.nextFloat() * 10f
            }
            
            // Generate final result
            testResult = SpeedTestResult(
                downloadSpeed = 78.5f + Random.nextFloat() * 20f,
                uploadSpeed = 42.3f + Random.nextFloat() * 15f,
                ping = 28 + Random.nextInt(20),
                jitter = 3 + Random.nextInt(8),
                packetLoss = Random.nextFloat() * 0.5f,
                server = "Speedtest.net Server",
                location = "New York, NY",
                timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date()),
                connectionType = "WiFi"
            )
            
            testState = TestState.COMPLETED
            
            // Add to history
            testHistory = listOf(testResult!!) + testHistory.take(9)
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
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Cyan.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Cyan
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Speed Test",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
                
                Text(
                    text = "Test your internet connection speed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayDark
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        when (testState) {
            TestState.IDLE -> {
                IdleSpeedTestInterface(
                    testHistory = testHistory,
                    autoTestEnabled = autoTestEnabled,
                    selectedServer = selectedServer,
                    onAutoTestToggle = { autoTestEnabled = it },
                    onServerChange = { selectedServer = it },
                    onStartTest = { startSpeedTest() }
                )
            }
            
            TestState.TESTING_PING, 
            TestState.TESTING_DOWNLOAD, 
            TestState.TESTING_UPLOAD -> {
                TestingInterface(
                    testState = testState,
                    currentTest = currentTest,
                    progress = testProgress,
                    currentSpeed = currentSpeed,
                    pulseScale = pulseScale
                )
            }
            
            TestState.COMPLETED -> {
                testResult?.let { result ->
                    TestCompletedInterface(
                        result = result,
                        onNewTest = {
                            testState = TestState.IDLE
                            testResult = null
                            currentSpeed = 0f
                        }
                    )
                }
            }
            
            TestState.ERROR -> {
                ErrorInterface(
                    onRetry = { startSpeedTest() }
                )
            }
        }
    }
}

@Composable
private fun IdleSpeedTestInterface(
    testHistory: List<SpeedTestResult>,
    autoTestEnabled: Boolean,
    selectedServer: String,
    onAutoTestToggle: (Boolean) -> Unit,
    onServerChange: (String) -> Unit,
    onStartTest: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Connection Status Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Connection Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ConnectionStat(
                            label = "Type",
                            value = "WiFi",
                            icon = Icons.Default.Wifi,
                            color = SuccessGreen
                        )
                        
                        ConnectionStat(
                            label = "Signal",
                            value = "Strong",
                            icon = Icons.Default.SignalWifiStatusbar4Bar,
                            color = LightBlue
                        )
                        
                        ConnectionStat(
                            label = "IP Address",
                            value = "192.168.1.15",
                            icon = Icons.Default.Computer,
                            color = Cyan
                        )
                    }
                }
            }
        }
        
        item {
            // Test Settings Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Test Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    TestSetting(
                        title = "Auto Test Schedule",
                        description = "Run speed tests automatically every hour",
                        enabled = autoTestEnabled,
                        onToggle = onAutoTestToggle,
                        icon = Icons.Default.Schedule
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Server Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Server",
                            tint = Cyan,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Test Server",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = GrayText
                            )
                            
                            Text(
                                text = "Currently: $selectedServer",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayDark
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { /* TODO: Server selection */ },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Cyan
                            )
                        ) {
                            Text("Change")
                        }
                    }
                }
            }
        }
        
        item {
            // Start Test Button
            Button(
                onClick = onStartTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brush.horizontalGradient(
                        colors = listOf(LightBlue, Cyan)
                    ).let { LightBlue }, // Simplified for now
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Start Test",
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "START SPEED TEST",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (testHistory.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Tests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
            }
            
            items(testHistory.take(5)) { result ->
                TestHistoryCard(result = result)
            }
        }
    }
}

@Composable
private fun ConnectionStat(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = GrayText,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GrayDark,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TestSetting(
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
            tint = if (enabled) Cyan else GrayDark,
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
                checkedThumbColor = Cyan,
                checkedTrackColor = Cyan.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun TestingInterface(
    testState: TestState,
    currentTest: String,
    progress: Float,
    currentSpeed: Float,
    pulseScale: Float
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTest,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Speed Gauge
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Gauge background
                Canvas(
                    modifier = Modifier.size(200.dp)
                ) {
                    drawSpeedGauge(
                        progress = progress,
                        currentSpeed = if (testState == TestState.TESTING_PING) 0f else currentSpeed
                    )
                }
                
                // Center content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (testState == TestState.TESTING_PING) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = GrayText
                        )
                        Text(
                            text = "Testing",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayDark
                        )
                    } else {
                        Text(
                            text = "${currentSpeed.toInt()}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightBlue
                        )
                        Text(
                            text = "Mbps",
                            style = MaterialTheme.typography.titleMedium,
                            color = GrayDark
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when (testState) {
                    TestState.TESTING_PING -> WarningOrange
                    TestState.TESTING_DOWNLOAD -> LightBlue
                    TestState.TESTING_UPLOAD -> SuccessGreen
                    else -> LightBlue
                },
                trackColor = GrayLight
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${(progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.titleMedium,
                color = GrayText
            )
        }
    }
}

@Composable
private fun TestCompletedInterface(
    result: SpeedTestResult,
    onNewTest: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Results Summary Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Speed Test Complete",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GrayText
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Speed Results
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SpeedResult(
                            label = "Download",
                            speed = result.downloadSpeed,
                            icon = Icons.Default.CloudDownload,
                            color = LightBlue
                        )
                        
                        SpeedResult(
                            label = "Upload", 
                            speed = result.uploadSpeed,
                            icon = Icons.Default.CloudUpload,
                            color = SuccessGreen
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Additional Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricResult(
                            label = "Ping",
                            value = "${result.ping} ms",
                            color = if (result.ping < 50) SuccessGreen else WarningOrange
                        )
                        
                        MetricResult(
                            label = "Jitter",
                            value = "${result.jitter} ms",
                            color = if (result.jitter < 10) SuccessGreen else WarningOrange
                        )
                        
                        MetricResult(
                            label = "Loss",
                            value = "${String.format("%.1f", result.packetLoss)}%",
                            color = if (result.packetLoss < 1f) SuccessGreen else ErrorRed
                        )
                    }
                }
            }
        }
        
        item {
            // Test Details Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Test Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    TestDetail(label = "Server", value = result.server)
                    TestDetail(label = "Location", value = result.location)
                    TestDetail(label = "Connection", value = result.connectionType)
                    TestDetail(label = "Time", value = result.timestamp)
                }
            }
        }
        
        item {
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Share result */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LightBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
                
                Button(
                    onClick = onNewTest,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Test Again",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Again")
                }
            }
        }
    }
}

@Composable
private fun SpeedResult(
    label: String,
    speed: Float,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = String.format("%.1f", speed),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = "Mbps",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayDark
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = GrayDark
        )
    }
}

@Composable
private fun MetricResult(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GrayDark
        )
    }
}

@Composable
private fun TestDetail(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = GrayDark
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = GrayText
        )
    }
}

@Composable
private fun TestHistoryCard(result: SpeedTestResult) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.timestamp,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = GrayText
                )
                
                Text(
                    text = "${result.connectionType} • ${result.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayDark
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", result.downloadSpeed),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LightBlue
                    )
                    Text(
                        text = "↓ Mbps",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayDark
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", result.uploadSpeed),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text(
                        text = "↑ Mbps",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayDark
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.ping}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Cyan
                    )
                    Text(
                        text = "ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayDark
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorInterface(
    onRetry: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = ErrorRed,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Test Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Text(
                text = "Unable to complete speed test. Check your connection and try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayDark,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Test")
            }
        }
    }
}

private fun DrawScope.drawSpeedGauge(
    progress: Float,
    currentSpeed: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2.5f
    
    // Draw gauge background
    drawCircle(
        color = Color.Gray.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20.dp.toPx())
    )
    
    // Draw progress arc
    val sweepAngle = 270f * progress
    drawArc(
        color = Color(0xFF03DAC5),
        startAngle = 135f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 20.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    )
}

private fun loadTestHistory(): List<SpeedTestResult> {
    return listOf(
        SpeedTestResult(
            downloadSpeed = 82.4f,
            uploadSpeed = 45.2f,
            ping = 24,
            jitter = 2,
            packetLoss = 0.0f,
            server = "Speedtest.net",
            location = "New York, NY",
            timestamp = "Today, 14:30",
            connectionType = "WiFi"
        ),
        SpeedTestResult(
            downloadSpeed = 76.8f,
            uploadSpeed = 41.6f,
            ping = 28,
            jitter = 4,
            packetLoss = 0.1f,
            server = "Speedtest.net",
            location = "New York, NY", 
            timestamp = "Yesterday, 16:45",
            connectionType = "WiFi"
        )
    )
}