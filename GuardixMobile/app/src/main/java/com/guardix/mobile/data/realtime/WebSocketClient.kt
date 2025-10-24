package com.guardix.mobile.data.realtime

import android.util.Log
import com.guardix.mobile.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketClient(
    private val token: String? = null
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "WebSocketClient"
    
    // Base URL for WebSocket connection
    private val baseUrl = BuildConfig.API_BASE_URL.replace("http", "ws")
    
    // OkHttp client for WebSocket connection
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSocket
        .build()
    
    // Moshi for JSON parsing
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // WebSocket instance
    private var webSocket: WebSocket? = null
    
    // Connection status
    private var isConnected = false
    
    // Shared flows for different data types
    private val _systemMetricsFlow = MutableSharedFlow<SystemMetrics>(replay = 1)
    val systemMetricsFlow: SharedFlow<SystemMetrics> = _systemMetricsFlow
    
    private val _networkStatusFlow = MutableSharedFlow<NetworkStatus>(replay = 1)
    val networkStatusFlow: SharedFlow<NetworkStatus> = _networkStatusFlow
    
    private val _securityEventsFlow = MutableSharedFlow<SecurityEvent>(replay = 1)
    val securityEventsFlow: SharedFlow<SecurityEvent> = _securityEventsFlow
    
    private val _processInfoFlow = MutableSharedFlow<List<ProcessInfo>>(replay = 1)
    val processInfoFlow: SharedFlow<List<ProcessInfo>> = _processInfoFlow
    
    /**
     * Connect to the WebSocket server
     */
    fun connect(dataTypes: List<String> = listOf("all")) {
        if (isConnected) {
            Log.d(TAG, "Already connected to WebSocket")
            return
        }
        
        val url = buildUrl(dataTypes)
        val request = Request.Builder()
            .url(url)
            .build()
        
        webSocket = client.newWebSocket(request, WebSocketHandler())
        Log.d(TAG, "Connecting to WebSocket: $url")
    }
    
    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        isConnected = false
        Log.d(TAG, "Disconnected from WebSocket")
    }
    
    /**
     * Build the WebSocket URL with query parameters
     */
    private fun buildUrl(dataTypes: List<String>): String {
        val dataTypesParam = dataTypes.joinToString(",")
        val url = "$baseUrl/realtime/ws?data_types=$dataTypesParam"
        
        return if (token != null) {
            "$url&token=$token"
        } else {
            url
        }
    }
    
    /**
     * WebSocket event handler
     */
    private inner class WebSocketHandler : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            isConnected = true
            Log.d(TAG, "WebSocket connection established")
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received message: $text")
            
            try {
                val messageAdapter = moshi.adapter(RealtimeMessage::class.java)
                val message = messageAdapter.fromJson(text)
                
                message?.let {
                    processMessage(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message: ${e.message}")
            }
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code - $reason")
            webSocket.close(1000, null)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            isConnected = false
            Log.d(TAG, "WebSocket closed: $code - $reason")
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            isConnected = false
            Log.e(TAG, "WebSocket failure: ${t.message}")
        }
    }
    
    /**
     * Process incoming WebSocket messages
     */
    private fun processMessage(message: RealtimeMessage) {
        coroutineScope.launch(Dispatchers.IO) {
            when (message.type) {
                "system_metrics" -> {
                    val adapter = moshi.adapter(SystemMetrics::class.java)
                    val jsonString = moshi.adapter(Any::class.java).toJson(message.data)
                    val metrics = adapter.fromJson(jsonString)
                    metrics?.let { _systemMetricsFlow.emit(it) }
                }
                
                "network_status" -> {
                    val adapter = moshi.adapter(NetworkStatus::class.java)
                    val jsonString = moshi.adapter(Any::class.java).toJson(message.data)
                    val status = adapter.fromJson(jsonString)
                    status?.let { _networkStatusFlow.emit(it) }
                }
                
                "security_events" -> {
                    val adapter = moshi.adapter(SecurityEvent::class.java)
                    val jsonString = moshi.adapter(Any::class.java).toJson(message.data)
                    val event = adapter.fromJson(jsonString)
                    event?.let { _securityEventsFlow.emit(it) }
                }
                
                "process_list" -> {
                    val adapter = moshi.adapter<List<ProcessInfo>>(
                        Types.newParameterizedType(List::class.java, ProcessInfo::class.java)
                    )
                    val jsonString = moshi.adapter(Any::class.java).toJson(message.data)
                    val processes = adapter.fromJson(jsonString)
                    processes?.let { _processInfoFlow.emit(it) }
                }
            }
        }
    }
}