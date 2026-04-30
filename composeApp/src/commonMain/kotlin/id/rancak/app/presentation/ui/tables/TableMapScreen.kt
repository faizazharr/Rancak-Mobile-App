package id.rancak.app.presentation.ui.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.domain.model.UserRole
import id.rancak.app.domain.repository.UserSessionProvider
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.tables.components.AreaSummaryCard
import id.rancak.app.presentation.ui.tables.components.TableCell
import id.rancak.app.presentation.ui.tables.components.TableFormDialog
import id.rancak.app.presentation.ui.tables.components.TableSummaryCard
import id.rancak.app.presentation.viewmodel.TableUiState
import id.rancak.app.presentation.viewmodel.TableViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TableMapScreen(
    onBack: () -> Unit,
    onTableSelect: ((String) -> Unit)? = null,
    viewModel: TableViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canManage = true

    LaunchedEffect(Unit) { viewModel.loadTables() }

    TableMapScreenContent(
        uiState         = uiState,
        canManage       = canManage,
        onBack          = onBack,
        onRetry         = viewModel::loadTables,
        onTableSelect   = onTableSelect,
        onToggleAdmin   = { viewModel.setAdminMode(!uiState.adminMode) },
        onAddTable      = viewModel::openCreateDialog,
        onEditTable     = viewModel::openEditDialog,
        onRequestDelete = viewModel::requestDelete,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmSave   = viewModel::saveTable,
        onCancelDelete  = viewModel::cancelDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onConsumeMsg    = viewModel::consumeSnackbar
    )
}

