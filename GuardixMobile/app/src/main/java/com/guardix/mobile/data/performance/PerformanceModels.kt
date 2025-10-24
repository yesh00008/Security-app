package com.guardix.mobile.data.performance

import android.content.Context

data class PerformanceMetrics(
    val cpuInfo: CpuInfo,
    val memoryUsage: MemoryUsage,
    val storageInfo: StorageInfo,
    val batteryInfo: BatteryInfo,
    val timestamp: Long = System.currentTimeMillis()
)

data class CpuInfo(
    val usage: Float,
    val cores: Int
)

data class MemoryUsage(
    val usedRAM: Long,
    val totalRAM: Long,
    val usagePercentage: Float
)

data class StorageInfo(
    val usedStorage: Long,
    val totalStorage: Long,
    val availableStorage: Long,
    val usagePercentage: Float
)

data class BatteryInfo(
    val level: Int,
    val health: String,
    val isCharging: Boolean
)

class PerformanceMonitor(private val context: Context) {
    
    fun collectMetrics(): PerformanceMetrics {
        return PerformanceMetrics(
            cpuInfo = CpuInfo(
                usage = 25.0f, // Mock data
                cores = Runtime.getRuntime().availableProcessors()
            ),
            memoryUsage = MemoryUsage(
                usedRAM = 2048L * 1024 * 1024, // 2GB
                totalRAM = 8192L * 1024 * 1024, // 8GB
                usagePercentage = 25.0f
            ),
            storageInfo = StorageInfo(
                usedStorage = 32L * 1024 * 1024 * 1024, // 32GB
                totalStorage = 128L * 1024 * 1024 * 1024, // 128GB
                availableStorage = 96L * 1024 * 1024 * 1024, // 96GB
                usagePercentage = 25.0f
            ),
            batteryInfo = BatteryInfo(
                level = 85,
                health = "Good",
                isCharging = false
            )
        )
    }
    
    fun calculatePerformanceScore(metrics: PerformanceMetrics): Int {
        val cpuScore = (100 - metrics.cpuInfo.usage).coerceAtLeast(0f)
        val memoryScore = (100 - metrics.memoryUsage.usagePercentage).coerceAtLeast(0f)
        val storageScore = (100 - metrics.storageInfo.usagePercentage).coerceAtLeast(0f)
        val batteryScore = metrics.batteryInfo.level.toFloat()
        
        return ((cpuScore + memoryScore + storageScore + batteryScore) / 4).toInt()
    }
    
    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size > 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
}