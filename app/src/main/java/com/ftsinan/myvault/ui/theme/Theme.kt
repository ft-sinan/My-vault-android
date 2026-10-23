package com.ftsinan.myvault.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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


/** Orion's signature indigo + lime scheme, made the default My Vault look. */
private val OrionLightColorScheme = lightColorScheme(
    primary = Color(0xFF4C4ECD),
    onPrimary = Color(0xFFFBF7FF),
    primaryContainer = Color(0xFF7478F8),
    onPrimaryContainer = Color(0xFF000000),
    inversePrimary = Color(0xFF8083FF),
    secondary = Color(0xFF5C5D78),
    onSecondary = Color(0xFFFBF7FF),
    secondaryContainer = Color(0xFFE1E0FF),
    onSecondaryContainer = Color(0xFF4F4F6A),
    tertiary = Color(0xFF456900),
    onTertiary = Color(0xFFEFFFD1),
    tertiaryContainer = Color(0xFFCCFE82),
    onTertiaryContainer = Color(0xFF406200),
    background = Color(0xFFFCF8FE),
    onBackground = Color(0xFF32323B),
    surface = Color(0xFFFCF8FE),
    onSurface = Color(0xFF32323B),
    surfaceVariant = Color(0xFFE4E1ED),
    onSurfaceVariant = Color(0xFF5F5E68),
    surfaceTint = Color(0xFF4C4ECD),
    inverseSurface = Color(0xFF0E0E12),
    inverseOnSurface = Color(0xFF9E9CA2),
    error = Color(0xFFA8364B),
    onError = Color(0xFFFFF7F7),
    errorContainer = Color(0xFFF97386),
    onErrorContainer = Color(0xFF6E0523),
    outline = Color(0xFF7B7984),
    outlineVariant = Color(0xFFB3B0BC),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFCF8FE),
    surfaceContainer = Color(0xFFF0ECF6),
    surfaceContainerHigh = Color(0xFFEAE7F1),
    surfaceContainerHighest = Color(0xFFE4E1ED),
    surfaceContainerLow = Color(0xFFF6F2FB),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFDBD8E4)
)

private val OrionDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC0C1FF),
    onPrimary = Color(0xFF2A29AD),
    primaryContainer = Color(0xFF3F40C0),
    onPrimaryContainer = Color(0xFFE2E0FF),
    inversePrimary = Color(0xFF4C4ECD),
    secondary = Color(0xFFC5C4E3),
    onSecondary = Color(0xFF3D3E58),
    secondaryContainer = Color(0xFF393953),
    onSecondaryContainer = Color(0xFFBDBCDC),
    tertiary = Color(0xFFF1FFD4),
    onTertiary = Color(0xFF456900),
    tertiaryContainer = Color(0xFFCAFB80),
    onTertiaryContainer = Color(0xFF3F6000),
    background = Color(0xFF0E0E12),
    onBackground = Color(0xFFE7E4F0),
    surface = Color(0xFF0E0E12),
    onSurface = Color(0xFFE7E4F0),
    surfaceVariant = Color(0xFF25252E),
    onSurfaceVariant = Color(0xFFACAAB5),
    surfaceTint = Color(0xFFC0C1FF),
    inverseSurface = Color(0xFFFCF8FE),
    inverseOnSurface = Color(0xFF55545A),
    error = Color(0xFFF97386),
    onError = Color(0xFF490013),
    errorContainer = Color(0xFF871C34),
    onErrorContainer = Color(0xFFFF97A3),
    outline = Color(0xFF76747F),
    outlineVariant = Color(0xFF484750),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2C2B34),
    surfaceContainer = Color(0xFF191920),
    surfaceContainerHigh = Color(0xFF1F1F26),
    surfaceContainerHighest = Color(0xFF25252E),
    surfaceContainerLow = Color(0xFF131318),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceDim = Color(0xFF0E0E12)
)

