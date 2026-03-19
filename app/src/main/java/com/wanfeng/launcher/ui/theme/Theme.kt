package com.wanfeng.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentIndigo,
    tertiary = AccentGreen,
    background = DarkBg0,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkBg0,
    onSecondary = DarkBg0,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = DarkSubText,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.7f),
    error = StatusRed,
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentIndigo,
    tertiary = AccentGreen,
    background = LightBg0,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = LightSubText,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.7f),
    error = StatusRed,
)

@Composable
fun WanfengLauncherTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
