package com.faultcall.mobile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
                            .background(TennisVoltDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = TennisVolt,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Voice & Audio Engine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "One UI 8 Fluid Acoustic System",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Signature Identity Card
                Surface(
                    color = OneUiSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OneUiStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TennisVolt)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "THE AUTHORITY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Wimbledon RP Accent",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TennisVolt
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Centre Court chair umpire · Crisp RP British English · Resonant acoustic projection · Dramatic shouts on line calls.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary
                        )
                    }
                }

                // ElevenLabs AI Section
                Surface(
                    color = OneUiSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OneUiStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "ElevenLabs Neural Voice",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    "Hyper-realistic human umpire speech",
                                    fontSize = 11.sp,
                                    color = AccentCyan
                                )
                            }
                            Switch(
                                checked = useElevenLabs,
                                onCheckedChange = { useElevenLabs = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = TennisVolt,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = OneUiStroke
                                )
                            )
                        }

                        if (useElevenLabs) {
                            OutlinedTextField(
                                value = elevenLabsApiKey,
                                onValueChange = { elevenLabsApiKey = it },
                                label = { Text("ElevenLabs API Key", fontSize = 12.sp) },
                                placeholder = { Text("Paste key from elevenlabs.io", fontSize = 12.sp) },
                                singleLine = true,
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle visibility",
                                            tint = TextSecondary
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TennisVolt,
                                    unfocusedBorderColor = OneUiStroke,
                                    focusedLabelColor = TennisVolt,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = TennisVolt
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                "Voice Model Preset:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                presetVoices.forEach { (name, id) ->
                                    val selected = elevenLabsVoiceId == id
                                    FilterChip(
                                        selected = selected,
                                        onClick = { elevenLabsVoiceId = id },
                                        label = {
                                            Text(
                                                name.substringBefore(" "),
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
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

                            Text(
                                "Permanent disk cache saves quota: subsequent calls play instantly without internet.",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Speech Rate & Pitch Sliders
                Surface(
                    color = OneUiSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OneUiStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Rate
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Speech Rate (TTS)", fontSize = 12.sp, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentCyanDim)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${"%.2f".format(rate)}x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                }
                            }
                            Slider(
                                value = rate,
                                onValueChange = { rate = it },
                                valueRange = 0.8f..1.4f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentCyan,
                                    activeTrackColor = AccentCyan,
                                    inactiveTrackColor = OneUiStroke
                                )
                            )
                        }

                        HorizontalDivider(color = OneUiStroke)

                        // Pitch
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pitch (Lower = Authoritative)", fontSize = 12.sp, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentCyanDim)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${"%.2f".format(pitch)}x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                }
                            }
                            Slider(
                                value = pitch,
                                onValueChange = { pitch = it },
                                valueRange = 0.7f..1.3f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentCyan,
                                    activeTrackColor = AccentCyan,
                                    inactiveTrackColor = OneUiStroke
                                )
                            )
                        }
                    }
                }

                // Music Ducking Row
                Surface(
                    color = OneUiSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OneUiStroke),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Duck Background Music",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Smoothly dips Spotify to 5% & restores instantly",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = duckMusic,
                            onCheckedChange = { duckMusic = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = TennisVolt,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = OneUiStroke
                            )
                        )
                    }
                }

                // Test Speaker Button
                OutlinedButton(
                    onClick = onTest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OneUiStrokeGlow),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = OneUiSurfaceElevated,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = TennisVolt,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "TEST SPEAKER ANNOUNCEMENT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                            onSave(rate, pitch, elevenLabsUrl, elevenLabsApiKey, elevenLabsVoiceId, useElevenLabs, duckMusic)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TennisVolt,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("SAVE SETTINGS", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
