package com.guardix.mobile.data.realtime

data class SystemMetrics(
    val cpu: CpuMetrics,
    val memory: MemoryMetrics,
    val disk: DiskMetrics,
    val network: NetworkMetrics,
    val timestamp: String
)

data class CpuMetrics(
    val usagePercent: Float,
    val cores: Int = 4
)

data class MemoryMetrics(
    val percent: Float,
    val used: Long,
    val total: Long
)

data class DiskMetrics(
    val percent: Float,
    val used: Long,
    val total: Long
)

data class NetworkMetrics(
    val bytesSent: Long,
    val bytesReceived: Long
)

data class SecurityEvent(
    val id: String,
    val type: String,
    val severity: String,
    val source: String,
    val timestamp: String
)

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val cpuPercent: Float,
    val memoryPercent: Float
)

// WebSocket message wrapper
data class RealtimeMessage(
    val type: String,
    val data: Any
)

// Additional data models for WebSocket
data class NetworkStatus(
    val connected: Boolean,
    val latency: Long,
    val bandwidth: NetworkBandwidth,
    val timestamp: String
)

data class NetworkBandwidth(
    val download: Long,
    val upload: Long
)

data class SecurityEvents(
    val events: List<SecurityEvent>
)

data class ProcessList(
    val processes: List<ProcessInfo>
)

// Extension functions to convert to UI models
fun SecurityEvent.toUI() = com.guardix.mobile.ui.realtime.SecurityEventUI(
    id = id,
    type = type,
    severity = severity,
    source = source,
    timestamp = timestamp
)

fun ProcessInfo.toUI() = com.guardix.mobile.ui.realtime.ProcessInfoUI(
    pid = pid,
    name = name,
    cpuPercent = cpuPercent,
    memoryPercent = memoryPercent
)