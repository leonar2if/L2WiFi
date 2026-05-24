package com.l2wifi.di

import android.content.Context
import androidx.room.Room
import com.l2wifi.data.local.dao.AccountDao
import com.l2wifi.data.local.dao.ActiveSessionDao
import com.l2wifi.data.local.dao.HistoryDao
import com.l2wifi.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideActiveSessionDao(db: AppDatabase): ActiveSessionDao = db.activeSessionDao()

    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
}
