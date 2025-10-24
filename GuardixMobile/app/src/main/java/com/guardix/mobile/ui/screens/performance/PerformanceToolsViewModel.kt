package com.guardix.mobile.ui.screens.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.guardix.mobile.data.managers.*
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PerformanceToolsUiState(
    val isOptimizing: Boolean = false,
    val optimizationProgress: Float = 0f,
    val lastOptimizationResult: PerformanceOptimizationResult? = null,
    val showOptimizationResults: Boolean = false,
    val showBatteryDialog: Boolean = false,
    val showThermalDialog: Boolean = false,
    val memoryInfo: MemoryInfo? = null,
    val thermalInfo: ThermalInfo? = null,
    val batteryInfo: BatteryOptimizationInfo? = null,
    val recentActivity: List<PerformanceActivity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PerformanceToolsViewModel : ViewModel() {
    
    private var performanceManager: ComprehensivePerformanceManager? = null
    
    private val _uiState = MutableStateFlow(PerformanceToolsUiState())
    val uiState: StateFlow<PerformanceToolsUiState> = _uiState.asStateFlow()
    
    fun initializePerformanceManager(context: Context) {
        if (performanceManager == null) {
            performanceManager = ComprehensivePerformanceManager(context)
            
            // Observe performance manager state
            viewModelScope.launch {
                performanceManager?.isOptimizing?.collect { isOptimizing ->
                    _uiState.value = _uiState.value.copy(isOptimizing = isOptimizing)
                }
            }
            
            viewModelScope.launch {
                performanceManager?.optimizationProgress?.collect { progress ->
                    _uiState.value = _uiState.value.copy(optimizationProgress = progress)
                }
            }
            
            viewModelScope.launch {
                performanceManager?.memoryInfo?.collect { memoryInfo ->
                    _uiState.value = _uiState.value.copy(memoryInfo = memoryInfo)
                }
            }
            
            viewModelScope.launch {
                performanceManager?.thermalInfo?.collect { thermalInfo ->
                    _uiState.value = _uiState.value.copy(thermalInfo = thermalInfo)
                }
            }
            
            // Load initial data
            loadInitialData()
        }
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Load system information
                performanceManager?.let { manager ->
                    val memoryInfo = manager.memoryInfo.value
                    val thermalInfo = manager.getThermalInfo()
                    val batteryInfo = manager.getBatteryOptimizationInfo()
                    val recentActivity = generateRecentActivity()
                    
                    _uiState.value = _uiState.value.copy(
                        memoryInfo = memoryInfo,
                        thermalInfo = thermalInfo,
                        batteryInfo = batteryInfo,
                        recentActivity = recentActivity,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load performance data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun handleToolAction(toolType: PerformanceToolType) {
        viewModelScope.launch {
            try {
                when (toolType) {
                    PerformanceToolType.ONE_TAP_OPTIMIZATION -> performOneTapOptimization()
                    PerformanceToolType.PHONE_ACCELERATION -> acceleratePhone()
                    PerformanceToolType.JUNK_CLEANER -> performJunkCleanup()
                    PerformanceToolType.APP_STARTUP_MANAGER -> showAppStartupManager()
                    PerformanceToolType.POWER_CONSUMPTION_MANAGER -> showPowerConsumptionManager()
                    PerformanceToolType.THERMAL_COOLING -> showThermalDialog()
                    PerformanceToolType.BATTERY_MANAGEMENT -> showBatteryDialog()
                    PerformanceToolType.GAME_BOOST -> enableGameBoost()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Operation failed: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun performOneTapOptimization() {
        performanceManager?.let { manager ->
            val result = manager.performOneTapOptimization()
            
            _uiState.value = _uiState.value.copy(
                lastOptimizationResult = result,
                showOptimizationResults = true
            )
            
            addPerformanceActivity(
                PerformanceActivity(
                    description = "One-tap optimization completed - ${formatFileSize(result.memoryFreed + result.storageFreed)} freed",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.TouchApp,
                    color = SuccessGreen
                )
            )
        }
    }
    
    private suspend fun acceleratePhone() {
        performanceManager?.let { manager ->
            val memoryFreed = manager.acceleratePhone()
            
            addPerformanceActivity(
                PerformanceActivity(
                    description = "Phone acceleration completed - ${formatFileSize(memoryFreed)} memory freed",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Speed,
                    color = LightBlue
                )
            )
        }
    }
    
    private suspend fun performJunkCleanup() {
        performanceManager?.let { manager ->
            val storageInfo = manager.performJunkCleanup()
            val junkSize = storageInfo.junkFiles.sumOf { it.size }
            
            addPerformanceActivity(
                PerformanceActivity(
                    description = "Junk cleanup completed - ${formatFileSize(junkSize)} removed",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.DeleteSweep,
                    color = WarningOrange
                )
            )
        }
    }
    
    private fun showAppStartupManager() {
        performanceManager?.let { manager ->
            val startupApps = manager.getAppStartupInfo()
            val highImpactApps = startupApps.filter { it.startupImpact == "HIGH" }
            
            addPerformanceActivity(
                PerformanceActivity(
                    description = "App startup analysis - ${highImpactApps.size} high-impact apps found",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.PlayArrow,
                    color = Cyan
                )
            )
        }
    }
    
    private fun showPowerConsumptionManager() {
        performanceManager?.let { manager ->
            val powerHungryApps = manager.getPowerHungryApps()
            val highConsumptionApps = powerHungryApps.filter { it.powerUsagePercent > 10f }
            
            addPerformanceActivity(
                PerformanceActivity(
                    description = "Power analysis - ${highConsumptionApps.size} apps consuming high power",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Battery4Bar,
                    color = ErrorRed
                )
            )
        }
    }
    
    private fun showThermalDialog() {
        _uiState.value = _uiState.value.copy(showThermalDialog = true)
    }
    
    private fun showBatteryDialog() {
        _uiState.value = _uiState.value.copy(showBatteryDialog = true)
    }
    
    private suspend fun enableGameBoost() {
        // Get a sample game package for demonstration
        val sampleGamePackage = "com.example.game"
        performanceManager?.let { manager ->
            val success = manager.enableGameBoost(sampleGamePackage)
            
            addPerformanceActivity(
                PerformanceActivity(
                    description = if (success) "Game boost enabled successfully" else "Failed to enable game boost",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Gamepad,
                    color = if (success) SuccessGreen else ErrorRed
                )
            )
        }
    }
    
    fun optimizeBattery() {
        viewModelScope.launch {
            performanceManager?.let { manager ->
                val improvement = manager.performBatteryOptimization()
                
                addPerformanceActivity(
                    PerformanceActivity(
                        description = "Battery optimized - ${improvement}% improvement expected",
                        timestamp = getCurrentTimestamp(),
                        icon = Icons.Default.BatteryChargingFull,
                        color = SuccessGreen
                    )
                )
                
                _uiState.value = _uiState.value.copy(showBatteryDialog = false)
            }
        }
    }
    
    fun performThermalCooling() {
        viewModelScope.launch {
            performanceManager?.let { manager ->
                val success = manager.performThermalCooling()
                
                addPerformanceActivity(
                    PerformanceActivity(
                        description = if (success) "Thermal cooling completed" else "Thermal cooling failed",
                        timestamp = getCurrentTimestamp(),
                        icon = Icons.Default.Thermostat,
                        color = if (success) SuccessGreen else ErrorRed
                    )
                )
                
                _uiState.value = _uiState.value.copy(showThermalDialog = false)
                
                // Refresh thermal info
                val thermalInfo = manager.getThermalInfo()
                _uiState.value = _uiState.value.copy(thermalInfo = thermalInfo)
            }
        }
    }
    
    fun dismissOptimizationResults() {
        _uiState.value = _uiState.value.copy(showOptimizationResults = false)
    }
    
    fun dismissBatteryDialog() {
        _uiState.value = _uiState.value.copy(showBatteryDialog = false)
    }
    
    fun dismissThermalDialog() {
        _uiState.value = _uiState.value.copy(showThermalDialog = false)
    }
    
    private fun addPerformanceActivity(activity: PerformanceActivity) {
        val currentActivity = _uiState.value.recentActivity.toMutableList()
        currentActivity.add(0, activity) // Add to beginning
        if (currentActivity.size > 10) {
            currentActivity.removeAt(currentActivity.size - 1) // Keep only 10 recent activities
        }
        
        _uiState.value = _uiState.value.copy(recentActivity = currentActivity)
    }
    
    private fun generateRecentActivity(): List<PerformanceActivity> {
        return listOf(
            PerformanceActivity(
                description = "Performance tools initialized",
                timestamp = getCurrentTimestamp(),
                icon = Icons.Default.Speed,
                color = LightBlue
            ),
            PerformanceActivity(
                description = "System resources analyzed",
                timestamp = getTimestamp(-1),
                icon = Icons.Default.Analytics,
                color = SuccessGreen
            ),
            PerformanceActivity(
                description = "Memory usage monitored",
                timestamp = getTimestamp(-2),
                icon = Icons.Default.Memory,
                color = LightBlue
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
    
    private fun formatFileSize(bytes: Long): String {
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
}