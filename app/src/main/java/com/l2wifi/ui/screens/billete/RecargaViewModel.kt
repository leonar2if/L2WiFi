package com.l2wifi.ui.screens.billete

import android.content.Context
import androidx.lifecycle.ViewModel
import com.l2wifi.util.UssdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RecargaViewModel @Inject constructor() : ViewModel() {
    private val _bancoSeleccionado = MutableStateFlow("Seleccione banco")
    val bancoSeleccionado: StateFlow<String> = _bancoSeleccionado.asStateFlow()

    fun setBanco(banco: String) {
        _bancoSeleccionado.value = banco
    }

    fun autenticar(context: Context) {
        val ussd = when (_bancoSeleccionado.value) {
            "BPA" -> "*444*40*01#"
            "Bandec" -> "*444*40*02#"
            "Monedero MiTransfer" -> "*444*40*04#"
            else -> return
        }
        UssdManager.sendUssd(context, ussd)
    }

    fun recargarNauta(context: Context) {
        UssdManager.sendUssd(context, "*444*59#")
    }
}
