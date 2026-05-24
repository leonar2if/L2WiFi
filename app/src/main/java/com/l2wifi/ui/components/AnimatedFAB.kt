package com.l2wifi.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.l2wifi.ui.theme.GlowCyan

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedFAB(
    onAddClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Botón principal: "+"
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .size(56.dp)
                .background(
                    brush = Brush.radialGradient(listOf(GlowCyan, Color(0xFF0099FF))),
                    shape = CircleShape
                )
                .scale(if (expanded) 0.9f else 1f)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Abrir menú", tint = Color.White)
        }

        // Sub-botones animados
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = (-70).dp),
                horizontalAlignment = Alignment.End
            ) {
                // Añadir cuenta
                IconButton(
                    onClick = {
                        expanded = false
                        onAddClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir cuenta", tint = GlowCyan)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Refrescar
                IconButton(
                    onClick = {
                        expanded = false
                        onRefreshClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = GlowCyan)
                }
            }
        }
    }
}
