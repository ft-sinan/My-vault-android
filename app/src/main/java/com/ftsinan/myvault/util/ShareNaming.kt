package com.ftsinan.myvault.util

/**
 * Names and captions used when a document leaves the vault through the
 * Android share sheet, so the receiver can tell whose document it is
 * without opening it.
 */
object ShareNaming {

    private const val MAX_BASE_NAME_LENGTH = 100
    private val INVALID_FILE_CHARS = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")
    private val REPEATED_SPACES = Regex("\\s+")

    /**
     * Base display name for a shared document: the owner's name first for
     * personal documents, then the document title. Group documents simply
     * use their title.
     */
    fun shareBaseName(ownerName: String?, title: String): String {
        val owner = ownerName?.trim().orEmpty()
        val combined = if (owner.isNotBlank()) {
            "$owner - ${title.trim()}"
        } else {
            title.trim()
        }
        return sanitizeFileName(combined)
    }

    /** File name for a document shared as PDF, e.g. "Sinan mk - Driving Licence.pdf". */
    fun pdfFileName(ownerName: String?, title: String): String {
        val base = shareBaseName(ownerName, title)
            .let { name ->
                if (name.lowercase().endsWith(".pdf")) name.dropLast(4).trimEnd() else name
            }
            .ifBlank { "document" }
        return "$base.pdf"
    }

    /**
     * Caption sent together with shared images:
     *
     * ```
     * Sinan mk Aadhaar
     *
     * 8983 7738 2277
     * ```
     *
     * The ID number line is included only when a number is stored.
     */
    fun imageShareText(
        ownerName: String?,
        docType: String?,
        documentNumber: String?,
        fallbackTitle: String
    ): String {
        val type = docType?.trim().orEmpty()
            .ifBlank { fallbackTitle.trim() }
        val firstLine = listOfNotNull(
            ownerName?.trim()?.takeIf { it.isNotBlank() },
            type.takeIf { it.isNotBlank() }
        ).joinToString(" ").replace(REPEATED_SPACES, " ")

        val number = documentNumber?.trim().orEmpty()
        return when {
            firstLine.isNotBlank() && number.isNotBlank() -> "$firstLine\n\n$number"
            firstLine.isNotBlank() -> firstLine
            else -> number
        }
    }

    /** Makes a user-facing string safe to use as a file name, keeping spaces. */
    fun sanitizeFileName(name: String): String {
        val cleaned = name
            .replace(INVALID_FILE_CHARS, " ")
            .replace(REPEATED_SPACES, " ")
            .trim(' ', '.')
            .take(MAX_BASE_NAME_LENGTH)
            .trim(' ', '.')
        return cleaned.ifBlank { "document" }
    }
}
