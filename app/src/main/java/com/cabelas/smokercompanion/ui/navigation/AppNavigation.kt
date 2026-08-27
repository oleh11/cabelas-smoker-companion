package com.cabelas.smokercompanion.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cabelas.smokercompanion.SmokerApp
import com.cabelas.smokercompanion.data.wifi.WifiProvisioner
import com.cabelas.smokercompanion.ui.screens.cook.CookPlannerScreen
import com.cabelas.smokercompanion.ui.screens.cook.CookPlannerViewModel
import com.cabelas.smokercompanion.ui.screens.dashboard.DashboardScreen
import com.cabelas.smokercompanion.ui.screens.dashboard.DashboardViewModel
import com.cabelas.smokercompanion.ui.screens.scan.DeviceScanScreen
import com.cabelas.smokercompanion.ui.screens.scan.DeviceScanViewModel
import com.cabelas.smokercompanion.ui.screens.settings.SettingsScreen
import com.cabelas.smokercompanion.ui.screens.settings.SettingsViewModel
import com.cabelas.smokercompanion.ui.screens.wifi.WifiSetupScreen
import com.cabelas.smokercompanion.ui.screens.wifi.WifiSetupViewModel
import com.cabelas.smokercompanion.ui.theme.Charcoal800
import com.cabelas.smokercompanion.ui.theme.Charcoal900
import com.cabelas.smokercompanion.ui.theme.FlameOrange
import com.cabelas.smokercompanion.ui.theme.TextGray
import com.cabelas.smokercompanion.ui.theme.TextWhite

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val repository = (context.applicationContext as SmokerApp).repository
    val provisioner = WifiProvisioner(context)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Charcoal800,
                contentColor = TextWhite
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(text = screen.title) },
                        selected = selected,
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
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextWhite,
                            selectedTextColor = FlameOrange,
                            indicatorColor = FlameOrange,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )
                }
            }
        },
        containerColor = Charcoal900,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToScan = {
                        navController.navigate(Screen.DeviceScan.route)
                    }
                )
            }

            composable(Screen.CookPlanner.route) {
                val vm: CookPlannerViewModel = viewModel(factory = CookPlannerViewModel.Factory(repository))
                CookPlannerScreen(
                    viewModel = vm,
                    onPresetAppliedNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.DeviceScan.route) {
                val vm: DeviceScanViewModel = viewModel(factory = DeviceScanViewModel.Factory(repository))
                DeviceScanScreen(
                    viewModel = vm,
                    onConnectedNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.WifiSetup.route) {
                val vm: WifiSetupViewModel = viewModel(
                    factory = WifiSetupViewModel.Factory(repository, provisioner)
                )
                WifiSetupScreen(viewModel = vm)
            }

            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(repository, context)
                )
                SettingsScreen(viewModel = vm)
            }
        }
    }
}
