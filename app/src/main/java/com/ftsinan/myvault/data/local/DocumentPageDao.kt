package com.ftsinan.myvault.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ftsinan.myvault.data.model.DocumentPage
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentPageDao {

    @Query("SELECT * FROM document_pages ORDER BY documentId ASC, sortOrder ASC")
    fun observeAll(): Flow<List<DocumentPage>>

    @Query("SELECT * FROM document_pages WHERE documentId = :documentId ORDER BY sortOrder ASC")
    fun observeForDocument(documentId: Long): Flow<List<DocumentPage>>

    @Query("SELECT * FROM document_pages WHERE documentId = :documentId ORDER BY sortOrder ASC")
    suspend fun getForDocument(documentId: Long): List<DocumentPage>

    @Insert
    suspend fun insertAll(pages: List<DocumentPage>)

    @Query("SELECT * FROM document_pages ORDER BY documentId ASC, sortOrder ASC, id ASC")
    suspend fun getAllSnapshot(): List<DocumentPage>

    @Query("DELETE FROM document_pages")
    suspend fun deleteAll()

    @Query("UPDATE document_pages SET mimeType = :mimeType WHERE id = :pageId")
    suspend fun updateMimeType(pageId: Long, mimeType: String)

    @Query("DELETE FROM document_pages WHERE documentId = :documentId")
    suspend fun deleteForDocument(documentId: Long)
}
