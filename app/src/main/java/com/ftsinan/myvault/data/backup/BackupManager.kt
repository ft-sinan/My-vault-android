package com.ftsinan.myvault.data.backup

import android.content.Context
import android.net.Uri
import com.ftsinan.myvault.BuildConfig
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.DocumentPage
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.data.repository.VaultRestoreData
import com.ftsinan.myvault.data.storage.BackupStagedFile
import com.ftsinan.myvault.data.storage.VaultFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupSummary(
    val createdAt: Long,
    val protection: BackupProtection,
    val memberCount: Int,
    val documentCount: Int,
    val pageCount: Int,
    val totalBytes: Long
)

/** A validated restore whose files are staged but not yet committed. */
class PreparedBackupRestore internal constructor(
    val summary: BackupSummary,
    internal val restoreData: VaultRestoreData,
    internal val stagedFileNames: List<String>
)

/**
 * Creates and restores portable `.mvbackup` files.
 *
 * Payload layout: My Vault header + ZIP. The ZIP contains opaque file
 * entries first and `manifest.json` last. Restore stages every file under a
 * fresh device-encrypted name and only replaces Room metadata after the
 * complete payload (including the AES-GCM tag for password backups) and all
 * SHA-256 hashes have been verified.
 */
class BackupManager(
    private val context: Context,
    private val repository: DocumentRepository,
    private val fileManager: VaultFileManager
) {

    suspend fun createBackup(
        destination: Uri,
        protection: BackupProtection,
        password: CharArray?
    ): BackupSummary = withContext(Dispatchers.IO) {
        try {
            val snapshot = repository.backupSnapshot()
            val createdAt = System.currentTimeMillis()
            val pagesByDocument = snapshot.pages.groupBy { it.documentId }
            var entryIndex = 0
            var totalBytes = 0L
            var pageCount = 0

            val rawOutput = context.contentResolver.openOutputStream(destination, "wt")
                ?: error("Could not open the backup destination")

            BufferedOutputStream(rawOutput).use { bufferedOutput ->
                val payloadOutput = BackupCrypto.encryptingStream(
                    output = bufferedOutput,
                    protection = protection,
                    password = password?.copyOf()
                )
                ZipOutputStream(BufferedOutputStream(payloadOutput)).use { zip ->
                    fun writeVaultFile(encryptedFileName: String): WrittenBackupFile {
                        val bytes = fileManager.decryptToBytes(encryptedFileName)
                        require(bytes.size.toLong() <= MAX_FILE_BYTES) {
                            "A vault file is too large for this backup prototype"
                        }
                        entryIndex += 1
                        val entryName = "files/%06d.bin".format(entryIndex)
                        zip.putNextEntry(ZipEntry(entryName))
                        zip.write(bytes)
                        zip.closeEntry()
                        totalBytes += bytes.size.toLong()
                        return WrittenBackupFile(
                            entry = entryName,
                            size = bytes.size.toLong(),
                            sha256 = sha256(bytes)
                        )
                    }

                    val memberJson = JSONArray()
                    snapshot.members.forEach { member ->
                        val photo = member.photoEncryptedFileName?.let { writeVaultFile(it) }
                        memberJson.put(
                            JSONObject().apply {
                                put("id", member.id)
                                put("name", member.name)
                                put("relation", member.relation.name)
                                putOrNull("photoEntry", photo?.entry)
                                putOrNull("photoSize", photo?.size)
                                putOrNull("photoSha256", photo?.sha256)
                                putOrNull("photoMimeType", member.photoMimeType)
                                putOrNull("dateOfBirth", member.dateOfBirth)
                                putOrNull("gender", member.gender)
                                putOrNull("bloodGroup", member.bloodGroup)
                                put("isOwner", member.isOwner)
                                put("sortOrder", member.sortOrder)
                                put("createdAt", member.createdAt)
                                put("updatedAt", member.updatedAt)
                            }
                        )
                    }

                    val documentJson = JSONArray()
                    snapshot.documents.forEach { document ->
                        val storedPages = pagesByDocument[document.id]
                            ?.sortedBy { it.sortOrder }
                            .orEmpty()
                        val effectivePages = storedPages.ifEmpty {
                            listOf(
                                DocumentPage(
                                    documentId = document.id,
                                    encryptedFileName = document.encryptedFileName,
                                    mimeType = document.mimeType,
                                    originalFileName = document.originalFileName,
                                    fileSize = document.fileSize,
                                    sortOrder = 0
                                )
                            )
                        }
                        val pageJson = JSONArray()
                        effectivePages.forEach { page ->
                            val written = writeVaultFile(page.encryptedFileName)
                            pageCount += 1
                            pageJson.put(
                                JSONObject().apply {
                                    put("entry", written.entry)
                                    put("mimeType", page.mimeType)
                                    put("originalFileName", page.originalFileName)
                                    put("fileSize", written.size)
                                    put("sortOrder", page.sortOrder)
                                    put("sha256", written.sha256)
                                }
                            )
                        }
                        documentJson.put(
                            JSONObject().apply {
                                put("id", document.id)
                                put("title", document.title)
                                put("category", document.category.name)
                                put("docType", document.docType)
                                put("holderName", document.holderName)
                                put("relation", document.relation.name)
                                putOrNull("memberId", document.memberId)
                                putOrNull("assignedGroup", document.assignedGroup?.name)
                                putOrNull("documentNumber", document.documentNumber)
                                putOrNull("issueDate", document.issueDate)
                                putOrNull("expiryDate", document.expiryDate)
                                putOrNull("notes", document.notes)
                                put("isFavourite", document.isFavourite)
                                put("createdAt", document.createdAt)
                                put("updatedAt", document.updatedAt)
                                put("pages", pageJson)
                            }
                        )
                    }

                    val manifest = JSONObject().apply {
                        put("formatVersion", BackupCrypto.FORMAT_VERSION)
                        put("createdAt", createdAt)
                        put("appVersion", BuildConfig.VERSION_NAME)
                        put("members", memberJson)
                        put("documents", documentJson)
                    }
                    zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
                    zip.write(manifest.toString().toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
            }

            BackupSummary(
                createdAt = createdAt,
                protection = protection,
                memberCount = snapshot.members.size,
                documentCount = snapshot.documents.size,
                pageCount = pageCount,
                totalBytes = totalBytes
            )
        } finally {
            password?.fill('\u0000')
        }
    }

    suspend fun inspectProtection(source: Uri): BackupProtection =
        withContext(Dispatchers.IO) {
            val input = context.contentResolver.openInputStream(source)
                ?: error("Could not open the selected backup")
            BufferedInputStream(input).use { BackupCrypto.readHeader(it).protection }
        }

    suspend fun prepareRestore(
        source: Uri,
        password: CharArray?
    ): PreparedBackupRestore = withContext(Dispatchers.IO) {
        val staged = linkedMapOf<String, BackupStagedFile>()
        try {
            val rawInput = context.contentResolver.openInputStream(source)
                ?: error("Could not open the selected backup")
            var protection = BackupProtection.NONE
            var manifestText: String? = null
            var entryCount = 0
            var totalBytes = 0L

            BufferedInputStream(rawInput).use { bufferedInput ->
                val header = BackupCrypto.readHeader(bufferedInput)
                protection = header.protection
                val payload = BackupCrypto.decryptingStream(
                    input = bufferedInput,
                    header = header,
                    password = password?.copyOf()
                )
                try {
                    ZipInputStream(BufferedInputStream(NonClosingInputStream(payload))).use { zip ->
                        while (true) {
                            val entry = zip.nextEntry ?: break
                            entryCount += 1
                            require(entryCount <= MAX_ENTRY_COUNT) {
                                "Backup contains too many files"
                            }
                            if (entry.isDirectory) {
                                zip.closeEntry()
                                continue
                            }
                            when {
                                entry.name == MANIFEST_ENTRY -> {
                                    require(manifestText == null) { "Duplicate backup manifest" }
                                    manifestText = readEntryBytes(zip, MAX_MANIFEST_BYTES)
                                        .toString(Charsets.UTF_8)
                                }
                                entry.name.matches(FILE_ENTRY_REGEX) -> {
                                    require(!staged.containsKey(entry.name)) {
                                        "Duplicate backup file entry"
                                    }
                                    val bytes = readEntryBytes(zip, MAX_FILE_BYTES)
                                    totalBytes += bytes.size.toLong()
                                    require(totalBytes <= MAX_TOTAL_BYTES) {
                                        "Backup is too large for this prototype"
                                    }
                                    staged[entry.name] = fileManager.importBytesForRestore(bytes)
                                }
                                else -> error("Unexpected entry in backup: ${entry.name}")
                            }
                            zip.closeEntry()
                        }
                    }
                    // ZipInputStream can stop at the ZIP central directory before
                    // the underlying AES-GCM stream reaches its authentication
                    // tag. Drain it explicitly; this throws on a wrong password
                    // or modified ciphertext before anything is committed.
                    drain(payload)
                } finally {
                    runCatching { payload.close() }
                }
            }

            val manifest = JSONObject(manifestText ?: error("Backup manifest is missing"))
            val restoreData = parseAndValidateManifest(manifest, staged)
            val summary = BackupSummary(
                createdAt = manifest.optLong("createdAt", 0L),
                protection = protection,
                memberCount = restoreData.members.size,
                documentCount = restoreData.documents.size,
                pageCount = restoreData.pages.size,
                totalBytes = totalBytes
            )
            PreparedBackupRestore(
                summary = summary,
                restoreData = restoreData,
                stagedFileNames = staged.values.map { it.encryptedFileName }
            )
        } catch (error: Throwable) {
            fileManager.deleteEncryptedFiles(staged.values.map { it.encryptedFileName })
            throw error
        } finally {
            password?.fill('\u0000')
        }
    }

    suspend fun commitRestore(prepared: PreparedBackupRestore) {
        try {
            val oldFileNames = repository.restoreVault(prepared.restoreData)
            withContext(Dispatchers.IO) {
                fileManager.deleteEncryptedFiles(
                    oldFileNames.filterNot { it in prepared.stagedFileNames }
                )
            }
        } catch (error: Throwable) {
            discardRestore(prepared)
            throw error
        }
    }

    suspend fun discardRestore(prepared: PreparedBackupRestore) {
        withContext(Dispatchers.IO) {
            fileManager.deleteEncryptedFiles(prepared.stagedFileNames)
        }
    }

    private fun parseAndValidateManifest(
        manifest: JSONObject,
        staged: Map<String, BackupStagedFile>
    ): VaultRestoreData {
        require(manifest.optInt("formatVersion", -1) == BackupCrypto.FORMAT_VERSION) {
            "Unsupported backup format"
        }
        val memberArray = manifest.optJSONArray("members") ?: JSONArray()
        val documentArray = manifest.optJSONArray("documents") ?: JSONArray()
        require(memberArray.length() <= MAX_MEMBERS) { "Backup has too many members" }
        require(documentArray.length() <= MAX_DOCUMENTS) { "Backup has too many documents" }

        val referencedEntries = mutableSetOf<String>()
        fun requireStaged(entry: String, expectedSize: Long?, expectedSha: String?): BackupStagedFile {
            require(referencedEntries.add(entry)) { "Backup reuses a file entry" }
            val file = staged[entry] ?: error("Backup file is missing: $entry")
            if (expectedSize != null) {
                require(file.fileSize == expectedSize) { "Backup file size does not match" }
            }
            if (!expectedSha.isNullOrBlank()) {
                require(file.sha256.equals(expectedSha, ignoreCase = true)) {
                    "Backup file checksum does not match"
                }
            }
            return file
        }

        val members = mutableListOf<MemberProfile>()
        val memberIds = mutableSetOf<Long>()
        for (index in 0 until memberArray.length()) {
            val item = memberArray.getJSONObject(index)
            val backupId = item.optLong("id", 0L)
            require(backupId > 0 && memberIds.add(backupId)) { "Invalid member in backup" }
            val name = item.optString("name").trim()
            require(name.isNotBlank()) { "Backup member name is blank" }
            val photoEntry = item.optNullableString("photoEntry")
            val photo = photoEntry?.let {
                requireStaged(
                    entry = it,
                    expectedSize = item.optNullableLong("photoSize"),
                    expectedSha = item.optNullableString("photoSha256")
                )
            }
            members += MemberProfile(
                id = backupId,
                name = name,
                relation = enumOrDefault(item.optString("relation"), Relation.OTHER),
                photoEncryptedFileName = photo?.encryptedFileName,
                photoMimeType = item.optNullableString("photoMimeType"),
                dateOfBirth = item.optNullableLong("dateOfBirth"),
                gender = item.optNullableString("gender"),
                bloodGroup = item.optNullableString("bloodGroup"),
                isOwner = item.optBoolean("isOwner", false),
                sortOrder = item.optInt("sortOrder", index),
                createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
            )
        }
        if (members.isNotEmpty()) {
            require(members.count { it.isOwner } == 1) {
                "Backup must contain exactly one owner profile"
            }
        }

        val documents = mutableListOf<VaultDocument>()
        val pages = mutableListOf<DocumentPage>()
        val documentIds = mutableSetOf<Long>()
        for (index in 0 until documentArray.length()) {
            val item = documentArray.getJSONObject(index)
            val backupId = item.optLong("id", 0L)
            require(backupId > 0 && documentIds.add(backupId)) {
                "Invalid document in backup"
            }
            val memberId = item.optNullableLong("memberId")
            val assignedGroup = item.optNullableString("assignedGroup")?.let {
                enumOrNull<AssignedGroup>(it)
            }
            require((memberId != null) xor (assignedGroup != null)) {
                "Backup document has no clear owner"
            }
            if (memberId != null) {
                require(memberId in memberIds) { "Backup document owner is missing" }
            }
            val pageArray = item.optJSONArray("pages") ?: JSONArray()
            require(pageArray.length() in 1..MAX_PAGES_PER_DOCUMENT) {
                "Backup document has no pages"
            }
            val restoredPages = mutableListOf<DocumentPage>()
            for (pageIndex in 0 until pageArray.length()) {
                val pageItem = pageArray.getJSONObject(pageIndex)
                val entry = pageItem.optString("entry")
                require(entry.isNotBlank()) { "Backup page entry is missing" }
                val stagedFile = requireStaged(
                    entry = entry,
                    expectedSize = pageItem.optNullableLong("fileSize"),
                    expectedSha = pageItem.optNullableString("sha256")
                )
                restoredPages += DocumentPage(
                    id = 0,
                    documentId = backupId,
                    encryptedFileName = stagedFile.encryptedFileName,
                    mimeType = pageItem.optString("mimeType", "application/octet-stream"),
                    originalFileName = pageItem.optString("originalFileName", "document"),
                    fileSize = stagedFile.fileSize,
                    sortOrder = pageItem.optInt("sortOrder", pageIndex)
                )
            }
            val sortedPages = restoredPages.sortedBy { it.sortOrder }
            val firstPage = sortedPages.first()
            documents += VaultDocument(
                id = backupId,
                title = item.optString("title", "Document"),
                category = enumOrDefault(item.optString("category"), Category.OTHER),
                docType = item.optString("docType", "Document"),
                holderName = item.optString("holderName"),
                relation = enumOrDefault(item.optString("relation"), Relation.SELF),
                memberId = memberId,
                assignedGroup = assignedGroup,
                documentNumber = item.optNullableString("documentNumber"),
                issueDate = item.optNullableLong("issueDate"),
                expiryDate = item.optNullableLong("expiryDate"),
                notes = item.optNullableString("notes"),
                encryptedFileName = firstPage.encryptedFileName,
                mimeType = firstPage.mimeType,
                originalFileName = firstPage.originalFileName,
                fileSize = firstPage.fileSize,
                isFavourite = item.optBoolean("isFavourite", false),
                createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
            )
            pages += sortedPages
        }

        require(pages.size <= MAX_ENTRY_COUNT) { "Backup contains too many pages" }
        require(referencedEntries == staged.keys) {
            "Backup contains unreferenced files"
        }
        return VaultRestoreData(
            members = members.sortedWith(
                compareByDescending<MemberProfile> { it.isOwner }
                    .thenBy { it.sortOrder }
                    .thenBy { it.id }
            ),
            documents = documents,
            pages = pages
        )
    }

    private data class WrittenBackupFile(
        val entry: String,
        val size: Long,
        val sha256: String
    )

    private class NonClosingInputStream(delegate: InputStream) : FilterInputStream(delegate) {
        override fun close() = Unit
    }

    private fun readEntryBytes(input: InputStream, maxBytes: Long): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count == -1) break
            require(output.size().toLong() + count <= maxBytes) {
                "Backup entry is too large"
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun drain(input: InputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (input.read(buffer) != -1) {
            // Discard any plaintext already buffered beyond the ZIP entries;
            // reaching EOF is what verifies the AES-GCM tag.
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }

    private inline fun <reified T : Enum<T>> enumOrNull(value: String?): T? {
        if (value == null) return null
        return enumValues<T>().firstOrNull { it.name == value }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
        enumOrNull(value) ?: default

    private fun JSONObject.putOrNull(name: String, value: Any?) {
        put(name, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optNullableString(name: String): String? =
        if (!has(name) || isNull(name)) null else optString(name).takeIf { it.isNotBlank() }

    private fun JSONObject.optNullableLong(name: String): Long? =
        if (!has(name) || isNull(name)) null else optLong(name)

    companion object {
        private const val MANIFEST_ENTRY = "manifest.json"
        private const val MAX_MEMBERS = 500
        private const val MAX_DOCUMENTS = 5_000
        private const val MAX_PAGES_PER_DOCUMENT = 200
        private const val MAX_ENTRY_COUNT = 10_000
        private const val MAX_MANIFEST_BYTES = 10L * 1024L * 1024L
        private const val MAX_FILE_BYTES = 256L * 1024L * 1024L
        private const val MAX_TOTAL_BYTES = 1024L * 1024L * 1024L
        private val FILE_ENTRY_REGEX = Regex("files/[A-Za-z0-9._-]+")
    }
}