/** Pure-UI content — tanpa ViewModel, aman di-preview. */
@Composable
fun TableMapScreenContent(
    uiState: TableUiState,
    canManage: Boolean = false,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onTableSelect: ((String) -> Unit)? = null,
    onToggleAdmin: () -> Unit = {},
    onAddTable: () -> Unit = {},
    onEditTable: (Table) -> Unit = {},
    onRequestDelete: (Table) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onConfirmSave: (name: String, area: String?, capacity: Int, isActive: Boolean, sortOrder: Int) -> Unit = { _, _, _, _, _ -> },
    onCancelDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onConsumeMsg: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onConsumeMsg()
        }
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Denah Meja",
                icon     = Icons.Default.TableBar,
                subtitle = if (uiState.adminMode) "Mode Kelola Meja" else "Manajemen meja",
                onMenu   = onBack,
                actions  = {
                    if (canManage) {
                        FilterChip(
                            selected = uiState.adminMode,
                            onClick  = onToggleAdmin,
                            label    = { Text(if (uiState.adminMode) "Selesai" else "Kelola") },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null,
                                    modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (canManage && uiState.adminMode) {
                ExtendedFloatingActionButton(
                    onClick = onAddTable,
                    icon    = { Icon(Icons.Default.Add, contentDescription = null) },
                    text    = { Text("Tambah Meja") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // ── Status legend strip ────────────────────────────────────────
            TableStatusLegend()

            // ── Admin mode banner ──────────────────────────────────────────
            if (uiState.adminMode) {
                Surface(
                    color    = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(
                            "Mode Kelola — Tap untuk edit, tahan untuk hapus",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp
            when {
                uiState.isLoading                                -> LoadingScreen()
                uiState.error != null                            -> ErrorScreen(uiState.error, onRetry = onRetry)
                uiState.tables.isEmpty() && !uiState.adminMode   -> EmptyScreen("Belum ada meja")
                uiState.tables.isEmpty()                          -> EmptyScreen("Belum ada meja — tap “Tambah Meja”")
                isTablet                                         -> TabletTableLayout(
                    uiState         = uiState,
                    onTableSelect   = onTableSelect,
                    onEditTable     = onEditTable,
                    onRequestDelete = onRequestDelete
                )
                else -> CompactTableLayout(
                    uiState         = uiState,
                    onTableSelect   = onTableSelect,
                    onEditTable     = onEditTable,
                    onRequestDelete = onRequestDelete
                )
            }
        }
        } // end Column
    }

    // ── Dialogs ────────────────────────────────────────────────────────────

    if (uiState.showFormDialog) {
        TableFormDialog(
            editingTable  = uiState.editingTable,
            isSubmitting  = uiState.isSubmitting,
            existingAreas = uiState.tables.mapNotNull { it.area }.distinct().sorted(),
            onDismiss     = onDismissDialog,
            onConfirm     = onConfirmSave
        )
    }

    uiState.pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Hapus meja?") },
            text  = { Text("Meja '${target.name}' akan dihapus. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    enabled = !uiState.isSubmitting,
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (uiState.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete, enabled = !uiState.isSubmitting) { Text("Batal") }
            }
        )
    }
}

// ── Layouts ────────────────────────────────────────────────────────────────

@Composable
private fun CompactTableLayout(
    uiState: TableUiState,
    onTableSelect: ((String) -> Unit)?,
    onEditTable: (Table) -> Unit,
    onRequestDelete: (Table) -> Unit
) {
    val areas = uiState.tables.groupBy { it.area ?: "Umum" }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(108.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        areas.forEach { (area, tables) ->
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                AreaSectionHeader(area, tables.size)
            }
            items(tables, key = { it.uuid }) { table ->
                AdminAwareTableCell(
                    table         = table,
                    adminMode     = uiState.adminMode,
                    onSelect      = { onTableSelect?.invoke(table.uuid) },
                    onEdit        = { onEditTable(table) },
                    onDelete      = { onRequestDelete(table) }
                )
            }
        }
    }
}

@Composable
private fun TabletTableLayout(
    uiState: TableUiState,
    onTableSelect: ((String) -> Unit)?,
    onEditTable: (Table) -> Unit,
    onRequestDelete: (Table) -> Unit
) {
    val available = uiState.tables.count { it.status == TableStatus.AVAILABLE }
    val occupied  = uiState.tables.count { it.status == TableStatus.OCCUPIED }
    val inactive  = uiState.tables.count { it.status == TableStatus.INACTIVE }
    val areas = uiState.tables.groupBy { it.area ?: "Umum" }

    Row(Modifier.fillMaxSize()) {
        // Kiri — grid meja
        LazyVerticalGrid(
            columns = GridCells.Adaptive(130.dp),
            modifier = Modifier.weight(0.65f).fillMaxHeight(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            areas.forEach { (area, tables) ->
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    AreaSectionHeader(area, tables.size)
                }
                items(tables, key = { it.uuid }) { table ->
                    AdminAwareTableCell(
                        table     = table,
                        adminMode = uiState.adminMode,
                        size      = 130.dp,
                        onSelect  = { onTableSelect?.invoke(table.uuid) },
                        onEdit    = { onEditTable(table) },
                        onDelete  = { onRequestDelete(table) }
                    )
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        // Kanan — statistik + legenda
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ringkasan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            TableSummaryCard(
                total     = uiState.tables.size,
                available = available,
                occupied  = occupied,
                inactive  = inactive
            )

            Text("Area", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            areas.forEach { (area, tables) ->
                val areaOccupied = tables.count { it.status == TableStatus.OCCUPIED }
                AreaSummaryCard(
                    area          = area,
                    totalCount    = tables.size,
                    occupiedCount = areaOccupied
                )
            }
        }
    }
}

/**
 * Wrapper di sekitar [TableCell] yang menambahkan perilaku admin:
 * - Tap → edit (admin) / select (kasir)
 * - Long-press → konfirmasi hapus (admin saja)
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun AdminAwareTableCell(
    table: Table,
    adminMode: Boolean,
    size: androidx.compose.ui.unit.Dp = 100.dp,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    if (!adminMode) {
        TableCell(table = table, size = size, onClick = onSelect)
        return
    }
    Box(
        modifier = Modifier
            .size(size)
            .combinedClickable(
                onClick     = onEdit,
                onLongClick = onDelete
            )
    ) {
        // Re-use existing visual but disable inner click — outer Box handles it.
        TableCell(table = table, size = size, enabled = false, onClick = {})
        // Indikator pojok kanan-atas: pensil edit
        Icon(
            imageVector        = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier           = Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(4.dp).size(14.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
    }
}

// ── Helper composables ─────────────────────────────────────────────────────

/** Legenda status di bawah top bar. */
@Composable
private fun TableStatusLegend() {
    val semantic = id.rancak.app.presentation.designsystem.RancakColors.semantic
    Surface(
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendDot("Tersedia",  semantic.statusAvailable)
            LegendDot("Dipakai",   semantic.statusOccupied)
            LegendDot("Nonaktif",  semantic.statusMaintenance)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Header setiap area dengan jumlah meja. */
@Composable
private fun AreaSectionHeader(area: String, count: Int) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = androidx.compose.ui.Modifier.padding(vertical = 6.dp)
    ) {
        Text(
            area,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = androidx.compose.ui.Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

// ── Previews ───────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TableCellAvailablePreview() {
    RancakTheme {
        TableCell(
            table = Table(uuid = "1", name = "A1", area = "Indoor", capacity = 4, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 1, activeSaleUuid = null),
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun TableCellOccupiedPreview() {
    RancakTheme {
        TableCell(
            table = Table(uuid = "2", name = "B3", area = "Outdoor", capacity = 6, status = TableStatus.OCCUPIED, isActive = true, sortOrder = 2, activeSaleUuid = "sale-1"),
            onClick = {}
        )
    }
}

@Preview(name = "Table Map – Full Screen", widthDp = 600, heightDp = 800)
@Composable
private fun TableMapScreenPreview() {
    val tables = listOf(
        Table(uuid = "t1", name = "Meja 1", area = "Indoor", capacity = 4, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 1, activeSaleUuid = null),
        Table(uuid = "t2", name = "Meja 2", area = "Indoor", capacity = 2, status = TableStatus.OCCUPIED,  isActive = true, sortOrder = 2, activeSaleUuid = "sale-1"),
        Table(uuid = "t3", name = "Meja 3", area = "Indoor", capacity = 4, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 3, activeSaleUuid = null),
        Table(uuid = "t4", name = "Meja 4", area = "Outdoor",capacity = 6, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 4, activeSaleUuid = null),
        Table(uuid = "t5", name = "Meja 5", area = "Outdoor",capacity = 2, status = TableStatus.INACTIVE,  isActive = false, sortOrder = 5, activeSaleUuid = null)
    )
    RancakTheme {
        TableMapScreenContent(
            uiState = TableUiState(tables = tables),
            onBack  = {},
            onRetry = {}
        )
    }
}
