package com.ftsinan.myvault.ui.screens.addedit

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddEditUiState(
    val isEditMode: Boolean = false,
    val existing: VaultDocument? = null,
    val title: String = "",
    val category: Category = Category.IDENTITY,
    val docType: String = "",
    val holderName: String = "",
    val relation: Relation = Relation.SELF,
    val documentNumber: String = "",
    val issueDate: Long? = null,
    val expiryDate: Long? = null,
    val notes: String = "",
    /** Newly picked file waiting to be imported on Save. */
    val pendingUri: Uri? = null,
    val pendingFileLabel: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class AddEditDocumentViewModel(
    application: Application,
    private val repository: DocumentRepository
) : AndroidViewModel(application) {

    private val fileManager = repository.fileManager

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private var loadRequested = false

    fun load(documentId: Long?) {
        if (loadRequested) return
        loadRequested = true
        if (documentId == null) return
        viewModelScope.launch {
            val document = repository.getById(documentId) ?: return@launch
            _uiState.value = AddEditUiState(
                isEditMode = true,
                existing = document,
                title = document.title,
                category = document.category,
                docType = document.docType,
                holderName = document.holderName,
                relation = document.relation,
                documentNumber = document.documentNumber.orEmpty(),
                issueDate = document.issueDate,
                expiryDate = document.expiryDate,
                notes = document.notes.orEmpty(),
                pendingFileLabel = document.originalFileName
            )
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, error = null) }
    fun onCategoryChange(value: Category) = _uiState.update { it.copy(category = value) }
    fun onDocTypeChange(value: String) = _uiState.update { it.copy(docType = value, error = null) }
    fun onHolderNameChange(value: String) = _uiState.update { it.copy(holderName = value, error = null) }
    fun onRelationChange(value: Relation) = _uiState.update { it.copy(relation = value) }
    fun onDocumentNumberChange(value: String) = _uiState.update { it.copy(documentNumber = value) }
    fun onIssueDateChange(value: Long?) = _uiState.update { it.copy(issueDate = value) }
    fun onExpiryDateChange(value: Long?) = _uiState.update { it.copy(expiryDate = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun onFilePicked(uri: Uri?, label: String? = null) {
        if (uri == null) return
        _uiState.update {
            it.copy(
                pendingUri = uri,
                pendingFileLabel = label ?: "Selected file",
                error = null,
                // Sensible default title, e.g. "Aadhaar Card" once a type is chosen.
                title = if (it.title.isBlank() && it.docType.isNotBlank()) it.docType else it.title
            )
        }
    }

    /** Creates the cache file + FileProvider URI the camera app should write into. */
    fun createCaptureUri(): Uri {
        val app = getApplication<Application>()
        val file = fileManager.createCaptureFile()
        return FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
    }

    fun save() {
        val state = _uiState.value
        validationError(state)?.let { message ->
            _uiState.update { it.copy(error = message) }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val existing = state.existing
                    val imported = state.pendingUri?.let { fileManager.importFile(it) }

                    val document = VaultDocument(
                        id = existing?.id ?: 0,
                        title = state.title.trim(),
                        category = state.category,
                        docType = state.docType.trim(),
                        holderName = state.holderName.trim(),
                        relation = state.relation,
                        documentNumber = state.documentNumber.trim().ifBlank { null },
                        issueDate = state.issueDate,
                        expiryDate = state.expiryDate,
                        notes = state.notes.trim().ifBlank { null },
                        encryptedFileName = imported?.encryptedFileName
                            ?: existing?.encryptedFileName
                            ?: error("Please attach a photo or PDF of the document"),
                        mimeType = imported?.mimeType ?: existing?.mimeType
                        ?: "application/octet-stream",
                        originalFileName = imported?.originalFileName
                            ?: existing?.originalFileName
                            ?: "document",
                        fileSize = imported?.fileSize ?: existing?.fileSize ?: 0L,
                        isFavourite = existing?.isFavourite ?: false,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    if (existing == null) {
                        repository.add(document)
                    } else {
                        repository.update(document)
                        // File was replaced -> remove the old encrypted copy.
                        if (imported != null) {
                            fileManager.deleteEncrypted(existing.encryptedFileName)
                        }
                    }
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: "Could not save. Please try again."
                    )
                }
            }
        }
    }

    private fun validationError(state: AddEditUiState): String? = when {
        state.docType.isBlank() -> "Choose or type the document type (e.g. Aadhaar Card)"
        state.title.isBlank() -> "Give the document a title"
        state.holderName.isBlank() -> "Enter whose document this is"
        !state.isEditMode && state.pendingUri == null ->
            "Attach a photo or PDF of the document"
        else -> null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                AddEditDocumentViewModel(app, app.container.documentRepository)
            }
        }
    }
}
