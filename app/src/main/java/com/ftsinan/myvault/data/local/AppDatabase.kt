package com.ftsinan.myvault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.DocumentPage
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument

class Converters {
    @TypeConverter
    fun categoryToString(category: Category): String = category.name

    @TypeConverter
    fun stringToCategory(value: String): Category =
        runCatching { Category.valueOf(value) }.getOrDefault(Category.OTHER)

    @TypeConverter
    fun relationToString(relation: Relation): String = relation.name

    @TypeConverter
    fun stringToRelation(value: String): Relation =
        runCatching { Relation.valueOf(value) }.getOrDefault(Relation.SELF)

    @TypeConverter
    fun assignedGroupToString(group: AssignedGroup?): String? = group?.name

    @TypeConverter
    fun stringToAssignedGroup(value: String?): AssignedGroup? =
        value?.let { runCatching { AssignedGroup.valueOf(it) }.getOrNull() }
}

@Database(
    entities = [VaultDocument::class, MemberProfile::class, DocumentPage::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun memberDao(): MemberDao
    abstract fun documentPageDao(): DocumentPageDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * v1 -> v2: family profiles, document pages and member/group
         * assignment. Existing documents keep their encrypted files:
         * each old single file becomes page 1, and each distinct holder
         * becomes a member profile the user can complete later.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `member_profiles` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`relation` TEXT NOT NULL, " +
                        "`photoEncryptedFileName` TEXT, " +
                        "`photoMimeType` TEXT, " +
                        "`dateOfBirth` INTEGER, " +
                        "`gender` TEXT, " +
                        "`bloodGroup` TEXT, " +
                        "`isOwner` INTEGER NOT NULL, " +
                        "`sortOrder` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `document_pages` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`documentId` INTEGER NOT NULL, " +
                        "`encryptedFileName` TEXT NOT NULL, " +
                        "`mimeType` TEXT NOT NULL, " +
                        "`originalFileName` TEXT NOT NULL, " +
                        "`fileSize` INTEGER NOT NULL, " +
                        "`sortOrder` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`documentId`) REFERENCES `documents`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_document_pages_documentId` " +
                        "ON `document_pages` (`documentId`)"
                )
                db.execSQL("ALTER TABLE `documents` ADD COLUMN `memberId` INTEGER")
                db.execSQL("ALTER TABLE `documents` ADD COLUMN `assignedGroup` TEXT")

                // One member per distinct (holderName, relation) already in use.
                db.execSQL(
                    "INSERT INTO `member_profiles` " +
                        "(`name`, `relation`, `photoEncryptedFileName`, `photoMimeType`, " +
                        "`dateOfBirth`, `gender`, `bloodGroup`, `isOwner`, `sortOrder`, " +
                        "`createdAt`, `updatedAt`) " +
                        "SELECT `holderName`, `relation`, NULL, NULL, NULL, NULL, NULL, 0, 0, " +
                        "CAST(strftime('%s','now') AS INTEGER) * 1000, " +
                        "CAST(strftime('%s','now') AS INTEGER) * 1000 " +
                        "FROM (SELECT DISTINCT `holderName`, `relation` FROM `documents`)"
                )
                db.execSQL(
                    "UPDATE `documents` SET `memberId` = (" +
                        "SELECT `id` FROM `member_profiles` " +
                        "WHERE `member_profiles`.`name` = `documents`.`holderName` " +
                        "AND `member_profiles`.`relation` = `documents`.`relation` LIMIT 1)"
                )
                // Every existing single encrypted file becomes page 1.
                db.execSQL(
                    "INSERT INTO `document_pages` " +
                        "(`documentId`, `encryptedFileName`, `mimeType`, " +
                        "`originalFileName`, `fileSize`, `sortOrder`) " +
                        "SELECT `id`, `encryptedFileName`, `mimeType`, " +
                        "`originalFileName`, `fileSize`, 0 FROM `documents`"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myvault.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
