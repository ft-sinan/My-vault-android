package com.ftsinan.myvault.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.ftsinan.myvault.data.local.AppDatabase
import com.ftsinan.myvault.data.local.DocumentDao
import com.ftsinan.myvault.data.local.DocumentPageDao
import com.ftsinan.myvault.data.local.MemberDao
import com.ftsinan.myvault.data.model.DocumentPage
import com.ftsinan.myvault.data.model.DocumentSummary
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.storage.ImportedFile
import com.ftsinan.myvault.data.storage.VaultFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Single entry point for vault data: documents, their encrypted pages
 * and family member profiles.
 */
data class VaultBackupSnapshot(
    val members: List<MemberProfile>,
    val documents: List<VaultDocument>,
    val pages: List<DocumentPage>
)

/**
 * Restore input whose `id`, `memberId` and `documentId` fields still contain
 * backup-local IDs. [DocumentRepository.restoreVault] remaps them atomically.
 */
data class VaultRestoreData(
    val members: List<MemberProfile>,
    val documents: List<VaultDocument>,
    val pages: List<DocumentPage>
)

class DocumentRepository(
    private val database: AppDatabase,
    private val documentDao: DocumentDao,
    private val memberDao: MemberDao,
    private val pageDao: DocumentPageDao,
    val fileManager: VaultFileManager
) {

    val documents: Flow<List<VaultDocument>> = documentDao.observeAll()
    val documentCount: Flow<Int> = documentDao.observeCount()
    val totalSize: Flow<Long> = documentDao.observeTotalSize()
    val members: Flow<List<MemberProfile>> = memberDao.observeAll()
    val owner: Flow<MemberProfile?> = memberDao.observeOwner()

    /** Documents enriched with page counts, sizes and assignee names. */
    val documentSummaries: Flow<List<DocumentSummary>> = combine(
        documentDao.observeAll(),
        pageDao.observeAll(),
        memberDao.observeAll()
    ) { docs, pages, members ->
        val pagesByDocument = pages.groupBy { it.documentId }
        val membersById = members.associateBy { it.id }
        docs.map { document ->
            val documentPages = pagesByDocument[document.id].orEmpty()
            val member = document.memberId?.let { membersById[it] }
            DocumentSummary(
                document = document,
                pageCount = documentPages.size.coerceAtLeast(1),
                totalSize = if (documentPages.isNotEmpty()) {
                    documentPages.sumOf { it.fileSize }
                } else {
                    document.fileSize
                },
                assignedName = member?.name
                    ?: document.assignedGroup?.label
                    ?: document.holderName,
                member = member,
                isPdf = documentPages.any { it.isPdf } || document.isPdf
            )
        }
    }

    /** Members enriched with decrypted avatar bytes + document counts. */
    val memberListItems: Flow<List<MemberListItem>> = combine(
        memberDao.observeAll(),
        documentDao.observeAll()
    ) { members, docs ->
        val counts = docs.groupingBy { it.memberId }.eachCount()
        members.map { member ->
            MemberListItem(
                member = member,
                photoBytes = member.photoEncryptedFileName?.let { fileName ->
                    runCatching { fileManager.decryptToBytes(fileName) }.getOrNull()
                },
                documentCount = counts[member.id] ?: 0
            )
        }
    }.flowOn(Dispatchers.IO)

    fun observeById(id: Long): Flow<VaultDocument?> = documentDao.observeById(id)

    fun observeMember(id: Long): Flow<MemberProfile?> = memberDao.observeById(id)

    fun observePages(documentId: Long): Flow<List<DocumentPage>> =
        pageDao.observeForDocument(documentId)

    /** Consistent database view used as the source of an encrypted backup. */
    suspend fun backupSnapshot(): VaultBackupSnapshot = database.withTransaction {
        VaultBackupSnapshot(
            members = memberDao.getAllSnapshot(),
            documents = documentDao.getAllSnapshot(),
            pages = pageDao.getAllSnapshot()
        )
    }

    /**
     * Replaces the whole metadata vault in one Room transaction. Restored
     * encrypted files must already be staged; the returned old file names can
     * be deleted only after this function succeeds.
     */
    suspend fun restoreVault(data: VaultRestoreData): List<String> {
        val oldFileNames = database.withTransaction {
            val oldMembers = memberDao.getAllSnapshot()
            val oldDocuments = documentDao.getAllSnapshot()
            val oldPages = pageDao.getAllSnapshot()
            val oldNames = buildSet {
                oldMembers.mapNotNull { it.photoEncryptedFileName }.forEach(::add)
                oldDocuments.forEach { add(it.encryptedFileName) }
                oldPages.forEach { add(it.encryptedFileName) }
            }

            pageDao.deleteAll()
            documentDao.deleteAll()
            memberDao.deleteAll()

            val memberIds = mutableMapOf<Long, Long>()
            data.members.forEach { member ->
                val backupId = member.id
                val newId = memberDao.insert(member.copy(id = 0))
                if (backupId != 0L) memberIds[backupId] = newId
            }

            val documentIds = mutableMapOf<Long, Long>()
            data.documents.forEach { document ->
                val backupId = document.id
                val mappedMemberId = document.memberId?.let { oldMemberId ->
                    memberIds[oldMemberId]
                        ?: error("Backup document references a missing member")
                }
                val newId = documentDao.insert(
                    document.copy(id = 0, memberId = mappedMemberId)
                )
                if (backupId != 0L) documentIds[backupId] = newId
            }

            val restoredPages = data.pages.map { page ->
                page.copy(
                    id = 0,
                    documentId = documentIds[page.documentId]
                        ?: error("Backup page references a missing document")
                )
            }
            if (restoredPages.isNotEmpty()) pageDao.insertAll(restoredPages)
            oldNames.toList()
        }
        withContext(Dispatchers.IO) { fileManager.clearDecryptedCache() }
        return oldFileNames
    }

    suspend fun getById(id: Long): VaultDocument? = documentDao.getById(id)

    suspend fun getMember(id: Long): MemberProfile? = memberDao.getById(id)

    suspend fun getPages(documentId: Long): List<DocumentPage> =
        pageDao.getForDocument(documentId)

    suspend fun add(document: VaultDocument): Long = documentDao.insert(document)

    /**
     * Inserts a document together with its already-imported encrypted
     * pages. The document's legacy single-file fields must already
     * mirror the first imported page.
     */
    suspend fun addDocumentWithPages(
        document: VaultDocument,
        importedPages: List<ImportedFile>
    ): Long {
        val documentId = documentDao.insert(document)
        if (importedPages.isNotEmpty()) {
            pageDao.insertAll(
                importedPages.mapIndexed { index, imported ->
                    DocumentPage(
                        documentId = documentId,
                        encryptedFileName = imported.encryptedFileName,
                        mimeType = imported.mimeType,
                        originalFileName = imported.originalFileName,
                        fileSize = imported.fileSize,
                        sortOrder = index
                    )
                }
            )
        }
        return documentId
    }

    /**
     * Repairs a page stored with an unknown MIME type (common for ML Kit
     * scanner URIs) after its decrypted header has been identified.
     */
    suspend fun repairPageMimeType(
        document: VaultDocument,
        page: DocumentPage,
        detectedMimeType: String
    ) {
        if (page.id != 0L) {
            pageDao.updateMimeType(page.id, detectedMimeType)
        }
        if (page.encryptedFileName == document.encryptedFileName) {
            documentDao.updateMimeType(
                id = document.id,
                mimeType = detectedMimeType,
                encryptedFileName = document.encryptedFileName
            )
        }
    }

    suspend fun update(document: VaultDocument) =
        documentDao.update(document.copy(updatedAt = System.currentTimeMillis()))

    /** Deletes the metadata row, its page rows (cascade) and every encrypted file. */
    suspend fun delete(document: VaultDocument) {
        val pages = runCatching { pageDao.getForDocument(document.id) }
            .getOrDefault(emptyList())
        documentDao.delete(document)
        withContext(Dispatchers.IO) {
            val names = (pages.map { it.encryptedFileName } + document.encryptedFileName)
                .distinct()
            names.forEach { fileManager.deleteEncrypted(it) }
        }
    }

    suspend fun setFavourite(id: Long, favourite: Boolean) =
        documentDao.setFavourite(id, favourite, System.currentTimeMillis())

    // ---------------- Members ----------------

    /**
     * Creates or updates a member, importing a newly picked profile
     * photo (encrypted) when [newPhotoUri] is supplied.
     */
    suspend fun saveMember(member: MemberProfile, newPhotoUri: Uri?): Long {
        val now = System.currentTimeMillis()
        var toSave = member.copy(updatedAt = now)

        if (newPhotoUri != null) {
            val imported = withContext(Dispatchers.IO) { fileManager.importFile(newPhotoUri) }
            toSave.photoEncryptedFileName?.let { old ->
                withContext(Dispatchers.IO) { fileManager.deleteEncrypted(old) }
            }
            toSave = toSave.copy(
                photoEncryptedFileName = imported.encryptedFileName,
                photoMimeType = imported.mimeType
            )
        }

        val id = if (toSave.id == 0L) {
            val withOrder = toSave.copy(sortOrder = memberDao.maxSortOrder() + 1)
            memberDao.insert(withOrder)
        } else {
            memberDao.update(toSave)
            toSave.id
        }

        // Keep document display names in sync after a rename.
        documentDao.syncMemberDisplay(id, toSave.name, toSave.relation.name, now)
        return id
    }

    /** Deletes a member; their documents move to the Family group. */
    suspend fun deleteMember(member: MemberProfile) {
        if (member.isOwner) return
        documentDao.reassignMemberDocumentsToFamily(member.id, System.currentTimeMillis())
        memberDao.delete(member)
        member.photoEncryptedFileName?.let { photo ->
            withContext(Dispatchers.IO) { fileManager.deleteEncrypted(photo) }
        }
    }

    /** Persists the visual drag order chosen in Settings. */
    suspend fun updateMemberOrder(orderedMembers: List<MemberProfile>) {
        val now = System.currentTimeMillis()
        orderedMembers.forEachIndexed { index, member ->
            if (member.sortOrder != index) {
                memberDao.update(member.copy(sortOrder = index, updatedAt = now))
            }
        }
    }

    suspend fun memberPhotoBytes(member: MemberProfile): ByteArray? =
        member.photoEncryptedFileName?.let { fileName ->
            withContext(Dispatchers.IO) {
                runCatching { fileManager.decryptToBytes(fileName) }.getOrNull()
            }
        }
}
