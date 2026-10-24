package com.ftsinan.myvault.ui.screens.settings

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.backup.BackupManager
import com.ftsinan.myvault.data.backup.BackupProfileOption
import com.ftsinan.myvault.data.backup.BackupProtection
import com.ftsinan.myvault.data.backup.BackupRestoreMode
import com.ftsinan.myvault.data.backup.BackupSummary
import com.ftsinan.myvault.data.backup.PreparedBackupRestore
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.data.security.ScreenshotAllowanceManager
import com.ftsinan.myvault.data.settings.AccentColor
import com.ftsinan.myvault.data.settings.AppLanguage
import com.ftsinan.myvault.data.settings.ThemeMode
import com.ftsinan.myvault.data.settings.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.crypto.AEADBadTagException

data class SettingsMessage(
    @StringRes val textRes: Int,
    val isError: Boolean,
    val formatArg: Int? = null
)

enum class BackupPhase {
    IDLE,
    CREATING,
    INSPECTING,
    PASSWORD_REQUIRED,
    PREPARING,
    READY,
    RESTORING
}

data class BackupUiState(
    val phase: BackupPhase = BackupPhase.IDLE,
    val restoreUri: Uri? = null,
    val summary: BackupSummary? = null,
    val restoreMode: BackupRestoreMode = BackupRestoreMode.ADD_TO_VAULT,
    val profileOptions: List<BackupProfileOption> = emptyList(),
    val selectedOwnerBackupId: Long? = null,
    val createNewOwnerSelected: Boolean = false,
    val createdBackupUri: Uri? = null,
    val createdBackupProtection: BackupProtection? = null,
    @StringRes val messageRes: Int? = null,
    val messageIsError: Boolean = false
) {
    val isBusy: Boolean = phase == BackupPhase.CREATING ||
        phase == BackupPhase.INSPECTING ||
        phase == BackupPhase.PREPARING ||
        phase == BackupPhase.RESTORING
}

