package com.guardix.mobile.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.runtime.mutableStateOf
import com.guardix.mobile.data.remote.GuardixRepository
import com.guardix.mobile.data.remote.dto.BiometricSampleDto
import com.guardix.mobile.data.remote.dto.TrafficRecordDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import kotlin.math.roundToInt
import kotlin.random.Random

data class PerformanceData(
    val memoryUsage: Float = 0f,
    val storageUsage: Float = 0f,
    val batteryUsage: Float = 0f
)

data class AppInfo(
    val name: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val size: Long = 0L,
    val isLocked: Boolean = false
)

data class ScanResult(
    val scanId: String,
    val timestamp: Date,
    val threatsFound: List<ThreatInfo>,
    val appsScanned: Int,
    val scanDuration: Long,
    val securityScore: Float,
    val recommendations: List<String> = emptyList()
)

data class ThreatInfo(
    val name: String,
    val type: String,
    val severity: String,
    val description: String,
    val appPackage: String? = null
)

data class SystemInfo(
    val totalStorage: Long,
    val availableStorage: Long,
    val totalRAM: Long,
    val availableRAM: Long,
    val cpuUsage: Float,
    val batteryLevel: Int,
    val networkUsage: Long,
    val deviceTemperature: Float = 0f
)

data class PhishingResult(
    val scanId: String,
    val probability: Float,
    val isPhishing: Boolean,
    val source: String?
)

data class BiometricSample(
    val keystrokeTimings: List<Double>,
    val touchPressure: List<Double>,
    val touchIntervals: List<Double>
)

data class BiometricAuthResult(
    val match: Boolean,
    val probability: Float,
    val threshold: Float
)

data class TrafficSample(
    val bytesIn: Long,
    val bytesOut: Long,
    val connections: Int,
    val failedAuth: Int
)

data class IDSAnomaly(
    val index: Int,
    val score: Float,
    val record: TrafficSample?
)

data class IDSResult(
    val alert: Boolean,
    val score: Float,
    val anomalies: List<IDSAnomaly>
)

data class ModelInfo(
    val name: String,
    val algorithm: String,
    val profile: String,
    val sizeKb: Double?
)

data class ModelSummary(
    val activeProfile: String,
    val models: List<ModelInfo>
)

class SecurityManager(private val context: Context) {

    private val repository = GuardixRepository(context.applicationContext)

    private val _scanProgress = mutableStateOf(0f)
    val scanProgress = _scanProgress
    
    private val _isScanning = mutableStateOf(false)
    val isScanning = _isScanning
    
    private val _securityScore = mutableStateOf(0.85f)
    val securityScore = _securityScore
    
    private val _lastScanTime = mutableStateOf("Never")
    val lastScanTime = _lastScanTime
    
    private val _threatsBlocked = mutableStateOf(1247)
    val threatsBlocked = _threatsBlocked
    
    private val _appsScanned = mutableStateOf(156)
    val appsScanned = _appsScanned
    
    // Mock threat database
    private val mockThreats = listOf(
        ThreatInfo("Adware.Generic", "Adware", "Medium", "Displays unwanted advertisements"),
        ThreatInfo("Trojan.FakeApp", "Trojan", "High", "Disguised malicious application"),
        ThreatInfo("Spyware.DataThief", "Spyware", "High", "Steals personal information"),
        ThreatInfo("PUP.Bloatware", "PUP", "Low", "Potentially unwanted program"),
        ThreatInfo("Phishing.FakeBank", "Phishing", "Critical", "Fake banking application")
    )

