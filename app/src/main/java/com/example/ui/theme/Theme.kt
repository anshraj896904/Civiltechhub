package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BlueprintCyan,
    onPrimary = BlueprintNavyDark,
    primaryContainer = BlueprintSlate,
    onPrimaryContainer = BlueprintCyanLight,
    secondary = SafetyAmber,
    onSecondary = BlueprintNavyDark,
    secondaryContainer = Color(0xFF4A3500),
    onSecondaryContainer = Color(0xFFFFE08A),
    tertiary = SiteGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF144523),
    onTertiaryContainer = SiteGreenContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = Color(0xFFFF6B6B)
)

private val LightColorScheme = lightColorScheme(
    primary = BlueprintNavy,
    onPrimary = Color.White,
    primaryContainer = SurfaceBlueprintCard,
    onPrimaryContainer = BlueprintNavyDark,
    secondary = SafetyOrange,
    onSecondary = Color.White,
    secondaryContainer = AmberContainerLight,
    onSecondaryContainer = AmberOnContainerLight,
    tertiary = SiteGreen,
    onTertiary = Color.White,
    tertiaryContainer = SiteGreenContainer,
    onTertiaryContainer = Color(0xFF0A3618),
    background = PaperBackgroundLight,
    onBackground = InkDark,
    surface = SurfaceWhite,
    onSurface = InkDark,
    surfaceVariant = SurfaceBlueprintCard,
    onSurfaceVariant = InkMuted,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
