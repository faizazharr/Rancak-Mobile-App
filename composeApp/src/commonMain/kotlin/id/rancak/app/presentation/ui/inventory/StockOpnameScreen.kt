package id.rancak.app.presentation.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.ui.inventory.components.CreateOpnameDialog
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
                        ExtendedFloatingActionButton(
                            onClick = viewModel::openCreateDialog,
                            icon = { Icon(Icons.Default.Add, contentDescription = null) },
                            text = { Text("Sesi Baru") }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                if (isTablet) {
                    // ── Tablet: master-detail two-panel ─────────────────────
                    Row(Modifier.padding(padding).fillMaxSize()) {
                        // ── Left sidebar — compact session list ──────────────
                        Column(
                            modifier = Modifier
                                .width(248.dp)
                                .fillMaxHeight()
                        ) {
                            // Sidebar header: title + add button
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier         = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Primary),
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text(
                                        "Sesi Opname",
                                        style      = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier         = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick  = viewModel::openCreateDialog,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Buat opname baru",
                                            tint     = Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider()

                            // Filter chips — compact horizontal scroll
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    null       to "Semua",
                                    "draft"    to "Draft",
                                    "finalized" to "Final",
                                    "cancelled" to "Batal"
                                ).forEach { (value, label) ->
                                    FilterChip(
                                        selected = uiState.filterStatus == value,
                                        onClick  = { viewModel.setFilter(value) },
                                        label    = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            HorizontalDivider()

                            // Session list
                            Box(Modifier.weight(1f)) {
                                when {
                                    uiState.isLoading -> LoadingScreen()
                                    uiState.opnames.isEmpty() -> EmptyScreen(
                                        message  = "Belum ada sesi opname",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    else -> LazyColumn(
                                        contentPadding      = PaddingValues(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
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

                    // Modal dialog for creating a new session (tablet only)
                    if (uiState.showCreateDialog) {
                        CreateOpnameDialog(
                            isSubmitting = uiState.isSubmitting,
                            onDismiss    = viewModel::closeCreateDialog,
                            onConfirm    = { note -> viewModel.createOpname(note) }
                        )
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

