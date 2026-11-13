package com.faultcall.wear.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
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
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(WearOledBlack),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(top = 26.dp, bottom = 26.dp, start = 12.dp, end = 12.dp)
        ) {
            item {
                Text(
                    "MATCH CONTROLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = WearTennisVolt
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
                        .fillMaxWidth(0.92f)
                        .height(38.dp)
                        .border(1.dp, WearStroke, RoundedCornerShape(19.dp)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearSurfaceElevated),
                    shape = RoundedCornerShape(19.dp)
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
                        .fillMaxWidth(0.92f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearFaultCoral),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Text("DOUBLE FAULT", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            // Toggle Server
            item {
                val serverName = if (matchState.currentServer == Player.PLAYER_A) "Server: Player A 🎾" else "Server: Player B 🎾"
                Button(
                    onClick = { viewModel.onToggleServerTap() },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(38.dp)
                        .border(1.dp, WearStroke, RoundedCornerShape(19.dp)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearSurface),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Text(serverName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WearServerAmber)
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
                        .fillMaxWidth(0.92f)
                        .height(38.dp)
                        .border(1.dp, WearStroke, RoundedCornerShape(19.dp)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2C3440)),
                    shape = RoundedCornerShape(19.dp)
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
                        .fillMaxWidth(0.92f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Text("📱 OPEN PHONE APP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Refresh State from Phone
            item {
                Button(
                    onClick = { viewModel.refreshState() },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(38.dp)
                        .border(1.dp, WearStroke, RoundedCornerShape(19.dp)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearSurface),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Text("SYNC FROM PHONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WearAccentCyan)
                }
            }

            // Back button
            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = WearCourtEmerald),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("← BACK TO MATCH", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}
