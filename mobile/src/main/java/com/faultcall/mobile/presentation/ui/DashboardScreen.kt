package com.faultcall.mobile.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faultcall.mobile.audio.PlaybackState
import com.faultcall.mobile.presentation.MainViewModel
import com.faultcall.mobile.presentation.theme.*
import com.faultcall.shared.model.*

/**
 * Flagship Samsung One UI 8/9 Dashboard for FaultCall.
 * Emphasizes natural thumb reachability, fluid spring micro-interactions,
 * glanceable scoreboard with animated numerals, and live audio ducking visualizers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val matchState by viewModel.matchState.collectAsState()
    val isWatchConnected by viewModel.isWatchConnected.collectAsState()
    val watchName by viewModel.connectedWatchName.collectAsState()
    val audioRoute by viewModel.audioRoute.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val history by viewModel.history.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()

    var showSetupDialog by remember { mutableStateOf(false) }
    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "FaultCall",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = TennisVolt,
                            letterSpacing = (-0.5).sp
                        )

                        Surface(
                            color = FaultCoral,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "UMPIRE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OneUiSurfaceElevated)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Match History",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showVoiceSettingsDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OneUiSurfaceElevated)
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice & Ducking Settings",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OneUiBackground)
            )
        },
        containerColor = OneUiBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Connection & Audio Routing Strip
            item {
                DiagnosticsStrip(
                    isWatchConnected = isWatchConnected,
                    watchName = watchName ?: "Galaxy Watch8",
                    audioRouteName = audioRoute.deviceName,
                    isBluetooth = audioRoute.isBluetoothConnected,
                    onTestSpeaker = { viewModel.testSpeaker() }
                )
            }

            // 2. Dynamic Audio Ducking & Umpire Speech Aura Bar
            item {
                OneUiAnnouncementAuraBar(playbackState = playbackState)
            }

            // 3. Immersive Courtside Scoreboard (Animated Numerals & Active Serve Glow)
            item {
                ScoreboardCard(
                    matchState = matchState,
                    onSwitchServer = { viewModel.switchServer() }
                )
            }

            // 4. Ergonomic Interaction Zone: Hero FAULT!, SCORE, & Point Controls
            item {
                CourtsideControlsCard(
                    matchState = matchState,
                    onFault = { viewModel.announceFault() },
                    onScore = { viewModel.announceScore() },
                    onPointA = { viewModel.awardPoint(Player.PLAYER_A) },
                    onPointB = { viewModel.awardPoint(Player.PLAYER_B) },
                    onUndo = { viewModel.undo() },
                    onLet = { viewModel.announceLet() },
                    onDoubleFault = { viewModel.announceDoubleFault() }
                )
            }

            // 5. New Match Setup Pill
            item {
                OneUiSpringButton(
                    onClick = { showSetupDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OneUiSurfaceVariant),
                    border = BorderStroke(1.dp, OneUiStroke),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "START NEW MATCH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentCyan,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showSetupDialog) {
        MatchSetupDialog(
            currentConfig = matchState.config,
            onDismiss = { showSetupDialog = false },
            onConfirm = { config ->
                viewModel.startNewMatch(config)
                showSetupDialog = false
            }
        )
    }

    if (showVoiceSettingsDialog) {
        VoiceSettingsDialog(
            settings = voiceSettings,
            onDismiss = { showVoiceSettingsDialog = false },
            onSave = { rate, pitch, url, apiKey, voiceId, useElevenLabs, duckMusic ->
                viewModel.updateVoiceSettings(rate, pitch, url, apiKey, voiceId, useElevenLabs, duckMusic)
                showVoiceSettingsDialog = false
            },
            onTest = { viewModel.testSpeaker() }
        )
    }

    if (showHistoryDialog) {
        MatchHistoryDialog(
            history = history,
            onDismiss = { showHistoryDialog = false },
            onClear = { viewModel.clearHistory() }
        )
    }
}

/**
 * Clean One UI Squircle Diagnostics Card showing connected Watch status
 * and active audio speaker output routing.
 */
