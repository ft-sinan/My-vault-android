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
import com.ftsinan.myvault.data.settings.AccentColor

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

/** Fixed swatch shown beside each accent option in Settings. */
fun accentColorValue(accent: AccentColor): Color = when (accent) {
    AccentColor.TEAL -> Color(0xFF00696D)
    AccentColor.NAVY -> Color(0xFF245A94)
    AccentColor.EMERALD -> Color(0xFF147348)
    AccentColor.INDIGO -> Color(0xFF4655A5)
}

private fun brandLightColorScheme(accent: AccentColor) = when (accent) {
    AccentColor.TEAL -> LightColorScheme
    AccentColor.NAVY -> LightColorScheme.copy(
        primary = Color(0xFF245A94),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E6F7),
        onPrimaryContainer = Color(0xFF0A2A47),
        secondary = Color(0xFF48607A),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD9E5F2),
        onSecondaryContainer = Color(0xFF102A42),
        tertiary = Color(0xFF596B4F),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDCE8D2),
        onTertiaryContainer = Color(0xFF1B2A17)
    )
    AccentColor.EMERALD -> LightColorScheme.copy(
        primary = Color(0xFF147348),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC7EBD4),
        onPrimaryContainer = Color(0xFF06361E),
        secondary = Color(0xFF466354),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD3E9DC),
        onSecondaryContainer = Color(0xFF102A1E),
        tertiary = Color(0xFF496177),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD3E4F6),
        onTertiaryContainer = Color(0xFF122A3E)
    )
    AccentColor.INDIGO -> LightColorScheme.copy(
        primary = Color(0xFF4655A5),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDEE2FF),
        onPrimaryContainer = Color(0xFF101A4D),
        secondary = Color(0xFF596080),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFDFE2F2),
        onSecondaryContainer = Color(0xFF1A2036),
        tertiary = Color(0xFF00696D),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF9FF0F3),
        onTertiaryContainer = Color(0xFF002021)
    )
}

private fun brandDarkColorScheme(accent: AccentColor) = when (accent) {
    AccentColor.TEAL -> DarkColorScheme
    AccentColor.NAVY -> DarkColorScheme.copy(
        primary = Color(0xFFA8C9F2),
        onPrimary = Color(0xFF0B2A4A),
        primaryContainer = Color(0xFF173B61),
        onPrimaryContainer = Color(0xFFD6E6F7),
        secondary = Color(0xFFB9C9DA),
        onSecondary = Color(0xFF20374C),
        secondaryContainer = Color(0xFF364E66),
        onSecondaryContainer = Color(0xFFD9E5F2),
        tertiary = Color(0xFFBCCBAD),
        onTertiary = Color(0xFF26361E),
        tertiaryContainer = Color(0xFF3E4E33),
        onTertiaryContainer = Color(0xFFDCE8D2)
    )
    AccentColor.EMERALD -> DarkColorScheme.copy(
        primary = Color(0xFF8BD8AC),
        onPrimary = Color(0xFF06371F),
        primaryContainer = Color(0xFF14532D),
        onPrimaryContainer = Color(0xFFC7EBD4),
        secondary = Color(0xFFADCEBB),
        onSecondary = Color(0xFF1E3729),
        secondaryContainer = Color(0xFF314F3E),
        onSecondaryContainer = Color(0xFFD3E9DC),
        tertiary = Color(0xFFAACBE3),
        onTertiary = Color(0xFF1D354A),
        tertiaryContainer = Color(0xFF334C63),
        onTertiaryContainer = Color(0xFFD3E4F6)
    )
    AccentColor.INDIGO -> DarkColorScheme.copy(
        primary = Color(0xFFBDC6FF),
        onPrimary = Color(0xFF20295F),
        primaryContainer = Color(0xFF34408C),
        onPrimaryContainer = Color(0xFFE0E3FF),
        secondary = Color(0xFFC1C6DD),
        onSecondary = Color(0xFF2B3047),
        secondaryContainer = Color(0xFF41465F),
        onSecondaryContainer = Color(0xFFDFE2F2),
        tertiary = Color(0xFF83D3D7),
        onTertiary = Color(0xFF003739),
        tertiaryContainer = Color(0xFF004F52),
        onTertiaryContainer = Color(0xFF9FF0F3)
    )
}

/**
 * Material You theme.
 *
 * On Android 12+ (API 31) the app adopts the user's wallpaper-based
 * dynamic colour scheme by default; people can turn that off in
 * Settings and fall back to one of My Vault's professional brand accents.
 */
@Composable
fun MyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    accentColor: AccentColor = AccentColor.TEAL,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> brandDarkColorScheme(accentColor)
        else -> brandLightColorScheme(accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VaultShapes,
        content = content
    )
}
