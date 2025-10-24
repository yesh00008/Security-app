package com.guardix.mobile

import androidx.compose.runtime.*
import com.guardix.mobile.ui.navigation.MainNavigation
import androidx.hilt.navigation.compose.hiltViewModel
import com.guardix.mobile.data.realtime.RealtimeConnectionViewModel

@Composable
fun GuardixApp() {
    // Initialize realtime connection once for the app lifecycle
    hiltViewModel<RealtimeConnectionViewModel>()
    MainNavigation()
}
