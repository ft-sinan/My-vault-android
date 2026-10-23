package com.ftsinan.myvault.ui.screens.detail

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.DocumentPage
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.util.MimeTypeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed interface PagePreview {
    class Image(val pageId: Long, val bytes: ByteArray) : PagePreview
    data class Pdf(val pageId: Long, val file: File) : PagePreview
    data class Other(val pageId: Long, val file: File) : PagePreview
}

enum class ShareFormat { PDF, IMAGES }

data class SharePayload(
    val uris: List<Uri>,
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

    val pages: StateFlow<List<DocumentPage>> = repository.observePages(documentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _previews = MutableStateFlow<List<PagePreview>?>(null)
    val previews: StateFlow<List<PagePreview>?> = _previews.asStateFlow()

    private val _previewError = MutableStateFlow<String?>(null)
    val previewError: StateFlow<String?> = _previewError.asStateFlow()

    /** Includes internal pages of an imported PDF file. */
    private val _visualPageCount = MutableStateFlow(0)
    val visualPageCount: StateFlow<Int> = _visualPageCount.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    init {
        loadPreviews()
    }

    private suspend fun loadPagesOrLegacy(document: VaultDocument): List<DocumentPage> {
        val stored = repository.getPages(document.id)
        val pages = if (stored.isNotEmpty()) {
            stored
        } else {
            listOf(
                DocumentPage(
                    id = 0,
                    documentId = document.id,
                    encryptedFileName = document.encryptedFileName,
                    mimeType = document.mimeType,
                    originalFileName = document.originalFileName,
                    fileSize = document.fileSize,
                    sortOrder = 0
                )
            )
        }
        return pages.map { page -> resolveStoredMimeType(document, page) }
    }

    /**
     * Older scanned pages may have been saved as application/octet-stream
     * when the scanner provider exposed no MIME metadata. Detect the real
     * type from the decrypted header and persist the correction so preview,
     * sharing and PDF generation all work without rescanning the document.
     */
    private suspend fun resolveStoredMimeType(
        document: VaultDocument,
        page: DocumentPage
    ): DocumentPage {
        if (page.isImage || page.isPdf) return page

        val header = runCatching {
            val bytes = repository.fileManager.decryptToBytes(page.encryptedFileName)
            bytes.copyOf(minOf(16, bytes.size))
        }.getOrNull() ?: return page

        val detected = MimeTypeDetector.detect(header) ?: return page
        runCatching {
            repository.repairPageMimeType(document, page, detected)
        }
        return page.copy(mimeType = detected)
    }

    private fun loadPreviews() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val document = repository.getById(documentId)
                        ?: error("Document not found")
                    val pages = loadPagesOrLegacy(document)
                    var visualCount = 0
                    val result = pages.map { page ->
                        when {
                            page.isImage -> {
                                visualCount += 1
                                PagePreview.Image(
                                    pageId = page.id,
                                    bytes = repository.fileManager
                                        .decryptToBytes(page.encryptedFileName)
                                )
                            }
                            page.isPdf -> {
                                val file = repository.fileManager.decryptToCache(
                                    encryptedFileName = page.encryptedFileName,
                                    originalFileName = page.originalFileName,
                                    cacheKey = "${document.id}-${page.id}"
                                )
                                visualCount += pdfPageCount(file).coerceAtLeast(1)
                                PagePreview.Pdf(page.id, file)
                            }
                            else -> {
                                visualCount += 1
                                PagePreview.Other(
                                    page.id,
                                    repository.fileManager.decryptToCache(
                                        encryptedFileName = page.encryptedFileName,
                                        originalFileName = page.originalFileName,
                                        cacheKey = "${document.id}-${page.id}"
                                    )
                                )
                            }
                        }
                    }
                    _visualPageCount.value = visualCount
                    result
                }
            }.onSuccess {
                _previews.value = it
            }.onFailure {
                _previews.value = emptyList()
                _previewError.value = "PREVIEW_FAILED"
            }
        }
    }

    fun toggleFavourite() {
        val doc = document.value ?: return
        viewModelScope.launch { repository.setFavourite(doc.id, !doc.isFavourite) }
    }

    fun delete() {
        val doc = document.value ?: return
        viewModelScope.launch {
            repository.delete(doc)
            _isDeleted.value = true
        }
    }

    suspend fun prepareShare(format: ShareFormat): SharePayload? {
        val doc = document.value ?: return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val pages = loadPagesOrLegacy(doc)
                when (format) {
                    ShareFormat.PDF -> preparePdfShare(doc, pages)
                    ShareFormat.IMAGES -> prepareImagesShare(doc, pages)
                }
            }.getOrNull()
        }
    }

    private fun preparePdfShare(
        document: VaultDocument,
        pages: List<DocumentPage>
    ): SharePayload {
        val singlePdf = pages.singleOrNull()?.takeIf { it.isPdf }
        val file = if (singlePdf != null) {
            repository.fileManager.decryptToCache(
                encryptedFileName = singlePdf.encryptedFileName,
                originalFileName = singlePdf.originalFileName,
                cacheKey = "${document.id}-share",
                forSharing = true
            )
        } else {
            createPdfFromPages(document, pages)
        }
        return SharePayload(
            uris = listOf(file.toShareUri()),
            mimeType = "application/pdf",
            title = document.title
        )
    }

    private fun prepareImagesShare(
        document: VaultDocument,
        pages: List<DocumentPage>
    ): SharePayload {
        val files = mutableListOf<File>()
        pages.forEachIndexed { pageIndex, page ->
            when {
                page.isImage -> files += repository.fileManager.decryptToCache(
                    encryptedFileName = page.encryptedFileName,
                    originalFileName = page.originalFileName,
                    cacheKey = "${document.id}-share-$pageIndex",
                    forSharing = true
                )
                page.isPdf -> {
                    val pdfFile = repository.fileManager.decryptToCache(
                        encryptedFileName = page.encryptedFileName,
                        originalFileName = page.originalFileName,
                        cacheKey = "${document.id}-share-src-$pageIndex",
                        forSharing = true
                    )
                    renderPdfBitmaps(pdfFile).forEachIndexed { index, bitmap ->
                        val out = repository.fileManager.shareCacheFile(
                            "${document.id}-page-${pageIndex + 1}-${index + 1}.jpg"
                        )
                        FileOutputStream(out).use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it)
                        }
                        bitmap.recycle()
                        files += out
                    }
                }
                else -> files += repository.fileManager.decryptToCache(
                    encryptedFileName = page.encryptedFileName,
                    originalFileName = page.originalFileName,
                    cacheKey = "${document.id}-share-$pageIndex",
                    forSharing = true
                )
            }
        }
        val mime = if (files.size > 1) "image/*"
        else pages.firstOrNull()?.mimeType ?: "image/*"
        return SharePayload(
            uris = files.map { it.toShareUri() },
            mimeType = mime,
            title = document.title
        )
    }

    private fun createPdfFromPages(
        document: VaultDocument,
        pages: List<DocumentPage>
    ): File {
        val safeTitle = document.title.replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "document" }
        val outFile = repository.fileManager.shareCacheFile("${document.id}_$safeTitle.pdf")
        val pdfDocument = PdfDocument()
        try {
            var pageNumber = 1
            pages.forEach { page ->
                pageBitmaps(page).forEach { bitmap ->
                    val scaled = scaleForPdf(bitmap)
                    val info = PdfDocument.PageInfo
                        .Builder(scaled.width, scaled.height, pageNumber++)
                        .create()
                    val pdfPage = pdfDocument.startPage(info)
                    pdfPage.canvas.drawBitmap(scaled, 0f, 0f, null)
                    pdfDocument.finishPage(pdfPage)
                    if (scaled !== bitmap) scaled.recycle()
                    bitmap.recycle()
                }
            }
            FileOutputStream(outFile).use { pdfDocument.writeTo(it) }
        } finally {
            pdfDocument.close()
        }
        return outFile
    }

    private fun pageBitmaps(page: DocumentPage): List<Bitmap> = when {
        page.isImage -> {
            val bytes = repository.fileManager.decryptToBytes(page.encryptedFileName)
            listOfNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        }
        page.isPdf -> {
            val file = repository.fileManager.decryptToCache(
                encryptedFileName = page.encryptedFileName,
                originalFileName = page.originalFileName,
                cacheKey = "pdf-src-${page.documentId}-${page.id}",
                forSharing = true
            )
            renderPdfBitmaps(file)
        }
        else -> emptyList()
    }

    private fun scaleForPdf(bitmap: Bitmap): Bitmap {
        val maxWidth = 1400
        if (bitmap.width <= maxWidth) return bitmap
        val targetHeight = (bitmap.height.toFloat() * maxWidth / bitmap.width).toInt()
            .coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, maxWidth, targetHeight, true)
    }

    private fun renderPdfBitmaps(file: File): List<Bitmap> {
        val result = mutableListOf<Bitmap>()
        val descriptor = ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
        descriptor.use { fd ->
            PdfRenderer(fd).use { renderer ->
                for (index in 0 until renderer.pageCount) {
                    renderer.openPage(index).use { page ->
                        val width = (page.width * 2).coerceAtLeast(1)
                        val height = (page.height * 2).coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )
                        result += bitmap
                    }
                }
            }
        }
        return result
    }

    private fun pdfPageCount(file: File): Int = runCatching {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { it.pageCount }
        }
    }.getOrDefault(0)

    private fun File.toShareUri(): Uri {
        val app = getApplication<Application>()
        return FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", this)
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
