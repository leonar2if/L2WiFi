package com.l2wifi.ui.components.dialogs

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l2wifi.ui.components.InfoTooltip
import com.l2wifi.util.UssdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecargaDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var bancoSeleccionado by remember { mutableStateOf("Seleccione banco") }
    var expanded by remember { mutableStateOf(false) }
    val bancos = listOf("BPA", "Bandec", "Monedero MiTransfer")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recargar saldo",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = bancoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        bancos.forEach { banco ->
                            DropdownMenuItem(
                                text = { Text(banco, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    bancoSeleccionado = banco
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { autenticar(context, bancoSeleccionado) },
                        enabled = bancoSeleccionado != "Seleccione banco",
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Autenticar")
                    }
                    InfoTooltip(text = "Se le solicitará su PIN en una ventana del sistema") {
                        Icon(Icons.Default.Info, contentDescription = "Info Autenticar", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { recargarNauta(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Recargar Nauta")
                    }
                    InfoTooltip(text = "Siga los pasos a continuación para completar su recarga") {
                        Icon(Icons.Default.Info, contentDescription = "Info Recargar", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

private fun autenticar(context: Context, banco: String) {
    val ussd = when (banco) {
        "BPA" -> "*444*40*01#"
        "Bandec" -> "*444*40*02#"
        "Monedero MiTransfer" -> "*444*40*04#"
        else -> return
    }
    try {
        UssdManager.sendUssd(context, ussd)
    } catch (e: SecurityException) {
        // El permiso ya se maneja en MainActivity
    }
}

private fun recargarNauta(context: Context) {
    try {
        UssdManager.sendUssd(context, "*444*59#")
    } catch (e: SecurityException) {
        // El permiso ya se maneja en MainActivity
    }
}
