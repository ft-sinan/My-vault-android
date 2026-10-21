package com.ftsinan.myvault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ftsinan.myvault.data.model.VaultDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY isFavourite DESC, updatedAt DESC")
    fun observeAll(): Flow<List<VaultDocument>>

    @Query("SELECT * FROM documents WHERE id = :id")
    fun observeById(id: Long): Flow<VaultDocument?>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: Long): VaultDocument?

    @Insert
    suspend fun insert(document: VaultDocument): Long

    @Update
    suspend fun update(document: VaultDocument)

    @Delete
    suspend fun delete(document: VaultDocument)

    @Query("UPDATE documents SET isFavourite = :favourite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavourite(id: Long, favourite: Boolean, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM documents")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(fileSize), 0) FROM documents")
    fun observeTotalSize(): Flow<Long>
}
