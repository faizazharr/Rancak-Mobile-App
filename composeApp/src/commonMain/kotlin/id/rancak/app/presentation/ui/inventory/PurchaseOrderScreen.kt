package id.rancak.app.presentation.ui.inventory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.PurchaseOrder
import id.rancak.app.domain.model.PurchaseOrderItem
import id.rancak.app.domain.model.Supplier
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakOutlinedButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.components.StatusChip
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.PurchaseOrderUiState
import id.rancak.app.presentation.viewmodel.PurchaseOrderViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val statusLabels = mapOf(
    "draft"    to "Draft",
    "ordered"  to "Dikirim",
    "partial"  to "Sebagian",
    "received" to "Diterima",
    "cancelled" to "Dibatalkan"
)

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun PurchaseOrderScreen(
    onBack: () -> Unit,
    viewModel: PurchaseOrderViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearSuccessMessage()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar("⚠ $it") }
            viewModel.clearError()
        }
    }

    PurchaseOrderContent(
        uiState              = uiState,
        onBack               = onBack,
        onAdd                = viewModel::openCreateDialog,
        onSelectOrder        = viewModel::selectOrder,
        onCloseDetail        = viewModel::closeDetail,
        onSend               = viewModel::sendOrder,
        onCancelClick        = viewModel::openCancelDialog,
        onConfirmCancel      = viewModel::cancelOrder,
        onCloseCancel        = viewModel::closeCancelDialog,
        onStatusFilter       = viewModel::setStatusFilter,
        onCreateOrder        = viewModel::createPurchaseOrder,
        onCloseCreate        = viewModel::closeCreateDialog,
        onSupplierChange     = viewModel::onFormSupplierChange,
        onOrderDateChange    = viewModel::onFormOrderDateChange,
        onExpectedDateChange = viewModel::onFormExpectedDateChange,
        onNotesChange        = viewModel::onFormNotesChange,
        // Edit header
        onEditHeader         = viewModel::openEditHeaderDialog,
        onCloseEditHeader    = viewModel::closeEditHeaderDialog,
        onSaveHeader         = viewModel::updatePOHeader,
        // Item CRUD
        onOpenAddItem        = viewModel::openAddItemDialog,
        onCloseAddItem       = viewModel::closeAddItemDialog,
        onItemProductChange  = viewModel::onItemProductChange,
        onItemQtyChange      = viewModel::onItemQtyChange,
        onItemUnitCostChange = viewModel::onItemUnitCostChange,
        onItemNotesChange    = viewModel::onItemNotesChange,
        onAddItem            = viewModel::addItem,
        onOpenEditItem       = viewModel::openEditItemDialog,
        onCloseEditItem      = viewModel::closeEditItemDialog,
        onUpdateItem         = viewModel::updateItem,
        onDeleteItem         = viewModel::deleteItem,
        // Receive
        onOpenReceive        = viewModel::openReceiveDialog,
        onCloseReceive       = viewModel::closeReceiveDialog,
        onReceiveQtyChange   = viewModel::onReceiveQtyChange,
        onReceiveNotesChange = viewModel::onReceiveNotesChange,
        onConfirmReceive     = viewModel::receiveOrder,
        snackbarHostState    = snackbarHostState
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Content (pure UI)
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOrderContent(
    uiState: PurchaseOrderUiState,
    onBack: () -> Unit = {},
    onAdd: () -> Unit = {},
    onSelectOrder: (PurchaseOrder) -> Unit = {},
    onCloseDetail: () -> Unit = {},
    onSend: (String) -> Unit = {},
    onCancelClick: () -> Unit = {},
    onConfirmCancel: () -> Unit = {},
    onCloseCancel: () -> Unit = {},
    onStatusFilter: (String?) -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onCloseCreate: () -> Unit = {},
    onSupplierChange: (String?) -> Unit = {},
    onOrderDateChange: (String) -> Unit = {},
    onExpectedDateChange: (String) -> Unit = {},
    onNotesChange: (String) -> Unit = {},
    // Edit header
    onEditHeader: () -> Unit = {},
    onCloseEditHeader: () -> Unit = {},
    onSaveHeader: () -> Unit = {},
    // Item CRUD
    onOpenAddItem: () -> Unit = {},
    onCloseAddItem: () -> Unit = {},
    onItemProductChange: (String) -> Unit = {},
    onItemQtyChange: (String) -> Unit = {},
    onItemUnitCostChange: (String) -> Unit = {},
    onItemNotesChange: (String) -> Unit = {},
    onAddItem: () -> Unit = {},
    onOpenEditItem: (id.rancak.app.domain.model.PurchaseOrderItem) -> Unit = {},
    onCloseEditItem: () -> Unit = {},
    onUpdateItem: () -> Unit = {},
    onDeleteItem: (id.rancak.app.domain.model.PurchaseOrderItem) -> Unit = {},
    // Receive
    onOpenReceive: () -> Unit = {},
    onCloseReceive: () -> Unit = {},
    onReceiveQtyChange: (String, String) -> Unit = { _, _ -> },
    onReceiveNotesChange: (String) -> Unit = {},
    onConfirmReceive: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (uiState.showCancelDialog) {
        AlertDialog(
            onDismissRequest = onCloseCancel,
            title  = { Text("Batalkan PO") },
            text   = { Text("Yakin ingin membatalkan purchase order ini? Tindakan tidak dapat diurungkan.") },
            confirmButton = {
                TextButton(onClick = onConfirmCancel) {
                    Text("Batalkan PO", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onCloseCancel) { Text("Tutup") } }
        )
    }

    if (uiState.showAddItemDialog) {
                           AddPOItemDialog(
            uiState             = uiState,
            onDismiss           = onCloseAddItem,
            onConfirm           = onAddItem,
            onProductChange     = onItemProductChange,
            onQtyChange         = onItemQtyChange,
            onUnitCostChange    = onItemUnitCostChange,
            onItemNotesChange   = onItemNotesChange
        )
    }

    if (uiState.showEditItemDialog) {
        EditPOItemDialog(
            uiState          = uiState,
            onDismiss        = onCloseEditItem,
            onConfirm        = onUpdateItem,
            onQtyChange      = onItemQtyChange,
            onUnitCostChange = onItemUnitCostChange,
            onNotesChange    = onItemNotesChange
        )
    }

    if (uiState.showReceiveDialog) {
        ReceivePODialog(
            uiState          = uiState,
            onDismiss        = onCloseReceive,
            onConfirm        = onConfirmReceive,
            onQtyChange      = onReceiveQtyChange,
            onNotesChange    = onReceiveNotesChange
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                RancakTopBar(
                    title    = "Purchase Order",
                    icon     = Icons.Default.ShoppingCart,
                    subtitle = "${uiState.orders.size} PO",
                    onMenu   = onBack
                )
            },
            floatingActionButton = {
                if (!isTablet) {
                    FloatingActionButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Buat PO baru")
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                // Phone full-screen: Create PO
                if (!isTablet && uiState.showCreateDialog) {
                    CreatePOFormContent(
                        uiState              = uiState,
                        onCreate             = onCreateOrder,
                        onDismiss            = onCloseCreate,
                        onSupplierChange     = onSupplierChange,
                        onOrderDateChange    = onOrderDateChange,
                        onExpectedDateChange = onExpectedDateChange,
                        onNotesChange        = onNotesChange,
                        isEdit               = false,
                        fullScreen           = true
                    )
                // Phone full-screen: Edit PO header
                } else if (!isTablet && uiState.showEditHeaderDialog) {
                    CreatePOFormContent(
                        uiState              = uiState,
                        onCreate             = onSaveHeader,
                        onDismiss            = onCloseEditHeader,
                        onSupplierChange     = onSupplierChange,
                        onOrderDateChange    = onOrderDateChange,
                        onExpectedDateChange = onExpectedDateChange,
                        onNotesChange        = onNotesChange,
                        isEdit               = true,
                        fullScreen           = true
                    )
                } else if (isTablet) {
                    TabletPOLayout(
                        uiState              = uiState,
                        onSelectOrder        = onSelectOrder,
                        onCloseDetail        = onCloseDetail,
                        onSend               = onSend,
                        onCancelClick        = onCancelClick,
                        onFilter             = onStatusFilter,
                        onCreateOrder        = onCreateOrder,
                        onCloseCreate        = onCloseCreate,
                        onSupplierChange     = onSupplierChange,
                        onOrderDateChange    = onOrderDateChange,
                        onExpectedDateChange = onExpectedDateChange,
                        onNotesChange        = onNotesChange,
                        onEditHeader         = onEditHeader,
                        onCloseEditHeader    = onCloseEditHeader,
                        onSaveHeader         = onSaveHeader,
                        onOpenAddItem        = onOpenAddItem,
                        onOpenEditItem       = onOpenEditItem,
                        onDeleteItem         = onDeleteItem,
                        onOpenReceive        = onOpenReceive
                    )
                } else {
                    PhonePOLayout(
                        uiState        = uiState,
                        onSelectOrder  = onSelectOrder,
                        onCloseDetail  = onCloseDetail,
                        onSend         = onSend,
                        onCancelClick  = onCancelClick,
                        onFilter       = onStatusFilter,
                        onEditHeader   = onEditHeader,
                        onOpenAddItem  = onOpenAddItem,
                        onOpenEditItem = onOpenEditItem,
                        onDeleteItem   = onDeleteItem,
                        onOpenReceive  = onOpenReceive
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletPOLayout(
    uiState: PurchaseOrderUiState,
    onSelectOrder: (PurchaseOrder) -> Unit,
    onCloseDetail: () -> Unit,
    onSend: (String) -> Unit,
    onCancelClick: () -> Unit,
    onFilter: (String?) -> Unit,
    onCreateOrder: () -> Unit,
    onCloseCreate: () -> Unit,
    onSupplierChange: (String?) -> Unit,
    onOrderDateChange: (String) -> Unit,
    onExpectedDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onEditHeader: () -> Unit,
    onCloseEditHeader: () -> Unit,
    onSaveHeader: () -> Unit,
    onOpenAddItem: () -> Unit,
    onOpenEditItem: (id.rancak.app.domain.model.PurchaseOrderItem) -> Unit,
    onDeleteItem: (id.rancak.app.domain.model.PurchaseOrderItem) -> Unit,
    onOpenReceive: () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        // List panel
        Column(Modifier.weight(0.4f).fillMaxHeight()) {
            StatusFilterRow(uiState.statusFilter, onFilter)
            HorizontalDivider()
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.orders.isEmpty() -> EmptyScreen("Tidak ada PO")
                else -> LazyColumn(
                    contentPadding      = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.orders) { po ->
                        POListItem(
                            po         = po,
                            isSelected = uiState.selectedOrder?.uuid == po.uuid,
                            onClick    = { onSelectOrder(po) }
                        )
                    }
                }
            }
        }

        VerticalDivider()

        // Right panel: form buat PO, edit header, atau detail PO
        Box(Modifier.weight(0.6f).fillMaxHeight()) {
            when {
                uiState.showCreateDialog -> CreatePOFormContent(
                    uiState              = uiState,
                    onCreate             = onCreateOrder,
                    onDismiss            = onCloseCreate,
                    onSupplierChange     = onSupplierChange,
                    onOrderDateChange    = onOrderDateChange,
                    onExpectedDateChange = onExpectedDateChange,
                    onNotesChange        = onNotesChange,
                    isEdit               = false,
                    fullScreen           = false
                )
                uiState.showEditHeaderDialog -> CreatePOFormContent(
                    uiState              = uiState,
                    onCreate             = onSaveHeader,
                    onDismiss            = onCloseEditHeader,
                    onSupplierChange     = onSupplierChange,
                    onOrderDateChange    = onOrderDateChange,
                    onExpectedDateChange = onExpectedDateChange,
                    onNotesChange        = onNotesChange,
                    isEdit               = true,
                    fullScreen           = false
                )
                uiState.selectedOrder == null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Pilih PO untuk melihat detail",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                uiState.isLoadingDetail -> LoadingScreen()
                else -> PODetailContent(
                    po            = uiState.selectedOrder,
                    onSend        = onSend,
                    onCancelClick = onCancelClick,
                    onEditHeader  = onEditHeader,
                    onOpenAddItem = onOpenAddItem,
                    onEditItem    = onOpenEditItem,
                    onDeleteItem  = onDeleteItem,
                    onOpenReceive = onOpenReceive
                )
            }
        }
    }
}

@Composable
private fun PhonePOLayout(
    uiState: PurchaseOrderUiState,
    onSelectOrder: (PurchaseOrder) -> Unit,
    onCloseDetail: () -> Unit,
    onSend: (String) -> Unit,
    onCancelClick: () -> Unit,
    onFilter: (String?) -> Unit,
    onEditHeader: () -> Unit,
    onOpenAddItem: () -> Unit,
    onOpenEditItem: (id.rancak.app.domain.model.PurchaseOrderItem) -> Unit,
    onDeleteItem: (id.rancak.app.domain.model.PurchaseOrderItem) -> Unit,
    onOpenReceive: () -> Unit
) {
    val detail = uiState.selectedOrder
    if (detail != null && !uiState.isLoadingDetail) {
        PODetailContent(
            po            = detail,
            onSend        = onSend,
            onCancelClick = onCancelClick,
            onBack        = onCloseDetail,
            onEditHeader  = onEditHeader,
            onOpenAddItem = onOpenAddItem,
            onEditItem    = onOpenEditItem,
            onDeleteItem  = onDeleteItem,
            onOpenReceive = onOpenReceive
        )
    } else if (uiState.isLoadingDetail) {
        LoadingScreen()
    } else {
        Column(Modifier.fillMaxSize()) {
            StatusFilterRow(uiState.statusFilter, onFilter)
            HorizontalDivider()
            when {
                uiState.isLoading -> LoadingScreen(Modifier.weight(1f))
                uiState.orders.isEmpty() -> EmptyScreen("Tidak ada PO", modifier = Modifier.weight(1f))
                else -> LazyColumn(
                    modifier            = Modifier.weight(1f),
                    contentPadding      = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.orders) { po ->
                        POListItem(po = po, isSelected = false, onClick = { onSelectOrder(po) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(selected: String?, onFilter: (String?) -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick  = { onFilter(null) },
            label    = { Text("Semua") }
        )
        statusLabels.forEach { (key, label) ->
            FilterChip(
                selected = selected == key,
                onClick  = { onFilter(key) },
                label    = { Text(label) }
            )
        }
    }
}

@Composable
private fun POListItem(po: PurchaseOrder, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick    = onClick,
        elevation  = CardDefaults.cardElevation(if (isSelected) 2.dp else 1.dp),
        colors     = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape      = MaterialTheme.shapes.medium,
        modifier   = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(po.poNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                POStatusChip(po.status)
            }
            po.supplierName?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                formatRupiah(po.total.toLong()),
                style  = MaterialTheme.typography.bodySmall,
                color  = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(po.orderDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PODetailContent(
    po: PurchaseOrder,
    onSend: (String) -> Unit,
    onCancelClick: () -> Unit,
    onEditHeader: () -> Unit = {},
    onOpenAddItem: () -> Unit = {},
    onEditItem: (PurchaseOrderItem) -> Unit = {},
    onDeleteItem: (PurchaseOrderItem) -> Unit = {},
    onOpenReceive: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        onBack?.let {
            Row(Modifier.padding(8.dp)) {
                TextButton(onClick = it) { Text("← Kembali") }
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(po.poNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    po.supplierName?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    POStatusChip(po.status)
                    if (po.status == "draft") {
                        IconButton(onClick = onEditHeader) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit PO", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Text("Tanggal: ${po.orderDate}", style = MaterialTheme.typography.bodySmall)
            po.expectedDate?.let { Text("Estimasi terima: $it", style = MaterialTheme.typography.bodySmall) }
            po.notes?.let {
                Text("Catatan: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider()

            // ── Items ─────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Item", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (po.status == "draft") {
                    TextButton(onClick = onOpenAddItem) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Tambah Item")
                    }
                }
            }

            if (po.items.isEmpty()) {
                Text(
                    "Belum ada item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            po.items.forEach { item ->
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${formatQty(item.qtyOrdered)} × ${formatRupiah(item.unitCost.toLong())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.qtyReceived > 0) {
                            Text(
                                "Diterima: ${formatQty(item.qtyReceived)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        formatRupiah(item.subtotal.toLong()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (po.status == "draft") {
                        IconButton(onClick = { onEditItem(item) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit item", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onDeleteItem(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus item", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (po.taxAmount > 0) Text("Pajak: ${formatRupiah(po.taxAmount.toLong())}", style = MaterialTheme.typography.bodySmall)
                    if (po.shippingCost > 0) Text("Ongkos kirim: ${formatRupiah(po.shippingCost.toLong())}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Total: ${formatRupiah(po.total.toLong())}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Action buttons ────────────────────────────────────────────────
            if (po.status == "draft" && po.items.isNotEmpty()) {
                RancakButton(
                    text     = "Kirim ke Supplier",
                    onClick  = { onSend(po.uuid) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (po.status in listOf("ordered", "partial")) {
                RancakButton(
                    text     = "Terima Barang",
                    onClick  = onOpenReceive,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (po.status in listOf("draft", "ordered")) {
                RancakOutlinedButton(
                    text     = "Batalkan PO",
                    onClick  = onCancelClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun POStatusChip(status: String) {
    val label = statusLabels[status] ?: status
    val sem   = RancakColors.semantic
    val color = when (status) {
        "draft"     -> sem.warning
        "ordered"   -> MaterialTheme.colorScheme.primary
        "partial"   -> sem.info
        "received"  -> sem.success
        "cancelled" -> MaterialTheme.colorScheme.error
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    StatusChip(text = label, color = color)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePOFormContent(
    uiState: PurchaseOrderUiState,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
    onSupplierChange: (String?) -> Unit,
    onOrderDateChange: (String) -> Unit,
    onExpectedDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    isEdit: Boolean,
    fullScreen: Boolean
) {
    val title  = if (isEdit) "Edit Purchase Order" else "Buat Purchase Order"
    val btnLabel = if (isEdit) "Simpan Perubahan" else "Buat PO"
    var supplierExpanded by remember { mutableStateOf(false) }
    val selectedSupplierName = uiState.suppliers.find { it.uuid == uiState.formSupplierUuid }?.name
        ?: "Pilih Supplier (opsional)"

    Column(Modifier.fillMaxSize()) {
        // ── Header ────────────────────────────────────────────────────────────
        if (fullScreen) {
            Row(
                modifier          = Modifier.fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(
                modifier              = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        }
        HorizontalDivider()

        // ── Form fields ───────────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded         = supplierExpanded,
                onExpandedChange = { supplierExpanded = it }
            ) {
                OutlinedTextField(
                    value         = selectedSupplierName,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Supplier") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(supplierExpanded) },
                    shape         = MaterialTheme.shapes.medium,
                    modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded         = supplierExpanded,
                    onDismissRequest = { supplierExpanded = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text("Tanpa Supplier") },
                        onClick = { onSupplierChange(null); supplierExpanded = false }
                    )
                    uiState.suppliers.forEach { s ->
                        DropdownMenuItem(
                            text    = { Text(s.name) },
                            onClick = { onSupplierChange(s.uuid); supplierExpanded = false }
                        )
                    }
                }
            }

            RancakTextField(
                value         = uiState.formOrderDate,
                onValueChange = onOrderDateChange,
                label         = "Tanggal Order (YYYY-MM-DD)",
                singleLine    = true
            )
            RancakTextField(
                value         = uiState.formExpectedDate,
                onValueChange = onExpectedDateChange,
                label         = "Estimasi Terima (YYYY-MM-DD)",
                singleLine    = true
            )
            RancakTextField(
                value         = uiState.formNotes,
                onValueChange = onNotesChange,
                label         = "Catatan",
                singleLine    = false,
                minLines      = 2
            )
        }

        // ── Bottom actions ────────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RancakOutlinedButton(
                text     = "Batal",
                onClick  = onDismiss,
                modifier = Modifier.weight(1f)
            )
            RancakButton(
                text      = btnLabel,
                onClick   = onCreate,
                enabled   = !uiState.isSaving,
                isLoading = uiState.isSaving,
                modifier  = Modifier.weight(1f)
            )
        }
    }
}

// ── Add PO Item Dialog ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPOItemDialog(
    uiState: PurchaseOrderUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onProductChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onUnitCostChange: (String) -> Unit,
    onItemNotesChange: (String) -> Unit
) {
    var productExpanded by remember { mutableStateOf(false) }
    val selectedProductName = uiState.products.find { it.uuid == uiState.formItemProductUuid }?.name
        ?: "Pilih Produk"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded         = productExpanded,
                    onExpandedChange = { productExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = selectedProductName,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Produk") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(productExpanded) },
                        shape         = MaterialTheme.shapes.medium,
                        modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded         = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        uiState.products.forEach { p ->
                            DropdownMenuItem(
                                text    = { Text(p.name) },
                                onClick = { onProductChange(p.uuid); productExpanded = false }
                            )
                        }
                    }
                }
                RancakTextField(
                    value         = uiState.formItemQty,
                    onValueChange = onQtyChange,
                    label         = "Jumlah",
                    singleLine    = true
                )
                RancakTextField(
                    value         = uiState.formItemUnitCost,
                    onValueChange = onUnitCostChange,
                    label         = "Harga Satuan (Rp)",
                    singleLine    = true
                )
                RancakTextField(
                    value         = uiState.formItemNotes,
                    onValueChange = onItemNotesChange,
                    label         = "Catatan (opsional)",
                    singleLine    = true
                )
            }
        },
        confirmButton = {
            RancakButton(
                text      = "Tambah",
                onClick   = onConfirm,
                enabled   = !uiState.isSaving && uiState.formItemProductUuid.isNotBlank() && uiState.formItemQty.isNotBlank(),
                isLoading = uiState.isSaving
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ── Edit PO Item Dialog ───────────────────────────────────────────────────────

@Composable
private fun EditPOItemDialog(
    uiState: PurchaseOrderUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onQtyChange: (String) -> Unit,
    onUnitCostChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item: ${uiState.editingItem?.productName.orEmpty()}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RancakTextField(
                    value         = uiState.formItemQty,
                    onValueChange = onQtyChange,
                    label         = "Jumlah",
                    singleLine    = true
                )
                RancakTextField(
                    value         = uiState.formItemUnitCost,
                    onValueChange = onUnitCostChange,
                    label         = "Harga Satuan (Rp)",
                    singleLine    = true
                )
                RancakTextField(
                    value         = uiState.formItemNotes,
                    onValueChange = onNotesChange,
                    label         = "Catatan (opsional)",
                    singleLine    = true
                )
            }
        },
        confirmButton = {
            RancakButton(
                text      = "Simpan",
                onClick   = onConfirm,
                enabled   = !uiState.isSaving && uiState.formItemQty.isNotBlank(),
                isLoading = uiState.isSaving
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ── Receive PO Dialog ─────────────────────────────────────────────────────────

@Composable
private fun ReceivePODialog(
    uiState: PurchaseOrderUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onQtyChange: (String, String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    val items = uiState.selectedOrder?.items ?: emptyList()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terima Barang") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (items.isEmpty()) {
                    Text("Tidak ada item untuk diterima.", style = MaterialTheme.typography.bodySmall)
                } else {
                    items.forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                "Dipesan: ${formatQty(item.qtyOrdered)} | Sudah diterima: ${formatQty(item.qtyReceived)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            RancakTextField(
                                value         = uiState.receiveEntries[item.uuid] ?: "",
                                onValueChange = { onQtyChange(item.uuid, it) },
                                label         = "Qty diterima sekarang",
                                singleLine    = true
                            )
                        }
                    }
                }
                RancakTextField(
                    value         = uiState.formReceiveNotes,
                    onValueChange = onNotesChange,
                    label         = "Catatan penerimaan (opsional)",
                    singleLine    = true
                )
            }
        },
        confirmButton = {
            RancakButton(
                text      = "Konfirmasi",
                onClick   = onConfirm,
                enabled   = !uiState.isSaving,
                isLoading = uiState.isSaving
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun formatQty(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else qty.toString()

