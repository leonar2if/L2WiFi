package com.l2wifi.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.l2wifi.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _themeMode = MutableStateFlow(0)
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.getThemeMode().collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            dataStore.saveThemeMode(mode)
            _themeMode.value = mode
        }
    }

    class Factory(private val dataStore: SettingsDataStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
                return ThemeViewModel(dataStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}