package com.guardix.mobile.ui.screens.tools

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardix.mobile.data.*
import com.guardix.mobile.ui.components.NeumorphicCard
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MemoryUsage(
    val totalRAM: Long,
    val usedRAM: Long,
    val availableRAM: Long,
    val bufferCache: Long,
    val systemUsage: Long,
    val appsUsage: Long
)

data class RunningApp(
    val name: String,
    val packageName: String,
    val memoryUsage: Long,
    val cpuUsage: Float,
    val priority: String,
    val isSystemApp: Boolean,
    val canKill: Boolean
)

enum class CleaningState {
    IDLE, ANALYZING, CLEANING, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryCleanerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val performanceManager = remember { PerformanceManager(context) }
    
    var cleaningState by remember { mutableStateOf(CleaningState.IDLE) }
    var memoryUsage by remember { mutableStateOf<MemoryUsage?>(null) }
    var runningApps by remember { mutableStateOf<List<RunningApp>>(emptyList()) }
    var selectedApps by remember { mutableStateOf<Set<String>>(emptySet()) }
    var cleanedMemory by remember { mutableStateOf(0L) }
    var autoCleanEnabled by remember { mutableStateOf(true) }
    var aggressiveMode by remember { mutableStateOf(false) }
    
    // Load initial data
    LaunchedEffect(Unit) {
        loadMemoryData(performanceManager) { usage, apps ->
            memoryUsage = usage
            runningApps = apps
        }
    }
    
    fun startMemoryCleaning() {
        scope.launch {
            cleaningState = CleaningState.ANALYZING
            kotlinx.coroutines.delay(1000)
            
            cleaningState = CleaningState.CLEANING
            val cleaned = performanceManager.cleanMemory()
            cleanedMemory = cleaned
            
            kotlinx.coroutines.delay(1500)
            
            // Reload memory data after cleaning
            loadMemoryData(performanceManager) { usage, apps ->
                memoryUsage = usage
                runningApps = apps
            }
            
            cleaningState = CleaningState.COMPLETED
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
                    .background(SuccessGreen.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = SuccessGreen
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Memory Cleaner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
                
                Text(
                    text = "Free up RAM and boost performance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayDark
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        when (cleaningState) {
            CleaningState.IDLE -> {
                IdleMemoryInterface(
                    memoryUsage = memoryUsage,
                    runningApps = runningApps,
                    selectedApps = selectedApps,
                    autoCleanEnabled = autoCleanEnabled,
                    aggressiveMode = aggressiveMode,
                    onAppToggle = { packageName ->
                        selectedApps = if (selectedApps.contains(packageName)) {
                            selectedApps - packageName
                        } else {
                            selectedApps + packageName
                        }
                    },
                    onAutoCleanToggle = { autoCleanEnabled = it },
                    onAggressiveModeToggle = { aggressiveMode = it },
                    onStartCleaning = { startMemoryCleaning() }
                )
            }
            
            CleaningState.ANALYZING -> {
                AnalyzingInterface()
            }
            
            CleaningState.CLEANING -> {
                CleaningInterface()
            }
            
            CleaningState.COMPLETED -> {
                CleaningCompletedInterface(
                    cleanedMemory = cleanedMemory,
                    memoryUsage = memoryUsage,
                    onNewClean = {
                        cleaningState = CleaningState.IDLE
                        cleanedMemory = 0L
                    }
                )
            }
        }
    }
}

private suspend fun loadMemoryData(
    performanceManager: PerformanceManager,
    onData: (MemoryUsage, List<RunningApp>) -> Unit
) {
    val systemInfo = performanceManager.getSystemInfo()
    
    val memoryUsage = MemoryUsage(
        totalRAM = systemInfo.totalRAM,
        usedRAM = systemInfo.totalRAM - systemInfo.availableRAM,
        availableRAM = systemInfo.availableRAM,
        bufferCache = systemInfo.totalRAM * 0.15.toLong(),
        systemUsage = systemInfo.totalRAM * 0.25.toLong(),
        appsUsage = systemInfo.totalRAM * 0.45.toLong()
    )
    
    val apps = listOf(
        RunningApp("Chrome", "com.android.chrome", 245 * 1024 * 1024, 15.4f, "High", false, true),
        RunningApp("WhatsApp", "com.whatsapp", 180 * 1024 * 1024, 8.2f, "Normal", false, true),
        RunningApp("Instagram", "com.instagram.android", 165 * 1024 * 1024, 12.1f, "Normal", false, true),
        RunningApp("Spotify", "com.spotify.music", 140 * 1024 * 1024, 5.3f, "Low", false, true),
        RunningApp("System UI", "com.android.systemui", 95 * 1024 * 1024, 3.2f, "Critical", true, false),
        RunningApp("Settings", "com.android.settings", 45 * 1024 * 1024, 1.1f, "Normal", true, false),
        RunningApp("YouTube", "com.google.android.youtube", 220 * 1024 * 1024, 18.7f, "High", false, true),
        RunningApp("Maps", "com.google.android.apps.maps", 130 * 1024 * 1024, 6.8f, "Normal", false, true)
    )
    
    onData(memoryUsage, apps)
}

@Composable
private fun IdleMemoryInterface(
    memoryUsage: MemoryUsage?,
    runningApps: List<RunningApp>,
    selectedApps: Set<String>,
    autoCleanEnabled: Boolean,
    aggressiveMode: Boolean,
    onAppToggle: (String) -> Unit,
    onAutoCleanToggle: (Boolean) -> Unit,
    onAggressiveModeToggle: (Boolean) -> Unit,
    onStartCleaning: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Memory Overview Card
            memoryUsage?.let { usage ->
                MemoryOverviewCard(usage = usage)
            }
        }
        
        item {
            // Clean Settings Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Cleaning Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    CleaningOption(
                        title = "Auto-Clean on Low Memory",
                        description = "Automatically clean when memory is low",
                        enabled = autoCleanEnabled,
                        onToggle = onAutoCleanToggle,
                        icon = Icons.Default.AutoMode
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CleaningOption(
                        title = "Aggressive Mode",
                        description = "Force close more apps for maximum cleanup",
                        enabled = aggressiveMode,
                        onToggle = onAggressiveModeToggle,
                        icon = Icons.Default.Bolt
                    )
                }
            }
        }
        
