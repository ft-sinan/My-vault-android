package com.ftsinan.myvault.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ftsinan.myvault.R
import com.ftsinan.myvault.ui.theme.LocalAnimationsEnabled
import com.ftsinan.myvault.ui.theme.LocalHapticsEnabled
import com.ftsinan.myvault.data.security.PinManager

@Composable
fun PinDots(
    entered: Int,
    modifier: Modifier = Modifier,
    total: Int = PinManager.PIN_LENGTH,
    isError: Boolean = false,
    dotSize: androidx.compose.ui.unit.Dp = 18.dp,
    spacing: androidx.compose.ui.unit.Dp = 18.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            PinDot(filled = index < entered, isError = isError, size = dotSize)
        }
    }
}

@Composable
private fun PinDot(
    filled: Boolean,
    isError: Boolean,
    size: androidx.compose.ui.unit.Dp = 18.dp
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val scale by animateFloatAsState(
        targetValue = if (filled) 1.18f else 1f,
        animationSpec = if (animationsEnabled) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        } else {
            snap()
        },
        label = "pinDotScale"
    )
    val animatedColor by animateColorAsState(
        targetValue = when {
            filled && isError -> MaterialTheme.colorScheme.error
            filled -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "pinDotColor"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(animatedColor)
            .then(
                if (filled) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.5.dp,
                        color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                }
            )
    )
}

@Composable
fun PinPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    onBiometricClick: (() -> Unit)? = null,
    keySize: androidx.compose.ui.unit.Dp = 76.dp,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 26.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 10.dp
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
                row.forEach { digit ->
                    PinKey(label = digit, size = keySize, onClick = { onDigit(digit) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
            // Bottom-left: fingerprint shortcut (lock screen only)
            if (onBiometricClick != null) {
                Box(
                    modifier = Modifier
                        .size(keySize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .springPress(CircleShape, onClick = onBiometricClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = stringResource(R.string.pin_biometric_content_description),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(keySize * 0.45f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(keySize))
            }
            PinKey(label = "0", size = keySize, onClick = { onDigit("0") })
            IconButton(onClick = onBackspace, modifier = Modifier.size(keySize)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.pin_delete_digit_content_description),
                    modifier = Modifier.size(keySize * 0.38f)
                )
            }
        }
    }
}

@Composable
private fun PinKey(
    label: String,
    size: androidx.compose.ui.unit.Dp = 76.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .springPress(CircleShape, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
