package com.l2wifi.ui.screens.activeconnection

import android.content.Intent
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.l2wifi.service.TimerService
import com.l2wifi.ui.components.TimerRing
import com.l2wifi.util.WidgetAction
import kotlinx.coroutines.launch

@Composable
fun ActiveConnectionScreen(
    navController: NavController,
    accountId: Long,
    pendingWidgetAction: WidgetAction? = null,
    onWidgetActionConsumed: () -> Unit = {},
    viewModel: ActiveConnectionViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val startServiceEvent by viewModel.startServiceEvent.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    LaunchedEffect(pendingWidgetAction) {
        when (pendingWidgetAction) {
            is WidgetAction.Logout -> {
                if (pendingWidgetAction.accountId == accountId) {
                    val success = viewModel.logout(accountId)
                    if (success) {
                        context.stopService(Intent(context, TimerService::class.java))
                        navController.popBackStack()
                    }
                    onWidgetActionConsumed()
                }
            }
            is WidgetAction.Refresh -> {
                if (pendingWidgetAction.accountId == accountId) {
                    viewModel.refreshStatus()
                    onWidgetActionConsumed()
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(startServiceEvent) {
        if (startServiceEvent && remainingSeconds > 0) {
            val serviceIntent = Intent(context, TimerService::class.java).apply {
                putExtra("START_TIME", remainingSeconds)
                putExtra("ACCOUNT_NAME", uiState.accountName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            viewModel.clearStartServiceEvent()
        }
    }

    val totalTime = 3600f
    val progress by animateFloatAsState(
        targetValue = (remainingSeconds / totalTime).coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "timerProgress"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Total: 01:00:00",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.Center) {
            TimerRing(
                progress = progress,
                modifier = Modifier.size(250.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = String.format("%d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                fontSize = 40.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        if (viewModel.logout(accountId)) {
                            context.stopService(Intent(context, TimerService::class.java))
                            navController.popBackStack()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Button(
                onClick = { scope.launch { viewModel.refreshStatus() } },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                Spacer(Modifier.width(8.dp))
                Text("Actualizar")
            }
        }
    }
}
