package com.faultcall.mobile.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OneUiDarkColorScheme = darkColorScheme(
    primary = TennisVolt,
    onPrimary = Color.Black,
    primaryContainer = CourtEmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    secondaryContainer = AccentCyanDim,
    onSecondaryContainer = AccentCyan,
    error = FaultCoral,
    onError = Color.White,
    background = OneUiBackground,
    onBackground = TextPrimary,
    surface = OneUiSurface,
    onSurface = TextPrimary,
    surfaceVariant = OneUiSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = OneUiStroke,
    outlineVariant = OneUiStrokeGlow
)

@Composable
fun FaultCallTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OneUiDarkColorScheme,
        content = content
    )
}
