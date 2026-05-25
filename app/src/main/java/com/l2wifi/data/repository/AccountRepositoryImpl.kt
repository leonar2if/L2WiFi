package com.l2wifi.data.repository

import android.content.Context
import com.l2wifi.data.local.dao.AccountDao
import com.l2wifi.data.local.entity.AccountEntity
import com.l2wifi.domain.model.Account
import com.l2wifi.domain.model.ConnectionState
import com.l2wifi.domain.repository.AccountRepository
import com.l2wifi.widget.WidgetSync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    @ApplicationContext private val context: Context
) : AccountRepository {
    override fun getAllAccounts(): Flow<List<Account>> = accountDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getAccountById(id: Long): Account? = accountDao.getById(id)?.toDomain()

    override suspend fun insertAccount(account: Account) {
        val currentAccounts = accountDao.getAll().firstOrNull() ?: emptyList()
        val maxOrder = currentAccounts.maxOfOrNull { it.orderIndex } ?: -1
        accountDao.insert(account.toEntity(orderIndex = maxOrder + 1))
        WidgetSync.requestUpdate(context)
    }

    override suspend fun updateAccount(account: Account) {
        val existing = accountDao.getById(account.id)
        accountDao.update(account.toEntity(orderIndex = existing?.orderIndex ?: 0))
        WidgetSync.requestUpdate(context)
    }

    override suspend fun deleteAccount(account: Account) {
        accountDao.delete(account.toEntity(orderIndex = 0))
        val remaining = accountDao.getAll().firstOrNull() ?: emptyList()
        remaining.forEachIndexed { index, entity ->
            accountDao.updateOrderIndex(entity.id, index)
        }
        WidgetSync.requestUpdate(context)
    }
    
    override suspend fun updateAccountsOrder(accounts: List<Account>) {
        accounts.forEachIndexed { index, account ->
            accountDao.updateOrderIndex(account.id, index)
        }
        WidgetSync.requestUpdate(context)
    }
}

fun AccountEntity.toDomain(): Account = Account(id, name, username, password, ConnectionState.INACTIVE)
fun Account.toEntity(orderIndex: Int): AccountEntity = AccountEntity(id, name, username, password, orderIndex)
