package com.ftsinan.myvault.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One encrypted page/file belonging to a [VaultDocument].
 *
 * Scanned documents have one image page per scanned side/page. An
 * imported PDF is preserved as a single page row whose mimeType is
 * application/pdf; the viewer renders its internal pages on demand.
 * Pages are fixed once the document is saved.
 */
@Entity(
    tableName = "document_pages",
    foreignKeys = [
        ForeignKey(
            entity = VaultDocument::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class DocumentPage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val encryptedFileName: String,
    val mimeType: String,
    val originalFileName: String,
    val fileSize: Long,
    val sortOrder: Int = 0
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isPdf: Boolean get() = mimeType == "application/pdf"
}
