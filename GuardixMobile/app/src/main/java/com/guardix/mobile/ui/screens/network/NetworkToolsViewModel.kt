package com.guardix.mobile.ui.screens.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.guardix.mobile.data.managers.*
import com.guardix.mobile.ui.theme.*
import com.guardix.mobile.data.realtime.RealtimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class NetworkToolsUiState(
    val networkInfo: NetworkInfo? = null,
    val dataUsage: DataUsageInfo? = null,
    val availableNetworks: List<WiFiNetwork> = emptyList(),
    val recentActivity: List<NetworkActivity> = emptyList(),
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val showSpeedTestDialog: Boolean = false,
    val showNetworkDetailsDialog: Boolean = false,
    val selectedNetwork: WiFiNetwork? = null,
    val speedTestResult: SpeedTestResult? = null,
    val isSpeedTestRunning: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NetworkToolsViewModel @Inject constructor(
    private val realtimeRepository: RealtimeRepository
) : ViewModel() {
    
    private var networkManager: NetworkManager? = null
    
    private val _uiState = MutableStateFlow(NetworkToolsUiState())
    val uiState: StateFlow<NetworkToolsUiState> = _uiState.asStateFlow()
    
    fun initializeNetworkManager(context: Context) {
        if (networkManager == null) {
            networkManager = NetworkManager(context)
            loadInitialData()
            // Connect to realtime backend for continuous updates
            realtimeRepository.connect(listOf("network_status"))
            viewModelScope.launch {
                realtimeRepository.networkStatusFlow.collect { status ->
                    val current = _uiState.value.networkInfo ?: networkManager?.getCurrentNetworkInfo()
                    val updated = current?.copy(
                        connectionSpeed = status.currentSpeeds.downloadMbps.toFloat()
                    )
                    if (updated != null) {
                        _uiState.value = _uiState.value.copy(networkInfo = updated)
                    }
                }
            }
        }
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                networkManager?.let { manager ->
                    val networkInfo = manager.getCurrentNetworkInfo()
                    val dataUsage = manager.getDataUsage()
                    val availableNetworks = manager.scanWiFiNetworks()
                    val recentActivity = generateRecentActivity()
                    
                    _uiState.value = _uiState.value.copy(
                        networkInfo = networkInfo,
                        dataUsage = dataUsage,
                        availableNetworks = availableNetworks,
                        recentActivity = recentActivity,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load network data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun handleNetworkTool(toolType: NetworkToolType) {
        viewModelScope.launch {
            try {
                when (toolType) {
                    NetworkToolType.SPEED_TEST -> showSpeedTestDialog()
                    NetworkToolType.WIFI_ANALYZER -> performWiFiAnalysis()
                    NetworkToolType.PING_TEST -> performPingTest()
                    NetworkToolType.PORT_SCANNER -> performPortScan()
                    NetworkToolType.DATA_MONITOR -> monitorDataUsage()
                    NetworkToolType.NETWORK_INFO -> showNetworkInfo()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Tool operation failed: ${e.message}"
                )
            }
        }
    }
    
    private fun showSpeedTestDialog() {
        _uiState.value = _uiState.value.copy(showSpeedTestDialog = true)
    }
    
    fun startSpeedTest() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSpeedTestRunning = true)
                
                // Simulate speed test
                delay(3000)
                
                val result = SpeedTestResult(
                    downloadSpeed = (20..100).random().toFloat(),
                    uploadSpeed = (5..50).random().toFloat(),
                    ping = (10..100).random(),
                    jitter = (1..20).random().toFloat(),
                    timestamp = getCurrentTimestamp()
                )
                
                _uiState.value = _uiState.value.copy(
                    speedTestResult = result,
                    isSpeedTestRunning = false
                )
                
                addNetworkActivity(
                    NetworkActivity(
                        description = "Speed test completed - ${result.downloadSpeed} Mbps down",
                        timestamp = getCurrentTimestamp(),
                        icon = Icons.Default.Speed,
                        color = LightBlue
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSpeedTestRunning = false,
                    errorMessage = "Speed test failed: ${e.message}"
                )
            }
        }
    }
    
    fun dismissSpeedTestDialog() {
        _uiState.value = _uiState.value.copy(
            showSpeedTestDialog = false,
            speedTestResult = null
        )
    }
    
    private suspend fun performWiFiAnalysis() {
        _uiState.value = _uiState.value.copy(isScanning = true)
        
        delay(2000) // Simulate scanning
        
        networkManager?.let { manager ->
            val networks = manager.analyzeWiFiNetworks()
            _uiState.value = _uiState.value.copy(
                availableNetworks = networks,
                isScanning = false
            )
            
            addNetworkActivity(
                NetworkActivity(
                    description = "WiFi analysis completed - ${networks.size} networks found",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Analytics,
                    color = SuccessGreen
                )
            )
        }
    }
    
    private suspend fun performPingTest() {
        networkManager?.let { manager ->
            val pingResult = manager.performPingTest("8.8.8.8")
            
            addNetworkActivity(
                NetworkActivity(
                    description = "Ping test to Google DNS - ${pingResult}ms",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Timeline,
                    color = WarningOrange
                )
            )
        }
    }
    
    private suspend fun performPortScan() {
        networkManager?.let { manager ->
            val openPorts = manager.scanPorts("192.168.1.1")
            
            addNetworkActivity(
                NetworkActivity(
                    description = "Port scan completed - ${openPorts.size} open ports found",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Scanner,
                    color = Purple
                )
            )
        }
    }
    
    private suspend fun monitorDataUsage() {
        networkManager?.let { manager ->
            val dataUsage = manager.getDetailedDataUsage()
            _uiState.value = _uiState.value.copy(dataUsage = dataUsage)
            
            addNetworkActivity(
                NetworkActivity(
                    description = "Data usage updated - ${formatDataSize(dataUsage.usedData)} used this month",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.DataUsage,
                    color = Cyan
                )
            )
        }
    }
    
    private suspend fun showNetworkInfo() {
        networkManager?.let { manager ->
            val networkInfo = manager.getDetailedNetworkInfo()
            _uiState.value = _uiState.value.copy(networkInfo = networkInfo)
            
            addNetworkActivity(
                NetworkActivity(
                    description = "Network information refreshed",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Info,
                    color = ErrorRed
                )
            )
        }
    }
    
    fun connectToWiFi(network: WiFiNetwork) {
        if (network.isConnected) {
            showNetworkDetails(network)
            return
        }
        
        viewModelScope.launch {
            try {
                networkManager?.let { manager ->
                    val success = manager.connectToWiFi(network.ssid, "")
                    
                    if (success) {
                        // Update network list
                        val updatedNetworks = _uiState.value.availableNetworks.map {
                            if (it.ssid == network.ssid) {
                                it.copy(isConnected = true)
                            } else {
                                it.copy(isConnected = false)
                            }
                        }
                        
                        _uiState.value = _uiState.value.copy(availableNetworks = updatedNetworks)
                        
                        addNetworkActivity(
                            NetworkActivity(
                                description = "Connected to ${network.ssid}",
                                timestamp = getCurrentTimestamp(),
                                icon = Icons.Default.Wifi,
                                color = SuccessGreen
                            )
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to connect to ${network.ssid}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Connection failed: ${e.message}"
                )
            }
        }
    }
    
    private fun showNetworkDetails(network: WiFiNetwork) {
        _uiState.value = _uiState.value.copy(
            selectedNetwork = network,
            showNetworkDetailsDialog = true
        )
    }
    
    fun dismissNetworkDetailsDialog() {
        _uiState.value = _uiState.value.copy(
            showNetworkDetailsDialog = false,
            selectedNetwork = null
        )
    }
    
    fun forgetNetwork(network: WiFiNetwork) {
        viewModelScope.launch {
            try {
                networkManager?.let { manager ->
                    val success = manager.forgetWiFiNetwork(network.ssid)
                    
                    if (success) {
                        val updatedNetworks = _uiState.value.availableNetworks.map {
                            if (it.ssid == network.ssid) {
                                it.copy(isConnected = false)
                            } else it
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            availableNetworks = updatedNetworks,
                            showNetworkDetailsDialog = false,
                            selectedNetwork = null
                        )
                        
                        addNetworkActivity(
                            NetworkActivity(
                                description = "Forgot network ${network.ssid}",
                                timestamp = getCurrentTimestamp(),
                                icon = Icons.Default.WifiOff,
                                color = WarningOrange
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to forget network: ${e.message}"
                )
            }
        }
    }
    
    private fun addNetworkActivity(activity: NetworkActivity) {
        val currentActivity = _uiState.value.recentActivity.toMutableList()
        currentActivity.add(0, activity) // Add to beginning
        if (currentActivity.size > 10) {
            currentActivity.removeAt(currentActivity.size - 1) // Keep only 10 recent activities
        }
        
        _uiState.value = _uiState.value.copy(recentActivity = currentActivity)
    }
    
    private fun generateRecentActivity(): List<NetworkActivity> {
        return listOf(
            NetworkActivity(
                description = "Network tools initialized",
                timestamp = getCurrentTimestamp(),
                icon = Icons.Default.NetworkCheck,
                color = LightBlue
            ),
            NetworkActivity(
                description = "WiFi networks scanned",
                timestamp = getTimestamp(-1),
                icon = Icons.Default.Wifi,
                color = SuccessGreen
            ),
            NetworkActivity(
                description = "Data usage monitored",
                timestamp = getTimestamp(-2),
                icon = Icons.Default.DataUsage,
                color = Cyan
            )
        )
    }
    
    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    
    private fun getTimestamp(hoursAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, hoursAgo)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }
    
    private fun formatDataSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            else -> String.format("%.0f KB", bytes / 1024.0)
        }
    }
}
