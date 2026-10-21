package com.ftsinan.myvault.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF83D3D7),
    onPrimary = Color(0xFF003739),
    primaryContainer = Color(0xFF004F52),
    onPrimaryContainer = Color(0xFF9FF0F3),
    secondary = Color(0xFFB1CBCB),
    onSecondary = Color(0xFF1C3435),
    secondaryContainer = Color(0xFF334B4C),
    onSecondaryContainer = Color(0xFFCDE8E9),
    tertiary = Color(0xFFB1C8E8),
    onTertiary = Color(0xFF1A324B),
    tertiaryContainer = Color(0xFF324863),
    onTertiaryContainer = Color(0xFFD3E4FF),
    background = Color(0xFF0E1415),
    onBackground = Color(0xFFDFE4E4),
    surface = Color(0xFF121A1B),
    onSurface = Color(0xFFDFE4E4),
    surfaceVariant = Color(0xFF3F4949),
    onSurfaceVariant = Color(0xFFBFC9C9),
    surfaceContainerLowest = Color(0xFF090F10),
    surfaceContainerLow = Color(0xFF171E1F),
    surfaceContainer = Color(0xFF1B2223),
    surfaceContainerHigh = Color(0xFF262D2E),
    surfaceContainerHighest = Color(0xFF303839),
    outline = Color(0xFF899393),
    outlineVariant = Color(0xFF3F4949),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00696D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9FF0F3),
    onPrimaryContainer = Color(0xFF002021),
    secondary = Color(0xFF4A6364),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE8E9),
    onSecondaryContainer = Color(0xFF051F20),
    tertiary = Color(0xFF48617E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3E4FF),
    onTertiaryContainer = Color(0xFF021C36),
    background = Color(0xFFF7FAFA),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFF7FAFA),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDAE4E4),
    onSurfaceVariant = Color(0xFF3F4949),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEFF5F4),
    surfaceContainer = Color(0xFFE9F0EF),
    surfaceContainerHigh = Color(0xFFE3EAE9),
    surfaceContainerHighest = Color(0xFFDDE4E3),
    outline = Color(0xFF6F7A7A),
    outlineVariant = Color(0xFFBFC9C9),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

/**
 * Material You theme.
 *
 * On Android 12+ (API 31) the app adopts the user's wallpaper-based
 * dynamic colour scheme by default; people can turn that off in
 * Settings and fall back to My Vault's sea-glass teal brand palette.
 */
@Composable
fun MyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VaultShapes,
        content = content
    )
}
