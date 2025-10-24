package com.guardix.mobile.data.realtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface RealtimeRepository {
    val systemMetricsFlow: Flow<SystemMetrics>
    val securityEventsFlow: Flow<SecurityEvent>
    val processInfoFlow: Flow<List<ProcessInfo>>

    fun connect()
    fun disconnect()
}

class RealtimeRepositoryImpl(
    private val webSocketClient: WebSocketClient
) : RealtimeRepository {
    
    override val systemMetricsFlow: Flow<SystemMetrics> = webSocketClient.systemMetricsFlow
    override val securityEventsFlow: Flow<SecurityEvent> = webSocketClient.securityEventsFlow
    override val processInfoFlow: Flow<List<ProcessInfo>> = webSocketClient.processInfoFlow
    
    override fun connect() {
        webSocketClient.connect()
    }
    
    override fun disconnect() {
        webSocketClient.disconnect()
    }
}

