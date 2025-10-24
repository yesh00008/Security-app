package com.guardix.mobile.data.remote

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guardix.mobile.BuildConfig
import com.guardix.mobile.data.AppInfo
import com.guardix.mobile.data.remote.dto.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "guardix_cache")

@Singleton
class EnhancedGuardixRepository @Inject constructor(
    private val context: Context,
    private val api: GuardixApiService,
    private val networkManager: NetworkManager,
    private val moshi: Moshi
) {

    private val tokenStore = TokenStore(context)
    
    // Cache keys
    private val CACHE_SECURITY_OVERVIEW = stringPreferencesKey("cache_security_overview")
    private val CACHE_MODEL_SUMMARY = stringPreferencesKey("cache_model_summary")
    private val CACHE_MEMORY_STATUS = stringPreferencesKey("cache_memory_status")
    private val CACHE_NETWORK_USAGE = stringPreferencesKey("cache_network_usage")
    private val CACHE_STORAGE_OVERVIEW = stringPreferencesKey("cache_storage_overview")
    
    // Cache timestamps
    private val CACHE_SECURITY_OVERVIEW_TIME = longPreferencesKey("cache_security_overview_time")
    private val CACHE_MODEL_SUMMARY_TIME = longPreferencesKey("cache_model_summary_time")
    private val CACHE_MEMORY_STATUS_TIME = longPreferencesKey("cache_memory_status_time")
    private val CACHE_NETWORK_USAGE_TIME = longPreferencesKey("cache_network_usage_time")
    private val CACHE_STORAGE_OVERVIEW_TIME = longPreferencesKey("cache_storage_overview_time")
    
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    private suspend fun ensureToken(): String = withContext(Dispatchers.IO) {
        val existing = tokenStore.getToken()
        if (!existing.isNullOrEmpty() && tokenStore.isValid()) {
            return@withContext existing
        }

        val userId = resolveUserId()
        val result = networkManager.executeWithRetry {
            api.login(TokenRequestDto(userId = userId))
        }
        
        if (result.isSuccess) {
            tokenStore.setToken(result.data!!.accessToken)
            result.data.accessToken
        } else {
            throw Exception("Failed to authenticate: ${result.error?.message}")
        }
    }

    private fun resolveUserId(): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: BuildConfig.DEFAULT_USER_ID.ifEmpty { Build.MODEL ?: "guardix-device" }
    }

    /**
     * Scan application with enhanced error handling and caching
     */
    suspend fun scanApplication(
        appInfo: AppInfo, 
        permissions: List<String>
    ): NetworkManager.NetworkResult<ApkScanResponseDto> = withContext(Dispatchers.IO) {
        networkManager.executeWithRetry {
            val token = ensureToken()
            val features = buildFeatures(appInfo, permissions)
            api.scanApk("Bearer $token", ApkScanRequestDto(appInfo.packageName, features))
        }
    }

    /**
     * Scan for phishing with retry logic
     */
    suspend fun scanPhishing(
        url: String?, 
        text: String?
    ): NetworkManager.NetworkResult<PhishingScanResponseDto> = withContext(Dispatchers.IO) {
        networkManager.executeWithRetry {
            val token = ensureToken()
            api.scanPhishing("Bearer $token", PhishingScanRequestDto(url = url, text = text))
        }
    }

    /**
     * Verify biometric with enhanced error handling
     */
    suspend fun verifyBiometric(
        sample: BiometricSampleDto
    ): NetworkManager.NetworkResult<BiometricAuthResponseDto> = withContext(Dispatchers.IO) {
        networkManager.executeWithRetry {
            val token = ensureToken()
            val userId = resolveUserId()
            api.biometricAuth("Bearer $token", BiometricAuthRequestDto(userId = userId, sample = sample))
        }
    }

    /**
     * Monitor network with retry logic
     */
    suspend fun monitorNetwork(
        traffic: List<TrafficRecordDto>
    ): NetworkManager.NetworkResult<IDSResponseDto> = withContext(Dispatchers.IO) {
        if (traffic.isEmpty()) {
            return@withContext NetworkManager.NetworkResult(
                error = NetworkManager.NetworkError.ApiError("No traffic data provided")
            )
        }
        
        networkManager.executeWithRetry {
            val token = ensureToken()
            api.monitorIds("Bearer $token", IDSRequestDto(traffic = traffic))
        }
    }

    /**
     * Fetch model summary with caching
     */
    suspend fun fetchModelSummary(): NetworkManager.NetworkResult<ModelSummaryDto> = withContext(Dispatchers.IO) {
        // Try cache first
        val cachedResult = getCachedData<ModelSummaryDto>(
            CACHE_MODEL_SUMMARY,
            CACHE_MODEL_SUMMARY_TIME,
            ModelSummaryDto::class.java
        )
        
        if (cachedResult != null) {
            return@withContext NetworkManager.NetworkResult(data = cachedResult, isCached = true)
        }
        
        // Fetch from network
        val result = networkManager.executeWithRetry {
            val token = ensureToken()
            api.models("Bearer $token")
        }
        
        // Cache successful result
        if (result.isSuccess) {
            cacheData(CACHE_MODEL_SUMMARY, CACHE_MODEL_SUMMARY_TIME, result.data!!)
        }
        
        result
    }

    /**
     * Get security overview with caching
     */
    suspend fun getSecurityOverview(): NetworkManager.NetworkResult<SecurityOverviewResponseDto> = withContext(Dispatchers.IO) {
        // Try cache first
        val cachedResult = getCachedData<SecurityOverviewResponseDto>(
            CACHE_SECURITY_OVERVIEW,
            CACHE_SECURITY_OVERVIEW_TIME,
            SecurityOverviewResponseDto::class.java
        )
        
        if (cachedResult != null) {
            return@withContext NetworkManager.NetworkResult(data = cachedResult, isCached = true)
        }
        
        // Fetch from network
        val result = networkManager.executeWithRetry {
            val token = ensureToken()
            api.securityOverview("Bearer $token")
        }
        
        // Cache successful result
        if (result.isSuccess) {
            cacheData(CACHE_SECURITY_OVERVIEW, CACHE_SECURITY_OVERVIEW_TIME, result.data!!)
        }
        
        result
    }

    /**
     * Optimize performance with enhanced feedback
     */
    suspend fun optimizePerformance(
        request: PerformanceOptimizeRequestDto = PerformanceOptimizeRequestDto()
    ): NetworkManager.NetworkResult<PerformanceOptimizeResponseDto> = withContext(Dispatchers.IO) {
        networkManager.executeWithRetry {
            val token = ensureToken()
            api.optimizePerformance("Bearer $token", request)
        }
    }

    /**
     * Get memory status with caching
     */
    suspend fun getMemoryStatus(): NetworkManager.NetworkResult<MemoryStatusResponseDto> = withContext(Dispatchers.IO) {
        // Try cache first (shorter cache for dynamic data)
        val cachedResult = getCachedData<MemoryStatusResponseDto>(
            CACHE_MEMORY_STATUS,
            CACHE_MEMORY_STATUS_TIME,
            MemoryStatusResponseDto::class.java,
            cacheValidityMs = 30 * 1000L // 30 seconds for memory data
        )
        
        if (cachedResult != null) {
            return@withContext NetworkManager.NetworkResult(data = cachedResult, isCached = true)
        }
        
        // Fetch from network
        val result = networkManager.executeWithRetry {
            val token = ensureToken()
            api.memoryStatus("Bearer $token")
        }
        
        // Cache successful result
        if (result.isSuccess) {
            cacheData(CACHE_MEMORY_STATUS, CACHE_MEMORY_STATUS_TIME, result.data!!)
        }
        
        result
    }

    /**
     * Get thermal status
     */
    suspend fun getThermalStatus(): NetworkManager.NetworkResult<ThermalStatusResponseDto> = withContext(Dispatchers.IO) {
        networkManager.executeWithRetry {
            val token = ensureToken()
            api.thermalStatus("Bearer $token")
        }
    }

    /**
     * Get network usage with caching
     */
    suspend fun getNetworkUsage(): NetworkManager.NetworkResult<NetworkUsageResponseDto> = withContext(Dispatchers.IO) {
        val cachedResult = getCachedData<NetworkUsageResponseDto>(
            CACHE_NETWORK_USAGE,
            CACHE_NETWORK_USAGE_TIME,
            NetworkUsageResponseDto::class.java,
            cacheValidityMs = 60 * 1000L // 1 minute for network data
        )
        
        if (cachedResult != null) {
            return@withContext NetworkManager.NetworkResult(data = cachedResult, isCached = true)
        }
        
        val result = networkManager.executeWithRetry {
            val token = ensureToken()
            api.networkUsage("Bearer $token")
        }
        
        if (result.isSuccess) {
            cacheData(CACHE_NETWORK_USAGE, CACHE_NETWORK_USAGE_TIME, result.data!!)
        }
        
        result
    }

    /**
     * Get storage overview with caching
     */
    suspend fun getStorageOverview(): NetworkManager.NetworkResult<StorageOverviewResponseDto> = withContext(Dispatchers.IO) {
        val cachedResult = getCachedData<StorageOverviewResponseDto>(
            CACHE_STORAGE_OVERVIEW,
            CACHE_STORAGE_OVERVIEW_TIME,
            StorageOverviewResponseDto::class.java
        )
        
        if (cachedResult != null) {
            return@withContext NetworkManager.NetworkResult(data = cachedResult, isCached = true)
        }
        
        val result = networkManager.executeWithRetry {
            val token = ensureToken()
            api.storageOverview("Bearer $token")
        }
        
        if (result.isSuccess) {
            cacheData(CACHE_STORAGE_OVERVIEW, CACHE_STORAGE_OVERVIEW_TIME, result.data!!)
        }
        
        result
    }

    /**
     * Detect behavior anomaly
     */
    suspend fun detectBehaviorAnomaly(
        metrics: List<Double>
    ): NetworkManager.NetworkResult<AnomalyResponseDto> = withContext(Dispatchers.IO) {
        if (metrics.isEmpty()) {
            return@withContext NetworkManager.NetworkResult(
                error = NetworkManager.NetworkError.ApiError("No metrics provided")
            )
        }
        
        networkManager.executeWithRetry {
            val token = ensureToken()
            api.behaviorAnomaly("Bearer $token", AnomalyRequestDto(metrics))
        }
    }

    /**
     * Detect system anomaly
     */
    suspend fun detectSystemAnomaly(
        metrics: List<Double>
    ): NetworkManager.NetworkResult<AnomalyResponseDto> = withContext(Dispatchers.IO) {
        if (metrics.isEmpty()) {
            return@withContext NetworkManager.NetworkResult(
                error = NetworkManager.NetworkError.ApiError("No metrics provided")
            )
        }
        
        networkManager.executeWithRetry {
            val token = ensureToken()
            api.systemAnomaly("Bearer $token", AnomalyRequestDto(metrics))
        }
    }

    /**
     * Health check
     */
    suspend fun getHealthStatus(): NetworkManager.NetworkResult<HealthResponseDto> = withContext(Dispatchers.IO) {
        networkManager.executeWithRetry(maxRetries = 1) { // Quick health check
            api.health()
        }
    }

    // Cache management functions
    private suspend inline fun <reified T> getCachedData(
        key: Preferences.Key<String>,
        timeKey: Preferences.Key<Long>,
        clazz: Class<T>,
        cacheValidityMs: Long = CACHE_DURATION_MS
    ): T? {
        val preferences = context.dataStore.data.first()
        val cachedTime = preferences[timeKey] ?: 0L
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - cachedTime > cacheValidityMs) {
            return null
        }
        
        val cachedJson = preferences[key] ?: return null
        return try {
            val adapter: JsonAdapter<T> = moshi.adapter(clazz)
            adapter.fromJson(cachedJson)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun <T> cacheData(
        key: Preferences.Key<String>,
        timeKey: Preferences.Key<Long>,
        data: T
    ) {
        try {
            val adapter: JsonAdapter<T> = moshi.adapter(data!!::class.java) as JsonAdapter<T>
            val json = adapter.toJson(data)
            
            context.dataStore.edit { preferences ->
                preferences[key] = json
                preferences[timeKey] = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }

    private fun buildFeatures(appInfo: AppInfo, permissions: List<String>): ApkFeatureDto {
        val behaviors = mutableSetOf<String>()
        val apiCalls = mutableSetOf<String>()

        permissions.forEach { permission ->
            val perm = permission.uppercase(Locale.US)
            when {
                "SMS" in perm -> {
                    behaviors.add("sms")
                    apiCalls.add("sendTextMessage")
                }
                "CONTACT" in perm -> {
                    behaviors.add("data_exfil")
                    apiCalls.add("contactsProvider")
                }
                "LOCATION" in perm -> {
                    behaviors.add("gps")
                    apiCalls.add("getLastKnownLocation")
                }
                "RECORD_AUDIO" in perm || "MICROPHONE" in perm -> {
                    behaviors.add("audio_capture")
                    apiCalls.add("MediaRecorder")
                }
                "CAMERA" in perm -> {
                    behaviors.add("camera")
                    apiCalls.add("Camera2")
                }
                "SYSTEM_ALERT_WINDOW" in perm -> {
                    behaviors.add("overlay")
                    apiCalls.add("drawOverlay")
                }
                "PACKAGE_USAGE_STATS" in perm -> {
                    behaviors.add("usage_monitor")
                    apiCalls.add("UsageStatsManager")
                }
                "PHONE" in perm || "CALL" in perm -> {
                    behaviors.add("call_monitor")
                    apiCalls.add("TelephonyManager")
                }
            }
        }

        if (permissions.any { it.contains("INTERNET", ignoreCase = true) }) {
            apiCalls.add("okhttp")
            behaviors.add("net")
        }

        if (permissions.any { it.contains("WRITE_EXTERNAL", ignoreCase = true) }) {
            behaviors.add("storage")
            apiCalls.add("FileOutputStream")
        }

        val metadata = mapOf(
            "app_name" to appInfo.name,
            "package" to appInfo.packageName,
            "is_system" to appInfo.isSystemApp.toString()
        )

        return ApkFeatureDto(
            permissions = permissions,
            apiCalls = apiCalls.toList(),
            behaviors = behaviors.toList(),
            metadata = metadata
        )
    }
}