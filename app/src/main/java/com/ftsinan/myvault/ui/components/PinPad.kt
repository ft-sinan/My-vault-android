package com.ftsinan.myvault.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import com.ftsinan.myvault.data.security.PinManager

@Composable
fun PinDots(
    entered: Int,
    modifier: Modifier = Modifier,
    total: Int = PinManager.PIN_LENGTH,
    isError: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            PinDot(filled = index < entered, isError = isError)
        }
    }
}

@Composable
private fun PinDot(filled: Boolean, isError: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (filled) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
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
            .size(18.dp)
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
    onBiometricClick: (() -> Unit)? = null
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                row.forEach { digit ->
                    PinKey(label = digit, onClick = { onDigit(digit) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            // Bottom-left: fingerprint shortcut (lock screen only)
            if (onBiometricClick != null) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBiometricClick, modifier = Modifier.size(76.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = stringResource(R.string.pin_biometric_content_description),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(76.dp))
            }
            PinKey(label = "0", onClick = { onDigit("0") })
            IconButton(onClick = onBackspace, modifier = Modifier.size(76.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.pin_delete_digit_content_description),
                    modifier = Modifier.size(29.dp)
                )
            }
        }
    }
}

@Composable
private fun PinKey(label: String, onClick: () -> Unit) {
    val view = LocalView.current
    TextButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
