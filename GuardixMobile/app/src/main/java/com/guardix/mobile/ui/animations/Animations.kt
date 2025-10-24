package com.guardix.mobile.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// Bouncy click animation
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncy_scale"
    )
    
    this
        .scale(scale)
        .clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // Using Material3 default ripple
            onClick = onClick
        )
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

// Shimmer effect for loading states
@Composable
fun shimmerEffect(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

// Slide up animation for cards
@Composable
fun slideUpEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseOutCubic
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 400,
            easing = EaseOut
        )
    )
}

// Pulse animation for notifications
@Composable
fun Modifier.pulseAnimation(
    enabled: Boolean = true,
    scale: Float = 1.1f,
    duration: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (enabled) scale else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    this.graphicsLayer {
        scaleX = pulseScale
        scaleY = pulseScale
    }
}

// Staggered animation for lists
@Composable
fun staggeredSlideIn(index: Int, @Suppress("UNUSED_PARAMETER") totalItems: Int): EnterTransition {
    val delay = (index * 100).coerceAtMost(500)
    return slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delay,
            easing = EaseOutCubic
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delay,
            easing = EaseOut
        )
    )
}