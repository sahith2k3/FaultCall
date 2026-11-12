package com.faultcall.wear.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.faultcall.shared.model.Player
import com.faultcall.wear.presentation.WearMainViewModel
import com.faultcall.wear.presentation.theme.*

@Composable
fun SecondaryControlsScreen(
    viewModel: WearMainViewModel,
    onBack: () -> Unit
) {
    val matchState by viewModel.matchState.collectAsState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WearCharcoal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp, start = 14.dp, end = 14.dp)
        ) {
            item {
                Text(
                    "MATCH CONTROLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = WearTennisGreen
                )
            }

            // Let Call
            item {
                Button(
                    onClick = {
                        viewModel.onLetTap()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearCharcoalCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CALL LET", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Double Fault
            item {
                Button(
                    onClick = {
                        viewModel.onDoubleFaultTap()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearFaultRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DOUBLE FAULT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Toggle Server
            item {
                val serverName = if (matchState.currentServer == Player.PLAYER_A) "Server: A 🎾" else "Server: B 🎾"
                Button(
                    onClick = { viewModel.onToggleServerTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearCharcoalCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(serverName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WearServerAmber)
                }
            }

            // Start New Match
            item {
                Button(
                    onClick = {
                        viewModel.onStartNewMatchTap()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF37474F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("START NEW MATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Trigger / Open Phone App
            item {
                Button(
                    onClick = {
                        viewModel.onLaunchPhoneAppTap()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📱 OPEN PHONE APP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Refresh State from Phone
            item {
                Button(
                    onClick = { viewModel.refreshState() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearCharcoalCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SYNC FROM PHONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WearAccentCyan)
                }
            }

            // Back button
            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearCourtGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("← BACK TO MATCH", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}
