package com.example.gun_app_kotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LinenItem::class, BatchIn::class, BatchInDetail::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun linenDao(): LinenDao
    abstract fun batchInDao(): BatchInDao
    abstract fun batchInDetailDao(): BatchInDetailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gun_app_database"
                )
                    // Since we changed the database schema (added tables), we need to handle migration.
                    // For development, the simplest way is to destroy and rebuild the database.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
