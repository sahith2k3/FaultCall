package com.faultcall.wear.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.faultcall.wear.presentation.theme.FaultCallWearTheme
import com.faultcall.wear.presentation.ui.MainWatchScreen
import com.faultcall.wear.presentation.ui.SecondaryControlsScreen

class WearMainActivity : ComponentActivity() {

    private val viewModel: WearMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep watch screen responsive during active tennis match
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            FaultCallWearTheme {
                var currentScreen by remember { mutableStateOf("main") }

                when (currentScreen) {
                    "main" -> MainWatchScreen(
                        viewModel = viewModel,
                        onNavigateToSecondary = { currentScreen = "secondary" }
                    )
                    "secondary" -> SecondaryControlsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "main" }
                    )
                }
            }
        }
    }
}
