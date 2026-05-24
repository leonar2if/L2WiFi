package com.l2wifi.widget

import android.content.Context
import com.l2wifi.data.local.database.AppDatabase
import com.l2wifi.data.local.entity.AccountEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull

object WidgetDatabaseHelper {
    fun getAccounts(context: Context): List<AccountEntity> {
        val db = AppDatabase.getInstance(context.applicationContext)
        return runBlocking {
            db.accountDao().getAll().firstOrNull() ?: emptyList()
        }
    }

    fun getActiveAccountId(context: Context): Long? {
        val db = AppDatabase.getInstance(context.applicationContext)
        return runBlocking {
            db.activeSessionDao().getActiveFlow().firstOrNull()?.accountId
        }
    }

    fun getAccountById(context: Context, id: Long): AccountEntity? {
        val db = AppDatabase.getInstance(context.applicationContext)
        return runBlocking {
            db.accountDao().getById(id)
        }
    }
}