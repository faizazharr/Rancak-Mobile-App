package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OpnameItem
import id.rancak.app.domain.model.OpnameItemEntry
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

@Composable
fun OpnameDetailContent(
    detail: StockOpnameDetail,
    products: ImmutableList<Product>,
    isSubmitting: Boolean,
    isLoading: Boolean,
    showFinalizeConfirm: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSaveItems: (List<OpnameItemEntry>) -> Unit,
    onFinalizeClick: () -> Unit,
    onFinalizeConfirm: () -> Unit,
    onFinalizeDismiss: () -> Unit,
    onDeleteItem: (String) -> Unit = {}
) {
    val stockInputs = remember(detail.opname.uuid) {
        mutableStateMapOf<String, String>().apply {
            detail.items.forEach { put(it.productUuid, it.actualStock.toString()) }
        }
    }
    val isDraft = detail.opname.status == "draft"
    var showPicker by remember { mutableStateOf(false) }
    val savedUuids = remember(detail.items) { detail.items.map { it.productUuid }.toSet() }
    val pendingItems by remember(stockInputs.keys.toSet(), savedUuids, products) {
        derivedStateOf {
            stockInputs.keys
                .filter { it !in savedUuids }
                .mapNotNull { uuid ->
                    products.find { it.uuid == uuid }?.let { p ->
                        val actual = stockInputs[uuid]?.toDoubleOrNull() ?: p.stock
                        OpnameItem(
                            productUuid = uuid,
                            productName = p.name,
                            systemStock = p.stock,
                            actualStock = actual,
                            difference  = actual - p.stock
                        )
                    }
                }
        }
    }

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                if (!isDraft) {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            SummaryChip("Kurang", detail.shortageCount, MaterialTheme.colorScheme.error)
                            SummaryChip("Lebih", detail.surplusCount, MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (isDraft) {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tambah Produk ke Opname")
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detail.items, key = { it.productUuid }) { item ->
                        OpnameItemCard(
                            item = item,
                            isDraft = isDraft,
                            stockInputValue = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                            onStockInputChange = { stockInputs[item.productUuid] = it },
                            onDelete = if (isDraft) ({ onDeleteItem(item.productUuid) }) else null
                        )
                    }
                    items(pendingItems, key = { "pending_${it.productUuid}" }) { item ->
                        OpnameItemCard(
                            item = item,
                            isDraft = true,
                            stockInputValue = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                            onStockInputChange = { stockInputs[item.productUuid] = it },
                            onDelete = { stockInputs.remove(item.productUuid) }
                        )
                    }
                }
            }
        }

        if (showPicker) {
            ProductPickerSheet(
                products = products,
                existingUuids = stockInputs.keys.toImmutableSet(),
                onConfirm = { entries ->
                    stockInputs.putAll(entries)
                    showPicker = false
                },
                onDismiss = { showPicker = false }
            )
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
                dismissButton = {
                    TextButton(onClick = onFinalizeDismiss, enabled = !isSubmitting) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
internal fun SummaryChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Tablet two-panel right panel ──────────────────────────────────────────────

@Composable
fun OpnameDetailTabletPanel(
    detail: StockOpnameDetail,
    products: ImmutableList<Product>,
    isSubmitting: Boolean,
    isLoading: Boolean,
    showFinalizeConfirm: Boolean,
    onClose: () -> Unit,
    onSaveItems: (List<OpnameItemEntry>) -> Unit,
    onFinalizeClick: () -> Unit,
    onFinalizeConfirm: () -> Unit,
    onFinalizeDismiss: () -> Unit,
    onDeleteItem: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stockInputs = remember(detail.opname.uuid) {
        mutableStateMapOf<String, String>().apply {
            detail.items.forEach { put(it.productUuid, it.actualStock.toString()) }
        }
    }
    val isDraft = detail.opname.status == "draft"
    var showPicker by remember { mutableStateOf(false) }
    val savedUuids = remember(detail.items) { detail.items.map { it.productUuid }.toSet() }
    val pendingItems by remember(stockInputs.keys.toSet(), savedUuids, products) {
        derivedStateOf {
            stockInputs.keys
                .filter { it !in savedUuids }
                .mapNotNull { uuid ->
                    products.find { it.uuid == uuid }?.let { p ->
                        val actual = stockInputs[uuid]?.toDoubleOrNull() ?: p.stock
                        OpnameItem(
                            productUuid = uuid,
                            productName = p.name,
                            systemStock = p.stock,
                            actualStock = actual,
                            difference  = actual - p.stock
                        )
                    }
                }
        }
    }

    val statusLabel = when (detail.opname.status) {
        "finalized" -> "Final"
        "cancelled" -> "Dibatalkan"
        else        -> "Draft"
    }

    Column(modifier = modifier) {
        // ── Header ────────────────────────────────────────────────────────────
        Surface(tonalElevation = 2.dp) {
            Row(
                modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Opname #${detail.opname.opnameNo}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${detail.items.size + pendingItems.size} item · $statusLabel · ${detail.opname.createdAt.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isDraft) {
                    OutlinedButton(
                        onClick = {
                            val entries = stockInputs.entries.mapNotNull { (uuid, text) ->
                                text.toDoubleOrNull()?.let { OpnameItemEntry(uuid, it) }
                            }
                            onSaveItems(entries)
                        },
                        enabled = !isSubmitting
                    ) { Text("Simpan") }
                    Button(
                        onClick  = onFinalizeClick,
                        enabled  = !isSubmitting && detail.items.isNotEmpty()
                    ) { Text("Finalisasi") }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup detail")
                }
            }
        }
        HorizontalDivider()

        if (isLoading) {
            LoadingScreen(Modifier.fillMaxSize())
        } else {
            // Summary chips (finalized/cancelled)
            if (!isDraft) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        SummaryChip("Kurang", detail.shortageCount, MaterialTheme.colorScheme.error)
                        SummaryChip("Lebih", detail.surplusCount, MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Tambah produk button (draft only)
            if (isDraft) {
                OutlinedButton(
                    onClick  = { showPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah Produk ke Opname")
                }
            }

            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                items(detail.items, key = { it.productUuid }) { item ->
                    OpnameItemCard(
                        item               = item,
                        isDraft            = isDraft,
                        stockInputValue    = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                        onStockInputChange = { stockInputs[item.productUuid] = it },
                        onDelete           = if (isDraft) ({ onDeleteItem(item.productUuid) }) else null
                    )
                }
                items(pendingItems, key = { "pending_${it.productUuid}" }) { item ->
                    OpnameItemCard(
                        item               = item,
                        isDraft            = true,
                        stockInputValue    = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                        onStockInputChange = { stockInputs[item.productUuid] = it },
                        onDelete           = { stockInputs.remove(item.productUuid) }
                    )
                }
            }
        }
    }

    if (showPicker) {
        ProductPickerSheet(
            products      = products,
            existingUuids = stockInputs.keys.toImmutableSet(),
            onConfirm     = { entries ->
                stockInputs.putAll(entries)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
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
            dismissButton = {
                TextButton(onClick = onFinalizeDismiss, enabled = !isSubmitting) { Text("Batal") }
            }
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun OpnameDetailContentPreview() {
    RancakTheme {
        OpnameDetailContent(
            detail = StockOpnameDetail(
                opname = StockOpname("1", "OP-001", "draft", "Opname bulanan", 2, createdAt = "2024-01-15"),
                items = listOf(
                    OpnameItem("p1", "Kopi Arabika", systemStock = 50.0, actualStock = 47.0, difference = -3.0)
                ),
                shortageCount = 1, surplusCount = 0
            ),
            products = persistentListOf(),
            isSubmitting = false, isLoading = false, showFinalizeConfirm = false,
            snackbarHostState = SnackbarHostState(),
            onBack = {}, onSaveItems = {}, onFinalizeClick = {}, onFinalizeConfirm = {}, onFinalizeDismiss = {}
        )
    }
}
