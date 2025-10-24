package com.guardix.mobile.data.remote.dto

import com.squareup.moshi.Json

data class TokenRequestDto(
    @Json(name = "user_id") val userId: String
)

data class TokenResponseDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String
)

data class ApkFeatureDto(
    @Json(name = "permissions") val permissions: List<String> = emptyList(),
    @Json(name = "api_calls") val apiCalls: List<String> = emptyList(),
    @Json(name = "behaviors") val behaviors: List<String> = emptyList(),
    @Json(name = "metadata") val metadata: Map<String, String> = emptyMap()
)

data class ApkScanRequestDto(
    @Json(name = "package_name") val packageName: String?,
    @Json(name = "features") val features: ApkFeatureDto
)

data class ScanClassificationDto(
    @Json(name = "label") val label: String,
    @Json(name = "probability") val probability: Double
)

data class ApkScanResponseDto(
    @Json(name = "scan_id") val scanId: String,
    @Json(name = "classification") val classification: ScanClassificationDto,
    @Json(name = "timestamp") val timestamp: String
)

data class PhishingScanRequestDto(
    @Json(name = "url") val url: String? = null,
    @Json(name = "text") val text: String? = null
)

data class PhishingScanResponseDto(
    @Json(name = "scan_id") val scanId: String,
    @Json(name = "probability") val probability: Double,
    @Json(name = "is_phishing") val isPhishing: Boolean,
    @Json(name = "timestamp") val timestamp: String
)

data class BiometricSampleDto(
    @Json(name = "keystroke_timings") val keystrokeTimings: List<Double>,
    @Json(name = "touch_pressure") val touchPressure: List<Double>,
    @Json(name = "touch_intervals") val touchIntervals: List<Double>
)

data class BiometricAuthRequestDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "sample") val sample: BiometricSampleDto
)

data class BiometricAuthResponseDto(
    @Json(name = "match") val match: Boolean,
    @Json(name = "probability") val probability: Double,
    @Json(name = "threshold") val threshold: Double,
    @Json(name = "timestamp") val timestamp: String
)

data class TrafficRecordDto(
    @Json(name = "bytes_in") val bytesIn: Long,
    @Json(name = "bytes_out") val bytesOut: Long,
    @Json(name = "connections") val connections: Int,
    @Json(name = "failed_auth") val failedAuth: Int
)

data class IDSRequestDto(
    @Json(name = "traffic") val traffic: List<TrafficRecordDto>,
    @Json(name = "logs") val logs: List<Map<String, String>> = emptyList()
)

data class IDSAnomalyDto(
    @Json(name = "index") val index: Int,
    @Json(name = "score") val score: Double,
    @Json(name = "record") val record: TrafficRecordDto?
)

data class IDSResponseDto(
    @Json(name = "anomalies") val anomalies: List<IDSAnomalyDto>,
    @Json(name = "alert") val alert: Boolean,
    @Json(name = "score") val score: Double,
    @Json(name = "timestamp") val timestamp: String
)

data class ModelInfoDto(
    @Json(name = "name") val name: String,
    @Json(name = "algorithm") val algorithm: String,
    @Json(name = "profile") val profile: String,
    @Json(name = "size_kb") val sizeKb: Double? = null
)

data class ModelSummaryDto(
    @Json(name = "active_profile") val activeProfile: String,
    @Json(name = "models") val models: List<ModelInfoDto>
)

data class HealthResponseDto(
    @Json(name = "message") val message: String,
    @Json(name = "version") val version: String,
    @Json(name = "status") val status: String
)

data class PerformanceOptimizeRequestDto(
    @Json(name = "aggressive_mode") val aggressiveMode: Boolean = false,
    @Json(name = "include_cache_clean") val includeCacheClean: Boolean = true,
    @Json(name = "include_storage_clean") val includeStorageClean: Boolean = true
)

data class PerformanceOptimizeResponseDto(
    @Json(name = "success") val success: Boolean,
    @Json(name = "memory_freed") val memoryFreed: Double,
    @Json(name = "storage_freed") val storageFreed: Double,
    @Json(name = "apps_optimized") val appsOptimized: Int,
    @Json(name = "battery_life_improvement") val batteryLifeImprovement: Int,
    @Json(name = "optimization_time") val optimizationTime: String,
    @Json(name = "recommendations") val recommendations: List<String>
)

data class RunningAppDto(
    @Json(name = "package") val packageName: String,
    @Json(name = "name") val name: String,
    @Json(name = "memory") val memory: Double,
    @Json(name = "importance") val importance: String
)

data class MemoryStatusResponseDto(
    @Json(name = "total_ram") val totalRam: Double,
    @Json(name = "available_ram") val availableRam: Double,
    @Json(name = "used_ram") val usedRam: Double,
    @Json(name = "usage_percent") val usagePercent: Double,
    @Json(name = "running_apps") val runningApps: List<RunningAppDto>,
    @Json(name = "optimization_tips") val optimizationTips: List<String>
)

data class ThermalStatusResponseDto(
    @Json(name = "temperature") val temperature: Double,
    @Json(name = "thermal_state") val thermalState: String,
    @Json(name = "cooling_recommendations") val coolingRecommendations: List<String>
)

data class NetworkAppUsageDto(
    @Json(name = "name") val name: String,
    @Json(name = "package") val packageName: String,
    @Json(name = "download") val download: Double,
    @Json(name = "upload") val upload: Double
)

data class NetworkInfoDto(
    @Json(name = "type") val type: String,
    @Json(name = "ssid") val ssid: String? = null,
    @Json(name = "ip_address") val ipAddress: String? = null,
    @Json(name = "signal_strength") val signalStrength: String? = null
)

data class NetworkUsageStatsDto(
    @Json(name = "download_today") val downloadToday: Double,
    @Json(name = "upload_today") val uploadToday: Double,
    @Json(name = "download_speed") val downloadSpeed: Double,
    @Json(name = "upload_speed") val uploadSpeed: Double
)

data class NetworkUsageResponseDto(
    @Json(name = "current_network") val currentNetwork: NetworkInfoDto,
    @Json(name = "usage_stats") val usageStats: NetworkUsageStatsDto,
    @Json(name = "top_apps") val topApps: List<NetworkAppUsageDto>,
    @Json(name = "connection_quality") val connectionQuality: String
)

data class StorageOverviewResponseDto(
    @Json(name = "total_storage") val totalStorage: Double,
    @Json(name = "used_storage") val usedStorage: Double,
    @Json(name = "available_storage") val availableStorage: Double,
    @Json(name = "usage_percentage") val usagePercentage: Double
)

data class AnomalyRequestDto(
    @Json(name = "metrics") val metrics: List<Double>
)

data class AnomalyResponseDto(
    @Json(name = "label") val label: String,
    @Json(name = "score") val score: Double,
    @Json(name = "probability") val probability: Double
)

data class SecurityOverviewResponseDto(
    @Json(name = "security_score") val securityScore: Int,
    @Json(name = "threat_summary") val threatSummary: Map<String, Int>,
    @Json(name = "recent_events") val recentEvents: List<Map<String, Any>>,
    @Json(name = "recommendations") val recommendations: List<String>,
    @Json(name = "protection_modules") val modules: Map<String, Any>
)
