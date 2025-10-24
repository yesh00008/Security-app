package com.guardix.mobile.data.remote

import android.content.Context
import android.util.Log
import com.guardix.mobile.data.remote.GuardixApiClient
import com.guardix.mobile.data.realtime.WebSocketClient
import com.guardix.mobile.data.realtime.SystemMetrics
import com.guardix.mobile.data.realtime.NetworkStatus
import com.guardix.mobile.data.realtime.SecurityEvents
import com.guardix.mobile.data.realtime.ProcessList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackendIntegrationManager serves as the central hub for all backend communications,
 * integrating both REST API calls and WebSocket real-time data streams.
 */
@Singleton
class BackendIntegrationManager @Inject constructor(
    private val context: Context
) {
    private val TAG = "BackendIntegrationManager"
    
    // Coroutine scope for background operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // WebSocket client for real-time data
    private val webSocketClient = WebSocketClient(coroutineScope)
    
    // Connection status
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    // Backend service status
    private val _backendServiceStatus = MutableStateFlow(BackendServiceStatus.UNKNOWN)
    val backendServiceStatus: StateFlow<BackendServiceStatus> = _backendServiceStatus.asStateFlow()
    
    // Single Retrofit API client
    private val api = GuardixApiClient.service
    
    /**
     * Initialize the backend integration
     */
    fun initialize() {
        coroutineScope.launch {
            try {
                // Check if backend services are available
                checkBackendStatus()
                
                // Connect to WebSocket for real-time data
                connectWebSocket()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing backend integration: ${e.message}")
                _backendServiceStatus.value = BackendServiceStatus.UNAVAILABLE
            }
        }
    }
    
    /**
     * Check if backend services are available
     */
    private suspend fun checkBackendStatus() {
        withContext(Dispatchers.IO) {
            try {
                // Call health endpoint without auth
                api.health()
                _backendServiceStatus.value = BackendServiceStatus.AVAILABLE
                Log.d(TAG, "Backend services are available")
            } catch (e: IOException) {
                _backendServiceStatus.value = BackendServiceStatus.UNAVAILABLE
                Log.e(TAG, "Backend services are unavailable: ${e.message}")
            } catch (e: Exception) {
                // If we get any other exception, the server is probably running but returned an error
                _backendServiceStatus.value = BackendServiceStatus.AVAILABLE
                Log.d(TAG, "Backend services are available but returned an error: ${e.message}")
            }
        }
    }
    
    /**
     * Connect to WebSocket for real-time data
     */
    fun connectWebSocket() {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
            Log.d(TAG, "Already connected to WebSocket")
            return
        }
        
        try {
            webSocketClient.connect()
            _connectionStatus.value = ConnectionStatus.CONNECTED
            Log.d(TAG, "Connected to WebSocket")
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            Log.e(TAG, "Error connecting to WebSocket: ${e.message}")
        }
    }
    
    /**
     * Disconnect from WebSocket
     */
    fun disconnectWebSocket() {
        webSocketClient.disconnect()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        Log.d(TAG, "Disconnected from WebSocket")
    }
    
    /**
     * Get system metrics flow
     */
    fun getSystemMetricsFlow(): Flow<SystemMetrics> {
        return webSocketClient.systemMetricsFlow
    }
    
    /**
     * Get network status flow
     */
    fun getNetworkStatusFlow(): Flow<NetworkStatus> {
        return webSocketClient.networkStatusFlow
    }
    
    /**
     * Get security events flow
     */
    fun getSecurityEventsFlow(): Flow<SecurityEvents> {
        return webSocketClient.securityEventsFlow
    }
    
    /**
     * Get process list flow
     */
    fun getProcessListFlow(): Flow<ProcessList> {
        return webSocketClient.processListFlow
    }
    
    /**
     * Connection status enum
     */
    enum class ConnectionStatus {
        CONNECTED,
        DISCONNECTED
    }
    
    /**
     * Backend service status enum
     */
    enum class BackendServiceStatus {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN
    }
}
