package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.ProductManagementUiState

// ─────────────────────────────────────────────────────────────────────────────
// Layout utama: tablet (sidebar kategori + list) / phone (chips + list)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductListContent(
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
            CategorySidePanel(
                uiState          = uiState,
                onCategorySelect = onCategorySelect,
                onAddCategory    = onAddCategory,
                onEditCategory   = onEditCategory,
                onDeleteCategory = onDeleteCategory
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            TabletDashboard(
                uiState          = uiState,
                onAddProduct     = onAddProduct,
                onSearchChange   = onSearchChange,
                onCategorySelect = onCategorySelect,
                onAdjustStock    = onAdjustStock,
                onAddBatch       = onAddBatch,
                on86Toggle       = on86Toggle,
                onEditProduct    = onEditProduct,
                onDeleteProduct  = onDeleteProduct,
                modifier         = Modifier.weight(1f).fillMaxHeight()
            )
        }
    } else {
        Column(modifier.fillMaxSize()) {
            CategoryFilterRow(
                uiState          = uiState,
                onCategorySelect = onCategorySelect,
                onAddCategory    = onAddCategory,
                onEditCategory   = onEditCategory,
                onDeleteCategory = onDeleteCategory
            )
            HorizontalDivider()
            ProductSearchAndList(
                uiState          = uiState,
                isTablet         = false,
                onAddProduct     = onAddProduct,
                onSearchChange   = onSearchChange,
                onCategorySelect = onCategorySelect,
                onAdjustStock    = onAdjustStock,
                onAddBatch       = onAddBatch,
                on86Toggle       = on86Toggle,
                onEditProduct    = onEditProduct,
                onDeleteProduct  = onDeleteProduct
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Panel kategori (tablet — sisi kiri)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategorySidePanel(
    uiState: ProductManagementUiState,
    onCategorySelect: (Category?) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Kategori", style = MaterialTheme.typography.titleSmall)
            FilledTonalIconButton(onClick = onAddCategory, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, "Tambah kategori", Modifier.size(16.dp))
            }
        }

        // Semua
        CategoryRow(
            label      = "Semua (${uiState.products.size})",
            isSelected = uiState.selectedCategory == null,
            onClick    = { onCategorySelect(null) }
        )

        // Per kategori
        uiState.categories.forEach { cat ->
            CategoryRow(
                label      = cat.name,
                isSelected = uiState.selectedCategory?.uuid == cat.uuid,
                onClick    = { onCategorySelect(cat) },
                onEdit     = { onEditCategory(cat) },
                onDelete   = { onDeleteCategory(cat) }
            )
        }
    }
}

