package com.l2wifi.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun saveThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }

    fun getThemeMode(): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_MODE] ?: 0
    }

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode")
    }
}
