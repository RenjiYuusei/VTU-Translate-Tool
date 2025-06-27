package com.vtu.translate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.vtu.translate.data.LogRepository
import com.vtu.translate.data.SettingsRepository
import com.vtu.translate.network.RetrofitInstance
import com.vtu.translate.ui.screens.MainScreen
import com.vtu.translate.ui.theme.VTUTranslateToolTheme
import com.vtu.translate.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VTUTranslateToolTheme {
                val context = LocalContext.current
                val settingsRepository = remember { SettingsRepository(context) }
                val logRepository = remember { LogRepository }
                val openRouterApi = remember { RetrofitInstance.api }

                val settingsViewModel: SettingsViewModel by viewModels {
                    SettingsViewModelFactory(settingsRepository)
                }
                val logsViewModel: LogsViewModel by viewModels {
                    LogsViewModelFactory(logRepository)
                }
                val translateViewModel: TranslateViewModel by viewModels {
                    TranslateViewModelFactory(context, openRouterApi, settingsRepository, logRepository)
                }

                MainScreen(
                    settingsViewModel = settingsViewModel,
                    logsViewModel = logsViewModel,
                    translateViewModel = translateViewModel
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VTUTranslateToolTheme {
        MainScreen()
    }
} 