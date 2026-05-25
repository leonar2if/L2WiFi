package com.l2wifi.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.l2wifi.ui.screens.about.AboutScreen
import com.l2wifi.ui.screens.activeconnection.ActiveConnectionScreen
import com.l2wifi.ui.screens.billete.PantallaBillete
import com.l2wifi.ui.screens.casa.PantallaCasa
import com.l2wifi.ui.screens.home.HomeScreen
import com.l2wifi.ui.screens.profile.ProfileScreen
import com.l2wifi.ui.screens.settings.SettingsScreen
import com.l2wifi.ui.screens.splash.SplashScreen
import com.l2wifi.util.WidgetAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    pendingWidgetAction: WidgetAction? = null,
    onWidgetActionConsumed: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("casa", "wifi", "billete")

    LaunchedEffect(pendingWidgetAction) {
        when (pendingWidgetAction) {
            is WidgetAction.Connect, is WidgetAction.Balance -> {
                navController.navigate("wifi") {
                    launchSingleTop = true
                }
            }
            is WidgetAction.Logout, is WidgetAction.Refresh -> {
                navController.navigate("activeConnection/${pendingWidgetAction.accountId}") {
                    launchSingleTop = true
                }
            }
            null -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 10.dp
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == "casa",
                            onClick = { navController.navigate("casa") { popUpTo("casa") { inclusive = false } } },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Casa") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "wifi",
                            onClick = { navController.navigate("wifi") { popUpTo("wifi") { inclusive = false } } },
                            icon = { Icon(Icons.Filled.Wifi, contentDescription = "WiFi") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "billete",
                            onClick = { navController.navigate("billete") { popUpTo("billete") { inclusive = false } } },
                            icon = {
                                Text(
                                    text = "₽",
                                    fontSize = 20.sp,
                                    color = if (currentRoute == "billete") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val startDestination = remember(pendingWidgetAction) {
            if (pendingWidgetAction == null) "splash" else "wifi"
        }
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("casa") { PantallaCasa() }
            composable("wifi") {
                HomeScreen(
                    navController = navController,
                    pendingWidgetAction = pendingWidgetAction,
                    onWidgetActionConsumed = onWidgetActionConsumed
                )
            }
            composable("billete") { PantallaBillete() }
            composable("settings") {
                SettingsScreen(navController)
            }
            composable("about") { AboutScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("activeConnection/{accountId}") { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: -1L
                ActiveConnectionScreen(
                    navController = navController,
                    accountId = accountId,
                    pendingWidgetAction = pendingWidgetAction,
                    onWidgetActionConsumed = onWidgetActionConsumed
                )
            }
        }
    }
}
