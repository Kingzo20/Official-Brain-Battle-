package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BrainBattleDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = BackgroundDark,
    primaryContainer = NeonViolet,
    onPrimaryContainer = TextPrimary,
    secondary = NeonGreen,
    onSecondary = BackgroundDark,
    secondaryContainer = CardSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonAmber,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = CardSurfaceBorder,
    error = NeonRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep the custom dark neon game branding consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrainBattleDarkColorScheme,
        typography = Typography,
        content = content
    )
}
