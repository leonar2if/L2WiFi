package com.l2wifi.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2wifi.data.local.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
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
}
