package com.ftsinan.myvault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ftsinan.myvault.data.model.MemberProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM member_profiles ORDER BY isOwner DESC, sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<MemberProfile>>

    @Query("SELECT * FROM member_profiles WHERE isOwner = 1 LIMIT 1")
    fun observeOwner(): Flow<MemberProfile?>

    @Query("SELECT * FROM member_profiles WHERE id = :id")
    fun observeById(id: Long): Flow<MemberProfile?>

    @Query("SELECT * FROM member_profiles WHERE id = :id")
    suspend fun getById(id: Long): MemberProfile?

    @Insert
    suspend fun insert(member: MemberProfile): Long

    @Update
    suspend fun update(member: MemberProfile)

    @Delete
    suspend fun delete(member: MemberProfile)

    @Query("SELECT * FROM member_profiles ORDER BY isOwner DESC, sortOrder ASC, id ASC")
    suspend fun getAllSnapshot(): List<MemberProfile>

    @Query("DELETE FROM member_profiles")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM member_profiles")
    suspend fun maxSortOrder(): Int
}
