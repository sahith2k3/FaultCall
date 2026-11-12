package com.faultcall.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val WearCharcoal = Color(0xFF101010)
val WearCharcoalCard = Color(0xFF1C1C1C)
val WearTennisGreen = Color(0xFFC6FF00)
val WearCourtGreen = Color(0xFF2E7D32)
val WearFaultRed = Color(0xFFE53935)
val WearAccentCyan = Color(0xFF00E5FF)
val WearServerAmber = Color(0xFFFFB300)

val WearColors = Colors(
    primary = WearTennisGreen,
    primaryVariant = WearCourtGreen,
    secondary = WearAccentCyan,
    error = WearFaultRed,
    background = WearCharcoal,
    surface = WearCharcoalCard,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onError = Color.White
)

@Composable
fun FaultCallWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColors,
        content = content
    )
}
