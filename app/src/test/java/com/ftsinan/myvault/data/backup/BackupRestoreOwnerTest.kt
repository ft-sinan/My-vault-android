package com.ftsinan.myvault.data.backup

import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.DocumentPage
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.repository.VaultRestoreData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestoreOwnerTest {

    @Test
    fun `selected profile becomes phone owner while full vault is kept`() {
        val restoreData = sampleRestoreData()

        val updated = restoreData.withPhoneOwner(selectedBackupMemberId = 20L)

        assertEquals(listOf(20L, 10L, 30L), updated.members.map { it.id })
        assertEquals(1, updated.members.count { it.isOwner })

        val brother = updated.members.first { it.id == 20L }
        assertTrue(brother.isOwner)
        assertEquals(Relation.SELF, brother.relation)

        val originalOwner = updated.members.first { it.id == 10L }
        assertFalse(originalOwner.isOwner)
        assertEquals(Relation.FAMILY, originalOwner.relation)

        val mother = updated.members.first { it.id == 30L }
        assertFalse(mother.isOwner)
        assertEquals(Relation.MOTHER, mother.relation)

        assertEquals(restoreData.documents.size, updated.documents.size)
        assertEquals(restoreData.pages, updated.pages)
        assertEquals(
            Relation.FAMILY,
            updated.documents.first { it.memberId == 10L }.relation
        )
        assertEquals(
            Relation.SELF,
            updated.documents.first { it.memberId == 20L }.relation
        )
        assertEquals(
            Relation.MOTHER,
            updated.documents.first { it.memberId == 30L }.relation
        )
        assertEquals(
            Relation.OTHER,
            updated.documents.first { it.assignedGroup == AssignedGroup.VEHICLE }.relation
        )
    }

    @Test
    fun `selecting original owner normalizes owner relation`() {
        val restoreData = sampleRestoreData().let { data ->
            data.copy(
                members = data.members.map { member ->
                    if (member.id == 10L) {
                        member.copy(relation = Relation.FAMILY)
                    } else {
                        member
                    }
                }
            )
        }

        val updated = restoreData.withPhoneOwner(selectedBackupMemberId = 10L)

        val owner = updated.members.single { it.isOwner }
        assertEquals(10L, owner.id)
        assertEquals(Relation.SELF, owner.relation)
        assertEquals(
            Relation.SELF,
            updated.documents.first { it.memberId == 10L }.relation
        )
    }

    @Test
    fun `profile outside backup is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            sampleRestoreData().withPhoneOwner(selectedBackupMemberId = 999L)
        }
    }

    private fun sampleRestoreData(): VaultRestoreData {
        val members = listOf(
            MemberProfile(
                id = 30L,
                name = "Mom",
                relation = Relation.MOTHER,
                sortOrder = 2
            ),
            MemberProfile(
                id = 20L,
                name = "Brother",
                relation = Relation.FAMILY,
                sortOrder = 1
            ),
            MemberProfile(
                id = 10L,
                name = "Sinan mk",
                relation = Relation.SELF,
                isOwner = true,
                sortOrder = 0
            )
        )

        val documents = listOf(
            document(
                id = 100L,
                memberId = 10L,
                holderName = "Sinan mk",
                relation = Relation.SELF
            ),
            document(
                id = 200L,
                memberId = 20L,
                holderName = "Brother",
                relation = Relation.FAMILY
            ),
            document(
                id = 300L,
                memberId = 30L,
                holderName = "Mom",
                relation = Relation.MOTHER
            ),
            document(
                id = 400L,
                memberId = null,
                holderName = "Vehicle",
                relation = Relation.OTHER,
                assignedGroup = AssignedGroup.VEHICLE
            )
        )

        val pages = documents.map { document ->
            DocumentPage(
                documentId = document.id,
                encryptedFileName = document.encryptedFileName,
                mimeType = document.mimeType,
                originalFileName = document.originalFileName,
                fileSize = document.fileSize
            )
        }

        return VaultRestoreData(
            members = members,
            documents = documents,
            pages = pages
        )
    }

    private fun document(
        id: Long,
        memberId: Long?,
        holderName: String,
        relation: Relation,
        assignedGroup: AssignedGroup? = null
    ): VaultDocument = VaultDocument(
        id = id,
        title = "Document $id",
        category = Category.IDENTITY,
        docType = "Aadhaar Card",
        holderName = holderName,
        relation = relation,
        memberId = memberId,
        assignedGroup = assignedGroup,
        encryptedFileName = "page-$id.enc",
        mimeType = "image/jpeg",
        originalFileName = "page-$id.jpg",
        fileSize = 100L + id,
        createdAt = 1L,
        updatedAt = 1L
    )
}
