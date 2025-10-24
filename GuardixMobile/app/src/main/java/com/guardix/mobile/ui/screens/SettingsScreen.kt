package com.guardix.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guardix.mobile.data.*
import com.guardix.mobile.ui.components.NeumorphicCard
import com.guardix.mobile.ui.theme.*

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val hasSwitch: Boolean = false,
    val switchState: Boolean = false,
    val hasArrow: Boolean = true,
    val onSwitchToggle: (Boolean) -> Unit = {},
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    
    // Initialize managers
    val securityManager = remember { SecurityManager(context) }
    val privacyManager = remember { PrivacyManager(context) }
    
    // State variables
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoScanEnabled by remember { mutableStateOf(true) }
    var biometricsEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    
    val settingsSections = listOf(
        SettingsSection(
            title = "Security",
            items = listOf(
                SettingsItem(
                    title = "Auto Scan",
                    subtitle = "Automatically scan for threats",
                    icon = Icons.Default.Security,
                    hasSwitch = true,
                    switchState = autoScanEnabled,
                    hasArrow = false,
                    onSwitchToggle = { autoScanEnabled = it }
                ),
                SettingsItem(
                    title = "Scan Schedule",
                    subtitle = "Daily at 2:00 AM",
                    icon = Icons.Default.Schedule,
                    hasArrow = true,
                    onClick = {
                        dialogTitle = "Scan Schedule"
                        dialogMessage = "Current schedule: Daily at 2:00 AM\n\nOptions:\n• Daily\n• Weekly\n• Custom"
                        showDialog = true
                    }
                ),
                SettingsItem(
                    title = "Quarantine",
                    subtitle = "Manage quarantined files",
                    icon = Icons.Default.FolderSpecial,
                    hasArrow = true,
                    onClick = {
                        dialogTitle = "Quarantine"
                        dialogMessage = "Quarantined files: 3\n\n• suspicious_app.apk\n• adware_component.so\n• tracking_script.js"
                        showDialog = true
                    }
                ),
                SettingsItem(
                    title = "Trusted Apps",
                    subtitle = "Apps excluded from scanning",
                    icon = Icons.Default.VerifiedUser,
                    hasArrow = true,
                    onClick = {
                        val trustedApps = securityManager.getInstalledApps().filter { !it.isSystemApp }.take(3)
                        dialogTitle = "Trusted Apps"
                        dialogMessage = "Apps excluded from scanning:\n" +
                                trustedApps.joinToString("\n") { "• ${it.name}" }
                        showDialog = true
                    }
                )
            )
        ),
        SettingsSection(
            title = "Privacy",
            items = listOf(
                SettingsItem(
                    title = "Biometric Lock",
                    subtitle = "Use fingerprint or face unlock",
                    icon = Icons.Default.Fingerprint,
                    hasSwitch = true,
                    switchState = biometricsEnabled,
                    hasArrow = false,
                    onSwitchToggle = { biometricsEnabled = it }
                ),
                SettingsItem(
                    title = "App Permissions",
                    subtitle = "Review app permissions",
                    icon = Icons.Default.AdminPanelSettings,
                    hasArrow = true,
                    onClick = {
                        val riskyApps = privacyManager.getPermissionRiskyApps()
                        dialogTitle = "App Permissions"
                        dialogMessage = "Apps with high-risk permissions:\n" +
                                riskyApps.joinToString("\n") { "• ${it.name}" } +
                                "\n\nRecommend reviewing these permissions"
                        showDialog = true
                    }
                ),
                SettingsItem(
                    title = "Data Usage",
                    subtitle = "Monitor data consumption",
                    icon = Icons.Default.DataUsage,
                    hasArrow = true,
                    onClick = {
                        val systemInfo = securityManager.getSystemInfo()
                        dialogTitle = "Data Usage"
                        dialogMessage = "Network usage this month:\n" +
                                "Total: ${formatFileSize(systemInfo.networkUsage)}\n" +
                                "WiFi: ${formatFileSize((systemInfo.networkUsage * 0.7f).toLong())}\n" +
                                "Mobile: ${formatFileSize((systemInfo.networkUsage * 0.3f).toLong())}"
                        showDialog = true
                    }
                )
            )
        ),
        SettingsSection(
            title = "Notifications",
            items = listOf(
                SettingsItem(
                    title = "Push Notifications",
                    subtitle = "Receive security alerts",
                    icon = Icons.Default.Notifications,
                    hasSwitch = true,
                    switchState = notificationsEnabled,
                    hasArrow = false,
                    onSwitchToggle = { notificationsEnabled = it }
                ),
                SettingsItem(
                    title = "Scan Reports",
                    subtitle = "Weekly security reports",
                    icon = Icons.Default.Assessment,
                    hasArrow = true,
                    onClick = {
                        dialogTitle = "Weekly Report"
                        dialogMessage = "This week:\n• 5 scans completed\n• 2 threats blocked\n• Security score: ${(securityManager.securityScore.value * 100).toInt()}%\n• Performance improved by 15%"
                        showDialog = true
                    }
                )
            )
        ),
        SettingsSection(
            title = "Appearance",
            items = listOf(
                SettingsItem(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    icon = Icons.Default.DarkMode,
                    hasSwitch = true,
                    switchState = darkModeEnabled,
                    hasArrow = false,
                    onSwitchToggle = { darkModeEnabled = it }
                ),
                SettingsItem(
                    title = "Language",
                    subtitle = "English",
                    icon = Icons.Default.Language,
                    hasArrow = true,
                    onClick = {
                        dialogTitle = "Language"
                        dialogMessage = "Available languages:\n• English (Current)\n• Spanish\n• French\n• German\n• Chinese\n• Japanese"
                        showDialog = true
                    }
                )
            )
        ),
        SettingsSection(
            title = "About",
            items = listOf(
                SettingsItem(
                    title = "App Version",
                    subtitle = "1.0.0 (Build 1)",
                    icon = Icons.Default.Info,
                    hasArrow = false
                ),
                SettingsItem(
                    title = "Check for Updates",
                    subtitle = "Auto-update enabled",
                    icon = Icons.Default.SystemUpdate,
                    hasArrow = true,
                    onClick = {
                        dialogTitle = "Updates"
                        dialogMessage = "You have the latest version!\n\nVersion: 1.0.0 (Build 1)\nLast updated: Today\nAuto-update: Enabled"
                        showDialog = true
                    }
                ),
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    icon = Icons.Default.Policy,
                    hasArrow = true,
                    onClick = {
                        dialogTitle = "Privacy Policy"
                        dialogMessage = "Guardix Mobile Privacy Policy\n\n• We don't collect personal data\n• All scans are performed locally\n• No data is shared with third parties\n• Your privacy is our priority"
                        showDialog = true
                    }
                ),
                SettingsItem(
                    title = "Support",
                    subtitle = "Get help and report issues",
                    icon = Icons.Default.Info,
                    hasArrow = true,
                    onClick = {
                        dialogTitle = "Support"
                        dialogMessage = "Need help?\n\n• Email: support@guardix.com\n• FAQ: Available in app\n• Report issues: Bug report form\n• Community: Guardix forums"
                        showDialog = true
                    }
                )
            )
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.05f),
                        BackgroundPrimary
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = GrayText,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Settings sections
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            settingsSections.forEach { section ->
                item {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LightBlue,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(section.items) { item ->
                    SettingsItemCard(item = item)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Settings Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { 
                Text(
                    dialogTitle,
                    color = GrayText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Text(
                    dialogMessage,
                    color = GrayText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", color = LightBlue)
                }
            },
            containerColor = BackgroundSecondary
        )
    }
}

@Composable
private fun SettingsItemCard(item: SettingsItem) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                LightBlue.copy(alpha = 0.15f),
                                Cyan.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = LightBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = GrayText
                )
                
                if (item.subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayDark
                    )
                }
            }
            
            // Switch or Arrow
            if (item.hasSwitch) {
                Switch(
                    checked = item.switchState,
                    onCheckedChange = item.onSwitchToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundSecondary,
                        checkedTrackColor = LightBlue,
                        uncheckedThumbColor = GrayMedium,
                        uncheckedTrackColor = GrayLight
                    )
                )
            } else if (item.hasArrow) {
                IconButton(
                    onClick = item.onClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open ${item.title}",
                        tint = GrayDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}