/** Fixed swatch shown beside each accent option in Settings. */
fun accentColorValue(accent: AccentColor): Color = when (accent) {
    AccentColor.ORION -> Color(0xFF4C4ECD)
    AccentColor.TEAL -> Color(0xFF00696D)
    AccentColor.NAVY -> Color(0xFF245A94)
    AccentColor.EMERALD -> Color(0xFF147348)
    AccentColor.INDIGO -> Color(0xFF4655A5)
    AccentColor.OCEAN -> Color(0xFF006592)
    AccentColor.GRAPE -> Color(0xFF634FA3)
    AccentColor.COCOA -> Color(0xFF845030)
    AccentColor.SLATE -> Color(0xFF46555F)
}

private fun brandLightColorScheme(accent: AccentColor) = when (accent) {
    AccentColor.ORION -> OrionLightColorScheme
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
    AccentColor.OCEAN -> LightColorScheme.copy(
        primary = Color(0xFF006592),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD0E9FB),
        onPrimaryContainer = Color(0xFF00324A),
        secondary = Color(0xFF4A6274),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD6EAF7),
        onSecondaryContainer = Color(0xFF243B4B),
        tertiary = Color(0xFF9C414F),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDADD),
        onTertiaryContainer = Color(0xFF5D1021)
    )
    AccentColor.GRAPE -> LightColorScheme.copy(
        primary = Color(0xFF634FA3),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE6DEFF),
        onPrimaryContainer = Color(0xFF1B0B4D),
        secondary = Color(0xFF5F5A78),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE3DFF5),
        onSecondaryContainer = Color(0xFF1B1930),
        tertiary = Color(0xFF96426E),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E8),
        onTertiaryContainer = Color(0xFF3B0A24)
    )
    AccentColor.COCOA -> LightColorScheme.copy(
        primary = Color(0xFF845030),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDBCB),
        onPrimaryContainer = Color(0xFF341100),
        secondary = Color(0xFF755747),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEDDCC),
        onSecondaryContainer = Color(0xFF2A170B),
        tertiary = Color(0xFF596331),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDCE8B1),
        onTertiaryContainer = Color(0xFF1A2000)
    )
    AccentColor.SLATE -> LightColorScheme.copy(
        primary = Color(0xFF46555F),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD8E4ED),
        onPrimaryContainer = Color(0xFF0C1D29),
        secondary = Color(0xFF5A666E),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0E9EF),
        onSecondaryContainer = Color(0xFF17242B),
        tertiary = Color(0xFF5A6080),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDEE1F9),
        onTertiaryContainer = Color(0xFF171A33)
    )
}

