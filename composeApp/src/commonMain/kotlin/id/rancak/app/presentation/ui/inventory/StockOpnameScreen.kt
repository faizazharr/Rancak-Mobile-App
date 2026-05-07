package id.rancak.app.presentation.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.inventory.components.OpnameCard
import id.rancak.app.presentation.ui.inventory.components.OpnameDetailContent
import id.rancak.app.presentation.ui.inventory.components.OpnameDetailTabletPanel
import id.rancak.app.presentation.viewmodel.StockOpnameViewModel
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StockOpnameScreen(
    onBack: () -> Unit
) {
    val viewModel: StockOpnameViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { scope.launch { snackbarHostState.showSnackbar(it) }; viewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { scope.launch { snackbarHostState.showSnackbar("⚠ $it") }; viewModel.clearError() }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        val detail = uiState.detail
        var inlineNote by remember { mutableStateOf("") }

        // Phone: navigate into detail as a full-screen view
        if (!isTablet && detail != null) {
            OpnameDetailContent(
                detail              = detail,
                products            = uiState.products.toImmutableList(),
                isSubmitting        = uiState.isSubmitting,
                isLoading           = uiState.isLoadingDetail,
                showFinalizeConfirm = uiState.showFinalizeConfirm,
                snackbarHostState   = snackbarHostState,
                onBack              = viewModel::closeDetail,
                onSaveItems         = viewModel::saveItems,
                onFinalizeClick     = viewModel::openFinalizeConfirm,
                onFinalizeConfirm   = viewModel::finalizeOpname,
                onFinalizeDismiss   = viewModel::closeFinalizeConfirm,
                onDeleteItem        = viewModel::deleteItem
            )
        } else {
            Scaffold(
                topBar = {
                    RancakTopBar(
                        title    = "Stok Opname",
                        icon     = Icons.Default.Inventory,
                        onMenu   = onBack,
                        subtitle = "${uiState.opnames.size} sesi"
                    )
                },
                floatingActionButton = {
                    if (!isTablet) {
                        FloatingActionButton(onClick = viewModel::openCreateDialog) {
                            Icon(Icons.Default.Add, contentDescription = "Buat opname baru")
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                if (isTablet) {
                    // ── Tablet: master-detail two-panel ─────────────────────
                    Row(Modifier.padding(padding).fillMaxSize()) {
                        // Left panel — opname list
                        Column(
                            modifier = Modifier
                                .width(300.dp)
                                .fillMaxHeight()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    null to "Semua",
                                    "draft" to "Draft",
                                    "finalized" to "Final",
                                    "cancelled" to "Dibatalkan"
                                ).forEach { (value, label) ->
                                    FilterChip(
                                        selected = uiState.filterStatus == value,
                                        onClick  = { viewModel.setFilter(value) },
                                        label    = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            HorizontalDivider()

                            Box(Modifier.weight(1f)) {
                                when {
                    uiState.isLoading -> LoadingScreen()
                                    uiState.opnames.isEmpty() -> EmptyScreen(
                                        message  = "Belum ada sesi opname",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    else -> LazyColumn(
                                        contentPadding      = PaddingValues(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(uiState.opnames, key = { it.uuid }) { opname ->
                                            OpnameCard(
                                                opname     = opname,
                                                isSelected = detail?.opname?.uuid == opname.uuid,
                                                onOpen     = { viewModel.loadDetail(opname.uuid) },
                                                onCancel   = { viewModel.cancelOpname(opname) }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()
                            Surface(tonalElevation = 2.dp) {
                                Button(
                                    onClick  = viewModel::openCreateDialog,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Buat Opname Baru")
                                }
                            }
                        }

                        VerticalDivider()

                        // Right panel — detail / empty state
                        if (detail != null) {
                            OpnameDetailTabletPanel(
                                detail              = detail,
                                products            = uiState.products.toImmutableList(),
                                isSubmitting        = uiState.isSubmitting,
                                isLoading           = uiState.isLoadingDetail,
                                showFinalizeConfirm = uiState.showFinalizeConfirm,
                                onClose             = viewModel::closeDetail,
                                onSaveItems         = viewModel::saveItems,
                                onFinalizeClick     = viewModel::openFinalizeConfirm,
                                onFinalizeConfirm   = viewModel::finalizeOpname,
                                onFinalizeDismiss   = viewModel::closeFinalizeConfirm,
                                onDeleteItem        = viewModel::deleteItem,
                                modifier            = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyScreen(
                                message  = "Pilih sesi opname untuk melihat detail",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    // ── Phone: list-only (detail handled above as full-screen) ─
                    Column(Modifier.padding(padding).fillMaxSize()) {
                        AnimatedVisibility(
                            visible = uiState.showCreateDialog,
                            enter   = expandVertically(),
                            exit    = shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Buat Sesi Opname Baru",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value         = inlineNote,
                                        onValueChange = { inlineNote = it },
                                        label         = { Text("Catatan (opsional)") },
                                        modifier      = Modifier.fillMaxWidth(),
                                        maxLines      = 3
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(
                                            onClick  = { inlineNote = ""; viewModel.closeCreateDialog() },
                                            enabled  = !uiState.isSubmitting
                                        ) { Text("Batal") }
                                        Spacer(Modifier.width(8.dp))
                                        Button(
                                            onClick  = { viewModel.createOpname(inlineNote.ifBlank { null }); inlineNote = "" },
                                            enabled  = !uiState.isSubmitting
                                        ) {
                                            if (uiState.isSubmitting)
                                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                            else Text("Buat")
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                null to "Semua",
                                "draft" to "Draft",
                                "finalized" to "Final",
                                "cancelled" to "Dibatalkan"
                            ).forEach { (value, label) ->
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
                            uiState.opnames.isEmpty() -> EmptyScreen(
                                message  = "Belum ada sesi opname",
                                modifier = Modifier.fillMaxSize()
                            )
                            else -> LazyColumn(
                                contentPadding      = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.opnames, key = { it.uuid }) { opname ->
                                    OpnameCard(
                                        opname   = opname,
                                        onOpen   = { viewModel.loadDetail(opname.uuid) },
                                        onCancel = { viewModel.cancelOpname(opname) }
                                    )
                                }
                            }
                        }
                    }
                }
            }


        }
    }
}

