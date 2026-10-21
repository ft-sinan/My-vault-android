package com.ftsinan.myvault.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.security.CryptoManager
import java.io.File
import java.util.UUID

data class ImportedFile(
    val encryptedFileName: String,
    val mimeType: String,
    val originalFileName: String,
    val fileSize: Long
)

/**
 * Owns everything file-related:
 *  - importing a picked photo/PDF into encrypted internal storage
 *  - decrypting on demand for preview/share (into cache only)
 *  - cleaning decrypted copies up again
 */
class VaultFileManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {

    private val vaultDir: File get() = File(context.filesDir, "vault")
    private val previewDir: File get() = File(context.cacheDir, "preview")
    private val shareDir: File get() = File(context.cacheDir, "shared")
    private val captureDir: File get() = File(context.cacheDir, "capture")

    /** Copies the picked [uri] into the vault, encrypted. */
    fun importFile(uri: Uri): ImportedFile {
        val resolver = context.contentResolver
        val info = queryFileInfo(uri)
        val mimeType = resolver.getType(uri) ?: guessMimeType(info.first)
        val encryptedName = "${UUID.randomUUID()}.enc"

        val input = resolver.openInputStream(uri)
            ?: error("Could not open the selected file")
        input.use { stream ->
            cryptoManager.encrypt(stream, File(vaultDir, encryptedName))
        }

        val size = when {
            info.second > 0 -> info.second
            else -> runCatching {
                resolver.openInputStream(uri)?.use { it.readBytes().size.toLong() } ?: 0L
            }.getOrDefault(0L)
        }

        return ImportedFile(
            encryptedFileName = encryptedName,
            mimeType = mimeType,
            originalFileName = info.first,
            fileSize = size
        )
    }

    fun encryptedFile(document: VaultDocument): File =
        File(vaultDir, document.encryptedFileName)

    /** Decrypted bytes for in-memory image rendering. */
    fun decryptToBytes(document: VaultDocument): ByteArray =
        cryptoManager.decryptToBytes(encryptedFile(document))

    /**
     * Decrypts into the cache dir and returns the plain file.
     * Needed for PdfRenderer and for sharing. Callers must rely on
     * [clearDecryptedCache] (done on lock) to wipe these again.
     */
    fun decryptToCache(document: VaultDocument, forSharing: Boolean = false): File {
        val dir = if (forSharing) shareDir else previewDir
        val safeName = document.originalFileName
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "document" }
        val outFile = File(dir, "${document.id}_$safeName")
        return cryptoManager.decryptToFile(encryptedFile(document), outFile)
    }

    /** Empty file used as the camera capture target. */
    fun createCaptureFile(): File {
        captureDir.mkdirs()
        return File.createTempFile("scan_", ".jpg", captureDir)
    }

    fun deleteEncrypted(encryptedFileName: String) {
        runCatching { File(vaultDir, encryptedFileName).delete() }
    }

    /** Removes every decrypted copy (preview + shared + camera captures). */
    fun clearDecryptedCache() {
        listOf(previewDir, shareDir, captureDir).forEach { dir ->
            runCatching { dir.listFiles()?.forEach { it.delete() } }
        }
    }

    private fun queryFileInfo(uri: Uri): Pair<String, Long> {
        var name = "document_${System.currentTimeMillis()}"
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }?.let { name = it }
                }
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        return name to size
    }

    private fun guessMimeType(fileName: String): String =
        when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
}
