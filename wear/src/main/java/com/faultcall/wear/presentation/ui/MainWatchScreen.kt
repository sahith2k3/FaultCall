package com.faultcall.wear.presentation.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
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

    val gamesA = matchState.playerAGames
    val gamesB = matchState.playerBGames

    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WearOledBlack)
        ) {
            // Concentric Circular Bezel Progress Arcs (Samsung One UI Watch Dynamic Rim)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.5.dp.toPx()
                val inset = 4.dp.toPx() + strokeWidth / 2f
                val diameter = size.minDimension - inset * 2
                val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
                val topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )

                // Background subtle bezel tracks (Left arc for Player A, Right arc for Player B)
                // Left track: from 135 deg to 225 deg (90 degrees)
                drawArc(
                    color = Color(0x22FFFFFF),
                    startAngle = 135f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Right track: from -45 deg to 45 deg (90 degrees)
                drawArc(
                    color = Color(0x22FFFFFF),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Progress: Player A Game Progress (Sweep up to 90 deg based on 6 games)
                val progressA: Float = (gamesA.toFloat() / 6f).coerceIn(0f, 1f)
                if (progressA > 0f) {
                    drawArc(
                        color = WearAccentCyan,
                        startAngle = 135f,
                        sweepAngle = 90f * progressA,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Active Progress: Player B Game Progress (Sweep up to 90 deg based on 6 games)
                val progressB: Float = (gamesB.toFloat() / 6f).coerceIn(0f, 1f)
                if (progressB > 0f) {
                    drawArc(
                        color = WearTennisVolt,
                        startAngle = 45f,
                        sweepAngle = -90f * progressB,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(top = 22.dp, bottom = 18.dp, start = 12.dp, end = 12.dp)
            ) {
                // 1. One UI Header: Connection Pill + Match State
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WearSurface)
                            .border(1.dp, WearStroke, RoundedCornerShape(12.dp))
                            .clickable { viewModel.onLaunchPhoneAppTap() }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPhoneConnected) WearCourtEmerald else WearServerAmber)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isPhoneConnected) matchState.formattedScoreSummary() else "TAP TO CONNECT PHONE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPhoneConnected) WearTennisVolt else WearServerAmber
                        )
                    }
                }

                // 2. High-Contrast Sports Athletic Scoreboard Badges
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player A Score Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isServingA) Color(0x2800E5FF) else WearSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isServingA) WearAccentCyan else WearStroke,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isServingA) {
                                        Text("🎾", fontSize = 9.sp)
                                        Spacer(Modifier.width(2.dp))
                                    }
                                    Text(
                                        "PLAYER A",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isServingA) WearAccentCyan else Color.LightGray
                                    )
                                }
                                Text(
                                    text = ptsA,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.width(6.dp))

                        // Player B Score Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isServingB) Color(0x28D6FF00) else WearSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isServingB) WearTennisVolt else WearStroke,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isServingB) {
                                        Text("🎾", fontSize = 9.sp)
                                        Spacer(Modifier.width(2.dp))
                                    }
                                    Text(
                                        "PLAYER B",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isServingB) WearTennisVolt else Color.LightGray
                                    )
                                }
                                Text(
                                    text = ptsB,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // 3. Ergonomic Hero Curved FAULT! Button
                item {
                    val faultInteraction = remember { MutableInteractionSource() }
                    val isFaultPressed by faultInteraction.collectIsPressedAsState()
                    val faultScale by animateFloatAsState(
                        targetValue = if (isFaultPressed) 0.92f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "faultBounce"
                    )

                    Button(
                        onClick = { viewModel.onFaultTap() },
                        interactionSource = faultInteraction,
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .height(46.dp)
                            .graphicsLayer {
                                scaleX = faultScale
                                scaleY = faultScale
                            },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = WearFaultCoral,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(23.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "FAULT!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // 4. Primary Point Scoring: [+ PT A] and [+ PT B]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.94f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val ptAInteraction = remember { MutableInteractionSource() }
                        val isPtAPressed by ptAInteraction.collectIsPressedAsState()
                        val ptAScale by animateFloatAsState(
                            targetValue = if (isPtAPressed) 0.92f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "ptABounce"
                        )

                        Button(
                            onClick = { viewModel.onPointATap() },
                            interactionSource = ptAInteraction,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .graphicsLayer {
                                    scaleX = ptAScale
                                    scaleY = ptAScale
                                },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = WearCourtEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text("+ PT A", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }

                        val ptBInteraction = remember { MutableInteractionSource() }
                        val isPtBPressed by ptBInteraction.collectIsPressedAsState()
                        val ptBScale by animateFloatAsState(
                            targetValue = if (isPtBPressed) 0.92f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "ptBBounce"
                        )

                        Button(
                            onClick = { viewModel.onPointBTap() },
                            interactionSource = ptBInteraction,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .graphicsLayer {
                                    scaleX = ptBScale
                                    scaleY = ptBScale
                                },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = WearCourtEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text("+ PT B", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // 5. Quick Actions: [SCORE] and [UNDO]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.94f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onScoreTap() },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF00838F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text("SCORE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.onUndoTap() },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(34.dp)
                                .border(1.dp, WearStroke, RoundedCornerShape(11.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = WearSurfaceElevated,
                                contentColor = WearAccentCyan
                            ),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text("UNDO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 6. Fluid Pill MORE ⚙ & Audio Feedback
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(0.94f)
                    ) {
                        CompactChip(
                            onClick = onNavigateToSecondary,
                            label = { Text("MORE ⚙", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = WearSurfaceElevated,
                                contentColor = WearTennisVolt
                            ),
                            modifier = Modifier
                                .height(26.dp)
                                .border(1.dp, WearStroke, RoundedCornerShape(13.dp))
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
}
