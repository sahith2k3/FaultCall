package com.faultcall.mobile.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faultcall.mobile.data.VoiceSettings
import com.faultcall.mobile.presentation.theme.*

@Composable
fun VoiceSettingsDialog(
    settings: VoiceSettings,
    onDismiss: () -> Unit,
    onSave: (rate: Float, pitch: Float, url: String, apiKey: String, voiceId: String, useElevenLabs: Boolean, duckMusic: Boolean) -> Unit,
    onTest: () -> Unit
) {
    var rate by remember { mutableStateOf(settings.speechRate) }
    var pitch by remember { mutableStateOf(settings.pitch) }
    var elevenLabsUrl by remember { mutableStateOf(settings.elevenLabsBackendUrl) }
    var elevenLabsApiKey by remember { mutableStateOf(settings.elevenLabsApiKey) }
    var elevenLabsVoiceId by remember { mutableStateOf(settings.elevenLabsVoiceId) }
    var useElevenLabs by remember { mutableStateOf(settings.useElevenLabsIfAvailable) }
    var duckMusic by remember { mutableStateOf(settings.duckBackgroundMusic) }
    var showApiKey by remember { mutableStateOf(false) }

    val presetVoices = listOf(
        Pair("George (British Umpire)", "JBFqnCBsd6RMkjVDRZzb"),
        Pair("Brian (Deep British)", "nPczCjzI2devNBz1zQrb"),
        Pair("Adam (Baritone Shout)", "pNInz6obpgDQGcFmaJgB")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = TennisBallGreen)
                Spacer(Modifier.width(8.dp))
                Text("FaultCall Voice Settings", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Signature Profile Card
                Surface(
                    color = CharcoalCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "SIGNATURE IDENTITY: THE AUTHORITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TennisBallGreen
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Centre Court chair umpire · Refined British English (RP) · Deep resonant delivery · Powerful shouts for line calls.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // ElevenLabs AI Engine Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ElevenLabs Wimbledon Voice", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Authentic human British shouting umpire", fontSize = 10.sp, color = AccentCyan)
                        }
                        Switch(
                            checked = useElevenLabs,
                            onCheckedChange = { useElevenLabs = it }
                        )
                    }

                    if (useElevenLabs) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = elevenLabsApiKey,
                            onValueChange = { elevenLabsApiKey = it },
                            label = { Text("ElevenLabs API Key") },
                            placeholder = { Text("Paste key from elevenlabs.io") },
                            singleLine = true,
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle key visibility",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(6.dp))
                        Text("Select Wimbledon Voice Model:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            presetVoices.forEach { (name, id) ->
                                val selected = elevenLabsVoiceId == id
                                FilterChip(
                                    selected = selected,
                                    onClick = { elevenLabsVoiceId = id },
                                    label = { Text(name.substringBefore(" "), fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CourtGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Free tier gives 10k chars/mo. FaultCall caches audio on disk permanently after the first call.",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Divider(color = Color(0xFF333333))

                // Rate slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate (TTS Fallback)", fontSize = 11.sp, color = TextPrimary)
                        Text("${"%.2f".format(rate)}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                    Slider(
                        value = rate,
                        onValueChange = { rate = it },
                        valueRange = 0.8f..1.4f
                    )
                }

                // Pitch slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pitch (Lower = More Authoritative)", fontSize = 11.sp, color = TextPrimary)
                        Text("${"%.2f".format(pitch)}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = 0.7f..1.3f
                    )
                }

                Divider(color = Color(0xFF333333))

                // Music Ducking / Attenuation Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Duck Background Music", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Dips Spotify/music to ~5% during calls", fontSize = 10.sp, color = AccentCyan)
                    }
                    Switch(
                        checked = duckMusic,
                        onCheckedChange = { duckMusic = it }
                    )
                }

                OutlinedButton(
                    onClick = onTest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("TEST SPEAKER ANNOUNCEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(rate, pitch, elevenLabsUrl, elevenLabsApiKey, elevenLabsVoiceId, useElevenLabs, duckMusic) },
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreen)
            ) {
                Text("SAVE SETTINGS")
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
