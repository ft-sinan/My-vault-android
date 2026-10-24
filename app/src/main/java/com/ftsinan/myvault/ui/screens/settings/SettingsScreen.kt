package com.ftsinan.myvault.ui.screens.settings

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.BuildConfig
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.backup.BackupProtection
import com.ftsinan.myvault.data.backup.BackupRestoreMode
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.data.settings.AccentColor
import com.ftsinan.myvault.data.settings.AppLanguage
import com.ftsinan.myvault.data.settings.ThemeMode
import com.ftsinan.myvault.ui.components.BackupOwnerSelector
import com.ftsinan.myvault.ui.components.BackupRestoreModeSelector
import com.ftsinan.myvault.ui.components.ExpressiveChip
import com.ftsinan.myvault.ui.components.ExpressiveStat
import com.ftsinan.myvault.ui.components.ExpressiveHeroHeader
import com.ftsinan.myvault.ui.components.HeaderCircleButton
import com.ftsinan.myvault.ui.components.BlobShape
import com.ftsinan.myvault.ui.components.MorphingShapeBox
import com.ftsinan.myvault.ui.components.VaultMark
import com.ftsinan.myvault.ui.components.expressivePastel
import com.ftsinan.myvault.ui.components.expressiveReveal
import com.ftsinan.myvault.ui.components.readableContentColor
import com.ftsinan.myvault.ui.components.springPress
import com.ftsinan.myvault.ui.screens.lock.canUseBiometric
import com.ftsinan.myvault.ui.theme.LocalAnimationsEnabled
import com.ftsinan.myvault.ui.theme.isDarkScheme
import com.ftsinan.myvault.ui.theme.localSelectionHaptic
import com.ftsinan.myvault.ui.theme.vaultColorScheme
import com.ftsinan.myvault.util.AppUpdateManager
import com.ftsinan.myvault.util.FormatUtils
import com.ftsinan.myvault.util.UpdateRelease
import com.ftsinan.myvault.util.UpdateVersion
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class SettingsPage {
    HUB,
    APPEARANCE,
    MOTION,
    SECURITY,
    LANGUAGE,
    STORAGE,
    ACKNOWLEDGEMENTS
}

private sealed interface UpdateStatus {
    object Idle : UpdateStatus
    object Checking : UpdateStatus
    object UpToDate : UpdateStatus
    data class Available(val release: UpdateRelease) : UpdateStatus
    data class Downloading(val release: UpdateRelease, val progress: Int?) : UpdateStatus
    data class Verifying(val release: UpdateRelease) : UpdateStatus
    data class NeedsInstallPermission(val release: UpdateRelease) : UpdateStatus
    data class Failed(@StringRes val messageRes: Int) : UpdateStatus
}

