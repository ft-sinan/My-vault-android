package com.ftsinan.myvault.ui.screens.lock

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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.crypto.AEADBadTagException

enum class FirstRunBackupPhase {
    IDLE,
    INSPECTING,
    PASSWORD_REQUIRED,
    PREPARING,
    READY,
    RESTORING,
    COMPLETED
}

data class FirstRunBackupUiState(
    val phase: FirstRunBackupPhase = FirstRunBackupPhase.IDLE,
    val restoreUri: Uri? = null,
    val summary: BackupSummary? = null,
    val profileOptions: List<BackupProfileOption> = emptyList(),
    val selectedOwnerBackupId: Long? = null,
    val createNewOwnerSelected: Boolean = false,
    @StringRes val messageRes: Int? = null,
    val messageIsError: Boolean = false
) {
    val isBusy: Boolean = phase == FirstRunBackupPhase.INSPECTING ||
        phase == FirstRunBackupPhase.PREPARING ||
        phase == FirstRunBackupPhase.RESTORING
}

/**
 * First-open restore has no existing vault PIN to confirm yet. The backup
 * password (when the file has one) is therefore the credential verified
 * before anything is staged. It uses the non-destructive merge path so any
 * partial no-owner data already on the phone is preserved.
 */
class FirstRunBackupViewModel(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FirstRunBackupUiState())
    val uiState: StateFlow<FirstRunBackupUiState> = _uiState.asStateFlow()

    private var preparedRestore: PreparedBackupRestore? = null

    fun onRestoreFileSelected(uri: Uri) {
        val phase = _uiState.value.phase
        if (_uiState.value.isBusy ||
            phase == FirstRunBackupPhase.READY ||
            phase == FirstRunBackupPhase.COMPLETED
        ) {
            return
        }

        val previous = preparedRestore
        preparedRestore = null
        _uiState.value = FirstRunBackupUiState(
            phase = FirstRunBackupPhase.INSPECTING,
            restoreUri = uri
        )

        viewModelScope.launch {
            if (previous != null) backupManager.discardRestore(previous)
            try {
                when (backupManager.inspectProtection(uri)) {
                    BackupProtection.NONE -> prepareRestore(uri, null)
                    BackupProtection.PASSWORD -> {
                        _uiState.value = FirstRunBackupUiState(
                            phase = FirstRunBackupPhase.PASSWORD_REQUIRED,
                            restoreUri = uri
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.value = FirstRunBackupUiState(
                    messageRes = R.string.backup_invalid_file,
                    messageIsError = true
                )
            }
        }
    }

    fun prepareProtectedRestore(password: String) {
        if (_uiState.value.phase != FirstRunBackupPhase.PASSWORD_REQUIRED) return
        val uri = _uiState.value.restoreUri ?: return
        viewModelScope.launch {
            prepareRestore(uri, password.toCharArray())
        }
    }

    private suspend fun prepareRestore(uri: Uri, password: CharArray?) {
        _uiState.value = FirstRunBackupUiState(
            phase = FirstRunBackupPhase.PREPARING,
            restoreUri = uri
        )
        try {
            val prepared = backupManager.prepareRestore(uri, password)
            preparedRestore = prepared
            _uiState.value = FirstRunBackupUiState(
                phase = FirstRunBackupPhase.READY,
                restoreUri = uri,
                summary = prepared.summary,
                profileOptions = prepared.profileOptions,
                selectedOwnerBackupId = prepared.profileOptions
                    .firstOrNull { it.isOriginalOwner }?.backupMemberId
                    ?: prepared.profileOptions.firstOrNull()?.backupMemberId,
                createNewOwnerSelected = false
            )
        } catch (_: AEADBadTagException) {
            _uiState.value = FirstRunBackupUiState(
                phase = FirstRunBackupPhase.PASSWORD_REQUIRED,
                restoreUri = uri,
                messageRes = R.string.backup_wrong_password,
                messageIsError = true
            )
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _uiState.value = FirstRunBackupUiState(
                messageRes = R.string.backup_invalid_file,
                messageIsError = true
            )
        }
    }

    fun selectRestoreOwner(backupMemberId: Long) {
        val state = _uiState.value
        if (state.phase != FirstRunBackupPhase.READY) return
        if (state.profileOptions.none { it.backupMemberId == backupMemberId }) return
        _uiState.value = state.copy(
            selectedOwnerBackupId = backupMemberId,
            createNewOwnerSelected = false,
            messageRes = null,
            messageIsError = false
        )
    }

    fun selectCreateNewOwnerProfile() {
        val state = _uiState.value
        if (state.phase != FirstRunBackupPhase.READY) return
        if (state.profileOptions.isEmpty()) return
        _uiState.value = state.copy(
            selectedOwnerBackupId = null,
            createNewOwnerSelected = true,
            messageRes = null,
            messageIsError = false
        )
    }

    fun commitRestore() {
        if (_uiState.value.phase != FirstRunBackupPhase.READY) return
        val prepared = preparedRestore ?: return
        val state = _uiState.value
        val createNewOwner = state.createNewOwnerSelected &&
            prepared.profileOptions.isNotEmpty()
        val selectedOwnerBackupId = when {
            createNewOwner -> null
            prepared.profileOptions.isEmpty() -> null
            else -> state.selectedOwnerBackupId
                ?: prepared.profileOptions.firstOrNull { it.isOriginalOwner }?.backupMemberId
                ?: prepared.profileOptions.firstOrNull()?.backupMemberId
        }
        _uiState.value = _uiState.value.copy(
            phase = FirstRunBackupPhase.RESTORING,
            profileOptions = prepared.profileOptions,
            selectedOwnerBackupId = selectedOwnerBackupId,
            createNewOwnerSelected = createNewOwner,
            messageRes = null,
            messageIsError = false
        )

        viewModelScope.launch {
            try {
                backupManager.commitRestore(
                    prepared = prepared,
                    restoreMode = BackupRestoreMode.ADD_TO_VAULT,
                    selectedOwnerBackupMemberId = selectedOwnerBackupId,
                    createNewOwnerProfile = createNewOwner
                )
                preparedRestore = null
                _uiState.value = FirstRunBackupUiState(
                    phase = FirstRunBackupPhase.COMPLETED,
                    summary = prepared.summary,
                    profileOptions = prepared.profileOptions,
                    selectedOwnerBackupId = selectedOwnerBackupId,
                    createNewOwnerSelected = createNewOwner,
                    messageRes = R.string.backup_restored
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // BackupManager discards the staged files when commit fails.
                preparedRestore = null
                _uiState.value = FirstRunBackupUiState(
                    messageRes = R.string.first_run_backup_restore_failed,
                    messageIsError = true
                )
            }
        }
    }

    fun cancelRestore() {
        if (_uiState.value.isBusy || _uiState.value.phase == FirstRunBackupPhase.COMPLETED) {
            return
        }
        val prepared = preparedRestore
        preparedRestore = null
        _uiState.value = FirstRunBackupUiState()
        if (prepared != null) {
            viewModelScope.launch { backupManager.discardRestore(prepared) }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            messageRes = null,
            messageIsError = false
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                FirstRunBackupViewModel(
                    backupManager = app.container.backupManager
                )
            }
        }
    }
}
