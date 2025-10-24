package com.guardix.mobile.data.security

import android.content.Context
import android.util.Log
import com.guardix.mobile.data.remote.BackendIntegrationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MonitoringService provides continuous monitoring of system resources,
 * network activity, and application behavior to detect potential security issues.
 */
@Singleton
class MonitoringService @Inject constructor(
    private val context: Context,
    private val backendIntegrationManager: BackendIntegrationManager,
    private val anomalyDetectionService: AnomalyDetectionService
) {
    private val TAG = "MonitoringService"
    
    // Coroutine scope for background operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Monitoring state
    private val _monitoringState = MutableStateFlow<MonitoringState>(MonitoringState.Inactive)
    val monitoringState: StateFlow<MonitoringState> = _monitoringState.asStateFlow()
    
    // Monitoring statistics
    private val _monitoringStats = MutableStateFlow(MonitoringStats())
    val monitoringStats: StateFlow<MonitoringStats> = _monitoringStats.asStateFlow()
    
    // Monitoring alerts
    private val _monitoringAlerts = MutableStateFlow<List<MonitoringAlert>>(emptyList())
    val monitoringAlerts: StateFlow<List<MonitoringAlert>> = _monitoringAlerts.asStateFlow()
    
    /**
     * Start monitoring services
     */
    fun startMonitoring() {
        if (_monitoringState.value is MonitoringState.Active) {
            Log.d(TAG, "Monitoring already active")
            return
        }
        
        _monitoringState.value = MonitoringState.Active
        
        // Connect to WebSocket if not already connected
        backendIntegrationManager.connectWebSocket()
        
        // Start anomaly detection
        anomalyDetectionService.startAnomalyDetection()
        
        // Start monitoring algorithms
        startNetworkMonitoring()
        startSystemResourceMonitoring()
        startApplicationBehaviorMonitoring()
        startPermissionUsageMonitoring()
        
        Log.d(TAG, "Monitoring started")
    }
    
    /**
     * Stop monitoring services
     */
    fun stopMonitoring() {
        _monitoringState.value = MonitoringState.Inactive
        
        // Stop anomaly detection
        anomalyDetectionService.stopAnomalyDetection()
        
        Log.d(TAG, "Monitoring stopped")
    }
    
    /**
     * Start network monitoring algorithm
     */
    private fun startNetworkMonitoring() {
        coroutineScope.launch {
            backendIntegrationManager.getNetworkStatusFlow().collect { status ->
                // Update monitoring stats
                _monitoringStats.value = _monitoringStats.value.copy(
                    networkConnections = status.connections.size,
                    downloadSpeed = status.stats.downloadSpeed,
                    uploadSpeed = status.stats.uploadSpeed,
                    lastNetworkUpdate = System.currentTimeMillis()
                )
                
                // Check for suspicious connections
                status.connections.forEach { connection ->
                    // Check for connections to known malicious domains
                    if (isSuspiciousDomain(connection.destination)) {
                        addAlert(
                            MonitoringAlert(
                                type = AlertType.NETWORK,
                                title = "Suspicious Connection Detected",
                                description = "Connection to suspicious domain: ${connection.destination}",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    
                    // Check for unusual ports
                    if (isUnusualPort(connection.port)) {
                        addAlert(
                            MonitoringAlert(
                                type = AlertType.NETWORK,
                                title = "Unusual Port Activity",
                                description = "Connection using unusual port: ${connection.port}",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Start system resource monitoring algorithm
     */
    private fun startSystemResourceMonitoring() {
        coroutineScope.launch {
            backendIntegrationManager.getSystemMetricsFlow().collect { metrics ->
                // Update monitoring stats
                _monitoringStats.value = _monitoringStats.value.copy(
                    cpuUsage = metrics.cpuUsage,
                    memoryUsage = (metrics.memoryUsed.toFloat() / metrics.memoryTotal) * 100,
                    diskUsage = (metrics.diskUsed.toFloat() / metrics.diskTotal) * 100,
                    lastSystemUpdate = System.currentTimeMillis()
                )
                
                // Check for resource exhaustion
                if (metrics.cpuUsage > 95) {
                    addAlert(
                        MonitoringAlert(
                            type = AlertType.SYSTEM,
                            title = "CPU Resource Exhaustion",
                            description = "CPU usage at critical level: ${metrics.cpuUsage}%",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                
                // Check for memory leaks
                val memoryUsagePercent = (metrics.memoryUsed.toFloat() / metrics.memoryTotal) * 100
                if (memoryUsagePercent > 95) {
                    addAlert(
                        MonitoringAlert(
                            type = AlertType.SYSTEM,
                            title = "Memory Resource Exhaustion",
                            description = "Memory usage at critical level: $memoryUsagePercent%",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
    
    /**
     * Start application behavior monitoring algorithm
     */
    private fun startApplicationBehaviorMonitoring() {
        coroutineScope.launch {
            backendIntegrationManager.getProcessListFlow().collect { processList ->
                // Update monitoring stats
                _monitoringStats.value = _monitoringStats.value.copy(
                    runningProcesses = processList.processes.size,
                    lastProcessUpdate = System.currentTimeMillis()
                )
                
                // Check for suspicious processes
                processList.processes.forEach { process ->
                    // Check for known malicious process names
                    if (isMaliciousProcess(process.name)) {
                        addAlert(
                            MonitoringAlert(
                                type = AlertType.APPLICATION,
                                title = "Malicious Process Detected",
                                description = "Potentially malicious process running: ${process.name}",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    
                    // Check for unusual resource usage patterns
                    if (process.cpuPercent > 80 && process.memoryPercent > 50) {
                        addAlert(
                            MonitoringAlert(
                                type = AlertType.APPLICATION,
                                title = "Unusual Process Behavior",
                                description = "Process ${process.name} using high resources: CPU ${process.cpuPercent}%, Memory ${process.memoryPercent}%",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Start permission usage monitoring algorithm
     */
    private fun startPermissionUsageMonitoring() {
        coroutineScope.launch {
            backendIntegrationManager.getSecurityEventsFlow().collect { events ->
                // Check for permission-related events
                events.events.forEach { event ->
                    if (event.type == "permission_usage") {
                        // Update monitoring stats
                        _monitoringStats.value = _monitoringStats.value.copy(
                            permissionEvents = _monitoringStats.value.permissionEvents + 1,
                            lastPermissionUpdate = System.currentTimeMillis()
                        )
                        
                        // Check for sensitive permission usage
                        if (isSensitivePermission(event.description)) {
                            addAlert(
                                MonitoringAlert(
                                    type = AlertType.PERMISSION,
                                    title = "Sensitive Permission Usage",
                                    description = event.description,
                                    timestamp = event.timestamp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add alert to the list of monitoring alerts
     */
    private fun addAlert(alert: MonitoringAlert) {
        val currentAlerts = _monitoringAlerts.value.toMutableList()
        currentAlerts.add(0, alert) // Add to the beginning of the list
        
        // Keep only the last 100 alerts
        if (currentAlerts.size > 100) {
            currentAlerts.removeAt(currentAlerts.size - 1)
        }
        
        _monitoringAlerts.value = currentAlerts
        Log.d(TAG, "Monitoring alert: ${alert.title} - ${alert.description}")
    }
    
    /**
     * Check if domain is suspicious
     */
    private fun isSuspiciousDomain(domain: String): Boolean {
        val suspiciousDomains = listOf(
            "malware.com", "phishing.net", "suspicious.org", "badactor.io"
        )
        
        return suspiciousDomains.any { domain.contains(it) }
    }
    
    /**
     * Check if port is unusual
     */
    private fun isUnusualPort(port: Int): Boolean {
        val unusualPorts = listOf(
            4444, 31337, 1337, 8090, 9999
        )
        
        return unusualPorts.contains(port)
    }
    
    /**
     * Check if process is malicious
     */
    private fun isMaliciousProcess(processName: String): Boolean {
        val maliciousProcesses = listOf(
            "malware", "trojan", "keylogger", "spyware", "backdoor"
        )
        
        return maliciousProcesses.any { processName.toLowerCase().contains(it) }
    }
    
    /**
     * Check if permission is sensitive
     */
    private fun isSensitivePermission(description: String): Boolean {
        val sensitivePermissions = listOf(
            "camera", "microphone", "location", "contacts", "sms", "call"
        )
        
        return sensitivePermissions.any { description.toLowerCase().contains(it) }
    }
    
    /**
     * Clear all monitoring alerts
     */
    fun clearAlerts() {
        _monitoringAlerts.value = emptyList()
    }
    
    /**
     * Monitoring state
     */
    sealed class MonitoringState {
        object Inactive : MonitoringState()
        object Active : MonitoringState()
    }
    
    /**
     * Monitoring statistics data class
     */
    data class MonitoringStats(
        val cpuUsage: Float = 0f,
        val memoryUsage: Float = 0f,
        val diskUsage: Float = 0f,
        val networkConnections: Int = 0,
        val downloadSpeed: Double = 0.0,
        val uploadSpeed: Double = 0.0,
        val runningProcesses: Int = 0,
        val permissionEvents: Int = 0,
        val lastSystemUpdate: Long = 0,
        val lastNetworkUpdate: Long = 0,
        val lastProcessUpdate: Long = 0,
        val lastPermissionUpdate: Long = 0
    )
    
    /**
     * Monitoring alert data class
     */
    data class MonitoringAlert(
        val type: AlertType,
        val title: String,
        val description: String,
        val timestamp: Long
    )
    
    /**
     * Alert type enum
     */
    enum class AlertType {
        SYSTEM,
        NETWORK,
        APPLICATION,
        PERMISSION
    }
}