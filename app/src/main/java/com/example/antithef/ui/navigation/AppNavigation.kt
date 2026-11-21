package com.example.antithef.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.antithef.ui.components.BottomNavBar
import com.example.antithef.ui.components.Screen
import com.example.antithef.ui.screens.AlarmScreen
import com.example.antithef.ui.screens.DashboardScreen
import com.example.antithef.ui.screens.HistoryScreen
import com.example.antithef.ui.screens.MonitorScreen
import com.example.antithef.ui.screens.SettingsScreen
import com.example.antithef.ui.screens.SplashScreen
import com.example.antithef.ui.theme.SecurityNavy
import com.example.antithef.viewmodel.SecurityViewModel

@Composable
fun AppNavigation(
    viewModel: SecurityViewModel
) {
    var showSplash by remember { mutableStateOf(true) }
    var currentRoute by remember { mutableStateOf(Screen.Dashboard.route) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = SecurityNavy,
                bottomBar = {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { newRoute ->
                            currentRoute = newRoute
                        }
                    )
                }
            ) { innerPadding ->
                Crossfade(
                    targetState = currentRoute,
                    modifier = Modifier.padding(innerPadding),
                    label = "tab_crossfade"
                ) { route ->
                    when (route) {
                        Screen.Dashboard.route -> DashboardScreen(viewModel = viewModel)
                        Screen.Monitor.route -> MonitorScreen(viewModel = viewModel)
                        Screen.History.route -> HistoryScreen(viewModel = viewModel)
                        Screen.Settings.route -> SettingsScreen(viewModel = viewModel)
                        else -> DashboardScreen(viewModel = viewModel)
                    }
                }
            }

            // Overlay Alarm Screen if alarm is active
            if (uiState.isAlarmActive) {
                AlarmScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
