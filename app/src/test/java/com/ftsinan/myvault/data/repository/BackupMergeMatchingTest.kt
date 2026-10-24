package com.ftsinan.myvault.data.repository

import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.MemberProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupMergeMatchingTest {

    @Test
    fun `profile names are normalized before matching`() {
        assertEquals(
            "muhammed sinan",
            normalizedProfileName("  Muhammed   Sinan ")
        )
    }

    @Test
    fun `profiles merge on normalized name and same date of birth`() {
        val backupProfile = MemberProfile(
            id = 10L,
            name = "  Muhammad   Sinan ",
            dateOfBirth = 946684800000L
        )
        val currentProfile = MemberProfile(
            id = 3L,
            name = "muhammad sinan",
            dateOfBirth = 946684800000L
        )

        assertTrue(backupProfile.matchesForBackupMerge(currentProfile))
    }

    @Test
    fun `profiles do not merge without both dates of birth`() {
        val namedProfile = MemberProfile(
            id = 10L,
            name = "Sinan"
        )
        val datedProfile = MemberProfile(
            id = 3L,
            name = "Sinan",
            dateOfBirth = 946684800000L
        )

        assertFalse(namedProfile.matchesForBackupMerge(datedProfile))
        assertFalse(datedProfile.matchesForBackupMerge(namedProfile))
    }

    @Test
    fun `profiles do not merge when name or date of birth differs`() {
        val backupProfile = MemberProfile(
            id = 10L,
            name = "Sinan",
            dateOfBirth = 946684800000L
        )

        assertFalse(
            backupProfile.matchesForBackupMerge(
                MemberProfile(
                    id = 3L,
                    name = "Sinan",
                    dateOfBirth = 978307200000L
                )
            )
        )
        assertFalse(
            backupProfile.matchesForBackupMerge(
                MemberProfile(
                    id = 4L,
                    name = "Another Person",
                    dateOfBirth = 946684800000L
                )
            )
        )
    }

    @Test
    fun `duplicate key requires same assignee and ordered page hashes`() {
        val first = mergeDocumentKey(
            memberId = 7L,
            assignedGroup = null,
            pageSha256 = listOf("ABC", "DEF")
        )
        val same = mergeDocumentKey(
            memberId = 7L,
            assignedGroup = null,
            pageSha256 = listOf("abc", "def")
        )
        val differentAssignee = mergeDocumentKey(
            memberId = 8L,
            assignedGroup = null,
            pageSha256 = listOf("abc", "def")
        )
        val differentOrder = mergeDocumentKey(
            memberId = 7L,
            assignedGroup = null,
            pageSha256 = listOf("def", "abc")
        )
        val groupKey = mergeDocumentKey(
            memberId = null,
            assignedGroup = AssignedGroup.VEHICLE,
            pageSha256 = listOf("abc", "def")
        )

        assertEquals(first, same)
        assertNotEquals(first, differentAssignee)
        assertNotEquals(first, differentOrder)
        assertNotEquals(first, groupKey)
    }

    @Test
    fun `duplicate key is unavailable when any page hash is missing`() {
        assertNull(
            mergeDocumentKey(
                memberId = 7L,
                assignedGroup = null,
                pageSha256 = emptyList()
            )
        )
        assertNull(
            mergeDocumentKey(
                memberId = 7L,
                assignedGroup = null,
                pageSha256 = listOf("abc", null)
            )
        )
        assertNull(
            mergeDocumentKey(
                memberId = 7L,
                assignedGroup = null,
                pageSha256 = listOf("abc", " ")
            )
        )
    }
}
