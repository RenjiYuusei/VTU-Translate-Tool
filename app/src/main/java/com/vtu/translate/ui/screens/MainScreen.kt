package com.vtu.translate.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vtu.translate.R
import com.vtu.translate.data.repository.LogRepository
import com.vtu.translate.di.AppContainer
import com.vtu.translate.ui.viewmodel.LogViewModel
import com.vtu.translate.ui.viewmodel.SettingsViewModel
import com.vtu.translate.ui.viewmodel.TranslateViewModel
import com.vtu.translate.ui.viewmodel.TranslateViewModelFactory

sealed class Screen(val route: String, val resourceId: Int, val icon: ImageVector) {
    object Translate : Screen("translate", R.string.tab_translate, Icons.Default.Translate)
    object Settings : Screen("settings", R.string.tab_settings, Icons.Default.Settings)
    object Log : Screen("log", R.string.tab_log, Icons.Default.List)
}

@Composable
fun MainScreen(appContainer: AppContainer) {
    val navController = rememberNavController()
    val items = listOf(Screen.Translate, Screen.Settings, Screen.Log)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentRoute == screen.route,
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
        NavHost(
            navController,
            startDestination = Screen.Translate.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Translate.route) {
                val translateViewModel: TranslateViewModel = viewModel(
                    factory = TranslateViewModelFactory(
                        appContainer.translationRepository,
                        appContainer.settingsRepository,
                        LogRepository,
                        appContainer.xmlParser,
                        appContainer.fileSaver
                    )
                )
                TranslateScreen(viewModel = translateViewModel)
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(appContainer.settingsRepository)
                )
                SettingsScreen(viewModel = settingsViewModel)
            }
            composable(Screen.Log.route) {
                val logViewModel: LogViewModel = viewModel(
                    factory = LogViewModel.provideFactory(LogRepository)
                )
                LogScreen(viewModel = logViewModel)
            }
        }
    }
} 