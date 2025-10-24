package com.guardix.mobile.data.security

import android.content.Context
import android.util.Log
import com.guardix.mobile.data.remote.BackendIntegrationManager
import com.guardix.mobile.data.realtime.NetworkStatus
import com.guardix.mobile.data.realtime.ProcessList
import com.guardix.mobile.data.realtime.SecurityEvents
import com.guardix.mobile.data.realtime.SystemMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AnomalyDetectionService monitors real-time data streams for security anomalies
 * and provides threat detection capabilities.
 */
@Singleton
class AnomalyDetectionService @Inject constructor(
    private val context: Context,
    private val backendIntegrationManager: BackendIntegrationManager
) {
    private val TAG = "AnomalyDetectionService"
    
    // Coroutine scope for background operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Anomaly detection state
    private val _anomalyDetectionState = MutableStateFlow<AnomalyDetectionState>(AnomalyDetectionState.Idle)
    val anomalyDetectionState: StateFlow<AnomalyDetectionState> = _anomalyDetectionState.asStateFlow()
    
    // Detected anomalies
    private val _detectedAnomalies = MutableStateFlow<List<Anomaly>>(emptyList())
    val detectedAnomalies: StateFlow<List<Anomaly>> = _detectedAnomalies.asStateFlow()
    
    // Threat level
    private val _threatLevel = MutableStateFlow(ThreatLevel.LOW)
    val threatLevel: StateFlow<ThreatLevel> = _threatLevel.asStateFlow()
    
    /**
     * Start anomaly detection
     */
    fun startAnomalyDetection() {
        if (_anomalyDetectionState.value is AnomalyDetectionState.Active) {
            Log.d(TAG, "Anomaly detection already active")
            return
        }
        
        _anomalyDetectionState.value = AnomalyDetectionState.Active
        
        // Connect to WebSocket if not already connected
        backendIntegrationManager.connectWebSocket()
        
        // Start monitoring data streams
        monitorSystemMetrics()
        monitorNetworkStatus()
        monitorSecurityEvents()
        monitorProcessList()
        
        Log.d(TAG, "Anomaly detection started")
    }
    
    /**
     * Stop anomaly detection
     */
    fun stopAnomalyDetection() {
        _anomalyDetectionState.value = AnomalyDetectionState.Idle
        Log.d(TAG, "Anomaly detection stopped")
    }
    
    /**
     * Monitor system metrics for anomalies
     */
    private fun monitorSystemMetrics() {
        coroutineScope.launch {
            backendIntegrationManager.getSystemMetricsFlow().collect { metrics ->
                // Check for CPU usage spikes
                if (metrics.cpuUsage > 90) {
                    addAnomaly(
                        Anomaly(
                            type = AnomalyType.SYSTEM,
                            description = "High CPU usage detected: ${metrics.cpuUsage}%",
                            severity = AnomalySeverity.MEDIUM,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                
                // Check for memory usage anomalies
                val memoryUsagePercent = (metrics.memoryUsed.toFloat() / metrics.memoryTotal) * 100
                if (memoryUsagePercent > 90) {
                    addAnomaly(
                        Anomaly(
                            type = AnomalyType.SYSTEM,
                            description = "High memory usage detected: $memoryUsagePercent%",
                            severity = AnomalySeverity.MEDIUM,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                
                // Update threat level based on system metrics
                updateThreatLevel()
            }
        }
    }
    
    /**
     * Monitor network status for anomalies
     */
    private fun monitorNetworkStatus() {
        coroutineScope.launch {
            backendIntegrationManager.getNetworkStatusFlow().collect { status ->
                // Check for unusual network activity
                val downloadSpeed = status.stats.downloadSpeed
                val uploadSpeed = status.stats.uploadSpeed
                
                if (downloadSpeed > 10.0 && uploadSpeed > 5.0) {
                    addAnomaly(
                        Anomaly(
                            type = AnomalyType.NETWORK,
                            description = "Unusual network activity: Download ${downloadSpeed}MB/s, Upload ${uploadSpeed}MB/s",
                            severity = AnomalySeverity.HIGH,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                
                // Update threat level based on network status
                updateThreatLevel()
            }
        }
    }
    
    /**
     * Monitor security events for anomalies
     */
    private fun monitorSecurityEvents() {
        coroutineScope.launch {
            backendIntegrationManager.getSecurityEventsFlow().collect { events ->
                // Process security events
                events.events.forEach { event ->
                    if (event.severity == "high" || event.severity == "critical") {
                        addAnomaly(
                            Anomaly(
                                type = AnomalyType.SECURITY,
                                description = event.description,
                                severity = if (event.severity == "critical") AnomalySeverity.CRITICAL else AnomalySeverity.HIGH,
                                timestamp = event.timestamp
                            )
                        )
                    }
                }
                
                // Update threat level based on security events
                updateThreatLevel()
            }
        }
    }
    
    /**
     * Monitor process list for anomalies
     */
    private fun monitorProcessList() {
        coroutineScope.launch {
            backendIntegrationManager.getProcessListFlow().collect { processList ->
                // Check for suspicious processes
                processList.processes.forEach { process ->
                    if (process.cpuPercent > 80) {
                        addAnomaly(
                            Anomaly(
                                type = AnomalyType.PROCESS,
                                description = "Process ${process.name} using high CPU: ${process.cpuPercent}%",
                                severity = AnomalySeverity.LOW,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                
                // Update threat level based on process list
                updateThreatLevel()
            }
        }
    }
    
    /**
     * Add anomaly to the list of detected anomalies
     */
    private fun addAnomaly(anomaly: Anomaly) {
        val currentAnomalies = _detectedAnomalies.value.toMutableList()
        currentAnomalies.add(0, anomaly) // Add to the beginning of the list
        
        // Keep only the last 100 anomalies
        if (currentAnomalies.size > 100) {
            currentAnomalies.removeAt(currentAnomalies.size - 1)
        }
        
        _detectedAnomalies.value = currentAnomalies
        Log.d(TAG, "Anomaly detected: ${anomaly.description}")
    }
    
    /**
     * Update threat level based on detected anomalies
     */
    private fun updateThreatLevel() {
        val anomalies = _detectedAnomalies.value
        
        // Calculate threat level based on anomalies in the last hour
        val recentAnomalies = anomalies.filter { it.timestamp > System.currentTimeMillis() - 3600000 }
        
        val threatLevel = when {
            recentAnomalies.any { it.severity == AnomalySeverity.CRITICAL } -> ThreatLevel.CRITICAL
            recentAnomalies.count { it.severity == AnomalySeverity.HIGH } >= 3 -> ThreatLevel.HIGH
            recentAnomalies.count { it.severity == AnomalySeverity.MEDIUM } >= 5 -> ThreatLevel.MEDIUM
            recentAnomalies.isNotEmpty() -> ThreatLevel.LOW
            else -> ThreatLevel.NORMAL
        }
        
        _threatLevel.value = threatLevel
    }
    
    /**
     * Clear all detected anomalies
     */
    fun clearAnomalies() {
        _detectedAnomalies.value = emptyList()
        _threatLevel.value = ThreatLevel.NORMAL
    }
    
    /**
     * Anomaly detection state
     */
    sealed class AnomalyDetectionState {
        object Idle : AnomalyDetectionState()
        object Active : AnomalyDetectionState()
    }
    
    /**
     * Anomaly data class
     */
    data class Anomaly(
        val type: AnomalyType,
        val description: String,
        val severity: AnomalySeverity,
        val timestamp: Long
    )
    
    /**
     * Anomaly type enum
     */
    enum class AnomalyType {
        SYSTEM,
        NETWORK,
        SECURITY,
        PROCESS
    }
    
    /**
     * Anomaly severity enum
     */
    enum class AnomalySeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    /**
     * Threat level enum
     */
    enum class ThreatLevel {
        NORMAL,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}