private fun brandDarkColorScheme(accent: AccentColor) = when (accent) {
    AccentColor.ORION -> OrionDarkColorScheme
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
    AccentColor.OCEAN -> DarkColorScheme.copy(
        primary = Color(0xFF89CEFF),
        onPrimary = Color(0xFF004564),
        primaryContainer = Color(0xFF004C73),
        onPrimaryContainer = Color(0xFFC9E8FF),
        secondary = Color(0xFFB1CADE),
        onSecondary = Color(0xFF2B4354),
        secondaryContainer = Color(0xFF3D5569),
        onSecondaryContainer = Color(0xFFCDE6FB),
        tertiary = Color(0xFFFFA2AB),
        onTertiary = Color(0xFF6A1B2A),
        tertiaryContainer = Color(0xFF82313F),
        onTertiaryContainer = Color(0xFFFFDADD)
    )
    AccentColor.GRAPE -> DarkColorScheme.copy(
        primary = Color(0xFFCDBDFF),
        onPrimary = Color(0xFF332063),
        primaryContainer = Color(0xFF4A3785),
        onPrimaryContainer = Color(0xFFE6DEFF),
        secondary = Color(0xFFC6C0DC),
        onSecondary = Color(0xFF302D47),
        secondaryContainer = Color(0xFF464260),
        onSecondaryContainer = Color(0xFFE3DFF5),
        tertiary = Color(0xFFECB8D1),
        onTertiary = Color(0xFF4E1E38),
        tertiaryContainer = Color(0xFF6E3350),
        onTertiaryContainer = Color(0xFFFFD8E8)
    )
    AccentColor.COCOA -> DarkColorScheme.copy(
        primary = Color(0xFFF2BC97),
        onPrimary = Color(0xFF4E2606),
        primaryContainer = Color(0xFF6A3A1D),
        onPrimaryContainer = Color(0xFFFFDBCB),
        secondary = Color(0xFFDFC0B0),
        onSecondary = Color(0xFF3F2B1F),
        secondaryContainer = Color(0xFF584236),
        onSecondaryContainer = Color(0xFFFEDDCC),
        tertiary = Color(0xFFC1CD97),
        onTertiary = Color(0xFF2D360E),
        tertiaryContainer = Color(0xFF444E22),
        onTertiaryContainer = Color(0xFFDCE8B1)
    )
    AccentColor.SLATE -> DarkColorScheme.copy(
        primary = Color(0xFFB5C6D2),
        onPrimary = Color(0xFF1E2D38),
        primaryContainer = Color(0xFF35434E),
        onPrimaryContainer = Color(0xFFD8E4ED),
        secondary = Color(0xFFBCC7CF),
        onSecondary = Color(0xFF263239),
        secondaryContainer = Color(0xFF3D494F),
        onSecondaryContainer = Color(0xFFE0E9EF),
        tertiary = Color(0xFFC0C4E6),
        onTertiary = Color(0xFF2B2E4A),
        tertiaryContainer = Color(0xFF434761),
        onTertiaryContainer = Color(0xFFDEE1F9)
    )
}

/** True-black surfaces over any dark scheme, for OLED screens. */
fun ColorScheme.asOled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0B0B0B),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFF1F1F1F),
    surfaceBright = Color(0xFF272727)
)

/** True when this scheme's background is a dark colour. */
val ColorScheme.isDarkScheme: Boolean get() = background.luminance() < 0.5f

/**
 * Resolves the effective colour scheme for the given look. Shared by
 * [MyVaultTheme] and the Settings theme/palette previews so what people
 * pick is exactly what they get.
 */
fun vaultColorScheme(
    context: Context,
    darkTheme: Boolean,
    oled: Boolean,
    useDynamicColor: Boolean,
    accentColor: AccentColor
): ColorScheme {
    val scheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> brandDarkColorScheme(accentColor)
        else -> brandLightColorScheme(accentColor)
    }
    return if (oled && darkTheme) scheme.asOled() else scheme
}



@Composable
private fun animatedSchemeColor(color: Color, spec: AnimationSpec<Color>, label: String): Color {
    val value by animateColorAsState(color, spec, label = label)
    return value
}

