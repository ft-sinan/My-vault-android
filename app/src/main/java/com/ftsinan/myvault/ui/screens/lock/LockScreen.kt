package com.ftsinan.myvault.ui.screens.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.ui.components.PinDots
import com.ftsinan.myvault.ui.components.MorphingShapeBox
import com.ftsinan.myvault.ui.components.PinPad
import com.ftsinan.myvault.ui.components.VaultMark

@Composable
fun LockScreen(viewModel: LockViewModel, ownerName: String? = null) {
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
        title = if (!ownerName.isNullOrBlank()) {
            stringResource(R.string.lock_welcome_back, ownerName)
        } else {
            stringResource(R.string.lock_my_vault)
        },
        subtitle = stringResource(R.string.lock_enter_pin),
        pinLength = pin.length,
        error = uiState.error?.let { message ->
            if (message == "WRONG_PIN") stringResource(R.string.pin_wrong) else message
        },
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
    val mismatchText = stringResource(R.string.pin_mismatch)

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
                        error = mismatchText
                        firstPin = ""
                        pin = ""
                        step = SetupStep.ENTER
                    }
                }
            }
        }
    }

    LockScaffold(
        title = stringResource(
            if (step == SetupStep.ENTER) R.string.pin_create_title
            else R.string.pin_confirm_title
        ),
        subtitle = stringResource(
            if (step == SetupStep.ENTER) R.string.pin_create_subtitle
            else R.string.pin_confirm_subtitle
        ),
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.size(122.dp), contentAlignment = Alignment.Center) {
                    MorphingShapeBox(
                        modifier = Modifier.size(122.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                        lobes = 12,
                        wave = 0.19f,
                        spinMillis = 24_000
                    )
                    VaultMark(size = 88.dp)
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
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = stringResource(R.string.lock_trust_pill),
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
