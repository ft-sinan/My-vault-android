package com.ftsinan.myvault.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * High-level buckets every document belongs to.
 */
enum class Category(val label: String) {
    IDENTITY("Identity"),
    EDUCATION("Education"),
    CERTIFICATES("Certificates"),
    VEHICLE("Vehicle"),
    BANKING("Banking & Finance"),
    HEALTH("Health"),
    PROPERTY("Property"),
    OTHER("Other")
}

/**
 * Whose document is this? Indian households usually keep documents
 * for the whole family in one place.
 */
enum class Relation(val label: String) {
    SELF("Self"),
    SPOUSE("Spouse"),
    FATHER("Father"),
    MOTHER("Mother"),
    SON("Son"),
    DAUGHTER("Daughter"),
    FAMILY("Family"),
    OTHER("Other")
}

/**
 * One document in the vault. The actual file (photo / PDF) is stored
 * AES-GCM encrypted in internal storage; this row only holds metadata.
 */
@Entity(tableName = "documents")
data class VaultDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** User-facing name, e.g. "My Aadhaar Card". */
    val title: String,

    val category: Category,

    /** Specific type, e.g. "Aadhaar Card", "PAN Card", "SSLC Certificate". */
    val docType: String,

    /** Name of the person this document belongs to. */
    val holderName: String,

    val relation: Relation = Relation.SELF,

    /** Aadhaar no., PAN no., passport no. etc. Shown masked by default. */
    val documentNumber: String? = null,

    val issueDate: Long? = null,
    val expiryDate: Long? = null,
    val notes: String? = null,

    /** File name (not path) of the encrypted file inside files/vault/. */
    val encryptedFileName: String,

    /** MIME type of the original file, e.g. image/jpeg or application/pdf. */
    val mimeType: String,

    val originalFileName: String,
    val fileSize: Long,

    val isFavourite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isPdf: Boolean get() = mimeType == "application/pdf"
}
