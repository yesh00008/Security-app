package com.guardix.mobile.data.managers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class PerformanceToolType {
    ONE_TAP_OPTIMIZATION,
    PHONE_ACCELERATION,
    JUNK_CLEANER,
    APP_STARTUP_MANAGER,
    POWER_CONSUMPTION_MANAGER,
    THERMAL_COOLING,
    BATTERY_MANAGEMENT,
    GAME_BOOST
}

data class PerformanceOptimizationResult(
    val memoryFreed: Long = 0L,
    val storageFreed: Long = 0L,
    val junkFilesRemoved: Int = 0,
    val appsOptimized: Int = 0,
    val batteryImprovement: Float = 0f,
    val thermalImprovement: Float = 0f,
    val executionTime: Long = 0L,
    val success: Boolean = true,
    val details: List<String> = emptyList()
)

data class MemoryInfo(
    val totalMemory: Long = 8589934592L, // 8GB
    val availableMemory: Long = 3435973836L, // ~3.2GB
    val usedMemory: Long = 5153960756L, // ~4.8GB
    val memoryUsagePercent: Float = 60f,
    val runningApps: Int = 24,
    val backgroundApps: Int = 18,
    val isMemoryPressureHigh: Boolean = false
)

data class ThermalInfo(
    val currentTemperature: Float = 35.2f,
    val maxTemperature: Float = 85.0f,
    val thermalState: ThermalState = ThermalState.NORMAL,
    val temperatureHistory: List<Float> = listOf(33.1f, 34.5f, 35.2f),
    val lastCoolingTime: String = "Never",
    val coolingRecommended: Boolean = false
)

enum class ThermalState {
    NORMAL, WARNING, CRITICAL
}

data class BatteryOptimizationInfo(
    val batteryLevel: Int = 75,
    val batteryHealth: Int = 95,
    val estimatedTimeRemaining: String = "8h 32m",
    val isCharging: Boolean = false,
    val chargingSpeed: String = "Not charging",
    val powerSavingMode: Boolean = false,
    val batteryUsageToday: List<BatteryUsageApp> = emptyList(),
    val optimizationSuggestions: List<String> = listOf(
        "Enable adaptive battery",
        "Optimize background apps",
        "Reduce screen brightness"
    )
)

data class BatteryUsageApp(
    val appName: String,
    val packageName: String,
    val usagePercent: Float,
    val backgroundUsage: Float,
    val icon: String? = null
)

data class PerformanceActivity(
    val description: String,
    val timestamp: String,
    val icon: ImageVector,
    val color: Color
)

data class AppStartupInfo(
    val appName: String,
    val packageName: String,
    val startupImpact: String, // LOW, MEDIUM, HIGH
    val startupTime: Long, // milliseconds
    val autoStartEnabled: Boolean,
    val lastStartup: String,
    val icon: String? = null
)

data class PowerHungryApp(
    val appName: String,
    val packageName: String,
    val powerUsagePercent: Float,
    val backgroundTime: Long,
    val foregroundTime: Long,
    val optimizable: Boolean,
    val icon: String? = null
)

data class JunkFile(
    val path: String,
    val name: String,
    val size: Long,
    val type: JunkFileType,
    val lastModified: Long,
    val canDelete: Boolean = true
)

enum class JunkFileType {
    CACHE, TEMP, LOG, APK, THUMBNAIL, EMPTY_FOLDER, DUPLICATE, OTHER
}

data class StorageCleanupInfo(
    val totalSize: Long,
    val availableSize: Long,
    val junkFiles: List<JunkFile>,
    val duplicateFiles: List<JunkFile>,
    val largeFiles: List<JunkFile>,
    val emptyFolders: List<String>,
    val cleanupRecommendations: List<String>
)

// Extension functions for better formatting
fun MemoryInfo.getFormattedTotalMemory(): String {
    return formatBytes(totalMemory)
}

fun MemoryInfo.getFormattedAvailableMemory(): String {
    return formatBytes(availableMemory)
}

fun MemoryInfo.getFormattedUsedMemory(): String {
    return formatBytes(usedMemory)
}

fun ThermalInfo.getTemperatureColor(): Color {
    return when (thermalState) {
        ThermalState.NORMAL -> Color(0xFF4CAF50) // Green
        ThermalState.WARNING -> Color(0xFFFF9800) // Orange
        ThermalState.CRITICAL -> Color(0xFFF44336) // Red
        else -> Color(0xFF757575) // Gray for unknown states
    }
}

fun BatteryOptimizationInfo.getBatteryColor(): Color {
    return when {
        batteryLevel > 60 -> Color(0xFF4CAF50) // Green
        batteryLevel > 30 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun formatBytes(bytes: Long): String {
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