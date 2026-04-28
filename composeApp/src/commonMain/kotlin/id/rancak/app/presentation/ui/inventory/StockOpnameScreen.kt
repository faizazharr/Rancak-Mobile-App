package id.rancak.app.presentation.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.OpnameItemEntry
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
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
        OpnameDetailScreen(
            detail       = detail,
            products     = uiState.products,
            isSubmitting = uiState.isSubmitting,
            isLoading    = uiState.isLoadingDetail,
            showFinalizeConfirm = uiState.showFinalizeConfirm,
            snackbarHostState   = snackbarHostState,
            onBack               = viewModel::closeDetail,
            onSaveItems          = viewModel::saveItems,
            onFinalizeClick      = viewModel::openFinalizeConfirm,
            onFinalizeConfirm    = viewModel::finalizeOpname,
            onFinalizeDismiss    = viewModel::closeFinalizeConfirm
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
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null to "Semua", "draft" to "Draft", "finalized" to "Final", "cancelled" to "Dibatalkan").forEach { (value, label) ->
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
                        Icon(Icons.Default.Inventory, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text("Belum ada sesi opname", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.opnames, key = { it.uuid }) { opname ->
                        OpnameCard(opname, onOpen = { viewModel.loadDetail(opname.uuid) }, onCancel = { viewModel.cancelOpname(opname) })
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

@Composable
private fun OpnameCard(opname: StockOpname, onOpen: () -> Unit, onCancel: () -> Unit) {
    val statusColor = when (opname.status) {
        "finalized"  -> MaterialTheme.colorScheme.primary
        "cancelled"  -> MaterialTheme.colorScheme.error
        else         -> MaterialTheme.colorScheme.secondary
    }
    val statusLabel = when (opname.status) {
        "finalized"  -> "Final"
        "cancelled"  -> "Dibatalkan"
        else         -> "Draft"
    }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Opname #${opname.opnameNo}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text("${opname.itemCount} item · ${opname.createdAt.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!opname.note.isNullOrBlank()) {
                    Text(opname.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.12f)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (opname.status == "draft") {
                    TextButton(onClick = onOpen) { Text("Buka") }
                    TextButton(onClick = onCancel) { Text("Batalkan", color = MaterialTheme.colorScheme.error) }
                } else {
                    TextButton(onClick = onOpen) { Text("Lihat") }
                }
            }
        }
    }
}

@Composable
private fun CreateOpnameDialog(isSubmitting: Boolean, onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Buat Sesi Opname Baru") },
        text  = {
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Catatan (opsional)") },
                modifier = Modifier.fillMaxWidth(), maxLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(note.ifBlank { null }) }, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Buat")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Opname detail (input stok aktual)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OpnameDetailScreen(
    detail: StockOpnameDetail,
    products: List<id.rancak.app.domain.model.Product>,
    isSubmitting: Boolean,
    isLoading: Boolean,
    showFinalizeConfirm: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSaveItems: (List<OpnameItemEntry>) -> Unit,
    onFinalizeClick: () -> Unit,
    onFinalizeConfirm: () -> Unit,
    onFinalizeDismiss: () -> Unit
) {
    // Local mutable map: productUuid → actualStock text
    val stockInputs = remember(detail.opname.uuid) {
        mutableStateMapOf<String, String>().apply {
            detail.items.forEach { put(it.productUuid, it.actualStock.toString()) }
        }
    }
    val isDraft = detail.opname.status == "draft"

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Opname #${detail.opname.opnameNo}",
                icon     = Icons.Default.Inventory,
                onBack   = onBack,
                subtitle = "${detail.items.size} item · ${if (isDraft) "Draft" else "Final"}"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isDraft) {
                Surface(shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val entries = stockInputs.entries.mapNotNull { (uuid, text) ->
                                    text.toDoubleOrNull()?.let { OpnameItemEntry(uuid, it) }
                                }
                                onSaveItems(entries)
                            },
                            modifier = Modifier.weight(1f),
                            enabled  = !isSubmitting
                        ) { Text("Simpan") }
                        Button(
                            onClick  = onFinalizeClick,
                            modifier = Modifier.weight(1f),
                            enabled  = !isSubmitting && detail.items.isNotEmpty()
                        ) { Text("Finalisasi") }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            LoadingScreen(Modifier.padding(padding))
        } else {
            Column(Modifier.padding(padding).fillMaxSize()) {
                // Summary card
                if (!isDraft) {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            SummaryChip("Kurang", detail.shortageCount, MaterialTheme.colorScheme.error)
                            SummaryChip("Lebih", detail.surplusCount, MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (isDraft) {
                    // Product picker for adding items
                    ProductSearchSection(products, stockInputs, detail)
                }

                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(detail.items, key = { it.productUuid }) { item ->
                        OpnameItemCard(item, isDraft, stockInputs)
                    }
                }
            }
        }

        if (showFinalizeConfirm) {
            AlertDialog(
                onDismissRequest = { if (!isSubmitting) onFinalizeDismiss() },
                title = { Text("Finalisasi Opname") },
                text  = { Text("Stok sistem akan disesuaikan berdasarkan hasil hitung fisik. Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = {
                    Button(onClick = onFinalizeConfirm, enabled = !isSubmitting) {
                        if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Finalisasi")
                    }
                },
                dismissButton = { TextButton(onClick = onFinalizeDismiss, enabled = !isSubmitting) { Text("Batal") } }
            )
        }
    }
}

@Composable
private fun SummaryChip(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProductSearchSection(
    products: List<id.rancak.app.domain.model.Product>,
    stockInputs: MutableMap<String, String>,
    detail: StockOpnameDetail
) {
    var query by remember { mutableStateOf("") }
    val existing = detail.items.map { it.productUuid }.toSet()

    OutlinedTextField(
        value = query, onValueChange = { query = it },
        placeholder = { Text("Tambah produk ke opname…") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true
    )

    if (query.isNotBlank()) {
        val filtered = products.filter { it.name.contains(query, ignoreCase = true) && it.uuid !in existing }.take(5)
        filtered.forEach { product ->
            ListItem(
                headlineContent  = { Text(product.name) },
                supportingContent = { Text("Stok saat ini: ${product.stock}") },
                trailingContent  = {
                    IconButton(onClick = {
                        stockInputs[product.uuid] = product.stock.toString()
                        query = ""
                    }) { Icon(Icons.Default.Add, null) }
                }
            )
        }
    }
}

@Composable
private fun OpnameItemCard(
    item: id.rancak.app.domain.model.OpnameItem,
    isDraft: Boolean,
    stockInputs: MutableMap<String, String>
) {
    val diffColor = when {
        item.difference < 0 -> MaterialTheme.colorScheme.error
        item.difference > 0 -> MaterialTheme.colorScheme.primary
        else                 -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Sistem: ${item.systemStock}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!isDraft) {
                    Text("Aktual: ${item.actualStock}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Selisih: ${if (item.difference >= 0) "+${item.difference}" else "${item.difference}"}",
                        style = MaterialTheme.typography.labelSmall, color = diffColor, fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (isDraft) {
                OutlinedTextField(
                    value         = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                    onValueChange = { stockInputs[item.productUuid] = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = { Text("Aktual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier      = Modifier.width(100.dp),
                    singleLine    = true
                )
            }
        }
    }
}
