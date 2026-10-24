package com.ftsinan.myvault.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.ftsinan.myvault.data.local.AppDatabase
import com.ftsinan.myvault.data.local.DocumentDao
import com.ftsinan.myvault.data.local.DocumentPageDao
import com.ftsinan.myvault.data.local.MemberDao
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.DocumentPage
import com.ftsinan.myvault.data.model.DocumentSummary
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.storage.ImportedFile
import com.ftsinan.myvault.data.storage.VaultFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.security.MessageDigest

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
    val pages: List<DocumentPage>,
    /** Plaintext SHA-256 for every staged backup file, keyed by encrypted name. */
    val fileSha256ByEncryptedName: Map<String, String> = emptyMap()
)

/** Result of a non-destructive backup merge. */
data class VaultMergeResult(
    val usedStagedFileNames: Set<String>,
    val addedMemberCount: Int,
    val matchedMemberCount: Int,
    val addedDocumentCount: Int,
    val addedPageCount: Int,
    val duplicateDocumentCount: Int,
    val updatedDocumentCount: Int
)

internal data class MergeDocumentKey(
    val memberId: Long?,
    val assignedGroup: AssignedGroup?,
    val pageSha256: List<String>
)

internal fun normalizedProfileName(name: String): String =
    name.trim().replace(Regex("\\s+"), " ").lowercase()

/** Backup profiles are merged only on the conservative name + DOB match. */
internal fun MemberProfile.matchesForBackupMerge(other: MemberProfile): Boolean {
    val dob = dateOfBirth ?: return false
    return dob == other.dateOfBirth &&
        normalizedProfileName(name) == normalizedProfileName(other.name)
}

