package com.ftsinan.myvault.ui.screens.lock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.ui.components.ShieldKeyholeMark
import com.ftsinan.myvault.ui.components.PinDots
import com.ftsinan.myvault.ui.components.PinPad
import com.ftsinan.myvault.ui.theme.LocalAnimationsEnabled
import kotlinx.coroutines.delay

/**
 * Quiet loading placeholder used only while the real entry state is read.
 * The celebratory opening animation intentionally happens after unlocking,
 * not as a fake cold-start splash.
 */
@Composable
fun VaultStartupScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShieldKeyholeMark(size = 76.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "made with 🤍 by sinan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * A short, polished "vault is opening" moment after a successful PIN or
 * biometric unlock. It lasts less than a second and is skipped entirely
 * when Reduce animations is enabled.
 */
@Composable
fun VaultUnlockAnimationScreen(
    ownerName: String?,
    onFinished: () -> Unit
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!animationsEnabled) {
            onFinished()
            return@LaunchedEffect
        }
        started = true
        delay(840)
        onFinished()
    }

    val markScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.74f,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = 0.58f, stiffness = 360f)
        } else {
            snap()
        },
        label = "unlockMarkScale"
    )
    val markAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = if (animationsEnabled) tween(260) else snap(),
        label = "unlockMarkAlpha"
    )
    val ringScale by animateFloatAsState(
        targetValue = if (started) 1.24f else 0.68f,
        animationSpec = if (animationsEnabled) tween(760) else snap(),
        label = "unlockRingScale"
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (started) 0f else 0.52f,
        animationSpec = if (animationsEnabled) tween(760) else snap(),
        label = "unlockRingAlpha"
    )
    val badgeScale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 340, delayMillis = 280)
        } else {
            snap()
        },
        label = "unlockBadgeScale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = if (animationsEnabled) {
            tween(durationMillis = 360, delayMillis = 180)
        } else {
            snap()
        },
        label = "unlockTextAlpha"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 86.dp, y = (-96).dp)
                    .size(210.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(154.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(132.dp)
                            .graphicsLayer {
                                scaleX = ringScale
                                scaleY = ringScale
                                alpha = ringAlpha
                            }
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .graphicsLayer {
                                scaleX = markScale
                                scaleY = markScale
                                alpha = markAlpha
                            }
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                            )
                    )
                    ShieldKeyholeMark(
                        size = 88.dp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = markScale
                            scaleY = markScale
                            alpha = markAlpha
                        }
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(38.dp)
                            .graphicsLayer {
                                scaleX = badgeScale
                                scaleY = badgeScale
                                alpha = badgeScale
                            }
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = if (!ownerName.isNullOrBlank()) {
                        stringResource(R.string.lock_welcome_back, ownerName)
                    } else {
                        stringResource(R.string.lock_my_vault)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = textAlpha
                        translationY = (1f - textAlpha) * 12.dp.toPx()
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.lock_unlocked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = textAlpha
                        translationY = (1f - textAlpha) * 10.dp.toPx()
                    }
                )
            }

            Text(
                text = "made with 🤍 by sinan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { alpha = textAlpha }
            )
        }
    }
}

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

/**
 * Fixed, professional lock layout. It deliberately does not scroll: the
 * emblem, typography, dots and PIN keys shrink with the available height,
 * while weighted spacers absorb the remaining space on taller phones.
 */
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight
            val tinyHeight = availableHeight < 500.dp
            val veryCompact = availableHeight < 620.dp
            val compact = availableHeight < 730.dp
            val markSize = when {
                tinyHeight -> 50.dp
                veryCompact -> 60.dp
                compact -> 68.dp
                else -> 76.dp
            }
            val keyHorizontalSpacing = when {
                tinyHeight -> 16.dp
                veryCompact -> 18.dp
                compact -> 22.dp
                else -> 26.dp
            }
            val keyVerticalSpacing = when {
                tinyHeight -> 6.dp
                veryCompact -> 8.dp
                compact -> 9.dp
                else -> 10.dp
            }
            val keySizeByHeight = when {
                tinyHeight -> ((availableHeight - 190.dp) * 0.25f).coerceIn(42.dp, 54.dp)
                veryCompact -> 54.dp
                compact -> 62.dp
                availableHeight < 840.dp -> 68.dp
                else -> 72.dp
            }
            val keySize = minOf(
                keySizeByHeight,
                (availableWidth - 48.dp - keyHorizontalSpacing * 2) * 0.334f
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 76.dp, y = (-92).dp)
                    .size(198.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 24.dp,
                        vertical = if (veryCompact) 10.dp else 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(if (veryCompact) 0.22f else 0.48f))

                Box(
                    modifier = Modifier.size(markSize + 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ShieldKeyholeMark(size = markSize)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(if (veryCompact) 24.dp else 28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(if (veryCompact) 14.dp else 16.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(
                        when {
                            tinyHeight -> 8.dp
                            compact -> 16.dp
                            else -> 20.dp
                        }
                    )
                )
                Text(
                    text = title,
                    style = when {
                        tinyHeight -> MaterialTheme.typography.headlineSmall
                        compact -> MaterialTheme.typography.headlineMedium
                        else -> MaterialTheme.typography.headlineLarge
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (availableHeight >= 410.dp) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!veryCompact) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.lock_trust_pill),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(if (veryCompact) 0.28f else 0.36f))

                PinDots(
                    entered = pinLength,
                    isError = error != null,
                    dotSize = when {
                        tinyHeight -> 12.dp
                        veryCompact -> 13.dp
                        else -> 15.dp
                    },
                    spacing = when {
                        tinyHeight -> 12.dp
                        veryCompact -> 13.dp
                        else -> 15.dp
                    }
                )
                Box(
                    modifier = Modifier
                        .padding(top = if (tinyHeight) 4.dp else 8.dp)
                        .heightIn(min = if (tinyHeight) 22.dp else 26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (error != null) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                PinPad(
                    onDigit = onDigit,
                    onBackspace = onBackspace,
                    onBiometricClick = onBiometricClick,
                    keySize = keySize,
                    horizontalSpacing = keyHorizontalSpacing,
                    verticalSpacing = keyVerticalSpacing
                )

                Spacer(modifier = Modifier.weight(0.28f))
                if (!veryCompact) {
                    Text(
                        text = "made with 🤍 by sinan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