class SettingsViewModel(
    private val pinManager: PinManager,
    repository: DocumentRepository,
    languageManager: LanguageManager,
    private val backupManager: BackupManager,
    private val screenshotAllowanceManager: ScreenshotAllowanceManager
) : ViewModel() {

    val language: StateFlow<AppLanguage?> =
        languageManager.language

    val biometricEnabled: StateFlow<Boolean> = pinManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val dynamicColorEnabled: StateFlow<Boolean> = pinManager.useDynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val themeMode: StateFlow<ThemeMode> = pinManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.LIGHT)

    val accentColor: StateFlow<AccentColor> = pinManager.accentColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccentColor.ORION)

    val hapticsEnabled: StateFlow<Boolean> = pinManager.hapticsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val compactMode: StateFlow<Boolean> = pinManager.compactMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val reduceAnimations: StateFlow<Boolean> = pinManager.reduceAnimations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val glassBottomBar: StateFlow<Boolean> = pinManager.glassBottomBar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** Null until DataStore has loaded; 0 means Settings has never checked. */
    val lastSettingsUpdateCheckAt: StateFlow<Long?> =
        pinManager.lastSettingsUpdateCheckAt
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val documentCount: StateFlow<Int> = repository.documentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalSize: StateFlow<Long> = repository.totalSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _message = MutableStateFlow<SettingsMessage?>(null)
    val message: StateFlow<SettingsMessage?> = _message.asStateFlow()

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()
    private var preparedRestore: PreparedBackupRestore? = null

    val screenshotAllowanceExpiresAt: StateFlow<Long> =
        screenshotAllowanceManager.expiresAtMillis

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { pinManager.setBiometricEnabled(enabled) }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch { pinManager.setUseDynamicColor(enabled) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { pinManager.setThemeMode(mode) }
    }

    fun setAccentColor(accent: AccentColor) {
        viewModelScope.launch { pinManager.setAccentColor(accent) }
    }

    /** Picking a palette turns wallpaper colours off so the choice shows at once. */
    fun selectAccentColor(accent: AccentColor) {
        viewModelScope.launch {
            pinManager.setAccentColor(accent)
            pinManager.setUseDynamicColor(false)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { pinManager.setHapticsEnabled(enabled) }
    }

    fun setCompactMode(enabled: Boolean) {
        viewModelScope.launch { pinManager.setCompactMode(enabled) }
    }

    fun setReduceAnimations(enabled: Boolean) {
        viewModelScope.launch { pinManager.setReduceAnimations(enabled) }
    }

    fun setGlassBottomBar(enabled: Boolean) {
        viewModelScope.launch { pinManager.setGlassBottomBar(enabled) }
    }

    fun markSettingsUpdateChecked(checkedAtMillis: Long) {
        viewModelScope.launch {
            pinManager.setLastSettingsUpdateCheckAt(checkedAtMillis)
        }
    }

    suspend fun verifyCurrentPin(pin: String): Boolean = pinManager.verifyPin(pin)

    /** Call only after the current PIN has been verified in the UI. */
    fun allowScreenshotsTemporarily() {
        screenshotAllowanceManager.allowTemporarily()
    }

    fun stopScreenshotAllowance() {
        screenshotAllowanceManager.clear()
    }

    fun createBackup(
        destination: Uri,
        protection: BackupProtection,
        password: String?
    ) {
        if (_backupState.value.isBusy) return
        _backupState.value = BackupUiState(phase = BackupPhase.CREATING)
        viewModelScope.launch {
            try {
                backupManager.createBackup(
                    destination = destination,
                    protection = protection,
                    password = if (protection == BackupProtection.PASSWORD) {
                        password.orEmpty().toCharArray()
                    } else {
                        null
                    }
                )
                _backupState.value = BackupUiState(
                    createdBackupUri = destination,
                    createdBackupProtection = protection,
                    messageRes = R.string.backup_created
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _backupState.value = BackupUiState(
                    messageRes = R.string.backup_create_failed,
                    messageIsError = true
                )
            }
        }
    }

    fun onRestoreFileSelected(uri: Uri) {
        if (_backupState.value.isBusy) return
        val previous = preparedRestore
        preparedRestore = null
        _backupState.value = BackupUiState(
            phase = BackupPhase.INSPECTING,
            restoreUri = uri
        )
        viewModelScope.launch {
            if (previous != null) backupManager.discardRestore(previous)
            try {
                when (backupManager.inspectProtection(uri)) {
                    BackupProtection.NONE -> prepareRestore(uri, null)
                    BackupProtection.PASSWORD -> {
                        _backupState.value = BackupUiState(
                            phase = BackupPhase.PASSWORD_REQUIRED,
                            restoreUri = uri
                        )
                    }
                }
            } catch (_: Exception) {
                _backupState.value = BackupUiState(
                    messageRes = R.string.backup_invalid_file,
                    messageIsError = true
                )
            }
        }
    }

    fun prepareProtectedRestore(password: String) {
        val uri = _backupState.value.restoreUri ?: return
        viewModelScope.launch {
            prepareRestore(uri, password.toCharArray())
        }
    }

    private suspend fun prepareRestore(uri: Uri, password: CharArray?) {
        _backupState.value = BackupUiState(
            phase = BackupPhase.PREPARING,
            restoreUri = uri
        )
        try {
            val prepared = backupManager.prepareRestore(uri, password)
            preparedRestore = prepared
            _backupState.value = BackupUiState(
                phase = BackupPhase.READY,
                restoreUri = uri,
                summary = prepared.summary,
                restoreMode = BackupRestoreMode.ADD_TO_VAULT,
                profileOptions = prepared.profileOptions,
                selectedOwnerBackupId = prepared.profileOptions
                    .firstOrNull { it.isOriginalOwner }?.backupMemberId
                    ?: prepared.profileOptions.firstOrNull()?.backupMemberId,
                createNewOwnerSelected = false
            )
        } catch (_: AEADBadTagException) {
            _backupState.value = BackupUiState(
                phase = BackupPhase.PASSWORD_REQUIRED,
                restoreUri = uri,
                messageRes = R.string.backup_wrong_password,
                messageIsError = true
            )
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _backupState.value = BackupUiState(
                messageRes = R.string.backup_invalid_file,
                messageIsError = true
            )
        }
    }

    fun selectRestoreMode(mode: BackupRestoreMode) {
        val state = _backupState.value
        if (state.phase != BackupPhase.READY) return
        _backupState.value = state.copy(
            restoreMode = mode,
            messageRes = null,
            messageIsError = false
        )
    }

    fun selectRestoreOwner(backupMemberId: Long) {
        val state = _backupState.value
        if (state.phase != BackupPhase.READY) return
        if (state.profileOptions.none { it.backupMemberId == backupMemberId }) return
        _backupState.value = state.copy(
            selectedOwnerBackupId = backupMemberId,
            createNewOwnerSelected = false,
            messageRes = null,
            messageIsError = false
        )
    }

    fun selectCreateNewOwnerProfile() {
        val state = _backupState.value
        if (state.phase != BackupPhase.READY) return
        if (state.restoreMode != BackupRestoreMode.REPLACE_VAULT) return
        if (state.profileOptions.isEmpty()) return
        _backupState.value = state.copy(
            selectedOwnerBackupId = null,
            createNewOwnerSelected = true,
            messageRes = null,
            messageIsError = false
        )
    }

    fun commitRestore(currentPin: String) {
        if (_backupState.value.phase != BackupPhase.READY) return
        val prepared = preparedRestore ?: return
        val summary = prepared.summary
        val profileOptions = prepared.profileOptions
        val state = _backupState.value
        val restoreMode = state.restoreMode
        val createNewOwner = restoreMode == BackupRestoreMode.REPLACE_VAULT &&
            state.createNewOwnerSelected &&
            profileOptions.isNotEmpty()
        val selectedOwnerBackupId = when {
            restoreMode == BackupRestoreMode.ADD_TO_VAULT -> null
            createNewOwner -> null
            profileOptions.isEmpty() -> null
            else -> state.selectedOwnerBackupId
                ?: profileOptions.firstOrNull { it.isOriginalOwner }?.backupMemberId
                ?: profileOptions.firstOrNull()?.backupMemberId
        }
        _backupState.value = BackupUiState(
            phase = BackupPhase.RESTORING,
            summary = summary,
            restoreMode = restoreMode,
            profileOptions = profileOptions,
            selectedOwnerBackupId = selectedOwnerBackupId,
            createNewOwnerSelected = createNewOwner
        )
        viewModelScope.launch {
            try {
                if (!pinManager.verifyPin(currentPin)) {
                    _backupState.value = BackupUiState(
                        phase = BackupPhase.READY,
                        summary = summary,
                        restoreMode = restoreMode,
                        profileOptions = profileOptions,
                        selectedOwnerBackupId = selectedOwnerBackupId,
                        createNewOwnerSelected = createNewOwner,
                        messageRes = R.string.settings_current_pin_wrong,
                        messageIsError = true
                    )
                    return@launch
                }
                backupManager.commitRestore(
                    prepared = prepared,
                    restoreMode = restoreMode,
                    selectedOwnerBackupMemberId = selectedOwnerBackupId,
                    createNewOwnerProfile = createNewOwner
                )
                preparedRestore = null
                _backupState.value = BackupUiState(
                    messageRes = if (restoreMode == BackupRestoreMode.ADD_TO_VAULT) {
                        R.string.backup_added
                    } else {
                        R.string.backup_restored
                    }
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                preparedRestore = null
                _backupState.value = BackupUiState(
                    messageRes = R.string.backup_restore_failed,
                    messageIsError = true
                )
            }
        }
    }

    fun cancelRestore() {
        val prepared = preparedRestore
        preparedRestore = null
        _backupState.value = BackupUiState()
        if (prepared != null) {
            viewModelScope.launch { backupManager.discardRestore(prepared) }
        }
    }

    fun clearBackupMessage() {
        _backupState.value = _backupState.value.copy(
            messageRes = null,
            messageIsError = false
        )
    }

    fun changePin(currentPin: String, newPin: String, confirmPin: String) {
        viewModelScope.launch {
            _message.value = when {
                newPin.length != PinManager.PIN_LENGTH || newPin.any { !it.isDigit() } ->
                    SettingsMessage(
                        R.string.settings_pin_length_error,
                        isError = true,
                        formatArg = PinManager.PIN_LENGTH
                    )
                newPin != confirmPin ->
                    SettingsMessage(R.string.settings_pin_mismatch_error, isError = true)
                !pinManager.verifyPin(currentPin) ->
                    SettingsMessage(R.string.settings_current_pin_wrong, isError = true)
                else -> {
                    pinManager.setPin(newPin)
                    SettingsMessage(R.string.settings_pin_changed, isError = false)
                }
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                SettingsViewModel(
                    pinManager = app.container.pinManager,
                    repository = app.container.documentRepository,
                    languageManager = app.container.languageManager,
                    backupManager = app.container.backupManager,
                    screenshotAllowanceManager = app.container.screenshotAllowanceManager
                )
            }
        }
    }
}
