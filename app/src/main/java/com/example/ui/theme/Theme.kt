package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BaseBlue,
    onPrimary = TextPrimary,
    primaryContainer = DarkCardElevated,
    onPrimaryContainer = BaseCyan,
    secondary = BaseCyan,
    onSecondary = DarkBackground,
    secondaryContainer = DarkCard,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonEmerald,
    onTertiary = DarkBackground,
    tertiaryContainer = RiskLowBg,
    onTertiaryContainer = NeonEmerald,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = DangerCrimson,
    onError = TextPrimary,
    errorContainer = RiskHighBg,
    onErrorContainer = DangerCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to rich dark Web3 theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

