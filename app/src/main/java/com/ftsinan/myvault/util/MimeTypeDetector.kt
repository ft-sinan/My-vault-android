package com.ftsinan.myvault.util

/**
 * Detects the small set of file types My Vault previews from their
 * magic-number header.
 *
 * This matters for ML Kit scanner URIs: their content provider does not
 * always expose a MIME type or a useful file extension, even though the
 * returned page is a valid JPEG. Trusting only ContentResolver metadata can
 * therefore save a scanned image as application/octet-stream and make it
 * appear unpreviewable later.
 */
object MimeTypeDetector {

    fun detect(header: ByteArray): String? = when {
        header.startsWith(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) ->
            "image/jpeg"

        header.startsWith(
            byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
            )
        ) -> "image/png"

        header.asciiAt(0, "GIF87a") || header.asciiAt(0, "GIF89a") -> "image/gif"

        header.asciiAt(0, "RIFF") && header.asciiAt(8, "WEBP") -> "image/webp"

        header.asciiAt(0, "%PDF-") -> "application/pdf"

        header.isHeifBrand() -> "image/heic"

        header.isAvifBrand() -> "image/avif"

        else -> null
    }

    fun extensionFor(mimeType: String): String? = when (mimeType.lowercase()) {
        "image/jpeg", "image/jpg" -> "jpg"
        "image/png" -> "png"
        "image/gif" -> "gif"
        "image/webp" -> "webp"
        "image/heic", "image/heif" -> "heic"
        "image/avif" -> "avif"
        "application/pdf" -> "pdf"
        else -> null
    }

    /** Appends a useful extension when a provider supplied no file name extension. */
    fun fileNameWithExtension(fileName: String, mimeType: String): String {
        val extension = extensionFor(mimeType) ?: return fileName
        val currentExtension = fileName.substringAfterLast('.', "")
        if (currentExtension.isNotBlank() && currentExtension.length <= 5) {
            return fileName
        }
        return "${fileName.trimEnd('.')}.$extension"
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) return false
        return prefix.indices.all { index -> this[index] == prefix[index] }
    }

    private fun ByteArray.asciiAt(offset: Int, value: String): Boolean {
        if (offset < 0 || size < offset + value.length) return false
        return value.indices.all { index ->
            this[offset + index] == value[index].code.toByte()
        }
    }

    private fun ByteArray.brand(): String? {
        if (size < 12 || !asciiAt(4, "ftyp")) return null
        return String(this, 8, 4, Charsets.US_ASCII)
    }

    private fun ByteArray.isHeifBrand(): Boolean =
        brand() in setOf(
            "heic", "heix", "hevc", "heim", "heis", "hevm", "hevs",
            "mif1", "mif2", "msf1"
        )

    private fun ByteArray.isAvifBrand(): Boolean = brand() in setOf("avif", "avis")
}
