package com.example.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.presentation.screens.*
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberGray
import com.example.ui.theme.CyberMuted

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Hud : Screen("hud", "HUD", Icons.Default.Speed)
    object Chat : Screen("chat", "Sohbet", Icons.Default.Chat)
    object Camera : Screen("camera", "Optik", Icons.Default.PhotoCamera)
    object Reminders : Screen("reminders", "Görevler", Icons.Default.Alarm)
    object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.testTag("app_scaffold"),
        bottomBar = {
            JarvisBottomNavigationBar(navController = navController)
        },
        containerColor = CyberDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Hud.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Hud.route) {
                HudScreen(viewModel = viewModel) {
                    navController.navigate(Screen.Chat.route)
                }
            }
            composable(Screen.Chat.route) {
                ChatScreen(viewModel = viewModel)
            }
            composable(Screen.Camera.route) {
                CameraScreen(viewModel = viewModel)
            }
            composable(Screen.Reminders.route) {
                RemindersScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JarvisBottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Hud,
        Screen.Chat,
        Screen.Camera,
        Screen.Reminders,
        Screen.Settings
    )

    NavigationBar(
        containerColor = CyberDark,
        tonalElevation = 10.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) CyberCyan else CyberMuted,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        color = if (isSelected) CyberCyan else CyberMuted,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CyberGray
                )
            )
        }
    }
}
