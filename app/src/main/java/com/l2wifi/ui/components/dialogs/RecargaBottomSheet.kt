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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.l2wifi.ui.components.InfoTooltip
import com.l2wifi.util.UssdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecargaBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var bancoSeleccionado by remember { mutableStateOf("Seleccione banco") }
    var expanded by remember { mutableStateOf(false) }
    val bancos = listOf("BPA", "Bandec", "Monedero MiTransfer")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFF1A1F26),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF00FFCC)) }
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
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dropdown selector de banco
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
                        focusedBorderColor = Color(0xFF00FFCC),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF00FFCC)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    bancos.forEach { banco ->
                        DropdownMenuItem(
                            text = { Text(banco, color = Color.White) },
                            onClick = {
                                bancoSeleccionado = banco
                                expanded = false
                            },
                            colors = DropdownMenuItemDefaults.colors(
                                focusedTextColor = Color(0xFF00FFCC),
                                focusedContainerColor = Color(0xFF2A2F3A)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Autenticar con tooltip
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
                        containerColor = Color(0xFF00FFCC),
                        contentColor = Color(0xFF0B0F14)
                    )
                ) {
                    Text("Autenticar")
                }
                InfoTooltip(text = "Se le solicitará su PIN en una ventana del sistema") {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info Autenticar",
                        tint = Color(0xFF00FFCC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Recargar Nauta con tooltip
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
                        containerColor = Color(0xFF0099FF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Recargar Nauta")
                }
                InfoTooltip(text = "Siga los pasos a continuación para completar su recarga") {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info Recargar",
                        tint = Color(0xFF0099FF)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón Cancelar
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.Gray)
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
        // El permiso se maneja en MainActivity
    }
}

private fun recargarNauta(context: Context) {
    try {
        UssdManager.sendUssd(context, "*444*59#")
    } catch (e: SecurityException) {
        // El permiso se maneja en MainActivity
    }
}