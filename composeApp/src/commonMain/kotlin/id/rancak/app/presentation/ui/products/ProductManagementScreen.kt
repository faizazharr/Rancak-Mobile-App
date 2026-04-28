package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.random.Random
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
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

    // Detect tablet BEFORE Scaffold so we can control FAB visibility
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                RancakTopBar(
                    title    = "Manajemen Produk",
                    icon     = Icons.Default.Inventory2,
                    onBack   = onBack,
                    subtitle = "${uiState.filteredProducts.size} produk"
                )
            },
            // FAB only on phone — tablet gets inline "Tambah Produk" button in content
            floatingActionButton = {
                if (!isTablet) {
                    FloatingActionButton(onClick = { viewModel.openProductForm() }) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah produk")
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            when {
                uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
                else -> ProductListContent(
                    uiState          = uiState,
                    isTablet         = isTablet,
                    onAddProduct     = viewModel::openProductForm,
                    onSearchChange   = viewModel::setSearchQuery,
                    onCategorySelect = viewModel::setCategory,
                    onAdjustStock    = viewModel::openAdjustDialog,
                    onAddBatch       = viewModel::openBatchDialog,
                    on86Toggle       = viewModel::toggle86,
                    onEditProduct    = viewModel::openProductForm,
                    onDeleteProduct  = viewModel::openDeleteConfirm,
                    onAddCategory    = { viewModel.openCategoryForm() },
                    onEditCategory   = { viewModel.openCategoryForm(it) },
                    onDeleteCategory = { viewModel.deleteCategory(it) },
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

            if (uiState.showProductFormDialog) {
                ProductFormDialog(
                    editingProduct = uiState.actionProduct,
                    categories     = uiState.categories,
                    isSubmitting   = uiState.isSubmitting,
                    onDismiss      = viewModel::closeProductForm,
                    onConfirm      = { name, price, desc, sku, barcode, catUuid, unit, stock, hasExpiry ->
                        viewModel.saveProduct(name, price, desc, sku, barcode, catUuid, unit, stock, hasExpiry)
                    }
                )
            }

            if (uiState.showDeleteConfirmDialog && uiState.actionProduct != null) {
                AlertDialog(
                    onDismissRequest = { if (!uiState.isSubmitting) viewModel.closeDeleteConfirm() },
                    title = { Text("Hapus Produk") },
                    text  = { Text("Hapus produk \"${uiState.actionProduct!!.name}\"? Tindakan ini tidak dapat dibatalkan.") },
                    confirmButton = {
                        TextButton(onClick = viewModel::deleteProduct, enabled = !uiState.isSubmitting) {
                            if (uiState.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp))
                            else Text("Hapus", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::closeDeleteConfirm, enabled = !uiState.isSubmitting) {
                            Text("Batal")
                        }
                    }
                )
            }

            if (uiState.showCategoryFormDialog) {
                CategoryFormDialog(
                    editingCategory = uiState.editingCategory,
                    isSubmitting    = uiState.isSubmitting,
                    onDismiss       = viewModel::closeCategoryForm,
                    onConfirm       = { name, desc -> viewModel.saveCategory(name, desc) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// List + filter content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductListContent(
    uiState: ProductManagementUiState,
    isTablet: Boolean,
    onAddProduct: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onAdjustStock: (Product) -> Unit,
    onAddBatch: (Product) -> Unit,
    on86Toggle: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isTablet) {
        Row(modifier.fillMaxSize()) {
            // ── Kiri — panel kategori ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Kategori",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onAddCategory, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah kategori",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                // "Semua" row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (uiState.selectedCategory == null)
                                MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent
                        )
                        .clickable { onCategorySelect(null) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Semua (${uiState.products.size})",
                        modifier = Modifier.weight(1f),
                        style    = MaterialTheme.typography.bodyMedium
                    )
                }
                // Per-category rows with edit/delete
                uiState.categories.forEach { cat ->
                    val isSelected = uiState.selectedCategory?.uuid == cat.uuid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Transparent
                            )
                            .clickable { onCategorySelect(cat) }
                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            cat.name,
                            modifier = Modifier.weight(1f),
                            style    = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                        IconButton(
                            onClick  = { onEditCategory(cat) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit kategori",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick  = { onDeleteCategory(cat) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Hapus kategori",
                                modifier = Modifier.size(14.dp),
                                tint     = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            // ── Kanan — search + list ─────────────────────────────────────────
            Column(Modifier.weight(1f).fillMaxHeight()) {
                ProductSearchAndList(
                    uiState        = uiState,
                    isTablet       = true,
                    onAddProduct   = onAddProduct,
                    onSearchChange = onSearchChange,
                    onCategorySelect = onCategorySelect,
                    onAdjustStock  = onAdjustStock,
                    onAddBatch     = onAddBatch,
                    on86Toggle     = on86Toggle,
                    onEditProduct  = onEditProduct,
                    onDeleteProduct = onDeleteProduct
                )
            }
        }
    } else {
        Column(modifier.fillMaxSize()) {
            // ── Category filter chips + add-category button ───────────────────
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick  = { onCategorySelect(null) },
                    label    = { Text("Semua") }
                )
                uiState.categories.forEach { cat ->
                    FilterChip(
                        selected     = uiState.selectedCategory?.uuid == cat.uuid,
                        onClick      = { onCategorySelect(cat) },
                        label        = { Text(cat.name) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit kategori",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { onEditCategory(cat) }
                                )
                                Spacer(Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus kategori",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { onDeleteCategory(cat) },
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
                // Clearly labeled "Tambah Kategori" — visually distinct from the FAB (add product)
                AssistChip(
                    onClick      = onAddCategory,
                    label        = { Text("Tambah Kategori") },
                    leadingIcon  = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            Modifier.size(16.dp)
                        )
                    }
                )
            }
            HorizontalDivider()
            ProductSearchAndList(
                uiState         = uiState,
                isTablet        = false,
                onAddProduct    = onAddProduct,
                onSearchChange  = onSearchChange,
                onCategorySelect = onCategorySelect,
                onAdjustStock   = onAdjustStock,
                onAddBatch      = onAddBatch,
                on86Toggle      = on86Toggle,
                onEditProduct   = onEditProduct,
                onDeleteProduct = onDeleteProduct
            )
        }
    }
}

@Composable
private fun ProductSearchAndList(
    uiState: ProductManagementUiState,
    isTablet: Boolean,
    onAddProduct: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onAdjustStock: (Product) -> Unit,
    onAddBatch: (Product) -> Unit,
    on86Toggle: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        // ── Search bar + Tambah Produk (tablet only) ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder   = { Text("Cari nama, SKU, barcode…") },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                modifier   = Modifier.weight(1f),
                singleLine = true,
                shape      = MaterialTheme.shapes.medium
            )
            if (isTablet) {
                Button(
                    onClick = onAddProduct,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tambah Produk")
                }
            }
        }

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
                        on86Toggle   = { on86Toggle(product) },
                        onEdit       = { onEditProduct(product) },
                        onDelete     = { onDeleteProduct(product) }
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
    on86Toggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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

            // Header row: name + price + stock badge + action icons
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = formatRupiah(product.price),
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit produk", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus produk", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
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

/** Generate SKU from product name initials + 4 random digits. E.g. "Nasi Goreng" → "NG-4821" */
private fun generateSku(name: String): String {
    val prefix = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(3)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "PRD" }
    val suffix = Random.nextInt(1000, 9999)
    return "$prefix-$suffix"
}

/** Generate a 12-digit numeric barcode. */
private fun generateBarcode(): String =
    Random.nextLong(100_000_000_000L, 999_999_999_999L).toString()

// ─────────────────────────────────────────────────────────────────────────────
// ProductFormDialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormDialog(
    editingProduct: Product?,
    categories: List<Category>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Long, description: String?, sku: String?, barcode: String?, categoryUuid: String?, unit: String?, stock: Double, hasExpiry: Boolean) -> Unit
) {
    var name        by remember(editingProduct) { mutableStateOf(editingProduct?.name ?: "") }
    var priceText   by remember(editingProduct) { mutableStateOf(editingProduct?.price?.toString() ?: "") }
    var description by remember(editingProduct) { mutableStateOf(editingProduct?.description ?: "") }
    var sku         by remember(editingProduct) { mutableStateOf(editingProduct?.sku ?: "") }
    var barcode     by remember(editingProduct) { mutableStateOf(editingProduct?.barcode ?: "") }
    var unit        by remember(editingProduct) { mutableStateOf(editingProduct?.unit ?: "") }
    var stockText   by remember(editingProduct) { mutableStateOf(if (editingProduct == null) "0" else editingProduct.stock.toStockDisplay()) }
    var hasExpiry   by remember(editingProduct) { mutableStateOf(editingProduct?.hasExpiry ?: false) }
    var categoryUuid by remember(editingProduct) { mutableStateOf(editingProduct?.category?.uuid) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val price      = priceText.toLongOrNull()
    val stock      = stockText.toDoubleOrNull() ?: 0.0
    val canConfirm = !isSubmitting && name.isNotBlank() && price != null && price > 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editingProduct == null) "Tambah Produk" else "Edit Produk") },
        text  = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Nama Produk *") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    isError       = name.isBlank()
                )

                OutlinedTextField(
                    value         = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label         = { Text("Harga (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    isError       = priceText.isNotBlank() && (price == null || price <= 0),
                    supportingText = if (priceText.isNotBlank() && (price == null || price <= 0))
                        { { Text("Harga harus lebih dari 0") } } else null
                )

                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Deskripsi") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = sku,
                        onValueChange = { sku = it },
                        label         = { Text("SKU") },
                        placeholder   = { Text("Otomatis", style = MaterialTheme.typography.bodySmall) },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        trailingIcon  = {
                            if (sku.isBlank()) {
                                IconButton(onClick = { sku = generateSku(name) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Generate SKU", modifier = Modifier.size(16.dp))
                                }
                            } else {
                                IconButton(onClick = { sku = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus SKU", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                    OutlinedTextField(
                        value         = barcode,
                        onValueChange = { barcode = it.filter { c -> c.isDigit() } },
                        label         = { Text("Barcode") },
                        placeholder   = { Text("Otomatis", style = MaterialTheme.typography.bodySmall) },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon  = {
                            if (barcode.isBlank()) {
                                IconButton(onClick = { barcode = generateBarcode() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Generate Barcode", modifier = Modifier.size(16.dp))
                                }
                            } else {
                                IconButton(onClick = { barcode = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus Barcode", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = unit,
                        onValueChange = { unit = it },
                        label         = { Text("Satuan") },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true
                    )
                    if (editingProduct == null) {
                        OutlinedTextField(
                            value         = stockText,
                            onValueChange = { stockText = it.filter { c -> c.isDigit() || c == '.' } },
                            label         = { Text("Stok Awal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier      = Modifier.weight(1f),
                            singleLine    = true
                        )
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded         = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value         = categories.find { it.uuid == categoryUuid }?.name ?: "Tanpa kategori",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Kategori") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier      = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded         = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("Tanpa kategori") }, onClick = { categoryUuid = null; categoryExpanded = false })
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat.name) },
                                onClick = { categoryUuid = cat.uuid; categoryExpanded = false }
                            )
                        }
                    }
                }

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Produk kadaluarsa (FIFO)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = hasExpiry, onCheckedChange = { hasExpiry = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name.trim(),
                        price!!,
                        description.ifBlank { null },
                        sku.ifBlank { null },
                        barcode.ifBlank { null },
                        categoryUuid,
                        unit.ifBlank { null },
                        stock,
                        hasExpiry
                    )
                },
                enabled = canConfirm
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// CategoryFormDialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryFormDialog(
    editingCategory: Category?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?) -> Unit
) {
    var name        by remember(editingCategory) { mutableStateOf(editingCategory?.name ?: "") }
    var description by remember(editingCategory) { mutableStateOf(editingCategory?.description ?: "") }

    val canConfirm = !isSubmitting && name.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editingCategory == null) "Tambah Kategori" else "Edit Kategori") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Nama Kategori *") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    isError       = name.isBlank()
                )
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Deskripsi") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.trim(), description.ifBlank { null }) }, enabled = canConfirm) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") }
        }
    )
}
