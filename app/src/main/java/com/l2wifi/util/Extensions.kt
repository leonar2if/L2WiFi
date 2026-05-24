package com.l2wifi.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

@Composable
fun formatTimeComposable(seconds: Long): String {
    return formatTime(seconds)
}
