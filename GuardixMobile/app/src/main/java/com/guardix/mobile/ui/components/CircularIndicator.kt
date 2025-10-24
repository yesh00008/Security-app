package com.guardix.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardix.mobile.ui.theme.*

@Composable
fun CircularSecurityIndicator(
    modifier: Modifier = Modifier,
    progress: Float = 0.85f,
    onClick: () -> Unit = {}
) {
    var animatedProgress by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val isScanning = progress < 1.0f && progress > 0f && (progress * 1000) % 1 != 0f
    
    // Animation for progress
    LaunchedEffect(progress) {
        animatedProgress = progress
    }
    
    // Scanning animation
    val scanningRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val animatedScore by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "score"
    )
    
    Box(
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f - 20.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Background circle
            drawCircle(
                color = GrayLight,
                radius = radius,
                center = center,
                style = Stroke(width = 12.dp.toPx())
            )
            
            // Progress arc
            val sweepAngle = animatedScore * 360f
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    LightBlue,
                    Cyan,
                    LightBlueDark,
                    LightBlue
                ),
                center = center
            )
            
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = 12.dp.toPx(),
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            // Scanning indicator
            if (isScanning) {
                drawArc(
                    color = WarningOrange,
                    startAngle = scanningRotation - 90f,
                    sweepAngle = 30f,
                    useCenter = false,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    ),
                    topLeft = Offset(center.x - radius + 10.dp.toPx(), center.y - radius + 10.dp.toPx()),
                    size = Size((radius - 10.dp.toPx()) * 2, (radius - 10.dp.toPx()) * 2)
                )
            }
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isScanning) "Scanning..." else "${(animatedScore * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isScanning) WarningOrange else LightBlueDark
            )
            
            Text(
                text = if (isScanning) "Please wait" else "Security Score",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayDark
            )
            
            if (!isScanning) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to scan",
                    style = MaterialTheme.typography.labelMedium,
                    color = LightBlue
                )
            }
        }
    }
}