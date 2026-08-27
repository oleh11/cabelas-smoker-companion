package com.cabelas.smokercompanion.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Grill", Icons.Default.OutdoorGrill)
    data object CookPlanner : Screen("cook_planner", "Presets", Icons.Default.RestaurantMenu)
    data object DeviceScan : Screen("device_scan", "Connect", Icons.Default.Bluetooth)
    data object WifiSetup : Screen("wifi_setup", "Wi-Fi", Icons.Default.Wifi)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Dashboard, CookPlanner, DeviceScan, WifiSetup, Settings)
    }
}
