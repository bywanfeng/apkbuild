package com.wanfeng.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentIndigo,
    tertiary = AccentGreen,
    background = DarkBg0,
    surface = DarkBg45,
    onPrimary = DarkBg0,
    onSecondary = DarkBg0,
    onBackground = ToastText,
    onSurface = ToastText,
    error = StatusRed,
)

private val LightColorScheme = lightColorScheme(
    primary = LaunchMid,
    secondary = AccentBlue,
    tertiary = AccentGreen,
    background = LightBg0,
    surface = LightBg50,
    onPrimary = LightBg0,
    onSecondary = LightBg0,
    onBackground = DarkBg0,
    onSurface = DarkBg0,
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