        item {
            // Start Cleaning Button
            Button(
                onClick = onStartCleaning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = "Clean Memory",
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Clean Memory Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        item {
            Text(
                text = "Running Apps (${runningApps.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
        }
        
        items(runningApps) { app ->
            RunningAppCard(
                app = app,
                isSelected = selectedApps.contains(app.packageName),
                onToggle = { onAppToggle(app.packageName) }
            )
        }
    }
}

@Composable
private fun MemoryOverviewCard(usage: MemoryUsage) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Memory Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GrayText,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Memory usage visualization
            val usagePercentage = (usage.usedRAM.toFloat() / usage.totalRAM.toFloat())
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GrayLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(usagePercentage)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SuccessGreen, WarningOrange)
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = formatMemorySize(usage.usedRAM),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GrayText
                    )
                    Text(
                        text = "Used",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayDark
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMemorySize(usage.availableRAM),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Memory breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MemoryBreakdownItem(
                    label = "Apps",
                    size = usage.appsUsage,
                    color = LightBlue
                )
                
                MemoryBreakdownItem(
                    label = "System",
                    size = usage.systemUsage,
                    color = Cyan
                )
                
                MemoryBreakdownItem(
                    label = "Cache",
                    size = usage.bufferCache,
                    color = WarningOrange
                )
            }
        }
    }
}

@Composable
private fun MemoryBreakdownItem(
    label: String,
    size: Long,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GrayDark
        )
        
        Text(
            text = formatMemorySize(size),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun CleaningOption(
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
            tint = if (enabled) SuccessGreen else GrayDark,
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
                checkedThumbColor = SuccessGreen,
                checkedTrackColor = SuccessGreen.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun RunningAppCard(
    app: RunningApp,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (app.canKill) Modifier.clickable { onToggle() } else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (app.isSystemApp) Cyan.copy(alpha = 0.1f) 
                        else LightBlue.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (app.isSystemApp) Icons.Default.Settings else Icons.Default.Apps,
                    contentDescription = app.name,
                    tint = if (app.isSystemApp) Cyan else LightBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = GrayText
                )
                
                Text(
                    text = "${formatMemorySize(app.memoryUsage)} • ${app.cpuUsage}% CPU",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayDark
                )
                
                Text(
                    text = "${app.priority} Priority",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (app.priority) {
                        "Critical" -> ErrorRed
                        "High" -> WarningOrange
                        "Normal" -> LightBlue
                        else -> SuccessGreen
                    }
                )
            }
            
            if (app.canKill) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SuccessGreen,
                        uncheckedColor = GrayDark
                    )
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Cyan.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "System",
                        style = MaterialTheme.typography.labelSmall,
                        color = Cyan,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyzingInterface() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = SuccessGreen,
                strokeWidth = 6.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Analyzing Memory Usage",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Text(
                text = "Identifying apps and processes to clean...",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CleaningInterface() {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = SuccessGreen,
                    strokeWidth = 6.dp
                )
                
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = "Cleaning",
                    tint = SuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Cleaning Memory",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
            
            Text(
                text = "Freeing up RAM and closing background apps...",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CleaningCompletedInterface(
    cleanedMemory: Long,
    memoryUsage: MemoryUsage?,
    onNewClean: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SuccessGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Memory Cleaned Successfully!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Freed ${formatMemorySize(cleanedMemory)} of RAM",
                        style = MaterialTheme.typography.titleMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Your device performance has been improved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        item {
            memoryUsage?.let { usage ->
                MemoryOverviewCard(usage = usage)
            }
        }
        
        item {
            Button(
                onClick = onNewClean,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Clean Again",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clean Again")
            }
        }
    }
}

private fun formatMemorySize(bytes: Long): String {
    val mb = bytes / (1024 * 1024)
    val gb = bytes / (1024 * 1024 * 1024)
    
    return if (gb > 0) {
        String.format("%.1f GB", gb.toFloat())
    } else {
        "${mb} MB"
    }
}