internal fun mergeDocumentKey(
    memberId: Long?,
    assignedGroup: AssignedGroup?,
    pageSha256: List<String?>
): MergeDocumentKey? {
    if (pageSha256.isEmpty()) return null
    val hashes = mutableListOf<String>()
    for (hash in pageSha256) {
        if (hash.isNullOrBlank()) return null
        hashes += hash.lowercase()
    }
    return MergeDocumentKey(
        memberId = memberId,
        assignedGroup = assignedGroup,
        pageSha256 = hashes
    )
}

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
        // The transaction has committed; cache cleanup must not turn a
        // successful restore into a failure that could discard live files.
        withContext(NonCancellable + Dispatchers.IO) {
            runCatching { fileManager.clearDecryptedCache() }
        }
        return oldFileNames
    }

    /**
     * Adds a validated backup into the current vault without deleting it.
     * Profiles merge only on exact normalized name + DOB. A document is a
     * duplicate only when its mapped assignee and every ordered page hash
     * match; newer duplicate metadata updates the existing document while
     * its existing encrypted pages are kept.
     */
    suspend fun mergeVault(data: VaultRestoreData): VaultMergeResult {
        val before = backupSnapshot()
        val existingFileHashes = withContext(Dispatchers.IO) {
            buildMap {
                before.pages.forEach { page ->
                    if (!containsKey(page.encryptedFileName)) {
                        put(
                            page.encryptedFileName,
                            sha256(fileManager.decryptToBytes(page.encryptedFileName))
                        )
                    }
                }
                before.documents.forEach { document ->
                    if (!containsKey(document.encryptedFileName)) {
                        put(
                            document.encryptedFileName,
                            sha256(fileManager.decryptToBytes(document.encryptedFileName))
                        )
                    }
                }
            }
        }

        val result = database.withTransaction {
            val existingMembers = memberDao.getAllSnapshot()
            val existingDocuments = documentDao.getAllSnapshot()
            val existingPages = pageDao.getAllSnapshot()
            val pagesByExistingDocument = existingPages.groupBy { it.documentId }
            val pagesByBackupDocument = data.pages.groupBy { it.documentId }

            val availableMembers = existingMembers.toMutableList()
            val hadOwner = existingMembers.any { it.isOwner }
            var ownerAssigned = hadOwner
            var nextSortOrder = (existingMembers.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val memberIds = mutableMapOf<Long, Long>()
            val usedStagedFileNames = linkedSetOf<String>()
            var addedMemberCount = 0
            var matchedMemberCount = 0
            var addedDocumentCount = 0
            var addedPageCount = 0
            var duplicateDocumentCount = 0
            var updatedDocumentCount = 0

            fun replaceAvailableMember(updated: MemberProfile) {
                val index = availableMembers.indexOfFirst { it.id == updated.id }
                if (index >= 0) {
                    availableMembers[index] = updated
                } else {
                    availableMembers += updated
                }
            }

            data.members.forEach { backupMember ->
                val match = availableMembers.firstOrNull { existing ->
                    backupMember.matchesForBackupMerge(existing)
                }
                if (match != null) {
                    memberIds[backupMember.id] = match.id
                    matchedMemberCount += 1

                    var updated = match
                    if (updated.gender.isNullOrBlank() &&
                        !backupMember.gender.isNullOrBlank()
                    ) {
                        updated = updated.copy(gender = backupMember.gender)
                    }
                    if (updated.bloodGroup.isNullOrBlank() &&
                        !backupMember.bloodGroup.isNullOrBlank()
                    ) {
                        updated = updated.copy(bloodGroup = backupMember.bloodGroup)
                    }
                    if (updated.photoEncryptedFileName == null &&
                        backupMember.photoEncryptedFileName != null
                    ) {
                        updated = updated.copy(
                            photoEncryptedFileName = backupMember.photoEncryptedFileName,
                            photoMimeType = backupMember.photoMimeType
                        )
                        usedStagedFileNames += backupMember.photoEncryptedFileName
                    }
                    if (!ownerAssigned && backupMember.isOwner) {
                        updated = updated.copy(
                            isOwner = true,
                            relation = Relation.SELF
                        )
                        ownerAssigned = true
                    }
                    if (updated != match) {
                        updated = updated.copy(
                            updatedAt = maxOf(updated.updatedAt, backupMember.updatedAt)
                        )
                        memberDao.update(updated)
                        replaceAvailableMember(updated)
                    }
                } else {
                    val becomesOwner = !ownerAssigned && backupMember.isOwner
                    val toInsert = backupMember.copy(
                        id = 0,
                        isOwner = becomesOwner,
                        relation = when {
                            becomesOwner -> Relation.SELF
                            backupMember.isOwner -> Relation.FAMILY
                            else -> backupMember.relation
                        },
                        sortOrder = nextSortOrder++
                    )
                    val newId = memberDao.insert(toInsert)
                    memberIds[backupMember.id] = newId
                    availableMembers += toInsert.copy(id = newId)
                    backupMember.photoEncryptedFileName?.let { photo ->
                        usedStagedFileNames += photo
                    }
                    if (becomesOwner) ownerAssigned = true
                    addedMemberCount += 1
                }
            }

            fun existingPageHashes(document: VaultDocument): List<String?> {
                val storedPages = pagesByExistingDocument[document.id].orEmpty()
                    .sortedWith(compareBy<DocumentPage> { it.sortOrder }.thenBy { it.id })
                val fileNames = if (storedPages.isNotEmpty()) {
                    storedPages.map { it.encryptedFileName }
                } else {
                    listOf(document.encryptedFileName)
                }
                return fileNames.map { fileName ->
                    existingFileHashes[fileName] ?: runCatching {
                        sha256(fileManager.decryptToBytes(fileName))
                    }.getOrNull()
                }
            }

            val documentsByMergeKey = mutableMapOf<MergeDocumentKey, VaultDocument>()
            existingDocuments.forEach { document ->
                val key = mergeDocumentKey(
                    memberId = document.memberId,
                    assignedGroup = document.assignedGroup,
                    pageSha256 = existingPageHashes(document)
                )
                if (key != null) {
                    val current = documentsByMergeKey[key]
                    if (current == null || document.updatedAt > current.updatedAt) {
                        documentsByMergeKey[key] = document
                    }
                }
            }

            for (backupDocument in data.documents) {
                val backupPages = pagesByBackupDocument[backupDocument.id].orEmpty()
                    .sortedBy { it.sortOrder }
                require(backupPages.isNotEmpty()) { "Backup document has no pages" }

                val mappedMemberId = backupDocument.memberId?.let { backupMemberId ->
                    memberIds[backupMemberId]
                        ?: error("Backup document references a missing member")
                }
                val mappedMember = mappedMemberId?.let { id ->
                    availableMembers.firstOrNull { it.id == id }
                }
                val holderName = mappedMember?.name ?: backupDocument.holderName
                val relation = mappedMember?.relation ?: backupDocument.relation
                val key = mergeDocumentKey(
                    memberId = mappedMemberId,
                    assignedGroup = backupDocument.assignedGroup,
                    pageSha256 = backupPages.map { page ->
                        data.fileSha256ByEncryptedName[page.encryptedFileName]
                            ?: runCatching {
                                sha256(fileManager.decryptToBytes(page.encryptedFileName))
                            }.getOrNull()
                    }
                )
                val duplicate = key?.let { documentsByMergeKey[it] }
                if (duplicate != null) {
                    duplicateDocumentCount += 1
                    if (backupDocument.updatedAt > duplicate.updatedAt) {
                        val updated = duplicate.copy(
                            title = backupDocument.title,
                            category = backupDocument.category,
                            docType = backupDocument.docType,
                            holderName = holderName,
                            relation = relation,
                            memberId = mappedMemberId,
                            assignedGroup = backupDocument.assignedGroup,
                            documentNumber = backupDocument.documentNumber,
                            issueDate = backupDocument.issueDate,
                            expiryDate = backupDocument.expiryDate,
                            notes = backupDocument.notes,
                            isFavourite = backupDocument.isFavourite,
                            updatedAt = backupDocument.updatedAt
                        )
                        documentDao.update(updated)
                        if (key != null) documentsByMergeKey[key] = updated
                        updatedDocumentCount += 1
                    }
                    continue
                }

                val toInsert = backupDocument.copy(
                    id = 0,
                    holderName = holderName,
                    relation = relation,
                    memberId = mappedMemberId
                )
                val newDocumentId = documentDao.insert(toInsert)
                val insertedPages = backupPages.map { page ->
                    page.copy(id = 0, documentId = newDocumentId)
                }
                pageDao.insertAll(insertedPages)
                usedStagedFileNames += backupPages.map { it.encryptedFileName }
                usedStagedFileNames += toInsert.encryptedFileName
                if (key != null) {
                    documentsByMergeKey[key] = toInsert.copy(id = newDocumentId)
                }
                addedDocumentCount += 1
                addedPageCount += insertedPages.size
            }

            VaultMergeResult(
                usedStagedFileNames = usedStagedFileNames,
                addedMemberCount = addedMemberCount,
                matchedMemberCount = matchedMemberCount,
                addedDocumentCount = addedDocumentCount,
                addedPageCount = addedPageCount,
                duplicateDocumentCount = duplicateDocumentCount,
                updatedDocumentCount = updatedDocumentCount
            )
        }

        // The transaction has committed; cache cleanup must not turn a
        // successful merge into a failure that could discard live files.
        withContext(NonCancellable + Dispatchers.IO) {
            runCatching { fileManager.clearDecryptedCache() }
        }
        return result
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

    /**
     * Creates the recipient's own profile after a restore and makes it the
     * phone owner atomically. The restored previous owner stays in the vault
     * as a normal family profile.
     */
    suspend fun createMemberAndMakeOwner(
        member: MemberProfile,
        newPhotoUri: Uri?
    ): Long {
        val now = System.currentTimeMillis()
        val imported = newPhotoUri?.let { uri ->
            withContext(Dispatchers.IO) { fileManager.importFile(uri) }
        }
        var ownerInsertAttempted = false
        return try {
            database.withTransaction {
                memberDao.getAllSnapshot()
                    .filter { it.isOwner }
                    .forEach { owner ->
                        memberDao.update(
                            owner.copy(
                                isOwner = false,
                                relation = Relation.FAMILY,
                                updatedAt = now
                            )
                        )
                        documentDao.syncMemberDisplay(
                            memberId = owner.id,
                            name = owner.name,
                            relation = Relation.FAMILY.name,
                            updatedAt = now
                        )
                    }

                val toInsert = member.copy(
                    id = 0,
                    relation = Relation.SELF,
                    photoEncryptedFileName = imported?.encryptedFileName
                        ?: member.photoEncryptedFileName,
                    photoMimeType = imported?.mimeType ?: member.photoMimeType,
                    isOwner = true,
                    sortOrder = memberDao.maxSortOrder() + 1,
                    updatedAt = now
                )
                memberDao.insert(toInsert).also {
                    // From here the imported photo may be referenced by the
                    // committed owner row; on any later failure, leaving an
                    // orphan is safer than deleting a live profile photo.
                    ownerInsertAttempted = true
                }
            }
        } catch (error: Throwable) {
            if (!ownerInsertAttempted) {
                imported?.let { file ->
                    withContext(NonCancellable + Dispatchers.IO) {
                        runCatching {
                            fileManager.deleteEncrypted(file.encryptedFileName)
                        }
                    }
                }
            }
            throw error
        }
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

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        bytes.fill(0)
        return digest.joinToString("") { "%02x".format(it) }
    }

    suspend fun memberPhotoBytes(member: MemberProfile): ByteArray? =
        member.photoEncryptedFileName?.let { fileName ->
            withContext(Dispatchers.IO) {
                runCatching { fileManager.decryptToBytes(fileName) }.getOrNull()
            }
        }
}
