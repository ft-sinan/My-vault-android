package com.ftsinan.myvault.ui.screens.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.ui.components.PinDots
import com.ftsinan.myvault.ui.components.PinPad

@Composable
fun LockScreen(viewModel: LockViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var hasAutoPrompted by remember { mutableStateOf(false) }

    val biometricAvailable = remember { canUseBiometric(context) }
    val showBiometric = uiState.isBiometricEnabled && biometricAvailable

    fun launchBiometric() {
        context.findFragmentActivity()?.let { activity ->
            promptBiometricUnlock(
                activity = activity,
                onSuccess = { viewModel.unlockWithBiometric() }
            )
        }
    }

    // Offer fingerprint/face straight away, like banking apps do.
    LaunchedEffect(uiState.isLoading, uiState.isBiometricEnabled) {
        if (!uiState.isLoading && showBiometric && !hasAutoPrompted) {
            hasAutoPrompted = true
            launchBiometric()
        }
    }

    // Wrong PIN -> clear the dots so the user can retry.
    LaunchedEffect(uiState.failedAttempts) {
        if (uiState.failedAttempts > 0) pin = ""
    }

    LaunchedEffect(pin) {
        if (pin.length == PinManager.PIN_LENGTH) {
            viewModel.unlockWithPin(pin)
        }
    }

    LockScaffold(
        title = "My Vault",
        subtitle = "Enter your PIN to unlock",
        pinLength = pin.length,
        error = uiState.error,
        onDigit = { digit ->
            if (pin.length < PinManager.PIN_LENGTH) pin += digit
        },
        onBackspace = { pin = pin.dropLast(1) },
        onBiometricClick = if (showBiometric) ({ launchBiometric() }) else null
    )
}

private enum class SetupStep { ENTER, CONFIRM }

@Composable
fun SetupPinScreen(viewModel: LockViewModel) {
    var step by remember { mutableStateOf(SetupStep.ENTER) }
    var firstPin by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pin) {
        if (pin.length == PinManager.PIN_LENGTH) {
            when (step) {
                SetupStep.ENTER -> {
                    firstPin = pin
                    pin = ""
                    error = null
                    step = SetupStep.CONFIRM
                }
                SetupStep.CONFIRM -> {
                    if (pin == firstPin) {
                        viewModel.setupPin(pin)
                    } else {
                        error = "PINs don't match. Please start again."
                        firstPin = ""
                        pin = ""
                        step = SetupStep.ENTER
                    }
                }
            }
        }
    }

    LockScaffold(
        title = if (step == SetupStep.ENTER) "Create your PIN" else "Confirm your PIN",
        subtitle = if (step == SetupStep.ENTER) {
            "Choose a 4-digit PIN for your vault.\nRemember it — it cannot be recovered."
        } else {
            "Enter the same PIN once more to confirm."
        },
        pinLength = pin.length,
        error = error,
        onDigit = { digit ->
            if (pin.length < PinManager.PIN_LENGTH) pin += digit
        },
        onBackspace = { pin = pin.dropLast(1) },
        onBiometricClick = null
    )
}

@Composable
private fun LockScaffold(
    title: String,
    subtitle: String,
    pinLength: Int,
    error: String?,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: (() -> Unit)?
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.52f)
        )
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(38.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = 5.dp, y = 5.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "🔒 Offline  •  Encrypted  •  Private",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            PinDots(entered = pinLength, isError = error != null)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = error ?: " ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            PinPad(
                onDigit = onDigit,
                onBackspace = onBackspace,
                onBiometricClick = onBiometricClick
            )
        }
    }
}
