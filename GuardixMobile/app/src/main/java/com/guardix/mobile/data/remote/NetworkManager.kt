package com.guardix.mobile.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.math.pow

/**
 * Enhanced networking manager with retry logic, offline handling, and connection monitoring
 */
class NetworkManager(private val context: Context) {
    
    data class NetworkResult<T>(
        val data: T? = null,
        val error: NetworkError? = null,
        val isSuccess: Boolean = data != null,
        val isCached: Boolean = false
    )
    
    sealed class NetworkError(val message: String, val code: Int? = null) {
        object NoConnection : NetworkError("No internet connection available")
        object Timeout : NetworkError("Request timed out")
        data class ServerError(val statusCode: Int, val errorMessage: String) : NetworkError(errorMessage, statusCode)
        data class ApiError(val errorMessage: String) : NetworkError(errorMessage)
        data class UnknownError(val throwable: Throwable) : NetworkError(throwable.message ?: "Unknown error")
    }
    
    /**
     * Execute network request with automatic retry and error handling
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        baseDelayMs: Long = 1000,
        maxDelayMs: Long = 5000,
        backoffMultiplier: Double = 2.0,
        request: suspend () -> T
    ): NetworkResult<T> {
        if (!isNetworkAvailable()) {
            return NetworkResult(error = NetworkError.NoConnection)
        }
        
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                val result = request()
                return NetworkResult(data = result)
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxRetries) {
                    val delayMs = calculateBackoffDelay(attempt, baseDelayMs, maxDelayMs, backoffMultiplier)
                    delay(delayMs)
                }
            }
        }
        
        val error = when (lastException) {
            is SocketTimeoutException -> NetworkError.Timeout
            is IOException -> NetworkError.NoConnection
            else -> NetworkError.UnknownError(lastException!!)
        }
        
        return NetworkResult(error = error)
    }
    
    /**
     * Check if network is available
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    
    /**
     * Get network connection type
     */
    fun getConnectionType(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "None"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "None"
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }
    
    /**
     * Calculate exponential backoff delay
     */
    private fun calculateBackoffDelay(
        attempt: Int,
        baseDelayMs: Long,
        maxDelayMs: Long,
        backoffMultiplier: Double
    ): Long {
        val exponentialDelay = baseDelayMs * backoffMultiplier.pow(attempt.toDouble()).toLong()
        return minOf(exponentialDelay, maxDelayMs)
    }
}