package com.ftsinan.myvault.data.model

/** Document row enriched for list/dashboard display. */
data class DocumentSummary(
    val document: VaultDocument,
    val pageCount: Int,
    val totalSize: Long,
    /** Member name or assigned group label, for display/search. */
    val assignedName: String,
    val member: MemberProfile? = null,
    val isPdf: Boolean = false
)

/** Member enriched with a decrypted avatar (kept in memory only) and counts. */
class MemberListItem(
    val member: MemberProfile,
    val photoBytes: ByteArray?,
    val documentCount: Int
)
