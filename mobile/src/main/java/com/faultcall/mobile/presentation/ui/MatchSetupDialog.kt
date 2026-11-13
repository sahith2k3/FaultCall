package com.faultcall.mobile.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.faultcall.mobile.presentation.theme.*
import com.faultcall.shared.model.AdvantageRule
import com.faultcall.shared.model.MatchConfig
import com.faultcall.shared.model.MatchFormat
import com.faultcall.shared.model.Player

@Composable
fun MatchSetupDialog(
    currentConfig: MatchConfig,
    onDismiss: () -> Unit,
    onConfirm: (MatchConfig) -> Unit
) {
    var playerA by remember { mutableStateOf(currentConfig.playerAName) }
    var playerB by remember { mutableStateOf(currentConfig.playerBName) }
    var format by remember { mutableStateOf(currentConfig.matchFormat) }
    var advantageRule by remember { mutableStateOf(currentConfig.advantageRule) }
    var superTiebreak by remember { mutableStateOf(currentConfig.finalSetSuperTiebreak) }
    var server by remember { mutableStateOf(currentConfig.startingServer) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, OneUiStroke, RoundedCornerShape(32.dp)),
            color = OneUiSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // One UI Sheet Handle
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(OneUiStrokeGlow)
                )

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CourtEmeraldDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsTennis,
                            contentDescription = null,
                            tint = CourtEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Match Setup",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tournament Rules & Player Rosters",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Players Card
                Surface(
                    color = OneUiSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, OneUiStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "COMPETITORS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CourtEmerald
                        )

                        OutlinedTextField(
                            value = playerA,
                            onValueChange = { playerA = it },
                            label = { Text("Player / Team A Name", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CourtEmerald,
                                unfocusedBorderColor = OneUiStroke,
                                focusedLabelColor = CourtEmerald,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = CourtEmerald
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = playerB,
                            onValueChange = { playerB = it },
                            label = { Text("Player / Team B Name", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CourtEmerald,
                                unfocusedBorderColor = OneUiStroke,
                                focusedLabelColor = CourtEmerald,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = CourtEmerald
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Match Rules Card
                Surface(
                    color = OneUiSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, OneUiStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "FORMAT & SCORING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentCyan
                        )

                        // Format
                        Column {
                            Text("Set Length:", fontSize = 11.sp, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = format == MatchFormat.BEST_OF_3,
                                    onClick = { format = MatchFormat.BEST_OF_3 },
                                    label = { Text("Best of 3 Sets", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CourtEmerald,
                                        selectedLabelColor = Color.White,
                                        containerColor = OneUiSurfaceElevated,
                                        labelColor = TextSecondary
                                    )
                                )
                                FilterChip(
                                    selected = format == MatchFormat.BEST_OF_5,
                                    onClick = { format = MatchFormat.BEST_OF_5 },
                                    label = { Text("Best of 5 Sets", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CourtEmerald,
                                        selectedLabelColor = Color.White,
                                        containerColor = OneUiSurfaceElevated,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }

                        // Advantage Rule
                        Column {
                            Text("Deuce Rule:", fontSize = 11.sp, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = advantageRule == AdvantageRule.ADVANTAGE,
                                    onClick = { advantageRule = AdvantageRule.ADVANTAGE },
                                    label = { Text("Advantage (Standard)", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CourtEmerald,
                                        selectedLabelColor = Color.White,
                                        containerColor = OneUiSurfaceElevated,
                                        labelColor = TextSecondary
                                    )
                                )
                                FilterChip(
                                    selected = advantageRule == AdvantageRule.NO_AD,
                                    onClick = { advantageRule = AdvantageRule.NO_AD },
                                    label = { Text("No-Ad (Deciding Pt)", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CourtEmerald,
                                        selectedLabelColor = Color.White,
                                        containerColor = OneUiSurfaceElevated,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }

                        // Super tiebreak
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("10-pt Final Set Super Tiebreak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Replaces full final set with first to 10 points", fontSize = 10.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = superTiebreak,
                                onCheckedChange = { superTiebreak = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = TennisVolt,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = OneUiStroke
                                )
                            )
                        }
                    }
                }

                // Initial Server Card
                Surface(
                    color = OneUiSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, OneUiStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "FIRST SERVICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = ServerAmber
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = server == Player.PLAYER_A,
                                onClick = { server = Player.PLAYER_A },
                                label = { Text("${playerA.ifBlank { "Player A" }} serves 🎾", fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ServerAmber,
                                    selectedLabelColor = Color.Black,
                                    containerColor = OneUiSurfaceElevated,
                                    labelColor = TextSecondary
                                )
                            )
                            FilterChip(
                                selected = server == Player.PLAYER_B,
                                onClick = { server = Player.PLAYER_B },
                                label = { Text("${playerB.ifBlank { "Player B" }} serves 🎾", fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ServerAmber,
                                    selectedLabelColor = Color.Black,
                                    containerColor = OneUiSurfaceElevated,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onConfirm(
                                MatchConfig(
                                    playerAName = playerA.ifBlank { "Player A" },
                                    playerBName = playerB.ifBlank { "Player B" },
                                    matchFormat = format,
                                    advantageRule = advantageRule,
                                    finalSetSuperTiebreak = superTiebreak,
                                    startingServer = server
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CourtEmerald,
                            contentColor = Color.White
                        )
                    ) {
                        Text("START MATCH", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
