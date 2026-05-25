package com.l2wifi.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.l2wifi.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val options = listOf(
        SettingRowData(Icons.Default.Palette, "Tema"),
        SettingRowData(Icons.Default.Settings, "Ajuste 2"),
        SettingRowData(Icons.Default.Star, "Ajuste 3"),
        SettingRowData(Icons.Default.Notifications, "Ajuste 4"),
        SettingRowData(Icons.Default.Lock, "Ajuste 5")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val density = LocalDensity.current
        val expandedHeightPx = with(density) { maxHeight.toPx() * 0.40f }
        val minHeightPx = with(density) { 72.dp.toPx() }
        var headerHeightPx by remember { mutableStateOf(expandedHeightPx) }
        val listState = rememberLazyListState()

        val nestedScrollConnection = remember(headerHeightPx, expandedHeightPx, minHeightPx, listState) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val dy = available.y
                    val old = headerHeightPx
                    val newHeight = when {
                        dy < 0f && old > minHeightPx -> (old + dy).coerceAtLeast(minHeightPx)
                        dy > 0f && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 && old < expandedHeightPx -> (old + dy).coerceAtMost(expandedHeightPx)
                        else -> old
                    }
                    if (newHeight != old) {
                        headerHeightPx = newHeight
                        return Offset(0f, newHeight - old)
                    }
                    return Offset.Zero
                }
            }
        }

        val collapseFraction = ((expandedHeightPx - headerHeightPx) / (expandedHeightPx - minHeightPx).coerceAtLeast(1f)).coerceIn(0f, 1f)
        val titleSize = lerp(34.sp, 22.sp, collapseFraction)
        val separatorAlpha = ((collapseFraction - 0.55f) / 0.45f).coerceIn(0f, 1f)
        val headerHeightDp = with(density) { headerHeightPx.toDp() }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(top = headerHeightDp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            itemsIndexed(options) { index, item ->
                when (item.label) {
                    "Tema" -> ThemeSettingRow(themeMode = themeMode, onChange = { themeViewModel.setThemeMode(it) })
                    else -> FakeSettingRow(item)
                }
                if (index < options.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                        thickness = 1.dp
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeightDp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Ajustes",
                modifier = Modifier.align(Alignment.Center),
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = separatorAlpha))
            )
        }
    }
}

private data class SettingRowData(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSettingRow(themeMode: Int, onChange: (Int) -> Unit) {
    val labels = listOf("Automático", "Claro", "Oscuro")
    val value = labels.getOrNull(themeMode.coerceIn(0, 2)) ?: "Automático"
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "Tema",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp
        )
        Box(modifier = Modifier.width(160.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    labels.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                expanded = false
                                onChange(index)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FakeSettingRow(item: SettingRowData) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = item.label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp
        )
    }
}
