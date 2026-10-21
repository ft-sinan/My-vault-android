package com.ftsinan.myvault.ui.screens.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(46.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
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