@Composable
private fun CategoryRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        if (onEdit != null) {
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, "Edit kategori", Modifier.size(14.dp))
            }
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, "Hapus kategori", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tablet dashboard: metric summary + compact table
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabletDashboard(
    uiState: ProductManagementUiState,
    onAddProduct: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onAdjustStock: (Product) -> Unit,
    onAddBatch: (Product) -> Unit,
    on86Toggle: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {

        // ── Metric summary row ────────────────────────────────────────────────
        MetricSummaryRow(uiState = uiState)

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Search bar + Tambah Produk ────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder   = { Text("Cari nama, SKU, barcode…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, "Hapus pencarian")
                        }
                    }
                },
                modifier   = Modifier.weight(1f),
                singleLine = true,
                shape      = MaterialTheme.shapes.medium
            )
            Button(
                onClick        = onAddProduct,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tambah Produk")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Tabel produk ──────────────────────────────────────────────────────
        if (uiState.filteredProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier           = Modifier.size(64.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tidak ada produk ditemukan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { onSearchChange(""); onCategorySelect(null) }) {
                            Text("Reset filter")
                        }
                    }
                }
            }
        } else {
            // Header kolom tabel
            ProductTableHeader()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.filteredProducts, key = { it.uuid }) { product ->
                    ProductTableRow(
                        product       = product,
                        is86          = uiState.is86(product.uuid),
                        onAdjustStock = { onAdjustStock(product) },
                        onAddBatch    = { onAddBatch(product) },
                        on86Toggle    = { on86Toggle(product) },
                        onEdit        = { onEditProduct(product) },
                        onDelete      = { onDeleteProduct(product) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ── Metric summary ────────────────────────────────────────────────────────────

@Composable
private fun MetricSummaryRow(uiState: ProductManagementUiState) {
    val sem       = RancakColors.semantic
    val lowStock  = uiState.products.count { it.stock <= 5 && !uiState.is86(it.uuid) }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            modifier   = Modifier.weight(1f),
            icon       = Icons.Default.Inventory2,
            label      = "Total Produk",
            value      = "${uiState.products.size}",
            iconTint   = MaterialTheme.colorScheme.primary
        )
        MetricCard(
            modifier   = Modifier.weight(1f),
            icon       = Icons.Default.Category,
            label      = "Kategori",
            value      = "${uiState.categories.size}",
            iconTint   = sem.info
        )
        MetricCard(
            modifier   = Modifier.weight(1f),
            icon       = if (lowStock > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
            label      = "Stok Rendah",
            value      = if (lowStock > 0) "$lowStock produk" else "Semua aman",
            iconTint   = if (lowStock > 0) sem.warning else sem.success
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Card(
        modifier  = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(24.dp), tint = iconTint)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Header kolom tabel ────────────────────────────────────────────────────────

@Composable
private fun ProductTableHeader() {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail placeholder
        Spacer(Modifier.size(44.dp))
        Spacer(Modifier.width(12.dp))

        Text(
            text      = "Produk",
            style     = MaterialTheme.typography.labelMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier  = Modifier.weight(1f)
        )
        Text(
            text     = "Stok",
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text      = "Harga",
            style     = MaterialTheme.typography.labelMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier  = Modifier.width(100.dp)
        )
        // Kebab placeholder
        Spacer(Modifier.width(4.dp + 36.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter chip row (phone — bagian atas)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryFilterRow(
    uiState: ProductManagementUiState,
    onCategorySelect: (Category?) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                                    indication        = null,
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
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onDeleteCategory(cat) },
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
        AssistChip(
            onClick     = onAddCategory,
            label       = { Text("Tambah Kategori") },
            leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search bar + daftar produk
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductSearchAndList(
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
        // ── Search bar + tombol tambah produk (tablet) ────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder   = { Text("Cari nama, SKU, barcode…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, "Hapus pencarian")
                        }
                    }
                },
                modifier   = Modifier.weight(1f),
                singleLine = true,
                shape      = MaterialTheme.shapes.medium
            )
            if (isTablet) {
                Button(
                    onClick        = onAddProduct,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tambah Produk")
                }
            }
        }

        // ── Daftar produk / empty state ───────────────────────────────────────
        if (uiState.filteredProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory2,
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
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredProducts, key = { it.uuid }) { product ->
                    ProductManagementCard(
                        product       = product,
                        is86          = uiState.is86(product.uuid),
                        onAdjustStock = { onAdjustStock(product) },
                        onAddBatch    = { onAddBatch(product) },
                        on86Toggle    = { on86Toggle(product) },
                        onEdit        = { onEditProduct(product) },
                        onDelete      = { onDeleteProduct(product) }
                    )
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val previewCategories = listOf(
    Category("c1", "Makanan", null),
    Category("c2", "Minuman", null)
)
private val previewProducts = listOf(
    Product("1", "NGS-4821", null, "Nasi Goreng Spesial", null, previewCategories[0], 25000L, 10.0, "porsi", null, true, false, null),
    Product("2", null, null, "Es Teh Manis", null, previewCategories[1], 5000L, 0.0, "gelas", null, true, false, null)
)

@Preview(name = "ProductListContent – Phone")
@Composable
private fun ProductListContentPhonePreview() {
    RancakTheme {
        ProductListContent(
            uiState          = ProductManagementUiState(products = previewProducts, categories = previewCategories),
            isTablet         = false,
            onAddProduct     = {},
            onSearchChange   = {},
            onCategorySelect = {},
            onAdjustStock    = {},
            onAddBatch       = {},
            on86Toggle       = {},
            onEditProduct    = {},
            onDeleteProduct  = {},
            onAddCategory    = {},
            onEditCategory   = {},
            onDeleteCategory = {}
        )
    }
}

@Preview(name = "ProductSearchAndList – Empty")
@Composable
private fun ProductSearchAndListEmptyPreview() {
    RancakTheme {
        ProductSearchAndList(
            uiState          = ProductManagementUiState(searchQuery = "xyz"),
            isTablet         = false,
            onAddProduct     = {},
            onSearchChange   = {},
            onCategorySelect = {},
            onAdjustStock    = {},
            onAddBatch       = {},
            on86Toggle       = {},
            onEditProduct    = {},
            onDeleteProduct  = {}
        )
    }
}
