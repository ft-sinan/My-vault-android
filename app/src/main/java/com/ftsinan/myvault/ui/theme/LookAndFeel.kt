package com.ftsinan.myvault.ui.theme

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView

/**
 * "Motion & feel" preferences that app screens read through composition
 * locals. [MyVaultTheme] provides the stored values for the whole tree.
 */

/** Tactile taps on keys and choices (e.g. the PIN pad). */
val LocalHapticsEnabled = staticCompositionLocalOf { true }

/** When false, screens and lists change instantly instead of animating. */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/** Floating, slightly translucent bottom bar instead of the classic solid bar. */
val LocalGlassBottomBar = staticCompositionLocalOf { false }

/**
 * List item placement animation, honoured only when animations are enabled.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.animatedPlacement(): Modifier =
    if (LocalAnimationsEnabled.current) Modifier.animateItem() else Modifier

/** A short tap haptic for selections, respecting the haptics preference. */
@Composable
fun localSelectionHaptic(): () -> Unit {
    val view = LocalView.current
    val enabled = LocalHapticsEnabled.current
    return {
        if (enabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
}
