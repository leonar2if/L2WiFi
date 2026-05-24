package com.l2wifi.data.repository

import com.l2wifi.data.local.dao.ActiveSessionDao
import com.l2wifi.data.local.entity.ActiveSessionEntity
import com.l2wifi.data.remote.api.NautaApiService
import com.l2wifi.domain.model.Account
import com.l2wifi.domain.model.Balance
import com.l2wifi.domain.model.ConnectionState
import com.l2wifi.domain.repository.AccountRepository
import com.l2wifi.domain.repository.ConnectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import java.io.IOException
import javax.inject.Inject

class ConnectionRepositoryImpl @Inject constructor(
    private val nautaApi: NautaApiService,
    private val activeSessionDao: ActiveSessionDao,
    private val accountRepository: AccountRepository
) : ConnectionRepository {

    override fun getActiveAccount(): Flow<Account?> =
        activeSessionDao.getActiveFlow().mapLatest { session ->
            session?.accountId?.let { accountRepository.getAccountById(it) }
        }.flowOn(Dispatchers.IO)

    override suspend fun connect(account: Account): Flow<Result<Unit>> = flow {
        try {
            // Obtener la IP del usuario (para Nauta a veces es necesaria)
            val userIp = getLocalIpAddress()
            val response = nautaApi.login(account.username, account.password, userIp)
            if (response.isSuccessful && response.body()?.success == true) {
                // Guardar sesión activa localmente
                activeSessionDao.insert(ActiveSessionEntity(accountId = account.id))
                accountRepository.updateAccount(account.copy(state = ConnectionState.ACTIVE))
                emit(Result.success(Unit))
            } else {
                val errorMsg = response.body()?.message ?: "Credenciales incorrectas"
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: IOException) {
            emit(Result.failure(Exception("Error de red: ${e.message}")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun disconnect() {
        try {
            val logoutResponse = nautaApi.logout()
            if (logoutResponse.isSuccessful && logoutResponse.body()?.success == true) {
                // Limpiar sesión local
                val session = activeSessionDao.getActive()
                session?.let {
                    val activeAccount = accountRepository.getAccountById(it.accountId)
                    activeAccount?.let { acc ->
                        accountRepository.updateAccount(acc.copy(state = ConnectionState.INACTIVE))
                    }
                    activeSessionDao.deleteAll()
                }
            }
        } catch (e: Exception) {
            // Si falla el logout remoto, igual limpiamos local
            activeSessionDao.deleteAll()
        }
    }

    override suspend fun getBalance(account: Account): Balance? {
        return try {
            val response = nautaApi.getBalance()
            if (response.isSuccessful && response.body() != null) {
                val balance = response.body()!!
                Balance(
                    remainingTime = balance.remainingTime ?: 0,
                    remainingMoney = balance.credit ?: 0.0,
                    currency = "CUP"  // o "CUC" según respuesta
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getLocalIpAddress(): String? {
        // Implementación simple para obtener IP local (usada en el portal)
        // En producción se puede obtener de NetworkInterface
        return null
    }
}
