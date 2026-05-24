package com.l2wifi.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.l2wifi.data.local.dao.AccountDao
import com.l2wifi.data.local.dao.ActiveSessionDao
import com.l2wifi.data.local.dao.HistoryDao
import com.l2wifi.data.local.entity.AccountEntity
import com.l2wifi.data.local.entity.ActiveSessionEntity
import com.l2wifi.data.local.entity.HistoryEntity

@Database(
    entities = [AccountEntity::class, ActiveSessionEntity::class, HistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun activeSessionDao(): ActiveSessionDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "l2wifi_database"
                )
                    .fallbackToDestructiveMigration() // Manejo simplificado para migración
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
