package com.l2wifi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.l2wifi.data.local.datastore.SettingsDataStore
import com.l2wifi.ui.MainScreen
import com.l2wifi.ui.theme.L2WiFiTheme
import com.l2wifi.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permiso de llamada concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "El permiso de llamada es necesario para recargas", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestCallPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestCallPermission()

        val dataStore = SettingsDataStore(applicationContext)
        val themeViewModel = ViewModelProvider(
            this,
            ThemeViewModel.Factory(dataStore)
        ).get(ThemeViewModel::class.java)

        setContent {
            val themeModeState = themeViewModel.themeMode.collectAsStateWithLifecycle()
            val themeMode = themeModeState.value
            L2WiFiDynamicTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(navController = navController)
                }
            }
        }
    }
}

@Composable
fun L2WiFiDynamicTheme(
    themeMode: Int,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isSystemDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
    L2WiFiTheme(darkTheme = isSystemDark, content = content)
}