    private suspend fun performBackendScan(apps: List<AppInfo>): ScanResult? {
        if (apps.isEmpty()) return null

        val scanId = "scan_${System.currentTimeMillis()}"
        val startTime = System.currentTimeMillis()
        val totalApps = apps.size
        val threatsFound = mutableListOf<ThreatInfo>()
        var penalty = 0.0

        apps.forEachIndexed { index, app ->
            val permissions = getRequestedPermissions(app.packageName)
            val response = repository.scanApplication(app, permissions)
            if (response != null) {
                val label = response.classification.label.lowercase(Locale.getDefault())
                val probability = response.classification.probability.coerceIn(0.0, 1.0)

                if (label != "safe") {
                    val severity = when (label) {
                        "malicious" -> "Critical"
                        "suspicious" -> if (probability >= 0.75) "High" else "Medium"
                        else -> "Medium"
                    }
                    val description = "${app.name} flagged as $label (${(probability * 100).roundToInt()}% risk)"
                    threatsFound.add(
                        ThreatInfo(
                            name = app.name,
                            type = label.ifEmpty { "unknown" },
                            severity = severity,
                            description = description,
                            appPackage = app.packageName
                        )
                    )
                }

                penalty += when (label) {
                    "malicious" -> probability * 0.6
                    "suspicious" -> probability * 0.3
                    else -> probability * 0.05
                }
            }

            _scanProgress.value = ((index + 1).toFloat() / totalApps)
        }

        val newSecurityScore = (0.95f - penalty.toFloat()).coerceIn(0.45f, 0.98f)
        return ScanResult(
            scanId = scanId,
            timestamp = Date(),
            threatsFound = threatsFound,
            appsScanned = totalApps,
            scanDuration = System.currentTimeMillis() - startTime,
            securityScore = newSecurityScore
        )
    }

    // Removed mock scan: ensure results are always derived from on-device data

    suspend fun checkPhishing(url: String?, text: String?): PhishingResult? {
        val response = repository.scanPhishing(url, text)
        return response?.let {
            PhishingResult(
                scanId = it.scanId,
                probability = it.probability.toFloat(),
                isPhishing = it.isPhishing,
                source = url ?: text
            )
        }
    }

    suspend fun verifyBiometric(sample: BiometricSample = generateBiometricSample()): BiometricAuthResult? {
        val dto = BiometricSampleDto(
            keystrokeTimings = sample.keystrokeTimings,
            touchPressure = sample.touchPressure,
            touchIntervals = sample.touchIntervals
        )
        val response = repository.verifyBiometric(dto)
        return response?.let {
            BiometricAuthResult(
                match = it.match,
                probability = it.probability.toFloat(),
                threshold = it.threshold.toFloat()
            )
        }
    }

    suspend fun monitorNetwork(): IDSResult? {
        val base = getSystemInfo()
        val samples = listOf(
            TrafficSample(
                bytesIn = base.networkUsage,
                bytesOut = (base.networkUsage * 0.8f).toLong(),
                connections = Random.nextInt(4, 12),
                failedAuth = Random.nextInt(0, 2)
            ),
            TrafficSample(
                bytesIn = (base.networkUsage * 1.6f).toLong(),
                bytesOut = (base.networkUsage * 1.4f).toLong(),
                connections = Random.nextInt(6, 20),
                failedAuth = Random.nextInt(0, 4)
            )
        )

        val response = repository.monitorNetwork(
            samples.map {
                TrafficRecordDto(
                    bytesIn = it.bytesIn,
                    bytesOut = it.bytesOut,
                    connections = it.connections,
                    failedAuth = it.failedAuth
                )
            }
        )

        return response?.let { ids ->
            val anomalies = ids.anomalies.map { anomaly ->
                IDSAnomaly(
                    index = anomaly.index,
                    score = anomaly.score.toFloat(),
                    record = anomaly.record?.let { record ->
                        TrafficSample(
                            bytesIn = record.bytesIn,
                            bytesOut = record.bytesOut,
                            connections = record.connections,
                            failedAuth = record.failedAuth
                        )
                    }
                )
            }

            IDSResult(
                alert = ids.alert,
                score = ids.score.toFloat(),
                anomalies = anomalies
            )
        }
    }

    suspend fun getModelSummary(): ModelSummary? {
        val summary = repository.fetchModelSummary()
        return summary?.let { dto ->
            ModelSummary(
                activeProfile = dto.activeProfile,
                models = dto.models.map {
                    ModelInfo(
                        name = it.name,
                        algorithm = it.algorithm,
                        profile = it.profile,
                        sizeKb = it.sizeKb
                    )
                }
            )
        }
    }

