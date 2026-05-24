package com.l2wifi.ui.components.dialogs

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.l2wifi.domain.model.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCuentaDialog(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (name: String, username: String, password: String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var name by remember(account) { mutableStateOf(account.name) }
    var username by remember(account) { mutableStateOf(account.username) }
    var password by remember(account) { mutableStateOf(account.password) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var pendingToggleVisible by remember { mutableStateOf(false) } // para saber si se pidió mostrar

    // Launcher para la pantalla de autenticación del sistema (PIN, patrón o huella)
    val authLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Autenticación exitosa → mostrar contraseña
            passwordVisible = true
        }
        pendingToggleVisible = false
    }

    fun requestAuthenticationToShowPassword() {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isDeviceSecure) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                "Autenticación requerida",
                "Usa tu PIN, patrón o huella para ver la contraseña"
            )
            if (intent != null) {
                pendingToggleVisible = true
                authLauncher.launch(intent)
            } else {
                // Si no se puede crear el intent, mostramos directamente (fallback)
                passwordVisible = true
            }
        } else {
            // El dispositivo no tiene seguridad configurada, mostramos sin autenticar (o podrías mostrar un mensaje)
            passwordVisible = true
        }
    }

    fun togglePasswordVisibility() {
        if (passwordVisible) {
            // Si ya visible, simplemente ocultar
            passwordVisible = false
        } else {
            // Si oculta, pedir autenticación antes de mostrar
            requestAuthenticationToShowPassword()
        }
    }

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
                containerColor = Color(0xFF1A1F26)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar cuenta",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    
                    IconButton(
                        onClick = { showDeleteConfirmation = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFFF4D4D)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la cuenta") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FFCC),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF00FFCC)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Campo Usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FFCC),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF00FFCC)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Campo Contraseña con autenticación en el ojo
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color(0xFF00FFCC)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FFCC),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF00FFCC)
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones Aceptar/Cancelar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.3f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = { onSave(name, username, password) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FFCC),
                            contentColor = Color(0xFF0B0F14)
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Eliminar cuenta", color = Color.White) },
            text = { Text("¿Estás seguro de que quieres eliminar esta cuenta?", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF4D4D),
                        contentColor = Color.White
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color(0xFF1A1F26),
            shape = RoundedCornerShape(16.dp)
        )
    }
}