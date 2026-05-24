package com.l2wifi.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.l2wifi.ui.screens.about.AboutScreen
import com.l2wifi.ui.screens.billete.PantallaBillete
import com.l2wifi.ui.screens.casa.PantallaCasa
import com.l2wifi.ui.screens.home.HomeScreen
import com.l2wifi.ui.screens.profile.ProfileScreen
import com.l2wifi.ui.screens.settings.SettingsScreen
import com.l2wifi.ui.screens.activeconnection.ActiveConnectionScreen
import com.l2wifi.ui.screens.splash.SplashScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute in listOf("casa", "wifi", "billete")
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1A1F26),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "casa",
                        onClick = { navController.navigate("casa") { popUpTo("casa") { inclusive = false } } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Casa") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00FFCC),
                            unselectedIconColor = Color.White
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "wifi",
                        onClick = { navController.navigate("wifi") { popUpTo("wifi") { inclusive = false } } },
                        icon = { Icon(Icons.Filled.Wifi, contentDescription = "WiFi") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00FFCC),
                            unselectedIconColor = Color.White
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "billete",
                        onClick = { navController.navigate("billete") { popUpTo("billete") { inclusive = false } } },
                        icon = { Icon(Icons.Filled.AttachMoney, contentDescription = "Billete") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00FFCC),
                            unselectedIconColor = Color.White
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") {
                SplashScreen(navController)
            }
            composable("casa") {
                PantallaCasa()
            }
            composable("wifi") {
                HomeScreen(navController)
            }
            composable("billete") {
                PantallaBillete()
            }
            composable("settings") {
                SettingsScreen(navController)
            }
            composable("about") {
                AboutScreen(navController)
            }
            composable("profile") {
                ProfileScreen(navController)
            }
            composable("activeConnection/{accountId}") { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: -1L
                ActiveConnectionScreen(navController, accountId)
            }
        }
    }
}