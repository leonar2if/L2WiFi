package com.l2wifi.domain.repository

import com.l2wifi.domain.model.Account
import com.l2wifi.domain.model.Balance
import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    fun getActiveAccount(): Flow<Account?>
    suspend fun connect(account: Account): Flow<Result<Unit>>
    suspend fun disconnect()
    suspend fun getBalance(account: Account): Balance?
}
