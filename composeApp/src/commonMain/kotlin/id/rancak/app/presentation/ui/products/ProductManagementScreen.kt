package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.ProductManagementUiState
import id.rancak.app.presentation.viewmodel.ProductManagementViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductManagementScreen(
    onBack: () -> Unit,
    viewModel: ProductManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadAll() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar("⚠ $msg") }
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Manajemen Produk",
                icon = Icons.Default.Inventory2,
                onBack = onBack,
                subtitle = "${uiState.filteredProducts.size} produk"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            else -> ProductListContent(
                uiState          = uiState,
                onSearchChange   = viewModel::setSearchQuery,
                onCategorySelect = viewModel::setCategory,
                onAdjustStock    = viewModel::openAdjustDialog,
                onAddBatch       = viewModel::openBatchDialog,
                on86Toggle       = viewModel::toggle86,
                modifier         = Modifier.padding(padding)
            )
        }

        if (uiState.showAdjustDialog && uiState.actionProduct != null) {
            StockAdjustDialog(
                product     = uiState.actionProduct!!,
                isSubmitting = uiState.isSubmitting,
                onDismiss   = viewModel::closeAdjustDialog,
                onConfirm   = { type, qty, note ->
                    viewModel.adjustStock(uiState.actionProduct!!.uuid, type, qty, note)
                }
            )
        }

        if (uiState.showBatchDialog && uiState.actionProduct != null) {
            AddBatchDialog(
                product      = uiState.actionProduct!!,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = viewModel::closeBatchDialog,
                onConfirm    = { qty, expiry, cost, batch, note ->
                    viewModel.createBatch(uiState.actionProduct!!.uuid, qty, expiry, cost, batch, note)
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// List + filter content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductListContent(
    uiState: ProductManagementUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onAdjustStock: (Product) -> Unit,
    onAddBatch: (Product) -> Unit,
    on86Toggle: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        // ── Search bar ────────────────────────────────────────────────────────
        OutlinedTextField(
            value       = uiState.searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari nama, SKU, barcode…") },
            leadingIcon  = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                    }
                }
            },
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            singleLine = true,
            shape      = MaterialTheme.shapes.medium
        )

        // ── Category filter chips ─────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick  = { onCategorySelect(null) },
                label    = { Text("Semua") }
            )
            uiState.categories.forEach { cat ->
                FilterChip(
                    selected = uiState.selectedCategory?.uuid == cat.uuid,
                    onClick  = { onCategorySelect(cat) },
                    label    = { Text(cat.name) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()

        // ── Product list ──────────────────────────────────────────────────────
        if (uiState.filteredProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tidak ada produk ditemukan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null) {
                        Spacer(Modifier.height(6.dp))
                        TextButton(onClick = { onSearchChange(""); onCategorySelect(null) }) {
                            Text("Reset filter")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredProducts, key = { it.uuid }) { product ->
                    ProductCard(
                        product      = product,
                        is86         = uiState.is86(product.uuid),
                        onAdjustStock = { onAdjustStock(product) },
                        onAddBatch   = { onAddBatch(product) },
                        on86Toggle   = { on86Toggle(product) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Product card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductCard(
    product: Product,
    is86: Boolean,
    onAdjustStock: () -> Unit,
    onAddBatch: () -> Unit,
    on86Toggle: () -> Unit
) {
    val stockColor = when {
        is86            -> MaterialTheme.colorScheme.error
        product.stock <= 0 -> MaterialTheme.colorScheme.error
        product.stock <= 5 -> Color(0xFFD97706) // amber-600
        else            -> Color(0xFF059669)    // emerald-600
    }
    val stockLabel = when {
        is86            -> "86"
        product.stock <= 0 -> "Stok habis"
        else            -> "Stok: ${product.stock.toStockDisplay()}"
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header row: name + price + stock badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text       = product.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!product.sku.isNullOrBlank()) {
                        Text(
                            text  = "SKU: ${product.sku}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (product.category != null) {
                        Text(
                            text  = product.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (product.hasExpiry) {
                        Text(
                            text  = "Produk kadaluarsa (FIFO)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = formatRupiah(product.price),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = stockColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text       = stockLabel,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = stockColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                // Sesuaikan Stok (always shown)
                OutlinedButton(
                    onClick          = onAdjustStock,
                    modifier         = Modifier.weight(1f),
                    contentPadding   = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Tune, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sesuaikan Stok", style = MaterialTheme.typography.labelSmall)
                }

                // Tambah Batch (only for has_expiry products)
                if (product.hasExpiry) {
                    OutlinedButton(
                        onClick        = onAddBatch,
                        modifier       = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddBox, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah Batch", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // 86 toggle
                OutlinedButton(
                    onClick        = on86Toggle,
                    modifier       = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    colors         = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (is86) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector    = if (is86) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        modifier       = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = if (is86) "Aktifkan" else "86",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StockAdjustDialog(
    product: Product,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, note: String?) -> Unit
) {
    var adjustType   by remember { mutableStateOf("in") }
    var quantityText by remember { mutableStateOf("") }
    var noteText     by remember { mutableStateOf("") }

    val qty         = quantityText.toDoubleOrNull()
    val qtyError    = when {
        quantityText.isBlank()  -> null
        qty == null             -> "Angka tidak valid"
        qty <= 0                -> "Harus lebih dari 0"
        else                    -> null
    }
    val canConfirm = !isSubmitting && qty != null && qty > 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Sesuaikan Stok") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    product.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Stok saat ini: ${product.stock.toStockDisplay()} ${product.unit ?: ""}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Masuk / Keluar toggle chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected    = adjustType == "in",
                        onClick     = { adjustType = "in" },
                        label       = { Text("Masuk (+)") },
                        leadingIcon = {
                            if (adjustType == "in")
                                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected    = adjustType == "out",
                        onClick     = { adjustType = "out" },
                        label       = { Text("Keluar (−)") },
                        leadingIcon = {
                            if (adjustType == "out")
                                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        }
                    )
                }

                OutlinedTextField(
                    value         = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = { Text("Jumlah *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError       = qtyError != null,
                    supportingText = qtyError?.let { { Text(it) } },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                OutlinedTextField(
                    value         = noteText,
                    onValueChange = { noteText = it },
                    label         = { Text("Catatan (opsional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(adjustType, qty!!, noteText.ifBlank { null }) },
                enabled  = canConfirm
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") }
        }
    )
}

@Composable
private fun AddBatchDialog(
    product: Product,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (qty: Double, expiry: String?, cost: Long?, batch: String?, note: String?) -> Unit
) {
    var quantityText  by remember { mutableStateOf("") }
    var expiryDate    by remember { mutableStateOf("") }
    var costPriceText by remember { mutableStateOf("") }
    var batchNumber   by remember { mutableStateOf("") }
    var noteText      by remember { mutableStateOf("") }

    val qty        = quantityText.toDoubleOrNull()
    val canConfirm = !isSubmitting && qty != null && qty > 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Tambah Batch Stok") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    product.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value         = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = { Text("Jumlah *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    isError       = quantityText.isNotBlank() && (qty == null || qty <= 0),
                    supportingText = if (quantityText.isNotBlank() && (qty == null || qty <= 0))
                        { { Text("Jumlah harus lebih dari 0") } } else null
                )

                OutlinedTextField(
                    value         = expiryDate,
                    onValueChange = { expiryDate = it },
                    label         = { Text("Tanggal Kadaluarsa") },
                    placeholder   = { Text("YYYY-MM-DD") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                OutlinedTextField(
                    value         = costPriceText,
                    onValueChange = { costPriceText = it.filter { c -> c.isDigit() } },
                    label         = { Text("Harga Beli (opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                OutlinedTextField(
                    value         = batchNumber,
                    onValueChange = { batchNumber = it },
                    label         = { Text("Nomor Batch (opsional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                OutlinedTextField(
                    value         = noteText,
                    onValueChange = { noteText = it },
                    label         = { Text("Catatan (opsional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        qty!!,
                        expiryDate.ifBlank { null },
                        costPriceText.toLongOrNull(),
                        batchNumber.ifBlank { null },
                        noteText.ifBlank { null }
                    )
                },
                enabled = canConfirm
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Util
// ─────────────────────────────────────────────────────────────────────────────

private fun Double.toStockDisplay(): String =
    if (this == toLong().toDouble()) toLong().toString() else toString()
