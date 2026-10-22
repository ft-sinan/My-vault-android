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
 * Relationship of a family member to the phone owner.
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
 * Built-in non-personal owners for documents that do not belong to one
 * specific person (vehicle papers, home documents, family documents...).
 */
enum class AssignedGroup(val label: String) {
    FAMILY("Family"),
    VEHICLE("Vehicle"),
    HOME("Home / Property"),
    OTHER("Other")
}

/**
 * One document in the vault. Its pages live in [DocumentPage] rows and
 * are stored AES-GCM encrypted in internal storage.
 *
 * A document is assigned either to a family member ([memberId]) or to a
 * built-in non-personal group ([assignedGroup]).
 *
 * The single-file fields ([encryptedFileName] etc.) and the legacy
 * issue/expiry columns are kept for the v1 -> v2 migration: new
 * documents mirror their first page into them, and the current UI no
 * longer asks for issue/expiry dates.
 */
@Entity(tableName = "documents")
data class VaultDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** User-facing name, e.g. "My Aadhaar Card". */
    val title: String,

    val category: Category,

    /** Specific type, e.g. "Aadhaar Card", "PAN Card", "SSLC Certificate". */
    val docType: String,

    /** Snapshot of the assignee display name (kept in sync with the member). */
    val holderName: String,

    val relation: Relation = Relation.SELF,

    /** Family member this document belongs to, if personal. */
    val memberId: Long? = null,

    /** Non-personal group this document belongs to, if not personal. */
    val assignedGroup: AssignedGroup? = null,

    /** Aadhaar no., PAN no., licence no. etc. Shown masked by default. */
    val documentNumber: String? = null,

    // Legacy columns (not collected by the current UI).
    val issueDate: Long? = null,
    val expiryDate: Long? = null,
    val notes: String? = null,

    /** Legacy/first-page file name of the encrypted file inside files/vault/. */
    val encryptedFileName: String,

    /** MIME type of the first page/original file. */
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
