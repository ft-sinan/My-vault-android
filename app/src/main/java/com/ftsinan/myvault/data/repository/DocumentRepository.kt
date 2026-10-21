package com.ftsinan.myvault.data.repository

import com.ftsinan.myvault.data.local.DocumentDao
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.storage.VaultFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Single entry point for document data: Room metadata + encrypted files.
 */
class DocumentRepository(
    private val dao: DocumentDao,
    val fileManager: VaultFileManager
) {

    val documents: Flow<List<VaultDocument>> = dao.observeAll()
    val documentCount: Flow<Int> = dao.observeCount()
    val totalSize: Flow<Long> = dao.observeTotalSize()

    fun observeById(id: Long): Flow<VaultDocument?> = dao.observeById(id)

    suspend fun getById(id: Long): VaultDocument? = dao.getById(id)

    suspend fun add(document: VaultDocument): Long = dao.insert(document)

    suspend fun update(document: VaultDocument) =
        dao.update(document.copy(updatedAt = System.currentTimeMillis()))

    /** Deletes the metadata row and the encrypted file itself. */
    suspend fun delete(document: VaultDocument) {
        dao.delete(document)
        withContext(Dispatchers.IO) {
            fileManager.deleteEncrypted(document.encryptedFileName)
        }
    }

    suspend fun setFavourite(id: Long, favourite: Boolean) =
        dao.setFavourite(id, favourite, System.currentTimeMillis())
}
