package com.ftsinan.myvault.ui.screens.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.data.storage.VaultFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LockUiState(
    val isLoading: Boolean = true,
    val isPinSet: Boolean = false,
    val isUnlocked: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val error: String? = null,
    val failedAttempts: Int = 0
)

/**
 * Owns the vault lock state for the whole app session.
 * The vault starts locked; it re-locks when the app has been in the
 * background for a while (handled by MainActivity calling [lock]).
 */
class LockViewModel(
    private val pinManager: PinManager,
    private val fileManager: VaultFileManager
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
    }

    fun setupPin(pin: String) {
        viewModelScope.launch {
            pinManager.setPin(pin)
            _uiState.update {
                it.copy(isPinSet = true, isUnlocked = true, error = null)
            }
        }
    }

    fun unlockWithPin(pin: String) {
        viewModelScope.launch {
            if (pinManager.verifyPin(pin)) {
                _uiState.update {
                    it.copy(isUnlocked = true, error = null, failedAttempts = 0)
                }
            } else {
                _uiState.update {
                    it.copy(
                        error = "Wrong PIN. Try again.",
                        failedAttempts = it.failedAttempts + 1
                    )
                }
            }
        }
    }

    fun unlockWithBiometric() {
        _uiState.update { it.copy(isUnlocked = true, error = null, failedAttempts = 0) }
    }

    /** Locks the vault and wipes every decrypted preview/shared copy. */
    fun lock() {
        if (!_uiState.value.isUnlocked) return
        _uiState.update { it.copy(isUnlocked = false, error = null) }
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
                    fileManager = app.container.fileManager
                )
            }
        }
    }
}
