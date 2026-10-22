package com.ftsinan.myvault.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A person in the family vault. The phone owner is simply the member
 * with [isOwner] = true and relation SELF.
 *
 * Only [name] is meaningfully required in the UI; photo, DOB, gender and
 * blood group can be completed whenever convenient.
 */
@Entity(tableName = "member_profiles")
data class MemberProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relation: Relation = Relation.OTHER,
    val photoEncryptedFileName: String? = null,
    val photoMimeType: String? = null,
    val dateOfBirth: Long? = null,
    /** Stored as a stable English key, e.g. "Male", "Female", "Other". */
    val gender: String? = null,
    val bloodGroup: String? = null,
    val isOwner: Boolean = false,
    /** Visual order only (drag-to-reorder in Settings). */
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object ProfileOptions {
    val genders = listOf("Male", "Female", "Other", "Prefer not to say")
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown")
}