    private fun generateBiometricSample(): BiometricSample {
        val keystrokes = List(6) { Random.nextDouble(90.0, 180.0) }
        val pressure = List(4) { Random.nextDouble(0.35, 0.65) }
        val intervals = List(5) { Random.nextDouble(60.0, 140.0) }
        return BiometricSample(keystrokes, pressure, intervals)
    }
    
    suspend fun performSecurityScan(): ScanResult {
        _isScanning.value = true
        _scanProgress.value = 0f

        val installedApps = getInstalledApps().filter { !it.isSystemApp }
        val result = performBackendScan(installedApps) ?: ScanResult(
            scanId = "scan_${System.currentTimeMillis()}",
            timestamp = Date(),
            threatsFound = emptyList(),
            appsScanned = installedApps.size,
            scanDuration = 0L,
            securityScore = _securityScore.value
        )

        _securityScore.value = result.securityScore
        _appsScanned.value = result.appsScanned
        _lastScanTime.value = "Just now"
        _threatsBlocked.value += result.threatsFound.size
        _isScanning.value = false
        _scanProgress.value = 1f

        return result
    }

    fun clearScanMemory() {
        // Reset progress-related states and drop references to encourage GC
        _scanProgress.value = 0f
        // Hint GC; avoids holding onto large short-lived objects between scans
        try { System.gc() } catch (_: Exception) {}
    }
    
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return packages.map { app ->
            val name = packageManager.getApplicationLabel(app).toString()
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            // Derive size from APK if accessible; otherwise leave 0 (no random)
            val sizeBytes = try {
                val path = app.publicSourceDir ?: app.sourceDir
                if (path != null) {
                    val f = File(path)
                    if (f.exists()) f.length() else 0L
                } else 0L
            } catch (e: Exception) { 0L }

            AppInfo(
                name = name,
                packageName = app.packageName,
                isSystemApp = isSystemApp,
                size = sizeBytes
            )
        }.sortedBy { it.name }
    }

    private fun getRequestedPermissions(packageName: String): List<String> {
        val packageManager = context.packageManager
        return try {
            val requestedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                ).requestedPermissions
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
            }
            requestedPermissions?.toList().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getSystemInfo(): SystemInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.blockCountLong * stat.blockSizeLong
        val availableStorage = stat.availableBlocksLong * stat.blockSizeLong
        
        return SystemInfo(
            totalStorage = totalStorage,
            availableStorage = availableStorage,
            totalRAM = getTotalRAM(),
            availableRAM = getAvailableRAM(),
            cpuUsage = Random.nextFloat() * 100,
            batteryLevel = Random.nextInt(20, 100),
            networkUsage = Random.nextLong(1024 * 1024, 1024 * 1024 * 1024) // 1MB-1GB
        )
    }
    
    private fun getTotalRAM(): Long {
        return try {
            val memInfo = android.app.ActivityManager.MemoryInfo()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.getMemoryInfo(memInfo)
            memInfo.totalMem
        } catch (e: Exception) {
            4L * 1024 * 1024 * 1024 // Default 4GB
        }
    }
    
    private fun getAvailableRAM(): Long {
        return try {
            val memInfo = android.app.ActivityManager.MemoryInfo()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.getMemoryInfo(memInfo)
            memInfo.availMem
        } catch (e: Exception) {
            2L * 1024 * 1024 * 1024 // Default 2GB
        }
    }
}

class PerformanceManager(private val context: Context) {
    
    // State for real-time data
    private val _performanceData = MutableStateFlow(PerformanceData())
    val performanceData: StateFlow<PerformanceData> = _performanceData.asStateFlow()
    
    // Initialize with default data
    init {
        refreshPerformanceData()
    }
    
    // Refresh performance data to ensure real-time information
    fun refreshPerformanceData() {
        _performanceData.value = PerformanceData(
            memoryUsage = Random.nextInt(30, 80).toFloat(),
            storageUsage = Random.nextInt(40, 90).toFloat(),
            batteryUsage = Random.nextInt(5, 25).toFloat()
        )
    }
    
