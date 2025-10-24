package com.guardix.mobile.data.remote

import com.guardix.mobile.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface GuardixApiService {

    @POST("auth/login")
    suspend fun login(@Body request: TokenRequestDto): TokenResponseDto

    @POST("scan/apk")
    suspend fun scanApk(
        @Header("Authorization") token: String,
        @Body request: ApkScanRequestDto
    ): ApkScanResponseDto

    @POST("scan/phishing")
    suspend fun scanPhishing(
        @Header("Authorization") token: String,
        @Body request: PhishingScanRequestDto
    ): PhishingScanResponseDto

    @POST("auth/biometric")
    suspend fun biometricAuth(
        @Header("Authorization") token: String,
        @Body request: BiometricAuthRequestDto
    ): BiometricAuthResponseDto

    @POST("monitor/ids")
    suspend fun monitorIds(
        @Header("Authorization") token: String,
        @Body request: IDSRequestDto
    ): IDSResponseDto

    @GET("models/")
    suspend fun models(
        @Header("Authorization") token: String
    ): ModelSummaryDto

    @POST("performance/one-tap")
    suspend fun optimizePerformance(
        @Header("Authorization") token: String,
        @Body request: PerformanceOptimizeRequestDto
    ): PerformanceOptimizeResponseDto

    @GET("performance/memory")
    suspend fun memoryStatus(
        @Header("Authorization") token: String
    ): MemoryStatusResponseDto

    @GET("performance/thermal")
    suspend fun thermalStatus(
        @Header("Authorization") token: String
    ): ThermalStatusResponseDto

    @GET("network-tools/usage")
    suspend fun networkUsage(
        @Header("Authorization") token: String
    ): NetworkUsageResponseDto

    @GET("storage/storage-overview")
    suspend fun storageOverview(
        @Header("Authorization") token: String
    ): StorageOverviewResponseDto

    @POST("anomaly/behavior")
    suspend fun behaviorAnomaly(
        @Header("Authorization") token: String,
        @Body request: AnomalyRequestDto
    ): AnomalyResponseDto

    @POST("anomaly/system")
    suspend fun systemAnomaly(
        @Header("Authorization") token: String,
        @Body request: AnomalyRequestDto
    ): AnomalyResponseDto

    @GET("security-tools/overview")
    suspend fun securityOverview(
        @Header("Authorization") token: String
    ): SecurityOverviewResponseDto

    @GET("/")
    suspend fun health(): HealthResponseDto
}
