package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Substations : Screen("substations", "Grid Loading", Icons.Filled.Bolt, Icons.Outlined.Bolt)
    object Analytics : Screen("analytics", "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    object Appliances : Screen("appliances", "Appliances", Icons.Filled.Settings, Icons.Outlined.Settings)
    object HouseDetail : Screen("detail/{id}", "House Detail", Icons.Filled.Dashboard, Icons.Outlined.Dashboard) {
        fun createRoute(id: Int) = "detail/$id"
    }
}

@Composable
fun MainAppNavigation(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.Dashboard,
        Screen.Substations,
        Screen.Analytics,
        Screen.Appliances
    )

    Scaffold(
        bottomBar = {
            // Only show bottom bar on core level destinations (Dashboard, Substations, Analytics and Appliances)
            val showBottomBar = currentRoute in listOf(Screen.Dashboard.route, Screen.Substations.route, Screen.Analytics.route, Screen.Appliances.route)
            if (showBottomBar) {
                androidx.compose.foundation.layout.Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp
                    ) {
                        items.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { 
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    indicatorColor = Color.Transparent
                                ),
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
                                }
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onHouseClick = { houseId ->
                        viewModel.setSelectedHouseId(houseId)
                        navController.navigate(Screen.HouseDetail.createRoute(houseId))
                    }
                )
            }
            composable(Screen.Substations.route) {
                SubstationsScreen(
                    viewModel = viewModel,
                    onHouseClick = { houseId ->
                        viewModel.setSelectedHouseId(houseId)
                        navController.navigate(Screen.HouseDetail.createRoute(houseId))
                    }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onHouseClick = { houseId ->
                        viewModel.setSelectedHouseId(houseId)
                        navController.navigate(Screen.HouseDetail.createRoute(houseId))
                    }
                )
            }
            composable(Screen.Appliances.route) {
                AppliancesConfigScreen(
                    viewModel = viewModel
                )
            }
            composable(
                route = Screen.HouseDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val houseId = backStackEntry.arguments?.getInt("id") ?: -1
                HouseDetailScreen(
                    houseId = houseId,
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