private const val SETTINGS_UPDATE_CHECK_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLock: () -> Unit,
    onManageMembers: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsStateWithLifecycle()
    val compactMode by viewModel.compactMode.collectAsStateWithLifecycle()
    val reduceAnimations by viewModel.reduceAnimations.collectAsStateWithLifecycle()
    val glassBottomBar by viewModel.glassBottomBar.collectAsStateWithLifecycle()
    val lastSettingsUpdateCheckAt by viewModel.lastSettingsUpdateCheckAt
        .collectAsStateWithLifecycle()
    val selectionHaptic = localSelectionHaptic()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val documentCount by viewModel.documentCount.collectAsStateWithLifecycle()
    val totalSize by viewModel.totalSize.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val screenshotAllowanceExpiresAt by viewModel.screenshotAllowanceExpiresAt
        .collectAsStateWithLifecycle()
    val screenshotsTemporarilyAllowed =
        screenshotAllowanceExpiresAt > System.currentTimeMillis()
    val biometricAvailable = remember { canUseBiometric(context) }
    val dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    // Clear the PIN fields once a change succeeded.
    LaunchedEffect(message) {
        if (message?.isError == false) {
            currentPin = ""
            newPin = ""
            confirmPin = ""
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var page by rememberSaveable { mutableStateOf(SettingsPage.HUB) }
    var acknowledgementsReturnPage by rememberSaveable { mutableStateOf(SettingsPage.HUB) }

    fun openSettingsPage(key: String) {
        when (key) {
            "appearance" -> page = SettingsPage.APPEARANCE
            "motion" -> page = SettingsPage.MOTION
            "security" -> page = SettingsPage.SECURITY
            "language" -> page = SettingsPage.LANGUAGE
            "storage" -> page = SettingsPage.STORAGE
            "family" -> onManageMembers()
        }
    }

    fun openAcknowledgements(from: SettingsPage) {
        acknowledgementsReturnPage = from
        page = SettingsPage.ACKNOWLEDGEMENTS
    }

    fun closeSettingsSubpage() {
        page = if (page == SettingsPage.ACKNOWLEDGEMENTS) {
            acknowledgementsReturnPage
        } else {
            SettingsPage.HUB
        }
    }

    BackHandler(enabled = page != SettingsPage.HUB, onBack = ::closeSettingsSubpage)

    var updateStatus by remember { mutableStateOf<UpdateStatus>(UpdateStatus.Idle) }
    var downloadedUpdate by remember { mutableStateOf<File?>(null) }
    var showUpdateAvailableDialog by remember { mutableStateOf(false) }
    var automaticUpdateCheckStarted by remember { mutableStateOf(false) }

    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var showScreenshotAllowanceDialog by remember { mutableStateOf(false) }
    var pendingBackupProtection by remember { mutableStateOf(BackupProtection.PASSWORD) }
    var pendingBackupPassword by remember { mutableStateOf<String?>(null) }
    var restorePassword by remember { mutableStateOf("") }
    var restoreCurrentPin by remember { mutableStateOf("") }
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            viewModel.createBackup(
                destination = uri,
                protection = pendingBackupProtection,
                password = pendingBackupPassword
            )
        }
        pendingBackupPassword = null
    }
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onRestoreFileSelected(uri)
    }

    fun suggestedBackupFileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "My-Vault-Backup-$stamp.mvbackup"
    }

    if (showScreenshotAllowanceDialog) {
        ScreenshotAllowanceDialog(
            onDismiss = { showScreenshotAllowanceDialog = false },
            onVerifyPin = viewModel::verifyCurrentPin,
            onAllow = {
                viewModel.allowScreenshotsTemporarily()
                showScreenshotAllowanceDialog = false
            }
        )
    }

    if (showCreateBackupDialog) {
        CreateBackupDialog(
            onDismiss = { showCreateBackupDialog = false },
            onVerifyPin = { pin -> viewModel.verifyCurrentPin(pin) },
            onChooseLocation = { protection, password ->
                pendingBackupProtection = protection
                pendingBackupPassword = password
                showCreateBackupDialog = false
                createBackupLauncher.launch(suggestedBackupFileName())
            }
        )
    }

    if (backupState.phase == BackupPhase.PASSWORD_REQUIRED) {
        AlertDialog(
            onDismissRequest = {
                restorePassword = ""
                viewModel.cancelRestore()
            },
            title = { Text(stringResource(R.string.backup_restore_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.backup_restore_password_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    BackupPasswordField(
                        value = restorePassword,
                        onValueChange = {
                            restorePassword = it
                            viewModel.clearBackupMessage()
                        },
                        label = stringResource(R.string.backup_password)
                    )
                    backupState.messageRes?.let { messageRes ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(messageRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entered = restorePassword
                        restorePassword = ""
                        viewModel.prepareProtectedRestore(entered)
                    },
                    enabled = restorePassword.isNotEmpty()
                ) {
                    Text(stringResource(R.string.backup_check))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        restorePassword = ""
                        viewModel.cancelRestore()
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (backupState.phase == BackupPhase.READY || backupState.phase == BackupPhase.RESTORING) {
        val summary = backupState.summary
        if (summary != null) {
            AlertDialog(
                onDismissRequest = {
                    if (backupState.phase != BackupPhase.RESTORING) {
                        restoreCurrentPin = ""
                        viewModel.cancelRestore()
                    }
                },
                title = { Text(stringResource(R.string.backup_restore_title)) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(
                                R.string.backup_summary,
                                summary.memberCount,
                                summary.documentCount,
                                summary.pageCount
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (summary.createdAt > 0L) {
                            Text(
                                text = stringResource(
                                    R.string.backup_created_on,
                                    FormatUtils.formatDate(summary.createdAt)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = stringResource(
                                if (summary.protection == BackupProtection.PASSWORD) {
                                    R.string.backup_password_protected
                                } else {
                                    R.string.backup_unprotected
                                }
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))
                        BackupRestoreModeSelector(
                            selectedMode = backupState.restoreMode,
                            enabled = backupState.phase == BackupPhase.READY,
                            onSelect = viewModel::selectRestoreMode
                        )
                        if (
                            backupState.restoreMode == BackupRestoreMode.REPLACE_VAULT &&
                            backupState.profileOptions.isNotEmpty()
                        ) {
                            Spacer(Modifier.height(14.dp))
                            BackupOwnerSelector(
                                options = backupState.profileOptions,
                                selectedBackupMemberId = backupState.selectedOwnerBackupId,
                                createNewProfileSelected = backupState.createNewOwnerSelected,
                                enabled = backupState.phase == BackupPhase.READY,
                                onSelect = viewModel::selectRestoreOwner,
                                onCreateNewProfile = viewModel::selectCreateNewOwnerProfile
                            )
                        }
                        if (
                            backupState.restoreMode == BackupRestoreMode.ADD_TO_VAULT &&
                            backupState.profileOptions.isNotEmpty()
                        ) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.backup_merge_owner_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.backup_current_pin_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        PinField(
                            value = restoreCurrentPin,
                            onValueChange = {
                                restoreCurrentPin = it
                                viewModel.clearBackupMessage()
                            },
                            label = stringResource(R.string.settings_current_pin)
                        )
                        backupState.messageRes?.let { messageRes ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(messageRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (backupState.phase == BackupPhase.RESTORING) {
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.backup_restoring))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val enteredPin = restoreCurrentPin
                            restoreCurrentPin = ""
                            viewModel.commitRestore(enteredPin)
                        },
                        enabled = restoreCurrentPin.length == PinManager.PIN_LENGTH &&
                            backupState.phase == BackupPhase.READY &&
                            (backupState.restoreMode == BackupRestoreMode.ADD_TO_VAULT ||
                                backupState.profileOptions.isEmpty() ||
                                backupState.createNewOwnerSelected ||
                                backupState.selectedOwnerBackupId != null)
                    ) {
                        Text(
                            stringResource(
                                if (backupState.restoreMode ==
                                    BackupRestoreMode.ADD_TO_VAULT
                                ) {
                                    R.string.backup_add_action
                                } else {
                                    R.string.backup_replace
                                }
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            restoreCurrentPin = ""
                            viewModel.cancelRestore()
                        },
                        enabled = backupState.phase == BackupPhase.READY
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }

    fun runUpdateCheck(automatic: Boolean) {
        if (updateStatus is UpdateStatus.Checking ||
            updateStatus is UpdateStatus.Downloading ||
            updateStatus is UpdateStatus.Verifying
        ) return

        viewModel.markSettingsUpdateChecked(System.currentTimeMillis())
        coroutineScope.launch {
            updateStatus = UpdateStatus.Checking
            runCatching { AppUpdateManager.fetchLatestRelease() }
                .onSuccess { release ->
                    if (UpdateVersion.isNewer(release.versionName, BuildConfig.VERSION_NAME)) {
                        downloadedUpdate = null
                        updateStatus = UpdateStatus.Available(release)
                        if (automatic) showUpdateAvailableDialog = true
                    } else {
                        updateStatus = UpdateStatus.UpToDate
                    }
                }
                .onFailure {
                    updateStatus = if (automatic) {
                        UpdateStatus.Idle
                    } else {
                        UpdateStatus.Failed(R.string.settings_updates_failed)
                    }
                }
        }
    }

    fun installDownloadedUpdate(release: UpdateRelease) {
        val apk = downloadedUpdate ?: return
        if (!AppUpdateManager.canInstallPackages(context)) {
            updateStatus = UpdateStatus.NeedsInstallPermission(release)
            return
        }
        runCatching { AppUpdateManager.openInstaller(context, apk) }
            .onSuccess { updateStatus = UpdateStatus.Available(release) }
            .onFailure {
                updateStatus = UpdateStatus.Failed(R.string.settings_updates_open_installer_failed)
            }
    }

    fun downloadUpdate(release: UpdateRelease) {
        if (downloadedUpdate != null) {
            installDownloadedUpdate(release)
            return
        }
        coroutineScope.launch {
            try {
                updateStatus = UpdateStatus.Downloading(release, null)
                val apk = AppUpdateManager.downloadReleaseApk(context, release) { progress ->
                    updateStatus = UpdateStatus.Downloading(release, progress)
                }
                updateStatus = UpdateStatus.Verifying(release)
                val verified = AppUpdateManager.verifyChecksum(release, apk)
                if (release.checksumDownloadUrl != null && verified != true) {
                    apk.delete()
                    updateStatus = UpdateStatus.Failed(R.string.settings_updates_checksum_failed)
                    return@launch
                }
                downloadedUpdate = apk
                if (AppUpdateManager.canInstallPackages(context)) {
                    installDownloadedUpdate(release)
                } else {
                    updateStatus = UpdateStatus.NeedsInstallPermission(release)
                }
            } catch (_: Exception) {
                updateStatus = UpdateStatus.Failed(R.string.settings_updates_download_failed)
            }
        }
    }

    LaunchedEffect(lastSettingsUpdateCheckAt) {
        val lastCheck = lastSettingsUpdateCheckAt ?: return@LaunchedEffect
        if (!automaticUpdateCheckStarted &&
            updateStatus == UpdateStatus.Idle &&
            System.currentTimeMillis() - lastCheck >=
                SETTINGS_UPDATE_CHECK_INTERVAL_MILLIS
        ) {
            automaticUpdateCheckStarted = true
            runUpdateCheck(automatic = true)
        }
    }

    val updateAvailableRelease =
        (updateStatus as? UpdateStatus.Available)?.release
    if (showUpdateAvailableDialog && updateAvailableRelease != null) {
        AlertDialog(
            onDismissRequest = { showUpdateAvailableDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null
                )
            },
            title = {
                Text(stringResource(R.string.settings_updates_available_title))
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    UpdateReleaseDetails(updateAvailableRelease)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateAvailableDialog = false
                        page = SettingsPage.STORAGE
                        downloadUpdate(updateAvailableRelease)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_updates_download))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUpdateAvailableDialog = false }
                ) {
                    Text(stringResource(R.string.settings_updates_later))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                val duration = if (reduceAnimations) 0 else 280
                val forward = targetState.ordinal >= initialState.ordinal
                val direction = if (forward) 1 else -1
                (
                    fadeIn(tween(duration)) +
                        slideInHorizontally(tween(duration)) { width -> direction * width / 10 }
                    ).togetherWith(
                    fadeOut(tween(duration)) +
                        slideOutHorizontally(tween(duration)) { width -> -direction * width / 10 }
                )
            },
            label = "settingsPage"
        ) { targetPage ->
            val pageScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(pageScrollState)
                    .padding(top = if (targetPage == SettingsPage.HUB) 10.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            if (targetPage == SettingsPage.HUB) {
            SettingsHubHeader(
                onBack = onBack,
                documentCount = documentCount,
                biometricChecked = biometricEnabled && biometricAvailable,
                biometricEnabled = biometricAvailable,
                onBiometricChange = viewModel::setBiometricEnabled,
                dynamicChecked = dynamicColorEnabled && dynamicColorAvailable,
                dynamicEnabled = dynamicColorAvailable,
                onDynamicChange = viewModel::setDynamicColorEnabled,
                hapticsChecked = hapticsEnabled,
                onHapticsChange = viewModel::setHapticsEnabled,
                animationsChecked = !reduceAnimations,
                onAnimationsChange = { enabled -> viewModel.setReduceAnimations(!enabled) },
                compactChecked = compactMode,
                onCompactChange = viewModel::setCompactMode,
                glassChecked = glassBottomBar,
                onGlassChange = viewModel::setGlassBottomBar,
                themeMode = themeMode,
                accentColor = accentColor,
                languageName = language?.nativeName.orEmpty(),
                storageText = stringResource(
                    R.string.settings_storage_using,
                    FormatUtils.formatFileSize(totalSize)
                ),
                onJump = ::openSettingsPage,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .expressiveReveal(0, "settings-hub")
            )
            } else {
                ExpressiveHeroHeader(
                    title = stringResource(
                        when (targetPage) {
                            SettingsPage.APPEARANCE -> R.string.settings_appearance
                            SettingsPage.MOTION -> R.string.settings_motion_feel
                            SettingsPage.SECURITY -> R.string.settings_security
                            SettingsPage.LANGUAGE -> R.string.settings_language
                            SettingsPage.STORAGE -> R.string.settings_storage_privacy
                            SettingsPage.ACKNOWLEDGEMENTS -> R.string.settings_acknowledgements
                            SettingsPage.HUB -> R.string.tab_settings
                        }
                    ),
                    subtitle = stringResource(
                        when (targetPage) {
                            SettingsPage.APPEARANCE -> R.string.settings_page_appearance_subtitle
                            SettingsPage.MOTION -> R.string.settings_page_motion_subtitle
                            SettingsPage.SECURITY -> R.string.settings_page_security_subtitle
                            SettingsPage.LANGUAGE -> R.string.settings_page_language_subtitle
                            SettingsPage.STORAGE -> R.string.settings_page_storage_subtitle
                            SettingsPage.ACKNOWLEDGEMENTS -> R.string.settings_acknowledgements_subtitle
                            SettingsPage.HUB -> R.string.settings_subtitle
                        }
                    ),
                    icon = when (targetPage) {
                        SettingsPage.APPEARANCE -> Icons.Filled.Palette
                        SettingsPage.MOTION -> Icons.Filled.TouchApp
                        SettingsPage.SECURITY -> Icons.Filled.Lock
                        SettingsPage.LANGUAGE -> Icons.Filled.Language
                        SettingsPage.STORAGE -> Icons.Filled.Storage
                        SettingsPage.ACKNOWLEDGEMENTS -> Icons.Filled.WorkspacePremium
                        SettingsPage.HUB -> Icons.Filled.Settings
                    },
                    onBack = ::closeSettingsSubpage,
                    backContentDescription = stringResource(R.string.action_back),
                    includeStatusPadding = false
                )
            }

            // ---- Language --------------------------------------------------
            if (targetPage == SettingsPage.LANGUAGE) {
            SettingsCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = stringResource(R.string.settings_language),
                icon = { Icon(Icons.Filled.Language, null) }
            ) {
                AppLanguage.entries.forEachIndexed { index, option ->
                    LanguageOptionRow(
                        option = option,
                        selected = language == option,
                        onClick = { onLanguageSelected(option) }
                    )
                    if (index != AppLanguage.entries.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.settings_language_selected_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }

            if (targetPage == SettingsPage.APPEARANCE) {
            // ---- Appearance -------------------------------------------------
            SettingsCard(modifier = Modifier.padding(horizontal = 16.dp), title = stringResource(R.string.settings_appearance), icon = { Icon(Icons.Filled.Palette, null) }) {
                Text(
                    stringResource(R.string.settings_theme_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeModePreviews(
                    selected = themeMode,
                    accentColor = accentColor,
                    dynamicColorEnabled = dynamicColorEnabled && dynamicColorAvailable,
                    onSelect = { mode ->
                        selectionHaptic()
                        viewModel.setThemeMode(mode)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_wallpaper_colors),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (dynamicColorAvailable) {
                                stringResource(R.string.settings_wallpaper_colors_subtitle)
                            } else {
                                stringResource(R.string.settings_wallpaper_colors_unavailable)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dynamicColorEnabled && dynamicColorAvailable,
                        onCheckedChange = viewModel::setDynamicColorEnabled,
                        enabled = dynamicColorAvailable
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_brand_theme_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    stringResource(R.string.settings_colour_palette),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(R.string.settings_accent_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                PaletteSwatches(
                    selected = accentColor,
                    dynamicColorActive = dynamicColorEnabled && dynamicColorAvailable,
                    onSelect = { accent ->
                        selectionHaptic()
                        viewModel.selectAccentColor(accent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                AppearancePreview()
            }
            }

            if (targetPage == SettingsPage.MOTION) {
            // ---- Motion & feel ------------------------------------------------
            SettingsCard(modifier = Modifier.padding(horizontal = 16.dp), title = stringResource(R.string.settings_motion_feel), icon = { Icon(Icons.Filled.TouchApp, null) }) {
                FeelToggleRow(
                    icon = Icons.Filled.BlurOn,
                    title = stringResource(R.string.settings_glass_bottom_bar),
                    subtitle = stringResource(R.string.settings_glass_bottom_bar_subtitle),
                    checked = glassBottomBar,
                    onCheckedChange = viewModel::setGlassBottomBar
                )
                FeelToggleRow(
                    icon = Icons.Filled.Vibration,
                    title = stringResource(R.string.settings_haptics),
                    subtitle = stringResource(R.string.settings_haptics_subtitle),
                    checked = hapticsEnabled,
                    onCheckedChange = viewModel::setHapticsEnabled
                )
                FeelToggleRow(
                    icon = Icons.Filled.Compress,
                    title = stringResource(R.string.settings_compact_mode),
                    subtitle = stringResource(R.string.settings_compact_mode_subtitle),
                    checked = compactMode,
                    onCheckedChange = viewModel::setCompactMode
                )
                FeelToggleRow(
                    icon = Icons.Filled.Animation,
                    title = stringResource(R.string.settings_reduce_animations),
                    subtitle = stringResource(R.string.settings_reduce_animations_subtitle),
                    checked = reduceAnimations,
                    onCheckedChange = viewModel::setReduceAnimations,
                    showDivider = false
                )
            }
            }

            if (targetPage == SettingsPage.SECURITY) {
            // ---- Security -------------------------------------------------
            SettingsCard(modifier = Modifier.padding(horizontal = 16.dp), title = stringResource(R.string.settings_security), icon = { Icon(Icons.Filled.Lock, null) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_biometric_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (biometricAvailable) {
                                stringResource(R.string.settings_biometric_available)
                            } else {
                                stringResource(R.string.settings_biometric_unavailable)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricEnabled && biometricAvailable,
                        onCheckedChange = viewModel::setBiometricEnabled,
                        enabled = biometricAvailable
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.settings_change_pin),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                PinField(
                    value = currentPin,
                    onValueChange = { currentPin = it; viewModel.clearMessage() },
                    label = stringResource(R.string.settings_current_pin)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PinField(
                    value = newPin,
                    onValueChange = { newPin = it; viewModel.clearMessage() },
                    label = stringResource(R.string.settings_new_pin)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PinField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it; viewModel.clearMessage() },
                    label = stringResource(R.string.settings_confirm_new_pin)
                )
                message?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (msg.formatArg != null) {
                            stringResource(msg.textRes, msg.formatArg)
                        } else {
                            stringResource(msg.textRes)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (msg.isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.changePin(currentPin, newPin, confirmPin) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_change_pin))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_screenshots_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (screenshotsTemporarilyAllowed) {
                                stringResource(R.string.settings_screenshots_active)
                            } else {
                                stringResource(R.string.settings_screenshots_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        if (screenshotsTemporarilyAllowed) {
                            viewModel.stopScreenshotAllowance()
                        } else {
                            showScreenshotAllowanceDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (screenshotsTemporarilyAllowed) {
                            stringResource(R.string.settings_screenshots_stop)
                        } else {
                            stringResource(R.string.settings_screenshots_start)
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = onLock,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_lock_now))
            }
            }

            if (targetPage == SettingsPage.STORAGE) {
            // ---- Storage --------------------------------------------------
            SettingsCard(modifier = Modifier.padding(horizontal = 16.dp), title = stringResource(R.string.settings_storage), icon = { Icon(Icons.Filled.Storage, null) }) {
                Text(
                    text = if (documentCount == 1) {
                        stringResource(R.string.settings_storage_one)
                    } else {
                        stringResource(R.string.settings_storage_many, documentCount)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(
                        R.string.settings_storage_using,
                        FormatUtils.formatFileSize(totalSize)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SettingsCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = stringResource(R.string.backup_title),
                icon = { Icon(Icons.Filled.Shield, null) }
            ) {
                Text(
                    text = stringResource(R.string.backup_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                backupState.messageRes?.let { messageRes ->
                    if (backupState.phase == BackupPhase.IDLE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(messageRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (backupState.messageIsError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )

                        val createdBackupUri = backupState.createdBackupUri
                        if (!backupState.messageIsError && createdBackupUri != null) {
                            if (backupState.createdBackupProtection ==
                                BackupProtection.NONE
                            ) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(
                                        R.string.backup_share_no_password_warning
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val backupShareTitle =
                                stringResource(R.string.backup_share_title)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        shareBackupFile(
                                            context = context,
                                            uri = createdBackupUri,
                                            chooserTitle = backupShareTitle
                                        )
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            R.string.share_failed,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.backup_share))
                            }
                        }
                    }
                }
                if (backupState.isBusy) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(
                                when (backupState.phase) {
                                    BackupPhase.CREATING -> R.string.backup_creating
                                    BackupPhase.RESTORING -> R.string.backup_restoring
                                    else -> R.string.backup_preparing
                                }
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { showCreateBackupDialog = true },
                    enabled = backupState.phase == BackupPhase.IDLE,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.backup_create))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { restoreBackupLauncher.launch(arrayOf("*/*")) },
                    enabled = backupState.phase == BackupPhase.IDLE,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.backup_restore))
                }
            }

            // ---- Privacy info ---------------------------------------------
            SettingsCard(modifier = Modifier.padding(horizontal = 16.dp), title = stringResource(R.string.settings_how_safe), icon = { Icon(Icons.Filled.Info, null) }) {
                Text(
                    text = stringResource(R.string.settings_safety_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AppUpdateCard(
                status = updateStatus,
                hasDownloadedApk = downloadedUpdate != null,
                onCheck = { runUpdateCheck(automatic = false) },
                onDownload = ::downloadUpdate,
                onAllowInstalls = {
                    runCatching { AppUpdateManager.openInstallPermissionSettings(context) }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SettingsCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = stringResource(R.string.settings_acknowledgements),
                icon = { Icon(Icons.Filled.WorkspacePremium, null) }
            ) {
                Text(
                    text = stringResource(R.string.settings_acknowledgements_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { openAcknowledgements(SettingsPage.STORAGE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_page_continue))
                }
            }
            }

            if (targetPage == SettingsPage.ACKNOWLEDGEMENTS) {
                AcknowledgementsContent(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (targetPage == SettingsPage.HUB) {
                CreatorSpace(
                    onAcknowledgements = { openAcknowledgements(SettingsPage.HUB) }
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MorphingShapeBox(
                    modifier = Modifier.size(42.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    lobes = 9,
                    wave = 0.13f,
                    spinMillis = 24_000
                ) {
                    icon()
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content(this)
        }
    }
}

@Composable
private fun AppearancePreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.settings_appearance_preview),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )
    }
}

@Composable
private fun BackupPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ScreenshotAllowanceDialog(
    onDismiss: () -> Unit,
    onVerifyPin: suspend (String) -> Boolean,
    onAllow: () -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    val pinWrongText = stringResource(R.string.settings_current_pin_wrong)
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_screenshots_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.settings_screenshots_dialog_body),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_screenshots_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                PinField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it
                        errorText = ""
                    },
                    label = stringResource(R.string.settings_current_pin)
                )
                if (errorText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    checking = true
                    scope.launch {
                        if (onVerifyPin(currentPin)) {
                            onAllow()
                        } else {
                            errorText = pinWrongText
                        }
                        checking = false
                    }
                },
                enabled = currentPin.length >= 4 && !checking
            ) {
                Text(stringResource(R.string.settings_screenshots_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun CreateBackupDialog(
    onDismiss: () -> Unit,
    onVerifyPin: suspend (String) -> Boolean,
    onChooseLocation: (BackupProtection, String?) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var protection by remember { mutableStateOf(BackupProtection.PASSWORD) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var checkingPin by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val wrongPinText = stringResource(R.string.settings_current_pin_wrong)
    val passwordMinText = stringResource(R.string.backup_password_min, 8)
    val passwordMismatchText = stringResource(R.string.backup_password_mismatch)

    AlertDialog(
        onDismissRequest = { if (!checkingPin) onDismiss() },
        title = { Text(stringResource(R.string.backup_create_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_create_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.backup_current_pin_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PinField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it
                        errorText = null
                    },
                    label = stringResource(R.string.settings_current_pin)
                )

                BackupProtectionOption(
                    selected = protection == BackupProtection.PASSWORD,
                    title = stringResource(R.string.backup_use_password),
                    subtitle = stringResource(R.string.backup_use_password_subtitle),
                    onClick = {
                        protection = BackupProtection.PASSWORD
                        errorText = null
                    }
                )
                if (protection == BackupProtection.PASSWORD) {
                    BackupPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorText = null
                        },
                        label = stringResource(R.string.backup_password)
                    )
                    BackupPasswordField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorText = null
                        },
                        label = stringResource(R.string.backup_confirm_password)
                    )
                }

                BackupProtectionOption(
                    selected = protection == BackupProtection.NONE,
                    title = stringResource(R.string.backup_no_password),
                    subtitle = stringResource(R.string.backup_no_password_warning),
                    warning = true,
                    onClick = {
                        protection = BackupProtection.NONE
                        errorText = null
                    }
                )

                errorText?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (checkingPin) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.backup_preparing))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (protection == BackupProtection.PASSWORD) {
                        if (password.length < 8) {
                            errorText = passwordMinText
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorText = passwordMismatchText
                            return@Button
                        }
                    }
                    checkingPin = true
                    scope.launch {
                        val verified = onVerifyPin(currentPin)
                        checkingPin = false
                        if (!verified) {
                            errorText = wrongPinText
                        } else {
                            onChooseLocation(
                                protection,
                                if (protection == BackupProtection.PASSWORD) password else null
                            )
                        }
                    }
                },
                enabled = !checkingPin && currentPin.length == PinManager.PIN_LENGTH
            ) {
                Text(stringResource(R.string.backup_choose_location))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !checkingPin
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun BackupProtectionOption(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    warning: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    warning && selected -> MaterialTheme.colorScheme.error
                    selected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun PinField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(input.filter(Char::isDigit).take(PinManager.PIN_LENGTH))
        },
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun themeModeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
    ThemeMode.OLED -> R.string.settings_theme_oled
}

private fun accentLabelRes(accent: AccentColor): Int = when (accent) {
    AccentColor.ORION -> R.string.accent_orion
    AccentColor.TEAL -> R.string.accent_teal
    AccentColor.NAVY -> R.string.accent_navy
    AccentColor.EMERALD -> R.string.accent_emerald
    AccentColor.INDIGO -> R.string.accent_indigo
    AccentColor.OCEAN -> R.string.accent_ocean
    AccentColor.GRAPE -> R.string.accent_grape
    AccentColor.COCOA -> R.string.accent_cocoa
    AccentColor.SLATE -> R.string.accent_slate
}

/**
 * Theme choices as little phones drawn from the real colour schemes,
 * Orion-style: System shows light and dark halves side by side.
 */
@Composable
private fun ThemeModePreviews(
    selected: ThemeMode,
    accentColor: AccentColor,
    dynamicColorEnabled: Boolean,
    onSelect: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val lightScheme = remember(context, accentColor, dynamicColorEnabled) {
        vaultColorScheme(context, darkTheme = false, oled = false, dynamicColorEnabled, accentColor)
    }
    val darkScheme = remember(context, accentColor, dynamicColorEnabled) {
        vaultColorScheme(context, darkTheme = true, oled = false, dynamicColorEnabled, accentColor)
    }
    val oledScheme = remember(context, accentColor, dynamicColorEnabled) {
        vaultColorScheme(context, darkTheme = true, oled = true, dynamicColorEnabled, accentColor)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            val scheme = when (mode) {
                ThemeMode.SYSTEM, ThemeMode.LIGHT -> lightScheme
                ThemeMode.DARK -> darkScheme
                ThemeMode.OLED -> oledScheme
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MiniPhonePreview(
                    scheme = scheme,
                    splitScheme = if (mode == ThemeMode.SYSTEM) darkScheme else null,
                    selected = selected == mode,
                    onClick = { onSelect(mode) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(themeModeLabelRes(mode)),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected == mode) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected == mode) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MiniPhonePreview(
    scheme: ColorScheme,
    splitScheme: ColorScheme?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val corner by animateDpAsState(
        targetValue = if (selected) 20.dp else 14.dp,
        animationSpec = if (animationsEnabled) tween(180) else snap(),
        label = "themePreviewCorner"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.62f)
            .clip(RoundedCornerShape(corner))
            .background(scheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = RoundedCornerShape(corner)
            )
            .clickable(onClick = onClick)
    ) {
        if (splitScheme != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .background(splitScheme.surface)
            )
        }
        Column(modifier = Modifier.fillMaxSize().padding(7.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.surfaceContainerHigh)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(scheme.primaryContainer)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(scheme.tertiaryContainer)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.onSurface.copy(alpha = 0.45f))
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.onSurface.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.primary)
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

/**
 * Palettes as split swatches (primary over secondary-container/tertiary),
 * shown in the current light/dark brightness. Picking one turns wallpaper
 * colours off so the palette applies immediately.
 */
@Composable
private fun PaletteSwatches(
    selected: AccentColor,
    dynamicColorActive: Boolean,
    onSelect: (AccentColor) -> Unit
) {
    val context = LocalContext.current
    val dark = MaterialTheme.colorScheme.isDarkScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AccentColor.entries.forEach { accent ->
            val scheme = remember(context, accent, dark) {
                vaultColorScheme(
                    context,
                    darkTheme = dark,
                    oled = false,
                    useDynamicColor = false,
                    accentColor = accent
                )
            }
            val isSelected = !dynamicColorActive && selected == accent
            Column(
                modifier = Modifier.width(62.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable { onSelect(accent) }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(scheme.primary)
                        )
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(scheme.secondaryContainer)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(scheme.tertiary)
                            )
                        }
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(scheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(accentLabelRes(accent)),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun LanguageOptionRow(
    option: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val shape = RoundedCornerShape(24.dp)
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = if (animationsEnabled) tween(220) else snap(),
        label = "languageOptionContainer"
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(shape)
            .background(container)
            .springPress(shape, onClick = onClick)
            .padding(horizontal = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MorphingShapeBox(
                modifier = Modifier.size(44.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                lobes = 9,
                wave = 0.13f
            ) {
                Text(
                    text = option.code.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.nativeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (option.englishName != option.nativeName) {
                    Text(
                        text = option.englishName,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.78f)
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/** Shares the SAF document created for a backup without making a cache copy. */
private fun shareBackupFile(context: Context, uri: Uri, chooserTitle: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(chooserTitle, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(chooser)
}

@Composable
private fun AppUpdateCard(
    status: UpdateStatus,
    hasDownloadedApk: Boolean,
    onCheck: () -> Unit,
    onDownload: (UpdateRelease) -> Unit,
    onAllowInstalls: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        modifier = modifier,
        title = stringResource(R.string.settings_updates_title),
        icon = { Icon(Icons.Filled.SystemUpdate, null) }
    ) {
        Text(
            text = stringResource(R.string.settings_updates_current, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = stringResource(R.string.settings_updates_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        when (status) {
            UpdateStatus.Idle -> {
                Button(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.SystemUpdate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_updates_check))
                }
            }

            UpdateStatus.Checking -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.settings_updates_checking),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            UpdateStatus.UpToDate -> {
                Text(
                    text = stringResource(R.string.settings_updates_up_to_date),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.settings_updates_check))
                }
            }

            is UpdateStatus.Available -> {
                UpdateReleaseDetails(status.release)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onDownload(status.release) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_updates_download))
                }
                if (!hasDownloadedApk) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.settings_updates_check))
                    }
                }
            }

            is UpdateStatus.Downloading -> {
                val progressValue = status.progress
                Text(
                    text = progressValue?.let { progress ->
                        stringResource(R.string.settings_updates_downloading, progress)
                    } ?: stringResource(R.string.settings_updates_downloading_unknown),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (progressValue != null) {
                    LinearProgressIndicator(
                        progress = { progressValue / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            is UpdateStatus.Verifying -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.settings_updates_verify),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            is UpdateStatus.NeedsInstallPermission -> {
                Text(
                    text = stringResource(R.string.settings_updates_allow_install),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAllowInstalls, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.settings_updates_allow_install_action))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onDownload(status.release) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_updates_download))
                }
            }

            is UpdateStatus.Failed -> {
                Text(
                    text = stringResource(status.messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.settings_updates_check))
                }
            }
        }
    }
}

@Composable
private fun UpdateReleaseDetails(release: UpdateRelease) {
    Text(
        text = stringResource(R.string.settings_updates_available, "v${release.versionName}"),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
    if (release.apkSizeBytes > 0) {
        Text(
            text = stringResource(
                R.string.settings_updates_file_size,
                FormatUtils.formatFileSize(release.apkSizeBytes)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.settings_updates_release_notes),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = release.releaseNotes ?: stringResource(R.string.settings_updates_no_notes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AcknowledgementsContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AcknowledgementCard(
            title = stringResource(R.string.ack_orion_title),
            body = stringResource(R.string.ack_orion_body),
            icon = Icons.Filled.Star,
            url = "https://github.com/RookieEnough/Orion-Store"
        )
        AcknowledgementCard(
            title = stringResource(R.string.ack_space_grotesk_title),
            body = stringResource(R.string.ack_space_grotesk_body),
            icon = Icons.Filled.Palette,
            url = "https://fonts.google.com/specimen/Space+Grotesk"
        )
        AcknowledgementCard(
            title = stringResource(R.string.ack_androidx_title),
            body = stringResource(R.string.ack_androidx_body),
            icon = Icons.Filled.Code,
            url = "https://developer.android.com/jetpack/androidx"
        )
        AcknowledgementCard(
            title = stringResource(R.string.ack_mlkit_title),
            body = stringResource(R.string.ack_mlkit_body),
            icon = Icons.Filled.Info,
            url = "https://developers.google.com/ml-kit"
        )
        AcknowledgementCard(
            title = stringResource(R.string.ack_coil_title),
            body = stringResource(R.string.ack_coil_body),
            icon = Icons.Filled.Code,
            url = "https://coil-kt.github.io/coil/"
        )
        AcknowledgementCard(
            title = "My Vault · GPL-3.0",
            body = stringResource(R.string.ack_my_vault_license),
            icon = Icons.Filled.WorkspacePremium,
            url = "https://github.com/ft-sinan/My-vault-android/blob/main/LICENSE"
        )
    }
}

@Composable
private fun AcknowledgementCard(
    title: String,
    body: String,
    icon: ImageVector,
    url: String
) {
    val context = LocalContext.current
    SettingsCard(title = title, icon = { Icon(icon, null) }) {
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        ) {
            Text(stringResource(R.string.ack_open_project))
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FeelToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
    }
}


/** The Orion-style settings hub: quick-toggle pills and pastel bento shortcuts. */
@Composable
private fun SettingsHubHeader(
    onBack: () -> Unit,
    documentCount: Int,
    biometricChecked: Boolean,
    biometricEnabled: Boolean,
    onBiometricChange: (Boolean) -> Unit,
    dynamicChecked: Boolean,
    dynamicEnabled: Boolean,
    onDynamicChange: (Boolean) -> Unit,
    hapticsChecked: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    animationsChecked: Boolean,
    onAnimationsChange: (Boolean) -> Unit,
    compactChecked: Boolean,
    onCompactChange: (Boolean) -> Unit,
    glassChecked: Boolean,
    onGlassChange: (Boolean) -> Unit,
    themeMode: ThemeMode,
    accentColor: AccentColor,
    languageName: String,
    storageText: String,
    onJump: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCircleButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack
            )
            Spacer(Modifier.weight(1f))
            VaultMark(size = 42.dp)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.tab_settings),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "My Vault v${BuildConfig.VERSION_NAME} · " + if (documentCount == 1) {
                stringResource(R.string.settings_storage_one)
            } else {
                stringResource(R.string.settings_storage_many, documentCount)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.settings_quick_toggles),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickToggleTile(
                icon = Icons.Filled.Fingerprint,
                label = stringResource(R.string.settings_quick_biometric),
                checked = biometricChecked,
                enabled = biometricEnabled,
                onCheckedChange = onBiometricChange,
                modifier = Modifier.weight(1f)
            )
            QuickToggleTile(
                icon = Icons.Filled.Palette,
                label = stringResource(R.string.settings_quick_wallpaper),
                checked = dynamicChecked,
                enabled = dynamicEnabled,
                onCheckedChange = onDynamicChange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickToggleTile(
                icon = Icons.Filled.Vibration,
                label = stringResource(R.string.settings_haptics),
                checked = hapticsChecked,
                onCheckedChange = onHapticsChange,
                modifier = Modifier.weight(1f)
            )
            QuickToggleTile(
                icon = Icons.Filled.BlurOn,
                label = stringResource(R.string.settings_glass_bottom_bar),
                checked = glassChecked,
                onCheckedChange = onGlassChange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickToggleTile(
                icon = Icons.Filled.Animation,
                label = stringResource(R.string.settings_quick_animations),
                checked = animationsChecked,
                onCheckedChange = onAnimationsChange,
                modifier = Modifier.weight(1f)
            )
            QuickToggleTile(
                icon = Icons.Filled.Compress,
                label = stringResource(R.string.settings_compact_mode),
                checked = compactChecked,
                onCheckedChange = onCompactChange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.settings_everything_else),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        SettingsBentoCard(
            icon = Icons.Filled.Palette,
            title = stringResource(R.string.settings_appearance),
            subtitle = if (dynamicChecked) {
                "${stringResource(R.string.settings_quick_wallpaper)} · ${stringResource(themeModeLabelRes(themeMode))}"
            } else {
                "${stringResource(accentLabelRes(accentColor))} · ${stringResource(themeModeLabelRes(themeMode))}"
            },
            seed = Color(0xFF6B48BC),
            onClick = { onJump("appearance") },
            modifier = Modifier.fillMaxWidth().height(118.dp)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(196.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsBentoCard(
                icon = Icons.Filled.Lock,
                title = stringResource(R.string.settings_security),
                subtitle = stringResource(R.string.settings_biometric_title),
                seed = Color(0xFF087A4E),
                onClick = { onJump("security") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsBentoCard(
                    icon = Icons.Filled.Groups,
                    title = stringResource(R.string.settings_family),
                    subtitle = stringResource(R.string.settings_family_subtitle),
                    seed = Color(0xFF0077A8),
                    onClick = { onJump("family") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                SettingsBentoCard(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = languageName,
                    seed = Color(0xFF4C4ECD),
                    onClick = { onJump("language") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(118.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsBentoCard(
                icon = Icons.Filled.Storage,
                title = stringResource(R.string.settings_storage_privacy),
                subtitle = storageText,
                seed = Color(0xFFC65D00),
                onClick = { onJump("storage") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            SettingsBentoCard(
                icon = Icons.Filled.TouchApp,
                title = stringResource(R.string.settings_motion_feel),
                subtitle = stringResource(R.string.settings_page_motion_subtitle),
                seed = Color(0xFFB03070),
                onClick = { onJump("motion") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun QuickToggleTile(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val corner by animateDpAsState(
        targetValue = if (checked) 36.dp else 22.dp,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = 0.68f, stiffness = 420f)
        } else {
            snap()
        },
        label = "quickToggleCorner"
    )
    val shape = RoundedCornerShape(corner)
    val container by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = if (animationsEnabled) tween(220) else snap(),
        label = "quickToggleContainer"
    )
    val contentColor = if (checked) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .height(78.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.48f }
            .clip(shape)
            .background(container)
            .springPress(shape, enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(21.dp))
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(
                        if (checked) R.string.settings_toggle_on else R.string.settings_toggle_off
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.82f)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingsBentoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    seed: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val containerColor = expressivePastel(seed)
    val contentColor = readableContentColor(containerColor)
    val mutedContentColor = contentColor.copy(alpha = 0.72f)
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .springPress(shape, onClick = onClick)
    ) {
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 28.dp, y = (-32).dp)
                .size(92.dp),
            color = seed.copy(alpha = 0.17f),
            lobes = 12,
            wave = 0.18f,
            spinMillis = 26_000
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            MorphingShapeBox(
                modifier = Modifier.size(42.dp),
                color = seed.copy(alpha = 0.24f),
                lobes = 9,
                wave = 0.13f,
                spinMillis = 22_000
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedContentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** A full-width Creator Coin stage: the final, playful signature area of Settings. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CreatorSpace(
    onAcknowledgements: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = localSelectionHaptic()
    val copiedText = stringResource(R.string.action_copied)
    val animationsEnabled = LocalAnimationsEnabled.current

    var flipped by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<Int?>(null) }
    var tiltTargetX by remember { mutableStateOf(0f) }
    var tiltTargetY by remember { mutableStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = if (animationsEnabled) tween(680) else snap(),
        label = "creatorCoinFlip"
    )
    val tiltX by animateFloatAsState(
        targetValue = tiltTargetX,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = 0.62f, stiffness = 420f)
        } else {
            snap()
        },
        label = "creatorCoinTiltX"
    )
    val tiltY by animateFloatAsState(
        targetValue = tiltTargetY,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = 0.62f, stiffness = 420f)
        } else {
            snap()
        },
        label = "creatorCoinTiltY"
    )
    val creatorTransition = rememberInfiniteTransition(label = "creatorStage")
    val orbitAngle by creatorTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "creatorOrbit"
    )
    val shineOffset by creatorTransition.animateFloat(
        initialValue = -170f,
        targetValue = 170f,
        animationSpec = infiniteRepeatable(
            animation = tween(5_600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "creatorCoinShine"
    )
    val effectiveOrbitAngle = if (animationsEnabled) orbitAngle else 0.65f
    val effectiveShine = if (animationsEnabled) shineOffset else -170f

    LaunchedEffect(flipped) {
        if (flipped) {
            delay(2_400)
            flipped = false
        }
    }

    fun openUrl(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    fun copyCreatorText(value: String) {
        clipboardManager.setText(AnnotatedString(value))
        haptic()
        Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.68f),
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 62.dp, y = (-84).dp)
                .size(210.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            lobes = 12,
            wave = 0.19f,
            spinMillis = 30_000
        )
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-72).dp, y = 76.dp)
                .size(170.dp),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
            lobes = 11,
            wave = 0.17f,
            spinMillis = 34_000
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpressiveChip(
                text = stringResource(R.string.creator_title).uppercase(),
                icon = Icons.Filled.Star,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier.size(width = 276.dp, height = 228.dp),
                contentAlignment = Alignment.Center
            ) {
                MorphingShapeBox(
                    modifier = Modifier.size(204.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.58f),
                    lobes = 12,
                    wave = 0.19f,
                    spinMillis = 24_000
                )
                Box(
                    modifier = Modifier
                        .size(152.dp)
                        .graphicsLayer {
                            rotationY = rotation + tiltX
                            rotationX = -tiltY
                            cameraDistance = 18.dp.toPx()
                        }
                        .pointerInput(animationsEnabled) {
                            detectTapGestures(
                                onPress = { offset ->
                                    if (animationsEnabled) {
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        if (centerX > 0f) {
                                            tiltTargetX =
                                                ((offset.x - centerX) / centerX)
                                                    .coerceIn(-1f, 1f) * 9f
                                        }
                                        if (centerY > 0f) {
                                            tiltTargetY =
                                                ((offset.y - centerY) / centerY)
                                                    .coerceIn(-1f, 1f) * 9f
                                        }
                                    }
                                    tryAwaitRelease()
                                    tiltTargetX = 0f
                                    tiltTargetY = 0f
                                },
                                onTap = {
                                    haptic()
                                    flipped = !flipped
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        MorphingShapeBox(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            lobes = 12,
                            wave = 0.16f,
                            spinMillis = 18_000
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(x = effectiveShine.dp)
                                    .width(34.dp)
                                    .fillMaxHeight()
                                    .graphicsLayer { rotationZ = 18f }
                                    .background(Color.White.copy(alpha = 0.14f))
                            )
                            Text(
                                text = "S",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                                .clip(BlobShape(lobes = 12, wave = 0.16f))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }

                listOf(Icons.Filled.Star, Icons.Filled.Code, Icons.Filled.Shield)
                    .forEachIndexed { index, icon ->
                        val angle = effectiveOrbitAngle + index * ((2f * PI.toFloat()) / 3f)
                        CreatorOrbitBadge(
                            icon = icon,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(
                                    x = (cos(angle) * 106f).dp,
                                    y = (sin(angle) * 80f).dp
                                )
                        )
                    }
            }

            Text(
                text = if (flipped) {
                    stringResource(R.string.creator_back_title)
                } else {
                    stringResource(R.string.creator_name)
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            if (flipped) {
                Text(
                    text = stringResource(R.string.creator_back_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .springPress(CircleShape) { copyCreatorText("@ft-sinan") }
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "@ft-sinan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.creator_role),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.creator_bio),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.creator_tap_emblem),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreatorBadgeChip(
                    text = stringResource(R.string.creator_badge_creator),
                    icon = Icons.Filled.Star,
                    selected = selectedBadge == 0,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { selectedBadge = if (selectedBadge == 0) null else 0 }
                )
                CreatorBadgeChip(
                    text = stringResource(R.string.creator_badge_privacy),
                    icon = Icons.Filled.Shield,
                    selected = selectedBadge == 1,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { selectedBadge = if (selectedBadge == 1) null else 1 }
                )
                CreatorBadgeChip(
                    text = stringResource(R.string.creator_badge_offline),
                    icon = Icons.Filled.Lock,
                    selected = selectedBadge == 2,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { selectedBadge = if (selectedBadge == 2) null else 2 }
                )
                CreatorBadgeChip(
                    text = "Kotlin · Android",
                    icon = Icons.Filled.Code,
                    selected = selectedBadge == 3,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { selectedBadge = if (selectedBadge == 3) null else 3 }
                )
            }
            val selectedBadgeDescription = when (selectedBadge) {
                0 -> stringResource(R.string.creator_badge_creator_desc)
                1 -> stringResource(R.string.creator_badge_privacy_desc)
                2 -> stringResource(R.string.creator_badge_offline_desc)
                3 -> stringResource(R.string.creator_badge_code_desc)
                else -> null
            }
            if (selectedBadgeDescription != null) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.68f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = selectedBadgeDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.68f))
                    .padding(vertical = 14.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpressiveStat(
                    value = BuildConfig.VERSION_NAME,
                    label = stringResource(R.string.creator_version),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ExpressiveStat(
                    value = "Android",
                    label = stringResource(R.string.creator_platform),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ExpressiveStat(
                    value = "Kotlin",
                    label = stringResource(R.string.creator_built_with),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.creator_connect),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.creator_long_press_copy),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreatorConnectTile(
                    icon = Icons.Filled.Code,
                    label = "GitHub",
                    modifier = Modifier.weight(1f),
                    onClick = { openUrl("https://github.com/ft-sinan") },
                    onLongClick = { copyCreatorText("https://github.com/ft-sinan") }
                )
                CreatorConnectTile(
                    icon = Icons.Filled.Send,
                    label = "Telegram",
                    modifier = Modifier.weight(1f),
                    onClick = { openUrl("https://t.me/Exotatic") },
                    onLongClick = { copyCreatorText("@Exotatic") }
                )
                CreatorConnectTile(
                    icon = Icons.Filled.Chat,
                    label = "WhatsApp",
                    modifier = Modifier.weight(1f),
                    onClick = { openUrl("https://wa.me/917558874118") },
                    onLongClick = { copyCreatorText("+91 7558874118") }
                )
                CreatorConnectTile(
                    icon = Icons.Filled.Email,
                    label = stringResource(R.string.creator_email),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.parse("mailto:Mail.tosinaan@gmail.com")
                                )
                            )
                        }
                    },
                    onLongClick = { copyCreatorText("Mail.tosinaan@gmail.com") }
                )
            }

            Spacer(Modifier.height(10.dp))
            val websiteShape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .clip(websiteShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                    .springPress(websiteShape) { openUrl("https://sinanmk.my.id") }
                    .padding(horizontal = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorphingShapeBox(
                        modifier = Modifier.size(42.dp),
                        color = MaterialTheme.colorScheme.primary,
                        lobes = 9,
                        wave = 0.13f
                    ) {
                        Icon(
                            Icons.Filled.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.creator_visit_website),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Sinanmk.my.id",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Filled.OpenInNew, contentDescription = null)
                }
            }

            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onAcknowledgements) {
                Icon(
                    Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.creator_acknowledgements))
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "made with 🤍 by sinan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CreatorBadgeChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else containerColor
    val foreground = if (selected) MaterialTheme.colorScheme.onPrimary else contentColor
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .springPress(CircleShape, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CreatorConnectTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = localSelectionHaptic()
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onLongClick = {
                    haptic()
                    onLongClick()
                },
                onClick = {
                    haptic()
                    onClick()
                }
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MorphingShapeBox(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
                lobes = 9,
                wave = 0.13f
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CreatorOrbitBadge(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp)
        )
    }
}
