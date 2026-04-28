package id.rancak.app.presentation.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.inventory.components.*
import id.rancak.app.presentation.viewmodel.StockOpnameViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StockOpnameScreen(
    onBack: () -> Unit,
    viewModel: StockOpnameViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { scope.launch { snackbarHostState.showSnackbar(it) }; viewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { scope.launch { snackbarHostState.showSnackbar("⚠ $it") }; viewModel.clearError() }
    }

    val detail = uiState.detail
    if (detail != null) {
        OpnameDetailContent(
            detail              = detail,
            products            = uiState.products,
            isSubmitting        = uiState.isSubmitting,
            isLoading           = uiState.isLoadingDetail,
            showFinalizeConfirm = uiState.showFinalizeConfirm,
            snackbarHostState   = snackbarHostState,
            onBack              = viewModel::closeDetail,
            onSaveItems         = viewModel::saveItems,
            onFinalizeClick     = viewModel::openFinalizeConfirm,
            onFinalizeConfirm   = viewModel::finalizeOpname,
            onFinalizeDismiss   = viewModel::closeFinalizeConfirm
        )
        return
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Stok Opname",
                icon     = Icons.Default.Inventory,
                onBack   = onBack,
                subtitle = "${uiState.opnames.size} sesi"
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openCreateDialog) {
                Icon(Icons.Default.Add, contentDescription = "Buat opname baru")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(null to "Semua", "draft" to "Draft", "finalized" to "Final", "cancelled" to "Dibatalkan")
                        .forEach { (value, label) ->
                            FilterChip(
                                selected = uiState.filterStatus == value,
                                onClick  = { viewModel.setFilter(value) },
                                label    = { Text(label, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                }
                HorizontalDivider()

                when {
                    uiState.isLoading -> LoadingScreen()
                    uiState.opnames.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory, null, Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp))
                            Text("Belum ada sesi opname", style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    isTablet -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        gridItems(uiState.opnames, key = { it.uuid }) { opname ->
                            OpnameCard(opname, onOpen = { viewModel.loadDetail(opname.uuid) },
                                onCancel = { viewModel.cancelOpname(opname) })
                        }
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.opnames, key = { it.uuid }) { opname ->
                            OpnameCard(opname, onOpen = { viewModel.loadDetail(opname.uuid) },
                                onCancel = { viewModel.cancelOpname(opname) })
                        }
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreateOpnameDialog(
                isSubmitting = uiState.isSubmitting,
                onDismiss    = viewModel::closeCreateDialog,
                onConfirm    = viewModel::createOpname
            )
        }
    }
}

