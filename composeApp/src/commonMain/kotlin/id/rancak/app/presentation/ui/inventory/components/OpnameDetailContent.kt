package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OpnameItem
import id.rancak.app.domain.model.OpnameItemEntry
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakColors
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (showFinalizeConfirm) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color    = MaterialTheme.colorScheme.errorContainer,
                                shape    = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning, null,
                                        tint     = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Stok sistem akan disesuaikan. Tindakan ini tidak dapat dibatalkan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick  = onFinalizeDismiss,
                                    modifier = Modifier.weight(1f),
                                    enabled  = !isSubmitting
                                ) { Text("Batal") }
                                Button(
                                    onClick  = onFinalizeConfirm,
                                    modifier = Modifier.weight(1f),
                                    enabled  = !isSubmitting,
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    if (isSubmitting)
                                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                    else Text("Finalisasi")
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
            }
        }
    ) { padding ->
        if (isLoading) {
            LoadingScreen(Modifier.padding(padding))
        } else {
            Column(Modifier.padding(padding).fillMaxSize()) {
                if (!isDraft) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ElevatedCard(
                            modifier = Modifier.weight(1f),
                            colors   = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    detail.shortageCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Kurang Stok",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        ElevatedCard(
                            modifier = Modifier.weight(1f),
                            colors   = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    detail.surplusCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Lebih Stok",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
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
                    contentPadding = PaddingValues(
                        start  = if (isDraft) 16.dp else 0.dp,
                        end    = if (isDraft) 16.dp else 0.dp,
                        top    = if (isDraft) 16.dp else 0.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = if (isDraft) Arrangement.spacedBy(8.dp) else Arrangement.Top
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
                        "${detail.items.size + pendingItems.size} item · $statusLabel · ${formatOpnameDate(detail.opname.createdAt)}",
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
                    Column(horizontalAlignment = Alignment.End) {
                        Button(
                            onClick  = onFinalizeClick,
                            enabled  = !isSubmitting && detail.items.isNotEmpty()
                        ) { Text("Finalisasi") }
                        if (detail.items.isEmpty()) {
                            Text(
                                "Simpan item lebih dulu",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
            // ── Compact summary row (finalized / cancelled only) ──────────
            if (!isDraft) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier        = Modifier.weight(1f),
                        shape           = MaterialTheme.shapes.large,
                        color           = MaterialTheme.colorScheme.errorContainer,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier              = Modifier.padding(16.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier         = Modifier.size(36.dp).background(
                                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.12f),
                                    MaterialTheme.shapes.medium
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TrendingDown, null,
                                    tint     = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    detail.shortageCount.toString(),
                                    style      = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Kurang Stok",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Surface(
                        modifier        = Modifier.weight(1f),
                        shape           = MaterialTheme.shapes.large,
                        color           = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier              = Modifier.padding(16.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier         = Modifier.size(36.dp).background(
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                    MaterialTheme.shapes.medium
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp, null,
                                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    detail.surplusCount.toString(),
                                    style      = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Lebih Stok",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
            }

            // ── Add product button (draft only) ───────────────────────────
            if (isDraft) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick        = { showPicker = true },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier       = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tambah Produk", style = MaterialTheme.typography.labelMedium)
                    }
                }
                HorizontalDivider()
            }

            // ── Table header ──────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(start = 16.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Produk",
                    modifier = Modifier.weight(1f),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Sistem",
                    modifier   = Modifier.width(68.dp),
                    textAlign  = TextAlign.End,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(10.dp))
                Row(
                    modifier              = Modifier.width(96.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Aktual",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (isDraft) {
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.Default.Edit, null,
                            tint     = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Selisih",
                    modifier  = Modifier.width(68.dp),
                    textAlign = TextAlign.End,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(32.dp))
            }
            HorizontalDivider()

            // ── Product table rows ────────────────────────────────────────
            val totalItems = detail.items.size + pendingItems.size
            if (isDraft && totalItems == 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier         = Modifier.size(64.dp).background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.extraLarge
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Inventory, null,
                                modifier = Modifier.size(32.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Belum ada produk",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ketuk \"+ Tambah Produk\" untuk mulai menghitung stok",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier       = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(detail.items, key = { _, item -> item.productUuid }) { index, item ->
                        OpnameTableRow(
                            item               = item,
                            isDraft            = isDraft,
                            rowIndex           = index,
                            stockInputValue    = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                            onStockInputChange = { stockInputs[item.productUuid] = it },
                            onDelete           = if (isDraft) ({ onDeleteItem(item.productUuid) }) else null
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                    itemsIndexed(pendingItems, key = { _, item -> "pending_${item.productUuid}" }) { index, item ->
                        OpnameTableRow(
                            item               = item,
                            isDraft            = true,
                            rowIndex           = detail.items.size + index,
                            stockInputValue    = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                            onStockInputChange = { stockInputs[item.productUuid] = it },
                            onDelete           = { stockInputs.remove(item.productUuid) }
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    if (showPicker) {
        ProductPickerSheet(
            products      = products,
            existingUuids = stockInputs.keys.toImmutableSet(),
            isTablet      = true,
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

// ── Compact table row — used only inside OpnameDetailTabletPanel ─────────────

internal fun formatStockValue(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

internal fun formatOpnameDate(isoDate: String): String {
    val parts = isoDate.take(10).split("-")
    if (parts.size < 3) return isoDate.take(10)
    val day   = parts[2].toIntOrNull() ?: return isoDate.take(10)
    val month = when (parts[1]) {
        "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
        "05" -> "Mei"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Agu"
        "09" -> "Sep"; "10" -> "Okt"; "11" -> "Nov"; "12" -> "Des"
        else -> parts[1]
    }
    return "$day $month ${parts[0]}"
}

@Composable
private fun OpnameTableRow(
    item: OpnameItem,
    isDraft: Boolean,
    stockInputValue: String,
    onStockInputChange: (String) -> Unit,
    onDelete: (() -> Unit)? = null,
    rowIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val sem      = RancakColors.semantic
    val diffColor = when {
        item.difference == 0.0 -> sem.success
        else                   -> sem.warning
    }
    val diffText = if (item.difference >= 0)
        "+${formatStockValue(item.difference)}"
    else
        formatStockValue(item.difference)

    val rowBg = if (rowIndex % 2 != 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else Color.Transparent
    Row(
        modifier          = modifier.fillMaxWidth().background(rowBg).padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product icon + name
        Row(
            modifier              = Modifier.weight(1f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier         = Modifier.size(28.dp).background(
                    MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Inventory, null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                item.productName,
                modifier   = Modifier.weight(1f),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
        }
        Text(
            formatStockValue(item.systemStock),
            modifier  = Modifier.width(68.dp),
            textAlign = TextAlign.End,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(10.dp))
        if (isDraft) {
            OutlinedTextField(
                value           = stockInputValue,
                onValueChange   = { onStockInputChange(it.filter { c -> c.isDigit() || c == '.' }) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier        = Modifier.width(96.dp).height(48.dp),
                singleLine      = true,
                textStyle       = MaterialTheme.typography.bodyMedium,
                shape           = MaterialTheme.shapes.medium
            )
        } else {
            Text(
                formatStockValue(item.actualStock),
                modifier  = Modifier.width(96.dp),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.width(10.dp))
        // Colored diff badge
        Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.CenterEnd) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = diffColor.copy(alpha = 0.12f)
            ) {
                Text(
                    diffText,
                    modifier   = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = diffColor
                )
            }
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete, null,
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Spacer(Modifier.width(32.dp))
        }
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
