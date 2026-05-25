package com.l2wifi.widget

import android.content.Context
import com.l2wifi.data.local.database.AppDatabase
import com.l2wifi.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

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

    fun getDisplayAccount(context: Context): AccountEntity? {
        val activeId = getActiveAccountId(context)
        if (activeId != null) {
            val activeAccount = getAccountById(context, activeId)
            if (activeAccount != null) return activeAccount
        }
        return getAccounts(context).firstOrNull()
    }
}
