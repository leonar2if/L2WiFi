package com.l2wifi.ui.screens.activeconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2wifi.domain.model.Account
import com.l2wifi.domain.repository.AccountRepository
import com.l2wifi.domain.repository.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveConnectionViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveConnectionUiState())
    val uiState: StateFlow<ActiveConnectionUiState> = _uiState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _startServiceEvent = MutableStateFlow(false)
    val startServiceEvent: StateFlow<Boolean> = _startServiceEvent.asStateFlow()

    private var currentAccount: Account? = null

    fun loadAccount(accountId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                currentAccount = accountRepository.getAccountById(accountId)
                currentAccount?.let { account ->
                    _uiState.value = _uiState.value.copy(
                        accountName = account.name,
                        username = account.username
                    )
                    refreshStatus()
                } ?: run {
                    _errorMessage.value = "Cuenta no encontrada"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar cuenta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            val balance = currentAccount?.let { connectionRepository.getBalance(it) }
            if (balance != null) {
                _remainingSeconds.value = balance.remainingTime
                if (_remainingSeconds.value > 0) {
                    _startServiceEvent.value = true
                }
                _uiState.value = _uiState.value.copy(
                    balance = balance.remainingMoney.toString(),
                    currency = balance.currency,
                    remainingTime = balance.remainingTime
                )
            } else {
                _errorMessage.value = "No se pudo obtener el saldo"
            }
        }
    }

    fun clearStartServiceEvent() {
        _startServiceEvent.value = false
    }

    suspend fun logout(accountId: Long): Boolean {
        return try {
            connectionRepository.disconnect()
            true
        } catch (e: Exception) {
            _errorMessage.value = "Error al cerrar sesión: ${e.message}"
            false
        }
    }

    fun clearError() {
        _errorMessage.value = ""
    }
}

data class ActiveConnectionUiState(
    val accountName: String = "",
    val username: String = "",
    val balance: String = "0.00",
    val currency: String = "CUP",
    val remainingTime: Long = 0L
)
