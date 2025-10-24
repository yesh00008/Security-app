package com.guardix.mobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSize {
    Compact,    // Phones in portrait
    Medium,     // Phones in landscape, tablets in portrait
    Expanded    // Tablets in landscape, foldables
}

@Composable
fun getWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 600.dp -> WindowSize.Compact
        screenWidth < 840.dp -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
}

@Composable
fun getResponsivePadding(): Dp {
    return when (getWindowSize()) {
        WindowSize.Compact -> 16.dp
        WindowSize.Medium -> 24.dp
        WindowSize.Expanded -> 32.dp
    }
}

@Composable
fun getResponsiveColumns(): Int {
    return when (getWindowSize()) {
        WindowSize.Compact -> 2
        WindowSize.Medium -> 3
        WindowSize.Expanded -> 4
    }
}

@Composable
fun getCardElevation(): Dp {
    return when (getWindowSize()) {
        WindowSize.Compact -> 4.dp
        WindowSize.Medium -> 6.dp
        WindowSize.Expanded -> 8.dp
    }
}