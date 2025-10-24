package com.guardix.mobile.data.realtime

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Base class for all real-time data messages
 */
@JsonClass(generateAdapter = true)
data class RealtimeMessage(
    @Json(name = "type") val type: String,
    @Json(name = "data") val data: Any,
    @Json(name = "timestamp") val timestamp: String
)

/**
 * System metrics data model
 */
@JsonClass(generateAdapter = true)
data class SystemMetrics(
    @Json(name = "cpu") val cpu: CpuMetrics,
    @Json(name = "memory") val memory: MemoryMetrics,
    @Json(name = "disk") val disk: DiskMetrics,
    @Json(name = "network") val network: NetworkMetrics,
    @Json(name = "timestamp") val timestamp: String
)

@JsonClass(generateAdapter = true)
data class CpuMetrics(
    @Json(name = "usage_percent") val usagePercent: Float,
    @Json(name = "cores") val cores: Int,
    @Json(name = "physical_cores") val physicalCores: Int
)

@JsonClass(generateAdapter = true)
data class MemoryMetrics(
    @Json(name = "total") val total: Long,
    @Json(name = "available") val available: Long,
    @Json(name = "used") val used: Long,
    @Json(name = "percent") val percent: Float
)

@JsonClass(generateAdapter = true)
data class DiskMetrics(
    @Json(name = "total") val total: Long,
    @Json(name = "used") val used: Long,
    @Json(name = "free") val free: Long,
    @Json(name = "percent") val percent: Float
)

@JsonClass(generateAdapter = true)
data class NetworkMetrics(
    @Json(name = "bytes_sent") val bytesSent: Long,
    @Json(name = "bytes_recv") val bytesReceived: Long,
    @Json(name = "packets_sent") val packetsSent: Long,
    @Json(name = "packets_recv") val packetsReceived: Long
)

/**
 * Network status data model
 */
@JsonClass(generateAdapter = true)
data class NetworkStatus(
    @Json(name = "hostname") val hostname: String,
    @Json(name = "interfaces") val interfaces: List<NetworkInterface>,
    @Json(name = "statistics") val statistics: NetworkStatistics,
    @Json(name = "current_speeds") val currentSpeeds: NetworkSpeeds,
    @Json(name = "timestamp") val timestamp: String
)

@JsonClass(generateAdapter = true)
data class NetworkInterface(
    @Json(name = "name") val name: String,
    @Json(name = "addresses") val addresses: List<NetworkAddress>
)

@JsonClass(generateAdapter = true)
data class NetworkAddress(
    @Json(name = "ip") val ip: String,
    @Json(name = "netmask") val netmask: String
)

@JsonClass(generateAdapter = true)
data class NetworkStatistics(
    @Json(name = "bytes_sent") val bytesSent: Long,
    @Json(name = "bytes_recv") val bytesReceived: Long,
    @Json(name = "packets_sent") val packetsSent: Long,
    @Json(name = "packets_recv") val packetsReceived: Long,
    @Json(name = "errin") val errIn: Long,
    @Json(name = "errout") val errOut: Long,
    @Json(name = "dropin") val dropIn: Long,
    @Json(name = "dropout") val dropOut: Long
)

@JsonClass(generateAdapter = true)
data class NetworkSpeeds(
    @Json(name = "download_mbps") val downloadMbps: Float,
    @Json(name = "upload_mbps") val uploadMbps: Float
)

/**
 * Security events data model
 */
@JsonClass(generateAdapter = true)
data class SecurityEvents(
    @Json(name = "events") val events: List<SecurityEvent>,
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "timestamp") val timestamp: String
)

@JsonClass(generateAdapter = true)
data class SecurityEvent(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "source") val source: String,
    @Json(name = "timestamp") val timestamp: String
)

/**
 * Process list data model
 */
@JsonClass(generateAdapter = true)
data class ProcessList(
    @Json(name = "processes") val processes: List<ProcessInfo>,
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "timestamp") val timestamp: String
)

@JsonClass(generateAdapter = true)
data class ProcessInfo(
    @Json(name = "pid") val pid: Int,
    @Json(name = "name") val name: String,
    @Json(name = "username") val username: String,
    @Json(name = "cpu_percent") val cpuPercent: Float,
    @Json(name = "memory_percent") val memoryPercent: Float
)