@Composable
private fun DiagnosticsStrip(
    isWatchConnected: Boolean,
    watchName: String,
    audioRouteName: String,
    isBluetooth: Boolean,
    onTestSpeaker: () -> Unit
) {
    // Breathing pulse animation for connection dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OneUiSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, OneUiStroke)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Watch Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .graphicsLayer { alpha = if (isWatchConnected) 1f else dotAlpha }
                            .clip(CircleShape)
                            .background(if (isWatchConnected) CourtEmerald else ServerAmber)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isWatchConnected) "Watch Remote Synced" else "Searching for Watch...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = watchName,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Audio Route Chip
                Surface(
                    color = OneUiSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isBluetooth) AccentCyanDim else OneUiStroke)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isBluetooth) Icons.Default.BluetoothAudio else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = if (isBluetooth) AccentCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = if (isBluetooth) "BT Loudspeaker" else "Phone Speaker",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBluetooth) AccentCyan else TextSecondary
                            )
                            Text(
                                text = audioRouteName.take(18),
                                fontSize = 10.sp,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Quick speaker test pill
            OutlinedButton(
                onClick = onTestSpeaker,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = OneUiSurfaceVariant.copy(alpha = 0.5f),
                    contentColor = AccentCyan
                ),
                border = BorderStroke(1.dp, AccentCyanDim),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "TEST LOUDSPEAKER OUTPUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Dynamic Audio Ducking & Umpire Speech Aura Bar.
 * Displays animated soundwave bars and ducking indicators when the announcement plays.
 */
@Composable
private fun OneUiAnnouncementAuraBar(playbackState: PlaybackState) {
    val isPlaying = playbackState is PlaybackState.Playing
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")

    // Dynamic wave bar heights
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse), label = "w3"
    )

    Surface(
        color = if (isPlaying) OneUiSurfaceElevated else OneUiSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isPlaying) TennisVoltGlow else OneUiStroke),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isPlaying) 8.dp else 0.dp, RoundedCornerShape(16.dp), ambientColor = TennisVolt)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlaying) {
                    // Animated mini soundwave
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(22.dp)
                    ) {
                        Box(Modifier.width(3.dp).height(wave1.dp).clip(CircleShape).background(TennisVolt))
                        Box(Modifier.width(3.dp).height(wave2.dp).clip(CircleShape).background(TennisVolt))
                        Box(Modifier.width(3.dp).height(wave3.dp).clip(CircleShape).background(TennisVolt))
                    }
                    Spacer(Modifier.width(10.dp))
                } else {
                    Icon(
                        Icons.Default.MicNone,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }

                val statusText = when (playbackState) {
                    is PlaybackState.Idle -> "Ready • Awaiting smartwatch trigger"
                    is PlaybackState.Playing -> "Announcing: \"${playbackState.event.spokenText}\""
                    is PlaybackState.Error -> "Notice: ${playbackState.message}"
                }

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = if (isPlaying) Color.White else TextSecondary,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }

            if (isPlaying) {
                Surface(
                    color = TennisVolt,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "DUCKING 5%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Flagship One UI Tennis Scoreboard Card with continuous squircle corners,
 * fluid numeral animations, and Wimbledon/Centre Court styling.
 */
@Composable
private fun ScoreboardCard(
    matchState: MatchState,
    onSwitchServer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OneUiSurface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, OneUiStroke)
    ) {
        Column(Modifier.padding(18.dp)) {
            // Header summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = OneUiSurfaceElevated,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = matchState.config.matchFormat.name.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (matchState.isTiebreak) {
                        Surface(
                            color = ServerAmber,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TIEBREAK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Sets: ${matchState.formattedScoreSummary()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
            }

            Spacer(Modifier.height(16.dp))

            // Player A Row
            OneUiPlayerScoreRow(
                playerName = matchState.config.playerAName,
                isServing = matchState.currentServer == Player.PLAYER_A,
                setsWon = matchState.setsWonBy(Player.PLAYER_A),
                gamesWon = matchState.playerAGames,
                pointsDisplay = matchState.displayPoints(Player.PLAYER_A)
            )

            // Centre Court Division Line
            HorizontalDivider(
                color = OneUiStroke,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Player B Row
            OneUiPlayerScoreRow(
                playerName = matchState.config.playerBName,
                isServing = matchState.currentServer == Player.PLAYER_B,
                setsWon = matchState.setsWonBy(Player.PLAYER_B),
                gamesWon = matchState.playerBGames,
                pointsDisplay = matchState.displayPoints(Player.PLAYER_B)
            )

            // Winner Trophy Banner OR Change Server Action
            if (matchState.winner != null) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    color = CourtEmerald,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🏆 MATCH WON: ${matchState.config.nameOf(matchState.winner!!)}",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onSwitchServer,
                        colors = ButtonDefaults.textButtonColors(contentColor = ServerAmber)
                    ) {
                        Text(
                            "Change Server 🎾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Player Row featuring spring animated numerals and dynamic server indicator.
 */
@Composable
private fun OneUiPlayerScoreRow(
    playerName: String,
    isServing: Boolean,
    setsWon: Int,
    gamesWon: Int,
    pointsDisplay: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Player Name & Ball indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isServing) TennisVoltGlow else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isServing) {
                    Text("🎾", fontSize = 13.sp)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = playerName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isServing) Color.White else TextPrimary,
                maxLines = 1
            )
        }

        // Sets / Games / Fluid Animated Points
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SETS chip
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SETS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(
                    text = "$setsWon",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            // GAMES chip
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GAMES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(
                    text = "$gamesWon",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // High-visibility animated Points box
            Surface(
                color = when (pointsDisplay) {
                    "AD" -> FaultCoral
                    "40" -> TennisVolt
                    else -> TennisVolt
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isServing) Color.White.copy(alpha = 0.5f) else Color.Transparent),
                modifier = Modifier
                    .widthIn(min = 52.dp)
                    .height(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = pointsDisplay,
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                                    slideOutVertically { height -> -height } + fadeOut()
                        },
                        label = "scoreFlip"
                    ) { targetScore ->
                        Text(
                            text = targetScore,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (pointsDisplay == "AD") Color.White else Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ergonomic Courtside Action Card placed in the thumb interaction zone.
 */
@Composable
private fun CourtsideControlsCard(
    matchState: MatchState,
    onFault: () -> Unit,
    onScore: () -> Unit,
    onPointA: () -> Unit,
    onPointB: () -> Unit,
    onUndo: () -> Unit,
    onLet: () -> Unit,
    onDoubleFault: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OneUiSurface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, OneUiStroke)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "COURTSIDE TOUCH CONTROLS",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            // Row 1: Hero FAULT! (with gradient & spring feedback) and SCORE button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Giant Coral FAULT! Button
                OneUiSpringButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFault()
                    },
                    modifier = Modifier
                        .weight(1.35f)
                        .height(68.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFFF334B), Color(0xFFFF5E3A)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FAULT!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Teal SCORE button
                OneUiSpringButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScore()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F))
                ) {
                    Text(
                        text = "SCORE",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Row 2: + POINT A and + POINT B
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OneUiSpringButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPointA()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CourtEmeraldDark)
                ) {
                    Text(
                        text = "+ PT ${matchState.config.playerAName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                OneUiSpringButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPointB()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CourtEmeraldDark)
                ) {
                    Text(
                        text = "+ PT ${matchState.config.playerBName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            // Row 3: Auxiliary Controls (UNDO, LET, DOUBLE FAULT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onUndo()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = OneUiSurfaceElevated,
                        contentColor = AccentCyan
                    ),
                    border = BorderStroke(1.dp, OneUiStroke),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("UNDO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onLet()
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = OneUiSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, OneUiStroke),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("LET", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDoubleFault()
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = OneUiSurfaceElevated,
                        contentColor = FaultCoral
                    ),
                    border = BorderStroke(1.dp, OneUiStroke),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("DOUBLE FAULT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Custom Spring Button providing tactile bounce physics on touch.
 */
@Composable
fun OneUiSpringButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "springScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = PaddingValues(0.dp),
        content = content
    )
}
