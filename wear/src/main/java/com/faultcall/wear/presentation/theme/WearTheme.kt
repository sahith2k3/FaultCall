package com.faultcall.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

// Samsung One UI Watch 8/9 AMOLED Palette
val WearOledBlack = Color(0xFF000000)
val WearSurface = Color(0xFF12141A)
val WearSurfaceElevated = Color(0xFF1B1E26)
val WearStroke = Color(0xFF272C38)
val WearStrokeGlow = Color(0xFF3B4254)

val WearTennisVolt = Color(0xFFD6FF00)
val WearCourtEmerald = Color(0xFF00C853)
val WearFaultCoral = Color(0xFFFF3B30)
val WearFaultCoralDark = Color(0xFFD32F2F)
val WearAccentCyan = Color(0xFF00E5FF)
val WearServerAmber = Color(0xFFFFB300)

// Backward compatibility aliases
val WearCharcoal = WearOledBlack
val WearCharcoalCard = WearSurface
val WearTennisGreen = WearTennisVolt
val WearCourtGreen = WearCourtEmerald
val WearFaultRed = WearFaultCoral

val WearColors = Colors(
    primary = WearTennisVolt,
    primaryVariant = WearCourtEmerald,
    secondary = WearAccentCyan,
    error = WearFaultCoral,
    background = WearOledBlack,
    surface = WearSurface,
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
