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
            connectionRepository.connect(account).collect { result ->
                if (result.isSuccess) {
                    refreshAccounts()
                }
            }
        }
    }

    fun checkBalance(account: Account, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val balance = connectionRepository.getBalance(account)
                if (balance != null) {
                    val horas = balance.remainingTime / 3600
                    val minutos = (balance.remainingTime % 3600) / 60
                    onResult("Saldo: ${balance.remainingMoney} ${balance.currency}\nTiempo restante: ${horas}h ${minutos}m")
                } else {
                    onResult("No se pudo obtener el saldo. Verifica tu conexión.")
                }
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }

    fun addAccount(name: String, username: String, password: String) {
        viewModelScope.launch {
            val newAccount = Account(
                id = 0,
                name = name,
                username = username,
                password = password,
                state = ConnectionState.INACTIVE
            )
            accountRepository.insertAccount(newAccount)
            refreshAccounts()
        }
    }

    fun refreshAccounts() {
        viewModelScope.launch {
            try {
                val updatedList = accountRepository.getAllAccounts().firstOrNull() ?: emptyList()
                _accounts.value = updatedList
            } catch (e: Exception) {
                // mantener el valor actual
            }
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.updateAccount(account)
            refreshAccounts()
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
            refreshAccounts()
        }
    }

    fun updateAccountsOrder(accounts: List<Account>) {
        viewModelScope.launch {
            accountRepository.updateAccountsOrder(accounts)
        }
    }
}
