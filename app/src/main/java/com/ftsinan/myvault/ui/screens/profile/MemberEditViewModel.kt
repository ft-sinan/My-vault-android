package com.ftsinan.myvault.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemberEditUiState(
    val existing: MemberProfile? = null,
    val isOwner: Boolean = false,
    val name: String = "",
    val relation: Relation = Relation.OTHER,
    val dateOfBirth: Long? = null,
    val gender: String? = null,
    val bloodGroup: String? = null,
    val existingPhotoBytes: ByteArray? = null,
    val pendingPhotoUri: Uri? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class MemberEditViewModel(
    private val repository: DocumentRepository,
    private val memberId: Long?,
    private val ownerSetup: Boolean,
    private val replaceExistingOwner: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MemberEditUiState(
            isOwner = ownerSetup || replaceExistingOwner,
            relation = if (ownerSetup || replaceExistingOwner) {
                Relation.SELF
            } else {
                Relation.OTHER
            }
        )
    )
    val uiState: StateFlow<MemberEditUiState> = _uiState.asStateFlow()

    private var loadRequested = false

    fun load() {
        if (loadRequested) return
        loadRequested = true
        val id = memberId ?: return
        viewModelScope.launch {
            val member = repository.getMember(id) ?: return@launch
            _uiState.value = MemberEditUiState(
                existing = member,
                isOwner = member.isOwner,
                name = member.name,
                relation = member.relation,
                dateOfBirth = member.dateOfBirth,
                gender = member.gender,
                bloodGroup = member.bloodGroup,
                existingPhotoBytes = repository.memberPhotoBytes(member)
            )
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onRelationChange(value: Relation) = _uiState.update { it.copy(relation = value) }
    fun onDateOfBirthChange(value: Long?) = _uiState.update { it.copy(dateOfBirth = value) }
    fun onGenderChange(value: String?) = _uiState.update { it.copy(gender = value) }
    fun onBloodGroupChange(value: String?) = _uiState.update { it.copy(bloodGroup = value) }
    fun onPhotoPicked(uri: Uri?) {
        if (uri != null) _uiState.update { it.copy(pendingPhotoUri = uri) }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "NAME_REQUIRED") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val existing = state.existing
                val member = MemberProfile(
                    id = existing?.id ?: 0,
                    name = state.name.trim(),
                    relation = if (state.isOwner) Relation.SELF else state.relation,
                    photoEncryptedFileName = existing?.photoEncryptedFileName,
                    photoMimeType = existing?.photoMimeType,
                    dateOfBirth = state.dateOfBirth,
                    gender = state.gender,
                    bloodGroup = state.bloodGroup,
                    isOwner = state.isOwner,
                    sortOrder = existing?.sortOrder ?: 0,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                if (replaceExistingOwner && existing == null) {
                    repository.createMemberAndMakeOwner(member, state.pendingPhotoUri)
                } else {
                    repository.saveMember(member, state.pendingPhotoUri)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "PROFILE_SAVE_FAILED"
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            memberId: Long?,
            ownerSetup: Boolean,
            replaceExistingOwner: Boolean = false
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val app = this[APPLICATION_KEY] as MyVaultApplication
                    MemberEditViewModel(
                        repository = app.container.documentRepository,
                        memberId = memberId,
                        ownerSetup = ownerSetup,
                        replaceExistingOwner = replaceExistingOwner
                    )
                }
            }
    }
}
