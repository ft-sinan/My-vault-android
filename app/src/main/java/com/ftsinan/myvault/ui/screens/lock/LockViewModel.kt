package com.ftsinan.myvault.ui.screens.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.data.security.ScreenshotAllowanceManager
import com.ftsinan.myvault.data.settings.AccentColor
import com.ftsinan.myvault.data.settings.AppLanguage
import com.ftsinan.myvault.data.settings.ThemeMode
import com.ftsinan.myvault.data.settings.LanguageManager
import com.ftsinan.myvault.data.storage.VaultFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LockUiState(
    val isLoading: Boolean = true,
    val language: AppLanguage? = null,
    val isLanguageLoaded: Boolean = false,
    val isPinSet: Boolean = false,
    val isUnlocked: Boolean = false,
    /** Brief post-unlock moment shown after PIN/biometric success, not on cold start. */
    val showUnlockAnimation: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val useDynamicColor: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val accentColor: AccentColor = AccentColor.ORION,
    val hapticsEnabled: Boolean = true,
    val compactMode: Boolean = false,
    val reduceAnimations: Boolean = false,
    val glassBottomBar: Boolean = true,
    val isProfileLoaded: Boolean = false,
    val hasOwnerProfile: Boolean = false,
    val isRestoreOwnerSetupLoaded: Boolean = false,
    /** A restore recipient still has to create their own owner profile. */
    val needsRestoreOwnerSetup: Boolean = false,
    /** True after a first-run user chooses a new vault or restores a backup. */
    val hasSelectedInitialSetupPath: Boolean = false,
    /** Name only is shown on the lock screen (never the photo). */
    val ownerName: String? = null,
    val error: String? = null,
    val failedAttempts: Int = 0
)

/** Existing users with a PIN, and restored data awaiting its new PIN, skip this. */
internal fun shouldShowFirstRunBackupPrompt(
    isPinSet: Boolean,
    hasOwnerProfile: Boolean,
    hasSelectedInitialSetupPath: Boolean
): Boolean = !isPinSet && !hasOwnerProfile && !hasSelectedInitialSetupPath

/**
 * Owns app-entry state for the whole session: language choice, PIN lock
 * and whether the owner's profile has been created yet.
 */
class LockViewModel(
    private val pinManager: PinManager,
    private val fileManager: VaultFileManager,
    private val languageManager: LanguageManager,
    private val screenshotAllowanceManager: ScreenshotAllowanceManager,
    repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pinManager.isPinSet.collect { isPinSet ->
                _uiState.update { it.copy(isLoading = false, isPinSet = isPinSet) }
            }
        }
        viewModelScope.launch {
            pinManager.isBiometricEnabled.collect { enabled ->
                _uiState.update { it.copy(isBiometricEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            pinManager.useDynamicColor.collect { enabled ->
                _uiState.update { it.copy(useDynamicColor = enabled) }
            }
        }
        viewModelScope.launch {
            pinManager.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            pinManager.accentColor.collect { accent ->
                _uiState.update { it.copy(accentColor = accent) }
            }
        }
        viewModelScope.launch {
            pinManager.hapticsEnabled.collect { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            pinManager.compactMode.collect { enabled ->
                _uiState.update { it.copy(compactMode = enabled) }
            }
        }
        viewModelScope.launch {
            pinManager.reduceAnimations.collect { enabled ->
                _uiState.update { it.copy(reduceAnimations = enabled) }
            }
        }
        viewModelScope.launch {
            pinManager.glassBottomBar.collect { enabled ->
                _uiState.update { it.copy(glassBottomBar = enabled) }
            }
        }
        viewModelScope.launch {
            pinManager.pendingRestoreOwnerSetup.collect { pending ->
                _uiState.update {
                    it.copy(
                        isRestoreOwnerSetupLoaded = true,
                        needsRestoreOwnerSetup = pending
                    )
                }
            }
        }
        viewModelScope.launch {
            languageManager.language.collect { language ->
                _uiState.update { it.copy(language = language, isLanguageLoaded = true) }
            }
        }
        viewModelScope.launch {
            repository.owner.collect { owner ->
                _uiState.update {
                    it.copy(
                        isProfileLoaded = true,
                        hasOwnerProfile = owner != null,
                        ownerName = owner?.name
                    )
                }
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        languageManager.setLanguage(language)
    }

    fun selectNewVaultSetup() {
        _uiState.update { it.copy(hasSelectedInitialSetupPath = true) }
    }

    fun markInitialBackupRestored() {
        _uiState.update { it.copy(hasSelectedInitialSetupPath = true) }
    }

    fun completeRestoreOwnerSetup() {
        viewModelScope.launch {
            pinManager.setPendingRestoreOwnerSetup(false)
        }
    }

    fun setupPin(pin: String) {
        viewModelScope.launch {
            pinManager.setPin(pin)
            _uiState.update {
                it.copy(
                    isPinSet = true,
                    isUnlocked = true,
                    showUnlockAnimation = false,
                    error = null
                )
            }
        }
    }

    fun unlockWithPin(pin: String) {
        viewModelScope.launch {
            if (pinManager.verifyPin(pin)) {
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        showUnlockAnimation = !it.reduceAnimations,
                        error = null,
                        failedAttempts = 0
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        error = "WRONG_PIN",
                        failedAttempts = it.failedAttempts + 1
                    )
                }
            }
        }
    }

    fun unlockWithBiometric() {
        _uiState.update {
            it.copy(
                isUnlocked = true,
                showUnlockAnimation = !it.reduceAnimations,
                error = null,
                failedAttempts = 0
            )
        }
    }

    fun finishUnlockAnimation() {
        _uiState.update { it.copy(showUnlockAnimation = false) }
    }

    /** Locks the vault and wipes every decrypted preview/shared copy. */
    fun lock() {
        if (!_uiState.value.isUnlocked) return
        screenshotAllowanceManager.clear()
        _uiState.update {
            it.copy(isUnlocked = false, showUnlockAnimation = false, error = null)
        }
        viewModelScope.launch(Dispatchers.IO) {
            fileManager.clearDecryptedCache()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                LockViewModel(
                    pinManager = app.container.pinManager,
                    fileManager = app.container.fileManager,
                    languageManager = app.container.languageManager,
                    screenshotAllowanceManager = app.container.screenshotAllowanceManager,
                    repository = app.container.documentRepository
                )
            }
        }
    }
}
