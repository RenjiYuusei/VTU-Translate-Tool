package com.vtu.translate.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vtu.translate.R
import com.vtu.translate.ui.screens.LogScreen
import com.vtu.translate.ui.screens.SettingsScreen
import com.vtu.translate.ui.screens.TranslateScreen
import com.vtu.translate.ui.viewmodel.MainViewModel
import com.vtu.translate.ui.viewmodel.NavigationTab

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigation(currentTab, onTabSelected)
        }
    ) { innerPadding ->
        when (currentTab) {
            NavigationTab.TRANSLATE -> TranslateScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            NavigationTab.SETTINGS -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            NavigationTab.LOG -> LogScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun BottomNavigation(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_translate), contentDescription = null) },
            label = { Text(stringResource(R.string.tab_translate)) },
            selected = currentTab == NavigationTab.TRANSLATE,
            onClick = { onTabSelected(NavigationTab.TRANSLATE) }
        )
        
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_settings), contentDescription = null) },
            label = { Text(stringResource(R.string.tab_settings)) },
            selected = currentTab == NavigationTab.SETTINGS,
            onClick = { onTabSelected(NavigationTab.SETTINGS) }
        )
        
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_log), contentDescription = null) },
            label = { Text(stringResource(R.string.tab_log)) },
            selected = currentTab == NavigationTab.LOG,
            onClick = { onTabSelected(NavigationTab.LOG) }
        )
    }
}