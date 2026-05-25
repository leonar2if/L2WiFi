package com.l2wifi.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.l2wifi.domain.model.Account
import com.l2wifi.ui.components.*
import com.l2wifi.ui.components.dialogs.EditarCuentaDialog
import com.l2wifi.ui.components.dialogs.RecargaDialog
import com.l2wifi.ui.components.dialogs.SaldoDialog
import com.l2wifi.ui.screens.addaccount.AddAccountBottomSheet
import com.l2wifi.util.WidgetAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    pendingWidgetAction: WidgetAction? = null,
    onWidgetActionConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val activeAccount by viewModel.activeAccount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var showSaldoDialog by remember { mutableStateOf(false) }
    var showRecargaDialog by remember { mutableStateOf(false) }
    var showEditarDialog by remember { mutableStateOf(false) }
    var selectedAccountForBalance by remember { mutableStateOf<Account?>(null) }
    var selectedAccountForEdit by remember { mutableStateOf<Account?>(null) }
    var isReordering by remember { mutableStateOf(false) }
    var currentItems by remember { mutableStateOf(accounts) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(accounts, isReordering) {
        if (!isReordering) currentItems = accounts
    }

    LaunchedEffect(pendingWidgetAction, accounts) {
        when (val action = pendingWidgetAction) {
            is WidgetAction.Connect -> {
                val account = accounts.firstOrNull { it.id == action.accountId }
                if (account != null) {
                    Toast.makeText(context, "Conectando…", Toast.LENGTH_SHORT).show()
                    viewModel.connect(account)
                    onWidgetActionConsumed()
                }
            }
            is WidgetAction.Balance -> {
                val account = accounts.firstOrNull { it.id == action.accountId }
                if (account != null) {
                    selectedAccountForBalance = account
                    showSaldoDialog = true
                    onWidgetActionConsumed()
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            PremiumTopBar(
                title = "WIFI",
                onSettingsClick = { navController.navigate("settings") },
                onAboutClick = { navController.navigate("about") },
                onNautaClick = { navController.navigate("profile") }
            )
        },
        floatingActionButton = {
            if (!isReordering) {
                AnimatedFAB(
                    onAddClick = { showAddSheet = true },
                    onRefreshClick = { viewModel.refreshAccounts() }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                LoadingOverlay()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = currentItems,
                        key = { it.id }
                    ) { account ->
                        AccountCardReordering(
                            account = account,
                            isActive = activeAccount?.id == account.id,
                            isReordering = isReordering,
                            onConnect = { viewModel.connect(account) },
                            onCheckBalance = {
                                selectedAccountForBalance = account
                                showSaldoDialog = true
                            },
                            onEnter = {
                                if (activeAccount?.id == account.id) {
                                    navController.navigate("activeConnection/${account.id}")
                                }
                            },
                            onEdit = {
                                selectedAccountForEdit = account
                                showEditarDialog = true
                            },
                            onMoveUp = {
                                val index = currentItems.indexOf(account)
                                if (index > 0) {
                                    val mutable = currentItems.toMutableList()
                                    mutable[index] = mutable[index - 1].also { mutable[index - 1] = mutable[index] }
                                    currentItems = mutable
                                }
                            },
                            onMoveDown = {
                                val index = currentItems.indexOf(account)
                                if (index < currentItems.size - 1) {
                                    val mutable = currentItems.toMutableList()
                                    mutable[index] = mutable[index + 1].also { mutable[index + 1] = mutable[index] }
                                    currentItems = mutable
                                }
                            }
                        )
                    }
                }
                if (isReordering) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(onClick = {
                            viewModel.updateAccountsOrder(currentItems)
                            isReordering = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Orden guardado")
                            }
                        }) { Text("Guardar orden") }
                        Button(onClick = {
                            isReordering = false
                            currentItems = accounts
                        }) { Text("Cancelar") }
                    }
                }
            }
        }
    }

    if (showSaldoDialog && selectedAccountForBalance != null) {
        SaldoDialog(
            onDismiss = { showSaldoDialog = false; selectedAccountForBalance = null },
            onConsultar = {
                viewModel.checkBalance(selectedAccountForBalance!!) { resultado ->
                    scope.launch { snackbarHostState.showSnackbar(resultado) }
                }
            },
            onRecargar = { showRecargaDialog = true }
        )
    }
    if (showRecargaDialog) {
        RecargaDialog(onDismiss = { showRecargaDialog = false })
    }

    selectedAccountForEdit?.let { account ->
        EditarCuentaDialog(
            account = account,
            onDismiss = { showEditarDialog = false; selectedAccountForEdit = null },
            onSave = { name, username, password ->
                viewModel.updateAccount(account.copy(name = name, username = username, password = password))
                showEditarDialog = false
                selectedAccountForEdit = null
                scope.launch { snackbarHostState.showSnackbar("Cuenta actualizada") }
            },
            onDelete = {
                viewModel.deleteAccount(account)
                showEditarDialog = false
                selectedAccountForEdit = null
                scope.launch { snackbarHostState.showSnackbar("Cuenta eliminada") }
            }
        )
    }

    if (showAddSheet) {
        AddAccountBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, username, password ->
                viewModel.addAccount(name, username, password)
                showAddSheet = false
            }
        )
    }
}

@Composable
fun AccountCardReordering(
    account: Account,
    isActive: Boolean,
    isReordering: Boolean,
    onConnect: () -> Unit,
    onCheckBalance: () -> Unit,
    onEnter: () -> Unit,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val cardColor = if (isReordering && isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(if (isReordering) 8.dp else 4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (isReordering) {
                    Column {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = if (!isReordering) Modifier.weight(1f).clickable { onEdit() }.padding(4.dp)
                    else Modifier.weight(1f)
                ) {
                    Text(
                        account.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        account.username,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!isReordering) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Button(
                            onClick = onEnter,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Text("Activa >")
                        }
                    } else {
                        IconButton(onClick = onConnect, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.Wifi, contentDescription = "Conectar", tint = Color.White)
                        }
                        IconButton(onClick = onCheckBalance, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Filled.AttachMoney,
                                contentDescription = "Saldo",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
