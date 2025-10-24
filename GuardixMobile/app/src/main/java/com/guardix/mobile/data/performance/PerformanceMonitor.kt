package com.guardix.mobile.data.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton

data class PerformanceMetrics(
    val memoryUsage: MemoryInfo,
    val storageInfo: StorageInfo,
    val cpuInfo: CpuInfo,
    val batteryInfo: BatteryInfo,
    val networkInfo: NetworkInfo,
    val timestamp: Long = System.currentTimeMillis()
)

data class MemoryInfo(
    val totalRAM: Long,
    val availableRAM: Long,
    val usedRAM: Long,
    val usagePercentage: Float,
    val heapSize: Long,
    val heapAllocated: Long,
    val heapFree: Long
)

data class StorageInfo(
    val totalStorage: Long,
    val availableStorage: Long,
    val usedStorage: Long,
    val usagePercentage: Float,
    val cacheSize: Long
)

data class CpuInfo(
    val cores: Int,
    val architecture: String,
    val frequency: String,
    val usage: Float
)

data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val temperature: Float,
    val voltage: Int,
    val health: String
)

data class NetworkInfo(
    val connectionType: String,
    val isConnected: Boolean,
    val signalStrength: Int?,
    val dataUsage: Long
)

@Singleton
class PerformanceMonitor @Inject constructor(
    private val context: Context
) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val decimalFormat = DecimalFormat("#.##")
    
    /**
     * Collect comprehensive performance metrics
     */
    suspend fun collectMetrics(): PerformanceMetrics = withContext(Dispatchers.IO) {
        PerformanceMetrics(
            memoryUsage = getMemoryInfo(),
            storageInfo = getStorageInfo(),
            cpuInfo = getCpuInfo(),
            batteryInfo = getBatteryInfo(),
            networkInfo = getNetworkInfo()
        )
    }
    
    /**
     * Get detailed memory information
     */
    private fun getMemoryInfo(): MemoryInfo {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val totalRAM = memInfo.totalMem
        val availableRAM = memInfo.availMem
        val usedRAM = totalRAM - availableRAM
        val usagePercentage = (usedRAM.toFloat() / totalRAM.toFloat()) * 100
        
        // Heap information
        val heapSize = Debug.getNativeHeapSize()
        val heapAllocated = Debug.getNativeHeapAllocatedSize()
        val heapFree = Debug.getNativeHeapFreeSize()
        
        return MemoryInfo(
            totalRAM = totalRAM,
            availableRAM = availableRAM,
            usedRAM = usedRAM,
            usagePercentage = usagePercentage,
            heapSize = heapSize,
            heapAllocated = heapAllocated,
            heapFree = heapFree
        )
    }
    
    /**
     * Get storage information
     */
    private fun getStorageInfo(): StorageInfo {
        val internalPath = context.filesDir
        val stat = StatFs(internalPath.path)
        
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalStorage = totalBlocks * blockSize
        val availableStorage = availableBlocks * blockSize
        val usedStorage = totalStorage - availableStorage
        val usagePercentage = (usedStorage.toFloat() / totalStorage.toFloat()) * 100
        
        // Calculate cache size
        val cacheSize = calculateCacheSize()
        
        return StorageInfo(
            totalStorage = totalStorage,
            availableStorage = availableStorage,
            usedStorage = usedStorage,
            usagePercentage = usagePercentage,
            cacheSize = cacheSize
        )
    }
    
    /**
     * Get CPU information
     */
    private fun getCpuInfo(): CpuInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        
        // Read CPU info from /proc/cpuinfo
        val cpuInfoFile = File("/proc/cpuinfo")
        var architecture = "Unknown"
        var frequency = "Unknown"
        
        try {
            if (cpuInfoFile.exists()) {
                val lines = cpuInfoFile.readLines()
                for (line in lines) {
                    when {
                        line.startsWith("processor") -> break // We only need the first processor info
                        line.startsWith("Processor") -> {
                            architecture = line.substringAfter(":").trim()
                        }
                        line.startsWith("BogoMIPS") -> {
                            frequency = line.substringAfter(":").trim() + " MIPS"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback values
        }
        
        // Estimate CPU usage (simplified)
        val usage = estimateCpuUsage()
        
        return CpuInfo(
            cores = cores,
            architecture = architecture,
            frequency = frequency,
            usage = usage
        )
    }
    
    /**
     * Get battery information
     */
    private fun getBatteryInfo(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        
        val level = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val temperature = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 10.0f
        val voltage = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        
        // Check charging status via broadcast (simplified)
        val isCharging = batteryManager.isCharging
        
        val health = when (batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_STATUS)) {
            android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            android.os.BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
        
        return BatteryInfo(
            level = level,
            isCharging = isCharging,
            temperature = temperature,
            voltage = voltage,
            health = health
        )
    }
    
    /**
     * Get network information
     */
    private fun getNetworkInfo(): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        
        val connectionType = when {
            networkInfo?.type == android.net.ConnectivityManager.TYPE_WIFI -> "WiFi"
            networkInfo?.type == android.net.ConnectivityManager.TYPE_MOBILE -> "Mobile Data"
            networkInfo?.type == android.net.ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
            else -> "None"
        }
        
        val isConnected = networkInfo?.isConnected ?: false
        
        // Signal strength (simplified - would need more complex implementation for real values)
        val signalStrength = if (connectionType == "WiFi") {
            (50..100).random() // Placeholder
        } else if (connectionType == "Mobile Data") {
            (30..80).random() // Placeholder
        } else null
        
        // Data usage (placeholder - would need TrafficStats implementation)
        val dataUsage = android.net.TrafficStats.getTotalRxBytes() + android.net.TrafficStats.getTotalTxBytes()
        
        return NetworkInfo(
            connectionType = connectionType,
            isConnected = isConnected,
            signalStrength = signalStrength,
            dataUsage = dataUsage
        )
    }
    
    /**
     * Calculate cache size
     */
    private fun calculateCacheSize(): Long {
        return try {
            val cacheDir = context.cacheDir
            calculateDirectorySize(cacheDir)
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Calculate directory size recursively
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.exists()) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    calculateDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }
    
    /**
     * Estimate CPU usage (simplified implementation)
     */
    private fun estimateCpuUsage(): Float {
        return try {
            val statFile = File("/proc/stat")
            if (statFile.exists()) {
                val lines = statFile.readLines()
                if (lines.isNotEmpty()) {
                    val cpuLine = lines[0]
                    val values = cpuLine.split("\\s+".toRegex())
                    if (values.size >= 5) {
                        val idle = values[4].toLongOrNull() ?: 0L
                        val total = values.drop(1).take(7).sumOf { it.toLongOrNull() ?: 0L }
                        val usage = if (total > 0) ((total - idle).toFloat() / total.toFloat()) * 100 else 0f
                        return usage.coerceIn(0f, 100f)
                    }
                }
            }
            // Fallback random value for demonstration
            (20..60).random().toFloat()
        } catch (e: Exception) {
            (20..60).random().toFloat()
        }
    }
    
    /**
     * Format bytes to human readable format
     */
    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "${decimalFormat.format(size)} ${units[unitIndex]}"
    }
    
    /**
     * Get performance score based on metrics
     */
    fun calculatePerformanceScore(metrics: PerformanceMetrics): Int {
        var score = 100
        
        // Memory usage impact (30% weight)
        if (metrics.memoryUsage.usagePercentage > 80) score -= 20
        else if (metrics.memoryUsage.usagePercentage > 60) score -= 10
        
        // Storage usage impact (20% weight)
        if (metrics.storageInfo.usagePercentage > 90) score -= 15
        else if (metrics.storageInfo.usagePercentage > 75) score -= 8
        
        // CPU usage impact (25% weight)
        if (metrics.cpuInfo.usage > 80) score -= 20
        else if (metrics.cpuInfo.usage > 60) score -= 10
        
        // Battery impact (15% weight)
        if (metrics.batteryInfo.level < 20) score -= 10
        if (metrics.batteryInfo.temperature > 40) score -= 5
        
        // Network impact (10% weight)
        if (!metrics.networkInfo.isConnected) score -= 10
        else if (metrics.networkInfo.signalStrength != null && metrics.networkInfo.signalStrength!! < 30) score -= 5
        
        return score.coerceIn(0, 100)
    }
}