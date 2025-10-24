package com.guardix.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    onPrimary = BackgroundSecondary,
    primaryContainer = BackgroundCard,
    onPrimaryContainer = GrayText,
    secondary = Cyan,
    onSecondary = BackgroundSecondary,
    secondaryContainer = GrayLight,
    onSecondaryContainer = GrayText,
    tertiary = LightBlueDark,
    onTertiary = BackgroundSecondary,
    tertiaryContainer = BackgroundCard,
    onTertiaryContainer = GrayText,
    error = ErrorRed,
    onError = BackgroundSecondary,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = BackgroundPrimary,
    onBackground = GrayText,
    surface = BackgroundSecondary,
    onSurface = GrayText,
    surfaceVariant = GrayLight,
    onSurfaceVariant = GrayDark,
    outline = GrayMedium,
    outlineVariant = GrayLight,
    scrim = Color(0xFF000000),
    inverseSurface = GrayText,
    inverseOnSurface = BackgroundSecondary,
    inversePrimary = LightBlue,
    surfaceDim = GrayLight,
    surfaceBright = BackgroundSecondary,
    surfaceContainerLowest = BackgroundSecondary,
    surfaceContainerLow = BackgroundCard,
    surfaceContainer = GrayLight,
    surfaceContainerHigh = GrayMedium,
    surfaceContainerHighest = GrayDark
)

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = Color(0xFF001D36),
    primaryContainer = Color(0xFF004C6A),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Cyan,
    onSecondary = Color(0xFF001D2A),
    secondaryContainer = Color(0xFF00344D),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = LightBlueDark,
    onTertiary = Color(0xFF001F25),
    tertiaryContainer = Color(0xFF003A42),
    onTertiaryContainer = Color(0xFFB2DFDB),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF0F1419),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CE),
    outline = Color(0xFF8C9198),
    outlineVariant = Color(0xFF42474E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE1E2E8),
    inverseOnSurface = Color(0xFF2E3036),
    inversePrimary = LightBlue
)

@Composable
fun GuardixMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}