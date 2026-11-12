package com.faultcall.wear.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.faultcall.shared.model.Player
import com.faultcall.wear.presentation.WearMainViewModel
import com.faultcall.wear.presentation.theme.*

@Composable
fun MainWatchScreen(
    viewModel: WearMainViewModel,
    onNavigateToSecondary: () -> Unit
) {
    val matchState by viewModel.matchState.collectAsState()
    val isPhoneConnected by viewModel.isPhoneConnected.collectAsState()
    val lastAck by viewModel.lastAck.collectAsState()

    val ptsA = matchState.displayPoints(Player.PLAYER_A)
    val ptsB = matchState.displayPoints(Player.PLAYER_B)
    val isServingA = matchState.currentServer == Player.PLAYER_A
    val isServingB = matchState.currentServer == Player.PLAYER_B

    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WearCharcoal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            autoCentering = null,
            contentPadding = PaddingValues(top = 22.dp, bottom = 14.dp, start = 10.dp, end = 10.dp)
        ) {
            // 1. Ultra-compact Header: Status + Sets
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { viewModel.onLaunchPhoneAppTap() }
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isPhoneConnected) WearCourtGreen else WearServerAmber)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isPhoneConnected) matchState.formattedScoreSummary() else "TAP TO LAUNCH PHONE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPhoneConnected) WearAccentCyan else WearServerAmber
                    )
                }
            }

            // 2. Compact Score Badges (no huge numerals eating vertical space)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.90f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player A Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isServingA) Color(0x3300E676) else Color(0x22FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${if (isServingA) "🎾 " else ""}A: $ptsA",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isServingA) WearTennisGreen else Color.White
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Player B Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isServingB) Color(0x3300E676) else Color(0x22FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${if (isServingB) "🎾 " else ""}B: $ptsB",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isServingB) WearTennisGreen else Color.White
                        )
                    }
                }
            }

            // 3. Signature FAULT! Button (Prominent, High Contrast)
            item {
                Button(
                    onClick = { viewModel.onFaultTap() },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = WearFaultRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        text = "FAULT!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 4. Primary Point Scoring: [+ PT A] and [+ PT B] (Directly accessible without scrolling)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { viewModel.onPointATap() },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = WearCourtGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ PT A", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { viewModel.onPointBTap() },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = WearCourtGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ PT B", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // 5. Quick Actions: [SCORE] and [UNDO] (Side by side on the default screen)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { viewModel.onScoreTap() },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF0097A7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SCORE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.onUndoTap() },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = WearCharcoalCard,
                            contentColor = WearAccentCyan
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("UNDO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 6. Compact MORE ⚙ & Last ACK
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    CompactChip(
                        onClick = onNavigateToSecondary,
                        label = { Text("MORE ⚙", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFF262626),
                            contentColor = WearTennisGreen
                        ),
                        modifier = Modifier.height(26.dp)
                    )

                    if (!lastAck.isNullOrBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "🔊 ${lastAck!!}",
                            fontSize = 9.sp,
                            color = Color.LightGray,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
