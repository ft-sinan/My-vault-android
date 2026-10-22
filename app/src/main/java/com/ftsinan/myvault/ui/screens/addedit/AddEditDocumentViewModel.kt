package com.ftsinan.myvault.ui.screens.addedit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.DocumentPage
import com.ftsinan.myvault.data.model.DocumentTypes
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.util.OcrManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AssignmentMode { PERSON, GROUP }

data class AddEditUiState(
    val isEditMode: Boolean = false,
    val existing: VaultDocument? = null,
    val existingPages: List<DocumentPage> = emptyList(),
    val members: List<MemberProfile> = emptyList(),
    val title: String = "",
    val category: Category = Category.IDENTITY,
    val docType: String = "",
    val assignmentMode: AssignmentMode = AssignmentMode.PERSON,
    val selectedMemberId: Long? = null,
    val selectedGroup: AssignedGroup = AssignedGroup.FAMILY,
    val documentNumber: String = "",
    /** Pages waiting to be encrypted/imported on Save (new documents only). */
    val pendingUris: List<Uri> = emptyList(),
    val pendingFileLabel: String? = null,
    val pendingIsPdf: Boolean = false,
    val isOcrRunning: Boolean = false,
    val ocrCompleted: Boolean = false,
    val ocrFoundNumber: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class AddEditDocumentViewModel(
    application: Application,
    private val repository: DocumentRepository,
    private val ocrManager: OcrManager
) : AndroidViewModel(application) {

    private val fileManager = repository.fileManager

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private var loadRequested = false

    init {
        viewModelScope.launch {
            repository.members.collect { members ->
                _uiState.update { state ->
                    state.copy(
                        members = members,
                        selectedMemberId = state.selectedMemberId
                            ?: members.firstOrNull { it.isOwner }?.id
                            ?: members.firstOrNull()?.id
                    )
                }
            }
        }
    }

    fun load(documentId: Long?) {
        if (loadRequested) return
        loadRequested = true
        if (documentId == null) return
        viewModelScope.launch {
            val document = repository.getById(documentId) ?: return@launch
            val pages = repository.getPages(documentId)
            _uiState.update { state ->
                state.copy(
                    isEditMode = true,
                    existing = document,
                    existingPages = pages,
                    title = document.title,
                    category = document.category,
                    docType = document.docType,
                    assignmentMode = if (document.memberId != null) {
                        AssignmentMode.PERSON
                    } else {
                        AssignmentMode.GROUP
                    },
                    selectedMemberId = document.memberId ?: state.selectedMemberId,
                    selectedGroup = document.assignedGroup ?: AssignedGroup.FAMILY,
                    documentNumber = document.documentNumber.orEmpty(),
                    pendingFileLabel = document.originalFileName,
                    pendingIsPdf = document.isPdf
                )
            }
        }
    }

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value, error = null) }

    fun onCategoryChange(value: Category) {
        _uiState.update { state ->
            val suggested = suggestedAssignment(value)
            state.copy(
                category = value,
                assignmentMode = suggested.first ?: state.assignmentMode,
                selectedGroup = suggested.second ?: state.selectedGroup
            )
        }
    }

    fun onDocTypeChange(value: String) {
        _uiState.update { state ->
            val category = DocumentTypes.categoryForType(value) ?: state.category
            val suggested = suggestedAssignment(category)
            state.copy(
                docType = value,
                category = category,
                assignmentMode = suggested.first ?: state.assignmentMode,
                selectedGroup = suggested.second ?: state.selectedGroup,
                error = null
            )
        }
    }

    fun onAssignmentModeChange(value: AssignmentMode) =
        _uiState.update { it.copy(assignmentMode = value, error = null) }

    fun onMemberSelected(memberId: Long) =
        _uiState.update { it.copy(selectedMemberId = memberId, error = null) }

    fun onGroupSelected(group: AssignedGroup) =
        _uiState.update { it.copy(selectedGroup = group, error = null) }

    fun onDocumentNumberChange(value: String) =
        _uiState.update { it.copy(documentNumber = value) }

    private fun suggestedAssignment(
        category: Category
    ): Pair<AssignmentMode?, AssignedGroup?> = when (category) {
        Category.VEHICLE -> AssignmentMode.GROUP to AssignedGroup.VEHICLE
        Category.PROPERTY -> AssignmentMode.GROUP to AssignedGroup.HOME
        Category.IDENTITY, Category.EDUCATION, Category.HEALTH ->
            AssignmentMode.PERSON to null
        else -> null to null
    }

    /**
     * Accepts pages from the ML Kit scanner (JPEG page URIs) or a single
     * picked image/PDF file, then runs OCR for an ID-number suggestion.
     */
    fun onPagesPicked(uris: List<Uri>, label: String) {
        if (uris.isEmpty() || _uiState.value.isEditMode) return
        val app = getApplication<Application>()
        val firstMime = app.contentResolver.getType(uris.first())
        val isPdf = firstMime == "application/pdf"
        val accepted = if (isPdf) uris.take(1) else uris.take(10)

        _uiState.update {
            it.copy(
                pendingUris = accepted,
                pendingFileLabel = label,
                pendingIsPdf = isPdf,
                ocrCompleted = false,
                ocrFoundNumber = false,
                error = null
            )
        }

        if (!isPdf) runOcr(accepted) else {
            _uiState.update { it.copy(ocrCompleted = true, ocrFoundNumber = false) }
        }
    }

    fun onScannerError(message: String?) {
        _uiState.update { it.copy(error = message ?: "SCANNER_FAILED") }
    }

    private fun runOcr(uris: List<Uri>) {
        _uiState.update { it.copy(isOcrRunning = true) }
        viewModelScope.launch {
            val detected = runCatching { ocrManager.detectId(uris) }.getOrNull()
            _uiState.update { state ->
                if (state.pendingUris != uris) return@update state
                var updated = state.copy(
                    isOcrRunning = false,
                    ocrCompleted = true,
                    ocrFoundNumber = !detected?.number.isNullOrBlank()
                )
                if (detected != null) {
                    if (detected.number.isNotBlank() && updated.documentNumber.isBlank()) {
                        updated = updated.copy(documentNumber = detected.number)
                    }
                    if (updated.docType.isBlank()) {
                        val category = DocumentTypes.categoryForType(detected.docType)
                            ?: updated.category
                        val suggested = suggestedAssignment(category)
                        updated = updated.copy(
                            docType = detected.docType,
                            category = category,
                            assignmentMode = suggested.first ?: updated.assignmentMode,
                            selectedGroup = suggested.second ?: updated.selectedGroup,
                            title = if (updated.title.isBlank()) detected.docType
                            else updated.title
                        )
                    }
                }
                updated
            }
        }
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
                    val importedPages = if (existing == null) {
                        state.pendingUris.map { fileManager.importFile(it) }
                    } else {
                        emptyList()
                    }
                    val firstImported = importedPages.firstOrNull()

                    val member = if (state.assignmentMode == AssignmentMode.PERSON) {
                        state.members.firstOrNull { it.id == state.selectedMemberId }
                            ?: error("ERR_OWNER_REQUIRED")
                    } else {
                        null
                    }

                    val document = VaultDocument(
                        id = existing?.id ?: 0,
                        title = state.title.trim(),
                        category = state.category,
                        docType = state.docType.trim(),
                        holderName = member?.name ?: state.selectedGroup.label,
                        relation = member?.relation ?: Relation.FAMILY,
                        memberId = member?.id,
                        assignedGroup = if (member == null) state.selectedGroup else null,
                        documentNumber = state.documentNumber.trim().ifBlank { null },
                        issueDate = existing?.issueDate,
                        expiryDate = existing?.expiryDate,
                        notes = existing?.notes,
                        encryptedFileName = firstImported?.encryptedFileName
                            ?: existing?.encryptedFileName
                            ?: error("ERR_FILE_REQUIRED"),
                        mimeType = firstImported?.mimeType ?: existing?.mimeType
                        ?: "application/octet-stream",
                        originalFileName = firstImported?.originalFileName
                            ?: existing?.originalFileName
                            ?: "document",
                        fileSize = firstImported?.fileSize ?: existing?.fileSize ?: 0L,
                        isFavourite = existing?.isFavourite ?: false,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    if (existing == null) {
                        repository.addDocumentWithPages(document, importedPages)
                    } else {
                        // Pages are fixed after saving; only metadata changes.
                        repository.update(document)
                    }
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message?.takeIf { it.startsWith("ERR_") } ?: "ERR_SAVE_FAILED"
                    )
                }
            }
        }
    }

    private fun validationError(state: AddEditUiState): String? = when {
        state.docType.isBlank() -> "ERR_DOC_TYPE_REQUIRED"
        state.title.isBlank() -> "ERR_TITLE_REQUIRED"
        state.assignmentMode == AssignmentMode.PERSON && state.selectedMemberId == null ->
            "ERR_OWNER_REQUIRED"
        !state.isEditMode && state.pendingUris.isEmpty() ->
            "ERR_FILE_REQUIRED"
        else -> null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                AddEditDocumentViewModel(
                    application = app,
                    repository = app.container.documentRepository,
                    ocrManager = app.container.ocrManager
                )
            }
        }
    }
}
