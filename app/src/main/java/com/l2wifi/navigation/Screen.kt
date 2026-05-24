package com.l2wifi.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("wifi")
    object ActiveConnection : Screen("activeConnection/{accountId}") {
        fun pass(accountId: Long) = "activeConnection/$accountId"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object About : Screen("about")
}
