package com.guardix.mobile.data.realtime

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RealtimeConnectionViewModel @Inject constructor(
    private val realtimeRepository: RealtimeRepository
) : ViewModel() {

    init {
        // Ensure a single WebSocket connection app-wide
        realtimeRepository.connect(listOf("all"))
    }

    override fun onCleared() {
        super.onCleared()
        // Keep connection persistent; do not disconnect here unless desired
        // realtimeRepository.disconnect()
    }
}

