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
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedButton
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
import id.rancak.app.domain.model.Supplier
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
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
        uiState             = uiState,
        onBack              = onBack,
        onAdd               = viewModel::openCreateDialog,
        onSelectOrder       = viewModel::selectOrder,
        onCloseDetail       = viewModel::closeDetail,
        onSend              = viewModel::sendOrder,
        onCancelClick       = viewModel::openCancelDialog,
        onConfirmCancel     = viewModel::cancelOrder,
        onCloseCancel       = viewModel::closeCancelDialog,
        onStatusFilter      = viewModel::setStatusFilter,
        onCreateOrder       = viewModel::createPurchaseOrder,
        onCloseCreate       = viewModel::closeCreateDialog,
        onSupplierChange    = viewModel::onFormSupplierChange,
        onOrderDateChange   = viewModel::onFormOrderDateChange,
        onExpectedDateChange = viewModel::onFormExpectedDateChange,
        onNotesChange       = viewModel::onFormNotesChange,
        snackbarHostState   = snackbarHostState
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
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
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Buat PO baru")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp

            // HP: form buat PO menggantikan seluruh screen
            if (!isTablet && uiState.showCreateDialog) {
                CreatePOFormContent(
                    uiState              = uiState,
                    onCreate             = onCreateOrder,
                    onDismiss            = onCloseCreate,
                    onSupplierChange     = onSupplierChange,
                    onOrderDateChange    = onOrderDateChange,
                    onExpectedDateChange = onExpectedDateChange,
                    onNotesChange        = onNotesChange,
                    fullScreen           = true
                )
                return@BoxWithConstraints
            }

            if (isTablet) {
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
                    onNotesChange        = onNotesChange
                )
            } else {
                PhonePOLayout(
                    uiState       = uiState,
                    onSelectOrder = onSelectOrder,
                    onCloseDetail = onCloseDetail,
                    onSend        = onSend,
                    onCancelClick = onCancelClick,
                    onFilter      = onStatusFilter
                )
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
    onNotesChange: (String) -> Unit
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

        // Right panel: form buat PO atau detail PO
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
                    onCancelClick = onCancelClick
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
    onFilter: (String?) -> Unit
) {
    val detail = uiState.selectedOrder
    if (detail != null && !uiState.isLoadingDetail) {
        PODetailContent(
            po            = detail,
            onSend        = onSend,
            onCancelClick = onCancelClick,
            onBack        = onCloseDetail
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
        elevation  = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        colors     = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier   = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(po.poNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
    onBack: (() -> Unit)? = null
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        onBack?.let {
            Row(Modifier.padding(8.dp)) {
                TextButton(onClick = it) { Text("← Kembali") }
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(po.poNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                POStatusChip(po.status)
            }

            po.supplierName?.let {
                Text("Supplier: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text("Tanggal: ${po.orderDate}", style = MaterialTheme.typography.bodySmall)
            po.expectedDate?.let { Text("Estimasi terima: $it", style = MaterialTheme.typography.bodySmall) }
            po.notes?.let {
                Text("Catatan: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider()
            Text("Item", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            po.items.forEach { item ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${item.qtyOrdered} × ${formatRupiah(item.unitCost.toLong())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.qtyReceived > 0) {
                            Text("Diterima: ${item.qtyReceived}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(formatRupiah(item.subtotal.toLong()), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (po.taxAmount > 0) Text("Pajak: ${formatRupiah(po.taxAmount.toLong())}", style = MaterialTheme.typography.bodySmall)
                    if (po.shippingCost > 0) Text("Ongkos kirim: ${formatRupiah(po.shippingCost.toLong())}", style = MaterialTheme.typography.bodySmall)
                    Text("Total: ${formatRupiah(po.total.toLong())}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Action buttons based on status
            if (po.status == "draft") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick  = { onSend(po.uuid) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Text("Kirim ke Supplier", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            if (po.status in listOf("draft", "ordered")) {
                OutlinedButton(
                    onClick  = onCancelClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Text("Batalkan PO", modifier = Modifier.padding(start = 8.dp), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun POStatusChip(status: String) {
    val label = statusLabels[status] ?: status
    val color = when (status) {
        "draft"     -> MaterialTheme.colorScheme.secondary
        "ordered"   -> MaterialTheme.colorScheme.primary
        "partial"   -> MaterialTheme.colorScheme.tertiary
        "received"  -> MaterialTheme.colorScheme.primary
        "cancelled" -> MaterialTheme.colorScheme.error
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    AssistChip(
        onClick = {},
        label   = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors  = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.12f), labelColor = color)
    )
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
    fullScreen: Boolean
) {
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
                Text(
                    "Buat Purchase Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Row(
                modifier              = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Buat Purchase Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
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
            // Supplier dropdown
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

            OutlinedTextField(
                value         = uiState.formOrderDate,
                onValueChange = onOrderDateChange,
                label         = { Text("Tanggal Order (YYYY-MM-DD)") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formExpectedDate,
                onValueChange = onExpectedDateChange,
                label         = { Text("Estimasi Terima (YYYY-MM-DD)") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formNotes,
                onValueChange = onNotesChange,
                label         = { Text("Catatan") },
                minLines      = 2,
                modifier      = Modifier.fillMaxWidth()
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
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.weight(1f)
            ) { Text("Batal") }
            Button(
                onClick  = onCreate,
                enabled  = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            ) { Text("Buat PO") }
        }
    }
}
