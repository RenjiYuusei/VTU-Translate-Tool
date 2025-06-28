package com.vtu.translate.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vtu.translate.R

sealed class Screen(val route: String, val resourceId: Int, val icon: @Composable () -> Unit) {
    object Translate : Screen("translate", R.string.translate_tab, { Icon(Icons.Filled.Translate, contentDescription = null) })
    object Settings : Screen("settings", R.string.settings_tab, { Icon(Icons.Filled.Settings, contentDescription = null) })
    object Logs : Screen("logs", R.string.logs_tab, { Icon(Icons.Filled.Book, contentDescription = null) })
}

val items = listOf(
    Screen.Translate,
    Screen.Settings,
    Screen.Logs
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon() },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Translate.route, Modifier.padding(innerPadding)) {
            composable(Screen.Translate.route) { TranslateScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Logs.route) { LogsScreen() }
        }
    }
} 