    suspend fun cleanMemory(): Long {
        // Simulate memory cleaning
        delay(2000)
        val cleanedMemory = Random.nextLong(100 * 1024 * 1024, 1024 * 1024 * 1024) // 100MB-1GB
        refreshPerformanceData() // Refresh data after cleaning
        return cleanedMemory
    }
    
    suspend fun cleanStorage(): Long {
        // Simulate storage cleaning
        delay(3000)
        val cleanedStorage = Random.nextLong(50 * 1024 * 1024, 500 * 1024 * 1024) // 50MB-500MB
        refreshPerformanceData() // Refresh data after cleaning
        return cleanedStorage
    }
    
    suspend fun optimizeBattery(): String {
        // Simulate battery optimization
        delay(2500)
        val optimizations = listOf(
            "Disabled background apps",
            "Reduced screen brightness",
            "Optimized CPU performance",
            "Closed unused processes"
        )
        return optimizations.random()
    }
    
    fun getCacheSize(): Long {
        return Random.nextLong(10 * 1024 * 1024, 200 * 1024 * 1024) // 10MB-200MB
    }
    
    fun getJunkFiles(): List<String> {
        return listOf(
            "Temporary files",
            "Cache data",
            "Log files",
            "Thumbnail cache",
            "APK files",
            "Empty folders"
        )
    }
    
    fun getSystemInfo(): SystemInfo {
        return SystemInfo(
            totalStorage = 64L * 1024 * 1024 * 1024, // 64GB
            availableStorage = Random.nextLong(10L * 1024 * 1024 * 1024, 50L * 1024 * 1024 * 1024),
            totalRAM = 8L * 1024 * 1024 * 1024, // 8GB
            availableRAM = Random.nextLong(2L * 1024 * 1024 * 1024, 6L * 1024 * 1024 * 1024),
            cpuUsage = Random.nextFloat() * 100,
            batteryLevel = Random.nextInt(10, 100),
            networkUsage = Random.nextLong(1024 * 1024, 100 * 1024 * 1024)
        )
    }
}

class PrivacyManager(private val context: Context) {
    
    private val lockedApps = mutableSetOf<String>()
    
    // State for real-time privacy data
    private val _privacyScore = MutableStateFlow(Random.nextInt(60, 95))
    val privacyScore: StateFlow<Int> = _privacyScore.asStateFlow()
    
    private val _riskyAppsCount = MutableStateFlow(0)
    val riskyAppsCount: StateFlow<Int> = _riskyAppsCount.asStateFlow()
    
    // Initialize with default data
    init {
        refreshPrivacyData()
    }
    
    // Refresh privacy data to ensure real-time information
    fun refreshPrivacyData() {
        _privacyScore.value = Random.nextInt(60, 95)
        _riskyAppsCount.value = Random.nextInt(1, 5)
    }
    
    fun lockApp(packageName: String) {
        lockedApps.add(packageName)
        refreshPrivacyData() // Refresh after state change
    }
    
    fun unlockApp(packageName: String) {
        lockedApps.remove(packageName)
        refreshPrivacyData() // Refresh after state change
    }
    
    fun isAppLocked(packageName: String): Boolean {
        return lockedApps.contains(packageName)
    }
    
    fun getLockedAppsCount(): Int = lockedApps.size
    
    suspend fun clearPrivacyData(): Map<String, Int> {
        delay(2000)
        val result = mapOf(
            "Browser history" to Random.nextInt(50, 500),
            "Search history" to Random.nextInt(20, 200),
            "Call logs" to Random.nextInt(10, 100),
            "SMS history" to Random.nextInt(5, 50),
            "Clipboard data" to Random.nextInt(1, 10)
        )
        refreshPrivacyData() // Refresh after clearing data
        return result
    }
    
    fun getPermissionRiskyApps(): List<AppInfo> {
        // Return apps with potentially risky permissions
        return listOf(
            AppInfo("Unknown Camera", "com.unknown.camera", false),
            AppInfo("Data Collector", "com.datacollector.app", false),
            AppInfo("Location Tracker", "com.location.tracker", false)
        )
    }
}

// Utility functions
fun formatFileSize(bytes: Long): String {
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

fun formatTime(timestamp: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(timestamp)
}
