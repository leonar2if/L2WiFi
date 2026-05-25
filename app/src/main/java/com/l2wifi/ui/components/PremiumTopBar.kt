package com.l2wifi.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nauta.cu"))
                context.startActivity(intent)
                onNautaClick()
            }) {
                Icon(Icons.Default.Person, contentDescription = "Nauta", tint = Color.White)
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
