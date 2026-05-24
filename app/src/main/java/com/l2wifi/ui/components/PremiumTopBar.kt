package com.l2wifi.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTopBar(
    title: String,
    onSettingsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onNautaClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    TopAppBar(
        title = { Text(title, color = MaterialTheme.colorScheme.onSurface) },
        actions = {
            // Icono de usuario a la derecha (antes de los tres puntos)
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nauta.cu"))
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Person, contentDescription = "Nauta", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menú", tint = MaterialTheme.colorScheme.onSurface)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Ajustes") },
                    onClick = { expanded = false; onSettingsClick() }
                )
                DropdownMenuItem(
                    text = { Text("Acerca de") },
                    onClick = { expanded = false; onAboutClick() }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
