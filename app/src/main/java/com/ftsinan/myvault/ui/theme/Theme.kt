package com.ftsinan.myvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Teal40,
    onPrimary = Navy80,
    primaryContainer = Navy60,
    onPrimaryContainer = Navy90,
    secondary = Gold60,
    onSecondary = Navy80,
    background = Color(0xFF0A1622),
    onBackground = Navy95,
    surface = Color(0xFF10233A),
    onSurface = Navy95,
    surfaceVariant = Navy20,
    onSurfaceVariant = Navy90,
    error = Color(0xFFF2B8B5)
)

private val LightColorScheme = lightColorScheme(
    primary = Navy60,
    onPrimary = Color.White,
    primaryContainer = Navy90,
    onPrimaryContainer = Navy80,
    secondary = Teal80,
    onSecondary = Color.White,
    tertiary = Gold60,
    onTertiary = Navy80,
    background = Color(0xFFF7F9FC),
    onBackground = Navy10,
    surface = Color.White,
    onSurface = Navy10,
    surfaceVariant = Navy95,
    onSurfaceVariant = Navy20,
    error = ErrorRed
)

@Composable
fun MyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
