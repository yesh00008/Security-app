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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThreatDetectionManager provides advanced threat detection capabilities
 * by analyzing security events, anomalies, and monitoring data.
 */
@Singleton
class ThreatDetectionManager @Inject constructor(
    private val context: Context,
    private val backendIntegrationManager: BackendIntegrationManager,
    private val anomalyDetectionService: AnomalyDetectionService,
    private val monitoringService: MonitoringService
) {
    private val TAG = "ThreatDetectionManager"
    
    // Coroutine scope for background operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Threat detection state
    private val _threatDetectionState = MutableStateFlow<ThreatDetectionState>(ThreatDetectionState.Inactive)
    val threatDetectionState: StateFlow<ThreatDetectionState> = _threatDetectionState.asStateFlow()
    
    // Detected threats
    private val _detectedThreats = MutableStateFlow<List<Threat>>(emptyList())
    val detectedThreats: StateFlow<List<Threat>> = _detectedThreats.asStateFlow()
    
    // Overall security score (0-100)
    private val _securityScore = MutableStateFlow(100)
    val securityScore: StateFlow<Int> = _securityScore.asStateFlow()
    
    /**
     * Start threat detection
     */
    fun startThreatDetection() {
        if (_threatDetectionState.value is ThreatDetectionState.Active) {
            Log.d(TAG, "Threat detection already active")
            return
        }
        
        _threatDetectionState.value = ThreatDetectionState.Active
        
        // Start anomaly detection and monitoring services if not already started
        anomalyDetectionService.startAnomalyDetection()
        monitoringService.startMonitoring()
        
        // Start threat analysis
        startThreatAnalysis()
        
        Log.d(TAG, "Threat detection started")
    }
    
    /**
     * Stop threat detection
     */
    fun stopThreatDetection() {
        _threatDetectionState.value = ThreatDetectionState.Inactive
        Log.d(TAG, "Threat detection stopped")
    }
    
    /**
     * Start threat analysis
     */
    private fun startThreatAnalysis() {
        // Analyze anomalies
        coroutineScope.launch {
            anomalyDetectionService.detectedAnomalies.collect { anomalies ->
                analyzeAnomalies(anomalies)
            }
        }
        
        // Analyze monitoring alerts
        coroutineScope.launch {
            monitoringService.monitoringAlerts.collect { alerts ->
                analyzeMonitoringAlerts(alerts)
            }
        }
        
        // Analyze security events
        coroutineScope.launch {
            backendIntegrationManager.getSecurityEventsFlow().collect { events ->
                analyzeSecurityEvents(events)
            }
        }
        
        // Update security score periodically
        coroutineScope.launch {
            while (_threatDetectionState.value is ThreatDetectionState.Active) {
                updateSecurityScore()
                kotlinx.coroutines.delay(60000) // Update every minute
            }
        }
    }
    
    /**
     * Analyze anomalies for potential threats
     */
    private fun analyzeAnomalies(anomalies: List<AnomalyDetectionService.Anomaly>) {
        // Focus on recent anomalies (last hour)
        val recentAnomalies = anomalies.filter { it.timestamp > System.currentTimeMillis() - 3600000 }
        
        // Check for patterns indicating threats
        val criticalAnomalies = recentAnomalies.filter { it.severity == AnomalyDetectionService.AnomalySeverity.CRITICAL }
        val highSeverityAnomalies = recentAnomalies.filter { it.severity == AnomalyDetectionService.AnomalySeverity.HIGH }
        
        // Check for critical anomalies
        criticalAnomalies.forEach { anomaly ->
            addThreat(
                Threat(
                    type = mapAnomalyTypeToThreatType(anomaly.type),
                    title = "Critical Security Threat",
                    description = anomaly.description,
                    severity = ThreatSeverity.CRITICAL,
                    timestamp = anomaly.timestamp,
                    source = ThreatSource.ANOMALY_DETECTION
                )
            )
        }
        
        // Check for multiple high severity anomalies of the same type
        val highSeverityAnomaliesByType = highSeverityAnomalies.groupBy { it.type }
        highSeverityAnomaliesByType.forEach { (type, typeAnomalies) ->
            if (typeAnomalies.size >= 3) {
                addThreat(
                    Threat(
                        type = mapAnomalyTypeToThreatType(type),
                        title = "Persistent ${type.name} Threat",
                        description = "Multiple high severity ${type.name.toLowerCase()} anomalies detected",
                        severity = ThreatSeverity.HIGH,
                        timestamp = System.currentTimeMillis(),
                        source = ThreatSource.ANOMALY_DETECTION
                    )
                )
            }
        }
    }
    
    /**
     * Analyze monitoring alerts for potential threats
     */
    private fun analyzeMonitoringAlerts(alerts: List<MonitoringService.MonitoringAlert>) {
        // Focus on recent alerts (last hour)
        val recentAlerts = alerts.filter { it.timestamp > System.currentTimeMillis() - 3600000 }
        
        // Group alerts by type
        val alertsByType = recentAlerts.groupBy { it.type }
        
        // Check for multiple network alerts
        val networkAlerts = alertsByType[MonitoringService.AlertType.NETWORK] ?: emptyList()
        if (networkAlerts.size >= 3) {
            addThreat(
                Threat(
                    type = ThreatType.NETWORK,
                    title = "Network Security Threat",
                    description = "Multiple suspicious network activities detected",
                    severity = ThreatSeverity.HIGH,
                    timestamp = System.currentTimeMillis(),
                    source = ThreatSource.MONITORING
                )
            )
        }
        
        // Check for multiple permission alerts
        val permissionAlerts = alertsByType[MonitoringService.AlertType.PERMISSION] ?: emptyList()
        if (permissionAlerts.size >= 2) {
            addThreat(
                Threat(
                    type = ThreatType.PRIVACY,
                    title = "Privacy Threat",
                    description = "Multiple sensitive permission usages detected",
                    severity = ThreatSeverity.MEDIUM,
                    timestamp = System.currentTimeMillis(),
                    source = ThreatSource.MONITORING
                )
            )
        }
        
        // Check for application behavior alerts
        val appAlerts = alertsByType[MonitoringService.AlertType.APPLICATION] ?: emptyList()
        if (appAlerts.isNotEmpty()) {
            // Check for malicious process alerts
            val maliciousProcessAlerts = appAlerts.filter { it.title.contains("Malicious Process") }
            if (maliciousProcessAlerts.isNotEmpty()) {
                addThreat(
                    Threat(
                        type = ThreatType.MALWARE,
                        title = "Potential Malware Detected",
                        description = "Suspicious processes detected that may indicate malware",
                        severity = ThreatSeverity.CRITICAL,
                        timestamp = System.currentTimeMillis(),
                        source = ThreatSource.MONITORING
                    )
                )
            }
        }
    }
    
    /**
     * Analyze security events for potential threats
     */
    private fun analyzeSecurityEvents(securityEvents: SecurityEvents) {
        // Focus on recent events (last hour)
        val recentEvents = securityEvents.events.filter { it.timestamp > System.currentTimeMillis() - 3600000 }
        
        // Check for critical security events
        val criticalEvents = recentEvents.filter { it.severity == "critical" }
        criticalEvents.forEach { event ->
            addThreat(
                Threat(
                    type = mapEventTypeToThreatType(event.type),
                    title = "Critical Security Event",
                    description = event.description,
                    severity = ThreatSeverity.CRITICAL,
                    timestamp = event.timestamp,
                    source = ThreatSource.SECURITY_EVENTS
                )
            )
        }
        
        // Check for multiple high severity events of the same type
        val highSeverityEvents = recentEvents.filter { it.severity == "high" }
        val highSeverityEventsByType = highSeverityEvents.groupBy { it.type }
        highSeverityEventsByType.forEach { (type, typeEvents) ->
            if (typeEvents.size >= 2) {
                addThreat(
                    Threat(
                        type = mapEventTypeToThreatType(type),
                        title = "Persistent Security Threat",
                        description = "Multiple high severity security events of type $type",
                        severity = ThreatSeverity.HIGH,
                        timestamp = System.currentTimeMillis(),
                        source = ThreatSource.SECURITY_EVENTS
                    )
                )
            }
        }
    }
    
    /**
     * Map anomaly type to threat type
     */
    private fun mapAnomalyTypeToThreatType(anomalyType: AnomalyDetectionService.AnomalyType): ThreatType {
        return when (anomalyType) {
            AnomalyDetectionService.AnomalyType.SYSTEM -> ThreatType.SYSTEM
            AnomalyDetectionService.AnomalyType.NETWORK -> ThreatType.NETWORK
            AnomalyDetectionService.AnomalyType.SECURITY -> ThreatType.SECURITY
            AnomalyDetectionService.AnomalyType.PROCESS -> ThreatType.MALWARE
        }
    }
    
    /**
     * Map event type to threat type
     */
    private fun mapEventTypeToThreatType(eventType: String): ThreatType {
        return when (eventType) {
            "network" -> ThreatType.NETWORK
            "system" -> ThreatType.SYSTEM
            "malware" -> ThreatType.MALWARE
            "privacy" -> ThreatType.PRIVACY
            "permission" -> ThreatType.PRIVACY
            else -> ThreatType.SECURITY
        }
    }
    
    /**
     * Add threat to the list of detected threats
     */
    private fun addThreat(threat: Threat) {
        val currentThreats = _detectedThreats.value.toMutableList()
        
        // Check if similar threat already exists
        val similarThreat = currentThreats.find { 
            it.type == threat.type && 
            it.title == threat.title &&
            it.timestamp > System.currentTimeMillis() - 3600000 // Within the last hour
        }
        
        if (similarThreat == null) {
            currentThreats.add(0, threat) // Add to the beginning of the list
            
            // Keep only the last 50 threats
            if (currentThreats.size > 50) {
                currentThreats.removeAt(currentThreats.size - 1)
            }
            
            _detectedThreats.value = currentThreats
            Log.d(TAG, "Threat detected: ${threat.title} - ${threat.description}")
        }
    }
    
    /**
     * Update security score based on detected threats and anomalies
     */
    private fun updateSecurityScore() {
        var score = 100
        
        // Reduce score based on recent threats (last 24 hours)
        val recentThreats = _detectedThreats.value.filter { it.timestamp > System.currentTimeMillis() - 86400000 }
        
        // Critical threats have major impact
        val criticalThreats = recentThreats.filter { it.severity == ThreatSeverity.CRITICAL }
        score -= criticalThreats.size * 20
        
        // High severity threats have significant impact
        val highThreats = recentThreats.filter { it.severity == ThreatSeverity.HIGH }
        score -= highThreats.size * 10
        
        // Medium severity threats have moderate impact
        val mediumThreats = recentThreats.filter { it.severity == ThreatSeverity.MEDIUM }
        score -= mediumThreats.size * 5
        
        // Low severity threats have minor impact
        val lowThreats = recentThreats.filter { it.severity == ThreatSeverity.LOW }
        score -= lowThreats.size * 2
        
        // Ensure score is between 0 and 100
        score = score.coerceIn(0, 100)
        
        _securityScore.value = score
    }
    
    /**
     * Clear all detected threats
     */
    fun clearThreats() {
        _detectedThreats.value = emptyList()
        _securityScore.value = 100
    }
    
    /**
     * Get security recommendations based on detected threats
     */
    fun getSecurityRecommendations(): List<SecurityRecommendation> {
        val recommendations = mutableListOf<SecurityRecommendation>()
        val recentThreats = _detectedThreats.value.filter { it.timestamp > System.currentTimeMillis() - 86400000 }
        
        // Group threats by type
        val threatsByType = recentThreats.groupBy { it.type }
        
        // Generate recommendations based on threat types
        threatsByType.forEach { (type, threats) ->
            when (type) {
                ThreatType.MALWARE -> {
                    recommendations.add(
                        SecurityRecommendation(
                            title = "Run Full System Scan",
                            description = "Perform a comprehensive scan to detect and remove malware",
                            priority = RecommendationPriority.HIGH
                        )
                    )
                }
                ThreatType.NETWORK -> {
                    recommendations.add(
                        SecurityRecommendation(
                            title = "Review Network Connections",
                            description = "Check active network connections and disable suspicious ones",
                            priority = RecommendationPriority.MEDIUM
                        )
                    )
                }
                ThreatType.PRIVACY -> {
                    recommendations.add(
                        SecurityRecommendation(
                            title = "Review App Permissions",
                            description = "Check and revoke unnecessary permissions from applications",
                            priority = RecommendationPriority.HIGH
                        )
                    )
                }
                ThreatType.SYSTEM -> {
                    recommendations.add(
                        SecurityRecommendation(
                            title = "Update System",
                            description = "Ensure your system is up to date with the latest security patches",
                            priority = RecommendationPriority.MEDIUM
                        )
                    )
                }
                ThreatType.SECURITY -> {
                    recommendations.add(
                        SecurityRecommendation(
                            title = "Enable Enhanced Security",
                            description = "Activate additional security features to protect your device",
                            priority = RecommendationPriority.HIGH
                        )
                    )
                }
            }
        }
        
        // Add general recommendations if no specific threats
        if (recommendations.isEmpty()) {
            recommendations.add(
                SecurityRecommendation(
                    title = "Regular Security Scan",
                    description = "Perform regular security scans to maintain device health",
                    priority = RecommendationPriority.LOW
                )
            )
        }
        
        return recommendations
    }
    
    /**
     * Threat detection state
     */
    sealed class ThreatDetectionState {
        object Inactive : ThreatDetectionState()
        object Active : ThreatDetectionState()
    }
    
    /**
     * Threat data class
     */
    data class Threat(
        val type: ThreatType,
        val title: String,
        val description: String,
        val severity: ThreatSeverity,
        val timestamp: Long,
        val source: ThreatSource
    )
    
    /**
     * Threat type enum
     */
    enum class ThreatType {
        MALWARE,
        NETWORK,
        PRIVACY,
        SYSTEM,
        SECURITY
    }
    
    /**
     * Threat severity enum
     */
    enum class ThreatSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    /**
     * Threat source enum
     */
    enum class ThreatSource {
        ANOMALY_DETECTION,
        MONITORING,
        SECURITY_EVENTS
    }
    
    /**
     * Security recommendation data class
     */
    data class SecurityRecommendation(
        val title: String,
        val description: String,
        val priority: RecommendationPriority
    )
    
    /**
     * Recommendation priority enum
     */
    enum class RecommendationPriority {
        LOW,
        MEDIUM,
        HIGH
    }
}