package com.l2wifi.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2wifi.domain.model.Account
import com.l2wifi.domain.model.ConnectionState
import com.l2wifi.domain.repository.AccountRepository
import com.l2wifi.domain.repository.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _activeAccount = MutableStateFlow<Account?>(null)
    val activeAccount: StateFlow<Account?> = _activeAccount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAccountsOnce()
        observeActiveSession()
    }

    private fun loadAccountsOnce() {
        viewModelScope.launch {
            _isLoading.value = true
            accountRepository.getAllAccounts().collect { list ->
                _accounts.value = list
                _isLoading.value = false
            }
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            connectionRepository.getActiveAccount().collect { account ->
                _activeAccount.value = account
            }
        }
    }

    fun connect(account: Account) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                connectionRepository.connect(account).collect { result ->
                    if (result.isSuccess) {
                        refreshAccounts()
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al conectar"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkBalance(account: Account, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val balance = connectionRepository.getBalance(account)
                if (balance != null) {
                    val horas = balance.remainingTime / 3600
                    val minutos = (balance.remainingTime % 3600) / 60
                    val segundos = balance.remainingTime % 60
                    onResult("Saldo: ${balance.remainingMoney} ${balance.currency}\nTiempo restante: ${horas}h ${minutos}m ${segundos}s")
                } else {
                    onResult("No se pudo obtener el saldo. Verifica tu conexión.")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al consultar saldo: ${e.message}"
                onResult("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAccount(name: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                val newAccount = Account(
                    id = 0,
                    name = name,
                    username = username,
                    password = password,
                    state = ConnectionState.INACTIVE
                )
                accountRepository.insertAccount(newAccount)
                refreshAccounts()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al añadir cuenta: ${e.message}"
            }
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountRepository.updateAccount(account)
                refreshAccounts()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar cuenta: ${e.message}"
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(account)
                refreshAccounts()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar cuenta: ${e.message}"
            }
        }
    }

    fun refreshAccounts() {
        viewModelScope.launch {
            try {
                val updatedList = accountRepository.getAllAccounts().firstOrNull() ?: emptyList()
                _accounts.value = updatedList
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar cuentas: ${e.message}"
            }
        }
    }

    fun updateAccountsOrder(accounts: List<Account>) {
        viewModelScope.launch {
            try {
                accountRepository.updateAccountsOrder(accounts)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar orden: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}