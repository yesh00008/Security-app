package com.guardix.mobile.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.guardix.mobile.ui.screens.*
import com.guardix.mobile.ui.screens.security.SecurityToolsScreen
import com.guardix.mobile.ui.screens.performance.PerformanceToolsScreen
import com.guardix.mobile.ui.screens.network.NetworkToolsScreen
import com.guardix.mobile.ui.screens.storage.StorageToolsScreen
import com.guardix.mobile.ui.screens.tools.*
import com.guardix.mobile.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Tools : Screen("tools", "Tools", Icons.Filled.Build)
    object Scan : Screen("scan", "Scan", Icons.Filled.Security)
    object Reports : Screen("reports", "Reports", Icons.Filled.Analytics)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object SecurityTools : Screen("security_tools", "Security Tools", Icons.Filled.Security)
    object PerformanceTools : Screen("performance_tools", "Performance Tools", Icons.Filled.Speed)
    object NetworkTools : Screen("network_tools", "Network Tools", Icons.Filled.NetworkCheck)
    object StorageTools : Screen("storage_tools", "Storage Tools", Icons.Filled.Storage)
    
    // Individual Tool Screens
    object MalwareScanner : Screen("malware_scanner", "Malware Scanner", Icons.Filled.BugReport)
    object MemoryCleaner : Screen("memory_cleaner", "Memory Cleaner", Icons.Filled.Memory)
    object NetworkSpeedTest : Screen("network_speed_test", "Speed Test", Icons.Filled.Speed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = {
            TopNavigationBar(navController = navController)
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = BackgroundPrimary
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }

            composable(Screen.Tools.route) {
                ToolsScreen(
                    onNavigateToSecurity = {
                        navController.navigate(Screen.SecurityTools.route)
                    },
                    onNavigateToPerformance = {
                        navController.navigate(Screen.PerformanceTools.route)
                    },
                    onNavigateToNetwork = {
                        navController.navigate(Screen.NetworkTools.route)
                    },
                    onNavigateToStorage = {
                        navController.navigate(Screen.StorageTools.route)
                    },
                    onNavigateToTool = { toolRoute ->
                        navController.navigate(toolRoute)
                    }
                )
            }
            composable(Screen.SecurityTools.route) {
                SecurityToolsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.PerformanceTools.route) {
                PerformanceToolsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.NetworkTools.route) {
                NetworkToolsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.StorageTools.route) {
                StorageToolsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Scan.route) {
                ScanScreen()
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            
            // Individual Tool Screens
            composable(Screen.MalwareScanner.route) {
                MalwareScannerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.MemoryCleaner.route) {
                MemoryCleanerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.NetworkSpeedTest.route) {
                NetworkSpeedTestScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Get the current screen title
    val currentTitle = when (currentRoute) {
        Screen.Home.route -> "Guardix Mobile"
        Screen.Tools.route -> "Tools"
        Screen.Scan.route -> "Security Scan"
        Screen.Reports.route -> "Reports"
        Screen.SecurityTools.route -> "Security Tools"
        Screen.PerformanceTools.route -> "Performance"
        Screen.NetworkTools.route -> "Network Tools"
        Screen.StorageTools.route -> "Storage Tools"
        Screen.MalwareScanner.route -> "Malware Scanner"
        Screen.MemoryCleaner.route -> "Memory Cleaner" 
        Screen.NetworkSpeedTest.route -> "Speed Test"
        else -> "Guardix Mobile"
    }
    
    TopAppBar(
        title = {
            Text(
                text = currentTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )
        },
        actions = {
            // Notifications Icon
            IconButton(
                onClick = { /* TODO: Open notifications */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = GrayText,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Settings Icon
            IconButton(
                onClick = {
                    navController.navigate(Screen.Settings.route)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = GrayText,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundPrimary,
            titleContentColor = GrayText,
            actionIconContentColor = GrayText
        )
    )
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val items = listOf(
        Screen.Home,
        Screen.Tools,
        Screen.Scan,
        Screen.Reports
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundSecondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        NavigationBar(
            containerColor = BackgroundSecondary,
            contentColor = LightBlue,
            tonalElevation = 0.dp,
            modifier = Modifier.height(72.dp)
        ) {
            items.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LightBlue,
                        selectedTextColor = LightBlue,
                        unselectedIconColor = GrayDark,
                        unselectedTextColor = GrayDark,
                        indicatorColor = LightBlue.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}