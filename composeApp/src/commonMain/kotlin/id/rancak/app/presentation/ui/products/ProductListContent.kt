package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
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
            Column(Modifier.weight(1f).fillMaxHeight()) {
                ProductSearchAndList(
                    uiState          = uiState,
                    isTablet         = true,
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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Kategori", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onAddCategory, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, "Tambah kategori", Modifier.size(18.dp))
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
            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
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
