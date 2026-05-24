package com.l2wifi.ui.screens.billete

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.l2wifi.ui.components.InfoTooltip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBillete(viewModel: RecargaViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val bancoSeleccionado by viewModel.bancoSeleccionado.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val bancos = listOf("BPA", "Bandec", "Monedero MiTransfer")

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                bancos.forEach { banco ->
                    DropdownMenuItem(
                        text = { Text(banco) },
                        onClick = {
                            viewModel.setBanco(banco)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Autenticar con tooltip
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.autenticar(context) },
                enabled = bancoSeleccionado != "Seleccione banco",
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Autenticar")
            }
            InfoTooltip(text = "Se le solicitará su PIN en una ventana del sistema") {
                Icon(Icons.Default.Info, contentDescription = "Info Autenticar", modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Recargar Nauta con tooltip
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.recargarNauta(context) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Recargar Nauta")
            }
            InfoTooltip(text = "Siga los pasos a continuación para completar su recarga") {
                Icon(Icons.Default.Info, contentDescription = "Info Recargar", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}