package com.l2wifi.data.repository

import android.content.Context
import android.net.wifi.WifiManager
import com.l2wifi.data.local.dao.ActiveSessionDao
import com.l2wifi.data.local.entity.ActiveSessionEntity
import com.l2wifi.data.remote.api.NautaApiService
import com.l2wifi.domain.model.Account
import com.l2wifi.domain.model.Balance
import com.l2wifi.domain.model.ConnectionState
import com.l2wifi.domain.repository.AccountRepository
import com.l2wifi.domain.repository.ConnectionRepository
import com.l2wifi.service.TimerService
import com.l2wifi.widget.WidgetSync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject

class ConnectionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nautaApi: NautaApiService,
    private val activeSessionDao: ActiveSessionDao,
    private val accountRepository: AccountRepository
) : ConnectionRepository {

    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    override fun getActiveAccount(): Flow<Account?> =
        activeSessionDao.getActiveFlow().mapLatest { session ->
            session?.accountId?.let { accountRepository.getAccountById(it) }
        }.flowOn(Dispatchers.IO)

    override suspend fun connect(account: Account): Flow<Result<Unit>> = flow {
        try {
            if (account.username.isEmpty() || account.password.isEmpty()) {
                emit(Result.failure(Exception("Usuario y contraseña son requeridos")))
                return@flow
            }

            val userIp = getLocalIpAddress() ?: "0.0.0.0"
            val response = nautaApi.login(account.username, account.password, userIp)

            if (response.isSuccessful && response.body()?.success == true) {
                activeSessionDao.deleteAll()
                activeSessionDao.insert(ActiveSessionEntity(accountId = account.id))
                accountRepository.updateAccount(account.copy(state = ConnectionState.ACTIVE))

                TimerService.saveRemainingTime(context, response.body()?.remainingTime ?: 0L)
                WidgetSync.requestUpdate(context)

                emit(Result.success(Unit))
            } else {
                val errorMsg = response.body()?.message
                    ?: "Credenciales incorrectas o servidor no disponible"
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: IOException) {
            emit(Result.failure(Exception("Error de red: Verifica tu conexión a Internet. ${e.message}")))
        } catch (e: Exception) {
            emit(Result.failure(Exception("Error al conectar: ${e.localizedMessage}")))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun disconnect() {
        try {
            val logoutResponse = nautaApi.logout()
            if (logoutResponse.isSuccessful && logoutResponse.body()?.success == true) {
                val session = activeSessionDao.getActive()
                session?.let {
                    val activeAccount = accountRepository.getAccountById(it.accountId)
                    activeAccount?.let { acc ->
                        accountRepository.updateAccount(acc.copy(state = ConnectionState.INACTIVE))
                    }
                }
            }
        } catch (e: Exception) {
            // Si falla el logout remoto, igual limpiamos local
        } finally {
            activeSessionDao.deleteAll()
            TimerService.clearRemainingTime(context)
            WidgetSync.requestUpdate(context)
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
                    currency = "CUP"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (!networkInterface.isUp) continue
                if (networkInterface.isLoopback) continue

                val addresses = networkInterface.inetAddresses
                for (address in addresses) {
                    if (address is InetAddress && !address.isLoopbackAddress) {
                        val ip = address.hostAddress
                        if (ip != null && !ip.contains(":")) {
                            return ip
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