/** Smoothly blends every main colour role when the theme or palette changes. */
@Composable
private fun rememberAnimatedColorScheme(
    target: ColorScheme,
    animationsEnabled: Boolean
): ColorScheme {
    val spec: AnimationSpec<Color> = if (animationsEnabled) {
        spring(dampingRatio = 0.92f, stiffness = 240f)
    } else {
        snap()
    }
    return target.copy(
        primary = animatedSchemeColor(target.primary, spec, "primary"),
        onPrimary = animatedSchemeColor(target.onPrimary, spec, "onPrimary"),
        primaryContainer = animatedSchemeColor(target.primaryContainer, spec, "primaryContainer"),
        onPrimaryContainer = animatedSchemeColor(target.onPrimaryContainer, spec, "onPrimaryContainer"),
        inversePrimary = animatedSchemeColor(target.inversePrimary, spec, "inversePrimary"),
        secondary = animatedSchemeColor(target.secondary, spec, "secondary"),
        onSecondary = animatedSchemeColor(target.onSecondary, spec, "onSecondary"),
        secondaryContainer = animatedSchemeColor(target.secondaryContainer, spec, "secondaryContainer"),
        onSecondaryContainer = animatedSchemeColor(target.onSecondaryContainer, spec, "onSecondaryContainer"),
        tertiary = animatedSchemeColor(target.tertiary, spec, "tertiary"),
        onTertiary = animatedSchemeColor(target.onTertiary, spec, "onTertiary"),
        tertiaryContainer = animatedSchemeColor(target.tertiaryContainer, spec, "tertiaryContainer"),
        onTertiaryContainer = animatedSchemeColor(target.onTertiaryContainer, spec, "onTertiaryContainer"),
        background = animatedSchemeColor(target.background, spec, "background"),
        onBackground = animatedSchemeColor(target.onBackground, spec, "onBackground"),
        surface = animatedSchemeColor(target.surface, spec, "surface"),
        onSurface = animatedSchemeColor(target.onSurface, spec, "onSurface"),
        surfaceVariant = animatedSchemeColor(target.surfaceVariant, spec, "surfaceVariant"),
        onSurfaceVariant = animatedSchemeColor(target.onSurfaceVariant, spec, "onSurfaceVariant"),
        surfaceTint = animatedSchemeColor(target.surfaceTint, spec, "surfaceTint"),
        inverseSurface = animatedSchemeColor(target.inverseSurface, spec, "inverseSurface"),
        inverseOnSurface = animatedSchemeColor(target.inverseOnSurface, spec, "inverseOnSurface"),
        error = animatedSchemeColor(target.error, spec, "error"),
        onError = animatedSchemeColor(target.onError, spec, "onError"),
        errorContainer = animatedSchemeColor(target.errorContainer, spec, "errorContainer"),
        onErrorContainer = animatedSchemeColor(target.onErrorContainer, spec, "onErrorContainer"),
        outline = animatedSchemeColor(target.outline, spec, "outline"),
        outlineVariant = animatedSchemeColor(target.outlineVariant, spec, "outlineVariant"),
        scrim = animatedSchemeColor(target.scrim, spec, "scrim"),
        surfaceBright = animatedSchemeColor(target.surfaceBright, spec, "surfaceBright"),
        surfaceContainer = animatedSchemeColor(target.surfaceContainer, spec, "surfaceContainer"),
        surfaceContainerHigh = animatedSchemeColor(target.surfaceContainerHigh, spec, "surfaceContainerHigh"),
        surfaceContainerHighest = animatedSchemeColor(target.surfaceContainerHighest, spec, "surfaceContainerHighest"),
        surfaceContainerLow = animatedSchemeColor(target.surfaceContainerLow, spec, "surfaceContainerLow"),
        surfaceContainerLowest = animatedSchemeColor(target.surfaceContainerLowest, spec, "surfaceContainerLowest"),
        surfaceDim = animatedSchemeColor(target.surfaceDim, spec, "surfaceDim")
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
    oled: Boolean = false,
    useDynamicColor: Boolean = false,
    accentColor: AccentColor = AccentColor.ORION,
    hapticsEnabled: Boolean = true,
    compactMode: Boolean = false,
    reduceAnimations: Boolean = false,
    glassBottomBar: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val targetColorScheme = remember(context, darkTheme, oled, useDynamicColor, accentColor) {
        vaultColorScheme(context, darkTheme, oled, useDynamicColor, accentColor)
    }
    val colorScheme = rememberAnimatedColorScheme(
        target = targetColorScheme,
        animationsEnabled = !reduceAnimations
    )

    // Compact mode renders the whole UI at a slightly smaller density,
    // so rows and spacing get denser everywhere at once.
    val baseDensity = LocalDensity.current
    val density = remember(baseDensity, compactMode) {
        if (compactMode) {
            Density(baseDensity.density * 0.92f, baseDensity.fontScale)
        } else {
            baseDensity
        }
    }

    CompositionLocalProvider(
        LocalDensity provides density,
        LocalHapticsEnabled provides hapticsEnabled,
        LocalAnimationsEnabled provides !reduceAnimations,
        LocalGlassBottomBar provides glassBottomBar
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = VaultShapes,
            content = content
        )
    }
}
