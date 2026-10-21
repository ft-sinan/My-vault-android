package com.ftsinan.myvault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.ftsinan.myvault.data.model.Category
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
}

@Database(
    entities = [VaultDocument::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myvault.db"
                ).build().also { instance = it }
            }
    }
}
