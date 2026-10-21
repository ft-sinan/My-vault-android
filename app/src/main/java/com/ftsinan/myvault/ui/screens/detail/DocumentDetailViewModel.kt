package com.ftsinan.myvault.ui.screens.detail

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
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface PreviewState {
    data object Loading : PreviewState
    class Image(val bytes: ByteArray) : PreviewState
    data class Pdf(val file: File) : PreviewState
    data class OtherFile(val file: File) : PreviewState
    data class Error(val message: String) : PreviewState
}

data class SharePayload(
    val uri: Uri,
    val mimeType: String,
    val title: String
)

class DocumentDetailViewModel(
    application: Application,
    private val repository: DocumentRepository,
    private val documentId: Long
) : AndroidViewModel(application) {

    val document: StateFlow<VaultDocument?> = repository.observeById(documentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _preview = MutableStateFlow<PreviewState>(PreviewState.Loading)
    val preview: StateFlow<PreviewState> = _preview.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    init {
        loadPreview()
    }

    private fun loadPreview() {
        viewModelScope.launch {
            _preview.value = withContext(Dispatchers.IO) {
                runCatching {
                    val doc = repository.getById(documentId)
                        ?: return@runCatching PreviewState.Error("Document not found")
                    when {
                        doc.isImage -> PreviewState.Image(
                            repository.fileManager.decryptToBytes(doc)
                        )
                        doc.isPdf -> PreviewState.Pdf(
                            repository.fileManager.decryptToCache(doc)
                        )
                        else -> PreviewState.OtherFile(
                            repository.fileManager.decryptToCache(doc)
                        )
                    }
                }.getOrElse {
                    PreviewState.Error(it.message ?: "Could not open this document")
                }
            }
        }
    }

    fun toggleFavourite() {
        val doc = document.value ?: return
        viewModelScope.launch {
            repository.setFavourite(doc.id, !doc.isFavourite)
        }
    }

    fun delete() {
        val doc = document.value ?: return
        viewModelScope.launch {
            repository.delete(doc)
            _isDeleted.value = true
        }
    }

    /**
     * Decrypts a temporary copy for the system share sheet.
     * The copy lives in cache and is wiped when the vault locks.
     */
    suspend fun prepareShare(): SharePayload? {
        val doc = document.value ?: return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val file = repository.fileManager.decryptToCache(doc, forSharing = true)
                val app = getApplication<Application>()
                val uri = FileProvider.getUriForFile(
                    app,
                    "${app.packageName}.fileprovider",
                    file
                )
                SharePayload(uri = uri, mimeType = doc.mimeType, title = doc.title)
            }.getOrNull()
        }
    }

    companion object {
        fun factory(documentId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                DocumentDetailViewModel(
                    application = app,
                    repository = app.container.documentRepository,
                    documentId = documentId
                )
            }
        }
    }
}
