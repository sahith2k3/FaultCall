package com.faultcall.mobile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faultcall.mobile.audio.PlaybackState
import com.faultcall.mobile.presentation.MainViewModel
import com.faultcall.mobile.presentation.theme.*
import com.faultcall.shared.model.*

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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "FaultCall",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = TennisBallGreen
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = FaultRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    " UMPIRE ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            "Your voice on the court",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(Icons.Default.History, "History", tint = TextPrimary)
                    }
                    IconButton(onClick = { showVoiceSettingsDialog = true }) {
                        Icon(Icons.Default.RecordVoiceOver, "Voice Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalDark)
            )
        },
        containerColor = CharcoalDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Connection & Audio Route Diagnostics
            item {
                DiagnosticsCard(
                    isWatchConnected = isWatchConnected,
                    watchName = watchName ?: "Galaxy Watch8",
                    audioRouteName = audioRoute.deviceName,
                    isBluetooth = audioRoute.isBluetoothConnected,
                    onTestSpeaker = { viewModel.testSpeaker() }
                )
            }

            // Announcement Status Bar
            item {
                AnnouncementStatusBar(playbackState = playbackState)
            }

            // Main Scoreboard
            item {
                ScoreboardCard(
                    matchState = matchState,
                    onSwitchServer = { viewModel.switchServer() }
                )
            }

            // Primary Courtside Controls (FAULT, SCORE, POINTS, UNDO)
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

            // Start New Match Button
            item {
                Button(
                    onClick = { showSetupDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan)
                    Spacer(Modifier.width(8.dp))
                    Text("START NEW MATCH", fontWeight = FontWeight.Bold, color = AccentCyan)
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

@Composable
private fun DiagnosticsCard(
    isWatchConnected: Boolean,
    watchName: String,
    audioRouteName: String,
    isBluetooth: Boolean,
    onTestSpeaker: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Watch status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isWatchConnected) CourtGreen else ServerAmber)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isWatchConnected) "Watch Connected" else "Listening for Watch",
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

                // Audio output route
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBluetooth) Icons.Default.BluetoothAudio else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isBluetooth) AccentCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isBluetooth) "BT Speaker" else "Phone Speaker",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBluetooth) AccentCyan else TextSecondary
                        )
                        Text(
                            text = audioRouteName,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onTestSpeaker,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("TEST SPEAKER", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AnnouncementStatusBar(playbackState: PlaybackState) {
    val text = when (playbackState) {
        is PlaybackState.Idle -> "Ready · Awaiting watch tap"
        is PlaybackState.Playing -> "Announcing: \"${playbackState.event.spokenText}\""
        is PlaybackState.Error -> "Audio Notice: ${playbackState.message}"
    }

    Surface(
        color = CharcoalCard,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                tint = if (playbackState is PlaybackState.Playing) TennisBallGreen else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (playbackState is PlaybackState.Playing) Color.White else TextSecondary,
                fontWeight = if (playbackState is PlaybackState.Playing) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ScoreboardCard(
    matchState: MatchState,
    onSwitchServer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Format & Sets summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATCH SCORE (${matchState.config.matchFormat.name.replace("_", " ")})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "Sets: ${matchState.formattedScoreSummary()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan
                )
            }

            Spacer(Modifier.height(14.dp))

            // Player A Row
            PlayerScoreRow(
                playerName = matchState.config.playerAName,
                isServing = matchState.currentServer == Player.PLAYER_A,
                setsWon = matchState.setsWonBy(Player.PLAYER_A),
                gamesWon = matchState.playerAGames,
                pointsDisplay = matchState.displayPoints(Player.PLAYER_A),
                isTiebreak = matchState.isTiebreak
            )

            HorizontalDivider(color = CharcoalDark, modifier = Modifier.padding(vertical = 10.dp))

            // Player B Row
            PlayerScoreRow(
                playerName = matchState.config.playerBName,
                isServing = matchState.currentServer == Player.PLAYER_B,
                setsWon = matchState.setsWonBy(Player.PLAYER_B),
                gamesWon = matchState.playerBGames,
                pointsDisplay = matchState.displayPoints(Player.PLAYER_B),
                isTiebreak = matchState.isTiebreak
            )

            if (matchState.winner != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = CourtGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🏆 WINNER: ${matchState.config.nameOf(matchState.winner!!)}",
                        modifier = Modifier.padding(10.dp),
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            } else {
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onSwitchServer,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Change Server 🎾", fontSize = 11.sp, color = ServerAmber)
                }
            }
        }
    }
}

@Composable
private fun PlayerScoreRow(
    playerName: String,
    isServing: Boolean,
    setsWon: Int,
    gamesWon: Int,
    pointsDisplay: String,
    isTiebreak: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (isServing) "🎾 " else "   ",
                fontSize = 14.sp
            )
            Text(
                text = playerName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Sets / Games / Points
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SETS", fontSize = 9.sp, color = TextMuted)
                Text("$setsWon", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GAMES", fontSize = 9.sp, color = TextMuted)
                Text("$gamesWon", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Surface(
                color = if (pointsDisplay == "AD") FaultRed else TennisBallGreen,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.widthIn(min = 46.dp)
            ) {
                Text(
                    text = pointsDisplay,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "COURTSIDE CONTROLS (MIRRORS GALAXY WATCH8)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            // Row 1: Giant Red FAULT! and Cyan SCORE
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onFault,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FaultRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("FAULT!", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Button(
                    onClick = onScore,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097A7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SCORE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Row 2: Point A & Point B
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onPointA,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ POINT ${matchState.config.playerAName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPointB,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CourtGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ POINT ${matchState.config.playerBName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Row 3: Secondary controls (UNDO, LET, DOUBLE FAULT)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onUndo,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("UNDO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onLet,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("LET", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDoubleFault,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DOUBLE FAULT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
