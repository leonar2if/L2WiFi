package com.l2wifi.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    PlainTooltipBox(
        tooltip = { Text(text) },
        modifier = modifier
    ) {
        content()
    }
}
