package com.faultcall.mobile.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Match Setup", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = playerA,
                    onValueChange = { playerA = it },
                    label = { Text("Player A Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = playerB,
                    onValueChange = { playerB = it },
                    label = { Text("Player B Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Match Format", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = format == MatchFormat.BEST_OF_3,
                        onClick = { format = MatchFormat.BEST_OF_3 },
                        label = { Text("Best of 3") }
                    )
                    FilterChip(
                        selected = format == MatchFormat.BEST_OF_5,
                        onClick = { format = MatchFormat.BEST_OF_5 },
                        label = { Text("Best of 5") }
                    )
                }

                Text("Scoring Rule", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = advantageRule == AdvantageRule.ADVANTAGE,
                        onClick = { advantageRule = AdvantageRule.ADVANTAGE },
                        label = { Text("Advantage (Standard)") }
                    )
                    FilterChip(
                        selected = advantageRule == AdvantageRule.NO_AD,
                        onClick = { advantageRule = AdvantageRule.NO_AD },
                        label = { Text("No-Ad (Sudden Death)") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("10-pt Super Tiebreak (Final Set)", fontSize = 12.sp, color = TextPrimary)
                    Switch(
                        checked = superTiebreak,
                        onCheckedChange = { superTiebreak = it }
                    )
                }

                Text("Starting Server", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = server == Player.PLAYER_A,
                        onClick = { server = Player.PLAYER_A },
                        label = { Text("$playerA serves 🎾") }
                    )
                    FilterChip(
                        selected = server == Player.PLAYER_B,
                        onClick = { server = Player.PLAYER_B },
                        label = { Text("$playerB serves 🎾") }
                    )
                }
            }
        },
        confirmButton = {
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
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreen)
            ) {
                Text("START MATCH")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CharcoalSurface
    )
}
