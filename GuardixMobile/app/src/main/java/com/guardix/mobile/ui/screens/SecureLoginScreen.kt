package com.guardix.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.guardix.mobile.data.remote.AuthenticationManager
import com.guardix.mobile.ui.components.NeumorphicCard
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureLoginScreen(
    onLoginSuccess: () -> Unit,
    onSkipLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthenticationManager(context) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var biometricStatus by remember { mutableStateOf<AuthenticationManager.BiometricStatus?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    
    // Check biometric availability on startup
    LaunchedEffect(Unit) {
        biometricStatus = authManager.isBiometricAvailable()
        
        // Auto-check for existing valid token
        if (authManager.isTokenValid()) {
            onLoginSuccess()
        }
    }
    
    // Animated logo rotation
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        BackgroundPrimary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo and Branding
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                LightBlue.copy(alpha = 0.2f),
                                Cyan.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Guardix Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer { 
                            rotationZ = if (isLoading) rotationAngle else 0f
                        },
                    tint = LightBlue
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Guardix Security",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = GrayText,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Advanced Mobile Protection",
                style = MaterialTheme.typography.titleMedium,
                color = GrayDark,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Authentication Card
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Secure Authentication",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GrayText
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Use biometric authentication for secure access",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayDark,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Biometric Status Indicator
                    BiometricStatusIndicator(
                        status = biometricStatus,
                        isAuthenticating = isAuthenticating
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Authentication Buttons
                    when (biometricStatus) {
                        AuthenticationManager.BiometricStatus.AVAILABLE -> {
                            Button(
                                onClick = {
                                    if (!isAuthenticating) {
                                        isAuthenticating = true
                                        scope.launch {
                                            try {
                                                authManager.authenticateWithBiometric(
                                                    activity = context as FragmentActivity,
                                                    onSuccess = { token ->
                                                        scope.launch {
                                                            authManager.storeSecureToken(token)
                                                            isAuthenticating = false
                                                            onLoginSuccess()
                                                        }
                                                    },
                                                    onError = { error ->
                                                        isAuthenticating = false
                                                        errorMessage = error
                                                        showError = true
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                isAuthenticating = false
                                                errorMessage = "Authentication failed: ${e.message}"
                                                showError = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !isAuthenticating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LightBlue,
                                    contentColor = Color.White,
                                    disabledContainerColor = GrayDark
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (isAuthenticating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Authenticating...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Authenticate with Biometric",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        
                        AuthenticationManager.BiometricStatus.NOT_ENROLLED -> {
                            OutlinedButton(
                                onClick = { /* Navigate to biometric setup */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = WarningOrange
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Set up Biometric")
                            }
                        }
                        
                        else -> {
                            Button(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        // Simulate basic authentication
                                        kotlinx.coroutines.delay(1500)
                                        isLoading = false
                                        onLoginSuccess()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrayDark,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Connecting...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Continue Securely")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Skip option for demo/testing
                    TextButton(
                        onClick = onSkipLogin,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = GrayDark
                        )
                    ) {
                        Text("Skip for now")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Security Features List
            SecurityFeaturesList()
        }
    }
    
    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Authentication Error",
                        color = GrayText,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    errorMessage,
                    color = GrayText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showError = false }
                ) {
                    Text("OK", color = LightBlue)
                }
            },
            containerColor = BackgroundSecondary
        )
    }
}

@Composable
private fun BiometricStatusIndicator(
    status: AuthenticationManager.BiometricStatus?,
    isAuthenticating: Boolean
) {
    val (icon, color, message) = when (status) {
        AuthenticationManager.BiometricStatus.AVAILABLE -> Triple(
            Icons.Default.Fingerprint,
            SuccessGreen,
            "Biometric authentication ready"
        )
        AuthenticationManager.BiometricStatus.NO_HARDWARE -> Triple(
            Icons.Default.ErrorOutline,
            ErrorRed,
            "No biometric hardware available"
        )
        AuthenticationManager.BiometricStatus.UNAVAILABLE -> Triple(
            Icons.Default.Warning,
            WarningOrange,
            "Biometric hardware unavailable"
        )
        AuthenticationManager.BiometricStatus.NOT_ENROLLED -> Triple(
            Icons.Default.Settings,
            WarningOrange,
            "No biometric data enrolled"
        )
        AuthenticationManager.BiometricStatus.UNKNOWN_ERROR -> Triple(
            Icons.Default.Error,
            ErrorRed,
            "Biometric authentication error"
        )
        null -> Triple(
            Icons.Default.HourglassEmpty,
            GrayDark,
            "Checking biometric availability..."
        )
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (isAuthenticating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = color,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = if (isAuthenticating) "Authenticating..." else message,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SecurityFeaturesList() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Security Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = GrayText,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val features = listOf(
            "End-to-end encryption" to Icons.Default.Lock,
            "Biometric authentication" to Icons.Default.Fingerprint,
            "Real-time threat detection" to Icons.Default.Shield,
            "Secure token storage" to Icons.Default.Key
        )
        
        features.forEach { (feature, icon) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LightBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayDark
                )
            }
        }
    }
}