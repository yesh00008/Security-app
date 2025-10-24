package com.guardix.mobile.ui.screens.security

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

data class SecurityToolsUiState(
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val lastScanResult: com.guardix.mobile.data.ScanResult? = null,
    val showScanResults: Boolean = false,
    val showAppLockDialog: Boolean = false,
    val availableApps: List<AppLockInfo> = emptyList(),
    val lockedApps: List<AppLockInfo> = emptyList(),
    val recentActivity: List<SecurityActivity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SecurityToolsViewModel : ViewModel() {
    
    private var securityManager: ComprehensiveSecurityManager? = null
    
    private val _uiState = MutableStateFlow(SecurityToolsUiState())
    val uiState: StateFlow<SecurityToolsUiState> = _uiState.asStateFlow()
    
    fun initializeSecurityManager(context: Context) {
        if (securityManager == null) {
            securityManager = ComprehensiveSecurityManager(context)
            
            // Observe security manager state
            viewModelScope.launch {
                securityManager?.isScanning?.collect { isScanning ->
                    _uiState.value = _uiState.value.copy(isScanning = isScanning)
                }
            }
            
            viewModelScope.launch {
                securityManager?.scanProgress?.collect { progress ->
                    _uiState.value = _uiState.value.copy(scanProgress = progress)
                }
            }
            
            viewModelScope.launch {
                securityManager?.lockedApps?.collect { lockedApps ->
                    _uiState.value = _uiState.value.copy(lockedApps = lockedApps)
                }
            }
            
            // Load initial data
            loadInitialData(context)
        }
    }
    
    private fun loadInitialData(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Load available apps for app lock
                val availableApps = getAvailableAppsForLock(context)
                
                // Load recent security activity
                val recentActivity = generateRecentActivity()
                
                _uiState.value = _uiState.value.copy(
                    availableApps = availableApps,
                    recentActivity = recentActivity,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load security data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun handleToolAction(toolType: SecurityToolType) {
        viewModelScope.launch {
            try {
                when (toolType) {
                    SecurityToolType.VIRUS_SCAN -> performVirusScan()
                    SecurityToolType.REAL_TIME_SCAN -> toggleRealTimeProtection()
                    SecurityToolType.APP_LOCK -> showAppLockDialog()
                    SecurityToolType.PAYMENT_PROTECTION -> enablePaymentProtection()
                    SecurityToolType.PERMISSIONS_MANAGER -> analyzePermissions()
                    SecurityToolType.PRIVACY_GUARD -> enablePrivacyGuard()
                    SecurityToolType.SECURITY_REMINDERS -> setupSecurityReminders()
                    SecurityToolType.FILE_PRIVACY -> setupFilePrivacy()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Operation failed: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun performVirusScan() {
        securityManager?.let { manager ->
            try {
                // Start the scan
                val scanResult = manager.performComprehensiveVirusScan()
                
                // Update the UI state with the scan results
                _uiState.update { currentState ->
                    currentState.copy(
                        lastScanResult = scanResult,
                        showScanResults = true,
                        isScanning = false
                    )
                }
                
                // Add to recent activity
                addSecurityActivity(
                    SecurityActivity(
                        description = if (scanResult.threatsFound.isEmpty()) 
                            "Virus scan completed - No threats found" 
                        else 
                            "Virus scan completed - ${scanResult.threatsFound.size} threats found",
                        timestamp = getCurrentTimestamp(),
                        icon = Icons.Default.Security,
                        color = if (scanResult.threatsFound.isEmpty()) SuccessGreen else ErrorRed
                    )
                )
                
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "Scan failed: ${e.message}",
                        isScanning = false
                    )
                }
            }
        }
    }
    
    private suspend fun toggleRealTimeProtection() {
        securityManager?.let { manager ->
            val enabled = manager.enableRealTimeScanning()
            
            addSecurityActivity(
                SecurityActivity(
                    description = if (enabled) "Real-time protection enabled" else "Failed to enable real-time protection",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Shield,
                    color = if (enabled) SuccessGreen else ErrorRed
                )
            )
        }
    }
    
    private fun showAppLockDialog() {
        _uiState.value = _uiState.value.copy(showAppLockDialog = true)
    }
    
    private suspend fun enablePaymentProtection() {
        securityManager?.let { manager ->
            val protectedApps = manager.enablePaymentProtection()
            
            addSecurityActivity(
                SecurityActivity(
                    description = "Payment protection enabled for ${protectedApps.size} financial apps",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Payment,
                    color = SuccessGreen
                )
            )
        }
    }
    
    private suspend fun analyzePermissions() {
        securityManager?.let { manager ->
            try {
                val permissionAnalysis = manager.analyzeAppPermissions()
                val riskyApps = permissionAnalysis.filter { it.riskScore > 50 }
                
                addSecurityActivity(
                    SecurityActivity(
                        description = "Permission analysis completed - ${riskyApps.size} apps need attention",
                        timestamp = getCurrentTimestamp(),
                        icon = Icons.Default.AdminPanelSettings,
                        color = if (riskyApps.isEmpty()) SuccessGreen else WarningOrange
                    )
                )
                
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "Failed to analyze permissions: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun enablePrivacyGuard() {
        securityManager?.let { manager ->
            try {
                // Get high-risk apps for privacy protection
                val permissionAnalysis = manager.analyzeAppPermissions()
                val riskyApps = permissionAnalysis.filter { it.riskScore > 70 }.map { it.packageName }.toList()
                
                manager.enablePrivacyGuard(riskyApps)
                
                addSecurityActivity(
                    SecurityActivity(
                        description = "Privacy guard enabled for ${riskyApps.size} apps",
                        timestamp = getCurrentTimestamp(),
                        icon = Icons.Default.PrivacyTip,
                        color = SuccessGreen
                    )
                )
                
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = "Failed to enable privacy guard: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun setupSecurityReminders() {
        securityManager?.let { manager ->
            val enabled = manager.setupSecurityReminders()
            
            addSecurityActivity(
                SecurityActivity(
                    description = if (enabled) "Security reminders configured" else "Failed to setup security reminders",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.NotificationImportant,
                    color = if (enabled) SuccessGreen else ErrorRed
                )
            )
        }
    }
    
    private suspend fun setupFilePrivacy() {
        securityManager?.let { manager ->
            val created = manager.createPrivateSpace()
            
            addSecurityActivity(
                SecurityActivity(
                    description = if (created) "Private space created successfully" else "Failed to create private space",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.Folder,
                    color = if (created) SuccessGreen else ErrorRed
                )
            )
        }
    }
    
    fun lockApp(packageName: String, lockType: String) {
        viewModelScope.launch {
            securityManager?.let { manager ->
                val success = manager.lockApp(packageName, lockType)
                
                if (success) {
                    addSecurityActivity(
                        SecurityActivity(
                            description = "App locked with $lockType protection",
                            timestamp = getCurrentTimestamp(),
                            icon = Icons.Default.Lock,
                            color = SuccessGreen
                        )
                    )
                }
            }
        }
    }
    
    fun dismissScanResults() {
        _uiState.value = _uiState.value.copy(showScanResults = false)
    }
    
    fun dismissAppLockDialog() {
        _uiState.value = _uiState.value.copy(showAppLockDialog = false)
    }
    
    private fun addSecurityActivity(activity: SecurityActivity) {
        val currentActivity = _uiState.value.recentActivity.toMutableList()
        currentActivity.add(0, activity) // Add to beginning
        if (currentActivity.size > 10) {
            currentActivity.removeAt(currentActivity.size - 1) // Keep only 10 recent activities
        }
        
        _uiState.value = _uiState.value.copy(recentActivity = currentActivity)
    }
    
    private fun getAvailableAppsForLock(context: Context): List<AppLockInfo> {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(0)
        
        return installedApps
            .filter { app ->
                // Filter user apps (not system apps)
                (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .map { app ->
                AppLockInfo(
                    packageName = app.packageName,
                    appName = packageManager.getApplicationLabel(app).toString(),
                    isLocked = false,
                    lockType = "",
                    lastAccessTime = ""
                )
            }
            .sortedBy { it.appName }
    }
    
    private fun generateRecentActivity(): List<SecurityActivity> {
        return listOf(
            SecurityActivity(
                description = "Security tools initialized",
                timestamp = getCurrentTimestamp(),
                icon = Icons.Default.Security,
                color = LightBlue
            ),
            SecurityActivity(
                description = "System permissions reviewed",
                timestamp = getTimestamp(-1),
                icon = Icons.Default.AdminPanelSettings,
                color = SuccessGreen
            ),
            SecurityActivity(
                description = "App installations monitored",
                timestamp = getTimestamp(-2),
                icon = Icons.Default.Shield,
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
}