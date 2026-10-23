package com.ftsinan.myvault.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.security.CryptoManager
import com.ftsinan.myvault.util.MimeTypeDetector
import java.io.BufferedInputStream
import java.io.File
import java.util.UUID

data class ImportedFile(
    val encryptedFileName: String,
    val mimeType: String,
    val originalFileName: String,
    val fileSize: Long
)

private const val MIME_HEADER_SIZE = 16

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
        val encryptedName = "${UUID.randomUUID()}.enc"

        val input = resolver.openInputStream(uri)
            ?: error("Could not open the selected file")

        // Scanner providers may not report a MIME type or a useful file
        // extension. Sniff the actual file header before encrypting so a
        // scanned JPEG is never saved as application/octet-stream.
        var detectedMimeType: String? = null
        BufferedInputStream(input).use { stream ->
            stream.mark(MIME_HEADER_SIZE)
            val headerBuffer = ByteArray(MIME_HEADER_SIZE)
            var offset = 0
            while (offset < headerBuffer.size) {
                val read = stream.read(headerBuffer, offset, headerBuffer.size - offset)
                if (read <= 0) break
                offset += read
            }
            if (offset > 0) {
                detectedMimeType = MimeTypeDetector.detect(headerBuffer.copyOf(offset))
            }
            stream.reset()
            cryptoManager.encrypt(stream, File(vaultDir, encryptedName))
        }

        val reportedMimeType = resolver.getType(uri)
            ?.lowercase()
            ?.takeUnless { it == "application/octet-stream" }
        val guessedMimeType = guessMimeType(info.first)
            .takeUnless { it == "application/octet-stream" }
        val mimeType = detectedMimeType
            ?: reportedMimeType
            ?: guessedMimeType
            ?: "application/octet-stream"
        val originalFileName = MimeTypeDetector.fileNameWithExtension(info.first, mimeType)

        val size = when {
            info.second > 0 -> info.second
            else -> runCatching {
                resolver.openInputStream(uri)?.use { it.readBytes().size.toLong() } ?: 0L
            }.getOrDefault(0L)
        }

        return ImportedFile(
            encryptedFileName = encryptedName,
            mimeType = mimeType,
            originalFileName = originalFileName,
            fileSize = size
        )
    }

    fun encryptedFile(document: VaultDocument): File =
        File(vaultDir, document.encryptedFileName)

    fun encryptedFile(encryptedFileName: String): File =
        File(vaultDir, encryptedFileName)

    /** Decrypted bytes for in-memory image rendering. */
    fun decryptToBytes(document: VaultDocument): ByteArray =
        decryptToBytes(document.encryptedFileName)

    fun decryptToBytes(encryptedFileName: String): ByteArray =
        cryptoManager.decryptToBytes(encryptedFile(encryptedFileName))

    /**
     * Decrypts into the cache dir and returns the plain file.
     * Needed for PdfRenderer and for sharing. Callers must rely on
     * [clearDecryptedCache] (done on lock) to wipe these again.
     */
    fun decryptToCache(document: VaultDocument, forSharing: Boolean = false): File =
        decryptToCache(
            encryptedFileName = document.encryptedFileName,
            originalFileName = document.originalFileName,
            cacheKey = document.id.toString(),
            forSharing = forSharing
        )

    fun decryptToCache(
        encryptedFileName: String,
        originalFileName: String,
        cacheKey: String,
        forSharing: Boolean = false
    ): File {
        val dir = if (forSharing) shareDir else previewDir
        val safeName = originalFileName
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "document" }
        val outFile = File(dir, "${cacheKey}_$safeName")
        return cryptoManager.decryptToFile(encryptedFile(encryptedFileName), outFile)
    }

    /** Cache file a generated share image/PDF can be written into. */
    fun shareCacheFile(fileName: String): File {
        shareDir.mkdirs()
        return File(shareDir, fileName)
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
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)
        }.getOrNull()?.use { cursor ->
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
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "heic", "heif" -> "image/heic"
            "avif" -> "image/avif"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
}
