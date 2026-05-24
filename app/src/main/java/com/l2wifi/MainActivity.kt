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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.l2wifi.ui.MainScreen
import com.l2wifi.ui.theme.L2WiFiTheme
import com.l2wifi.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
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

    // Eventos para comunicar acciones del widget a la UI
    companion object {
        var pendingConnectAccountId: Long? = null
        var pendingShowBalanceAccountId: Long? = null
        var pendingLogout: Boolean = false
        var pendingRefreshBalance: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestCallPermission()

        // Procesar intents del widget
        intent?.let {
            when {
                it.hasExtra("connect_account") -> {
                    pendingConnectAccountId = it.getLongExtra("connect_account", -1L)
                    if (pendingConnectAccountId == -1L) pendingConnectAccountId = null
                }
                it.hasExtra("show_balance") -> {
                    pendingShowBalanceAccountId = it.getLongExtra("show_balance", -1L)
                    if (pendingShowBalanceAccountId == -1L) pendingShowBalanceAccountId = null
                }
                it.hasExtra("logout") -> {
                    pendingLogout = true
                }
                it.hasExtra("refresh_balance") -> {
                    pendingRefreshBalance = true
                }
            }
        }

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            L2WiFiDynamicTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        initialConnectAccountId = pendingConnectAccountId,
                        initialShowBalanceAccountId = pendingShowBalanceAccountId,
                        initialLogout = pendingLogout,
                        initialRefreshBalance = pendingRefreshBalance
                    )
                }
            }
        }

        // Limpiar eventos después de procesarlos
        pendingConnectAccountId = null
        pendingShowBalanceAccountId = null
        pendingLogout = false
        pendingRefreshBalance = false
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
