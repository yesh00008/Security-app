package com.guardix.mobile.data.remote

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.guardix.mobile.BuildConfig
import com.guardix.mobile.data.AppInfo
import com.guardix.mobile.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class GuardixRepository(
    context: Context,
    private val api: GuardixApiService = GuardixApiClient.service
) {

    private val appContext = context.applicationContext
    private val tokenStore = TokenStore(appContext)

    private suspend fun ensureToken(): String = withContext(Dispatchers.IO) {
        val existing = tokenStore.getToken()
        if (!existing.isNullOrEmpty() && tokenStore.isValid()) {
            return@withContext existing
        }

        val userId = resolveUserId()
        val response = api.login(TokenRequestDto(userId = userId))
        tokenStore.setToken(response.accessToken)
        response.accessToken
    }

    private fun resolveUserId(): String {
        val androidId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: BuildConfig.DEFAULT_USER_ID.ifEmpty { Build.MODEL ?: "guardix-device" }
    }

    suspend fun scanApplication(appInfo: AppInfo, permissions: List<String>): ApkScanResponseDto? {
        return runCatching {
            val token = ensureToken()
            val features = buildFeatures(appInfo, permissions)
            api.scanApk("Bearer $token", ApkScanRequestDto(appInfo.packageName, features))
        }.getOrNull()
    }

    suspend fun scanPhishing(url: String?, text: String?): PhishingScanResponseDto? {
        return runCatching {
            val token = ensureToken()
            api.scanPhishing("Bearer $token", PhishingScanRequestDto(url = url, text = text))
        }.getOrNull()
    }

    suspend fun verifyBiometric(sample: BiometricSampleDto): BiometricAuthResponseDto? {
        return runCatching {
            val token = ensureToken()
            val userId = resolveUserId()
            api.biometricAuth("Bearer $token", BiometricAuthRequestDto(userId = userId, sample = sample))
        }.getOrNull()
    }

    suspend fun monitorNetwork(traffic: List<TrafficRecordDto>): IDSResponseDto? {
        if (traffic.isEmpty()) return null
        return runCatching {
            val token = ensureToken()
            api.monitorIds("Bearer $token", IDSRequestDto(traffic = traffic))
        }.getOrNull()
    }

    suspend fun fetchModelSummary(): ModelSummaryDto? {
        return runCatching {
            val token = ensureToken()
            api.models("Bearer $token")
        }.getOrNull()
    }

    suspend fun optimizePerformance(request: PerformanceOptimizeRequestDto = PerformanceOptimizeRequestDto()): PerformanceOptimizeResponseDto? {
        return runCatching {
            val token = ensureToken()
            api.optimizePerformance("Bearer $token", request)
        }.getOrNull()
    }

    suspend fun memoryStatus(): MemoryStatusResponseDto? {
        return runCatching {
            val token = ensureToken()
            api.memoryStatus("Bearer $token")
        }.getOrNull()
    }

    suspend fun thermalStatus(): ThermalStatusResponseDto? {
        return runCatching {
            val token = ensureToken()
            api.thermalStatus("Bearer $token")
        }.getOrNull()
    }

    suspend fun networkUsage(): NetworkUsageResponseDto? {
        return runCatching {
            val token = ensureToken()
            api.networkUsage("Bearer $token")
        }.getOrNull()
    }

    suspend fun storageOverview(): StorageOverviewResponseDto? {
        return runCatching {
            val token = ensureToken()
            api.storageOverview("Bearer $token")
        }.getOrNull()
    }

    suspend fun behaviorAnomaly(metrics: List<Double>): AnomalyResponseDto? {
        if (metrics.isEmpty()) return null
        return runCatching {
            val token = ensureToken()
            api.behaviorAnomaly("Bearer $token", AnomalyRequestDto(metrics))
        }.getOrNull()
    }

    suspend fun systemAnomaly(metrics: List<Double>): AnomalyResponseDto? {
        if (metrics.isEmpty()) return null
        return runCatching {
            val token = ensureToken()
            api.systemAnomaly("Bearer $token", AnomalyRequestDto(metrics))
        }.getOrNull()
    }

    suspend fun securityOverview(): SecurityOverviewResponseDto? {
        return runCatching {
            val token = ensureToken()
            api.securityOverview("Bearer $token")
        }.getOrNull()
    }

    suspend fun health(): HealthResponseDto? = runCatching { api.health() }.getOrNull()

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
