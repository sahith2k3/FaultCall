package com.faultcall.mobile.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.faultcall.mobile.presentation.theme.FaultCallTheme
import com.faultcall.mobile.presentation.ui.DashboardScreen

import android.content.Intent
import android.view.WindowManager
import com.faultcall.mobile.FaultCallApplication
import com.faultcall.shared.command.CommandType
import com.faultcall.shared.command.WearableCommand
import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and display over lockscreen when triggered by smartwatch on court
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        requestAppPermissions()

        setContent {
            FaultCallTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            "com.faultcall.mobile.TEST_SPEECH" -> {
                viewModel.testSpeaker()
            }
            "com.faultcall.mobile.SIMULATE_COMMAND" -> {
                val cmdName = intent.getStringExtra("command") ?: "AWARD_POINT_A"
                val type = try {
                    CommandType.valueOf(cmdName)
                } catch (e: Exception) {
                    CommandType.AWARD_POINT_A
                }
                val fakeCmd = WearableCommand(type = type, matchId = viewModel.matchState.value.matchId)
                val json = Gson().toJson(fakeCmd)
                FaultCallApplication.instance.wearableManager.handleIncomingCommand(json, "debug-test-node")
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
