package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import id.rancak.app.presentation.viewmodel.ProductSortField
import id.rancak.app.presentation.viewmodel.StockFilter
import id.rancak.app.presentation.viewmodel.PriceFilter
import kotlinx.collections.immutable.persistentListOf

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
    onFormConfirm: (String, Long, String?, String?, String?, String?, String?, Double, Boolean) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onFormDismiss: () -> Unit = {},
    onAdjustConfirm: (type: String, qty: Double, note: String?) -> Unit = { _, _, _ -> },
    onAdjustDismiss: () -> Unit = {},
    onSortChange: (ProductSortField) -> Unit = {},
    onStockFilterChange: (StockFilter) -> Unit = {},
    onPriceFilterChange: (PriceFilter) -> Unit = {},
    isLoading: Boolean = false,
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
                uiState             = uiState,
                isLoading           = isLoading,
                onAddProduct        = onAddProduct,
                onSearchChange      = onSearchChange,
                onCategorySelect    = onCategorySelect,
                onAdjustStock       = onAdjustStock,
                onAddBatch          = onAddBatch,
                on86Toggle          = on86Toggle,
                onEditProduct       = onEditProduct,
                onDeleteProduct     = onDeleteProduct,
                onSortChange        = onSortChange,
                onStockFilterChange = onStockFilterChange,
                onPriceFilterChange = onPriceFilterChange,
                onFormConfirm       = onFormConfirm,
                onFormDismiss       = onFormDismiss,
                onAdjustConfirm     = onAdjustConfirm,
                onAdjustDismiss     = onAdjustDismiss,
                modifier            = Modifier.weight(1f).fillMaxHeight()
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
                isLoading        = isLoading,
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

        // Hitung produk per kategori:
        // - Saat Semua: groupBy seluruh produk → setiap kategori dapat badge
        // - Saat filter aktif: tampilkan hanya count untuk kategori yang dipilih
        //   (uiState.products berisi produk dari kategori itu saja)
        val countByCat: Map<String?, Int> = remember(uiState.selectedCategory, uiState.products) {
            if (uiState.selectedCategory == null)
                uiState.products.groupingBy { it.category?.uuid }.eachCount()
            else
                mapOf(uiState.selectedCategory.uuid to uiState.products.size)
        }

        // Semua
        CategoryRow(
            label      = "Semua",
            count      = if (uiState.selectedCategory == null) uiState.products.size else null,
            isSelected = uiState.selectedCategory == null,
            onClick    = { onCategorySelect(null) }
        )

        // Per kategori
        uiState.categories.forEach { cat ->
            CategoryRow(
                label      = cat.name,
                count      = countByCat[cat.uuid],
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
    count: Int? = null,
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
        if (count != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text  = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(2.dp))
        }
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
    isLoading: Boolean = false,
    onAddProduct: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onAdjustStock: (Product) -> Unit,
    onAddBatch: (Product) -> Unit,
    on86Toggle: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onSortChange: (ProductSortField) -> Unit,
    onStockFilterChange: (StockFilter) -> Unit,
    onPriceFilterChange: (PriceFilter) -> Unit,
    onFormConfirm: (String, Long, String?, String?, String?, String?, String?, Double, Boolean) -> Unit,
    onFormDismiss: () -> Unit,
    onAdjustConfirm: (type: String, qty: Double, note: String?) -> Unit,
    onAdjustDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Edit produk & sesuaikan stok selalu ditampilkan sebagai dialog (popup),
    // bukan inline panel — sehingga TabletDashboard selalu menampilkan tabel.
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Metric summary row ────────────────────────────────────────────────
        MetricSummaryRow(uiState = uiState)

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Search bar + Tambah Produk ────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

        // ── Filter row (stok + harga) ─────────────────────────────────────────
        ProductFilterRow(
            stockFilter       = uiState.stockFilter,
            priceFilter       = uiState.priceFilter,
            onStockFilter     = onStockFilterChange,
            onPriceFilter     = onPriceFilterChange
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Tabel produk ──────────────────────────────────────────────────────
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                if (targetState) {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(130))
                } else {
                    fadeIn(tween(320, delayMillis = 80)) togetherWith fadeOut(tween(180))
                }
            },
            label    = "tablet_product_list",
            modifier = Modifier.fillMaxSize()
        ) { loading ->
            if (loading) {
                ShimmerTableContent()
            } else if (uiState.filteredProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier           = Modifier.size(48.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null)
                                "Tidak ada produk ditemukan"
                            else
                                "Belum ada produk",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null) {
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onSearchChange(""); onCategorySelect(null) }) {
                                    Text("Reset filter")
                                }
                                Button(onClick = onAddProduct) {
                                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        if (uiState.selectedCategory != null)
                                            "Tambah di ${uiState.selectedCategory.name}"
                                        else
                                            "Tambah Produk"
                                    )
                                }
                            }
                        } else {
                            Spacer(Modifier.height(4.dp))
                            Button(onClick = onAddProduct) {
                                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Tambah Produk Pertama")
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProductTableHeader(
                        sortField     = uiState.sortField,
                        sortAscending = uiState.sortAscending,
                        onSort        = onSortChange
                    )
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
    } // closes Column (tabel + metrik)
}

// ── Metric summary ────────────────────────────────────────────────────────────

@Composable
private fun MetricSummaryRow(uiState: ProductManagementUiState) {
    val sem           = RancakColors.semantic
    val activeCount   = uiState.products.count { it.isActive }
    val inactive      = uiState.products.size - activeCount
    val marked86      = uiState.products86.size
    val outOfStock    = uiState.products.count { it.stock <= 0 && !uiState.is86(it.uuid) }
    val criticalStock = uiState.products.count { it.stock in 1.0..5.0 && !uiState.is86(it.uuid) }
    val lowStock      = outOfStock + criticalStock

    val productSubtitle = buildString {
        append("$activeCount aktif")
        if (inactive > 0) append(" · $inactive nonaktif")
        if (marked86 > 0) append(" · $marked86 produk 86")
    }

    val categorySubtitle = if (uiState.selectedCategory != null)
        null  // value card sudah menampilkan nama kategori — subtitle tidak perlu
    else
        "${uiState.categories.size} kategori aktif"

    val stockSubtitle = when {
        outOfStock > 0 && criticalStock > 0 -> "$outOfStock habis · $criticalStock kritis"
        outOfStock > 0                      -> "$outOfStock produk habis"
        criticalStock > 0                   -> "$criticalStock produk kritis"
        else                                -> "Semua stok normal"
    }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Default.Inventory2,
            label     = "Total Produk",
            value     = "${uiState.products.size}",
            subtitle  = productSubtitle,
            iconTint  = MaterialTheme.colorScheme.primary
        )
        MetricCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Default.Category,
            label     = "Kategori",
            value     = if (uiState.selectedCategory != null) uiState.selectedCategory.name else "${uiState.categories.size}",
            subtitle  = categorySubtitle,
            iconTint  = if (uiState.selectedCategory != null) sem.info else sem.info
        )
        MetricCard(
            modifier  = Modifier.weight(1f),
            icon      = if (lowStock > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
            label     = "Stok Rendah",
            value     = if (lowStock > 0) "$lowStock produk" else "Aman",
            subtitle  = stockSubtitle,
            iconTint  = if (lowStock > 0) sem.warning else sem.success
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String? = null,
    iconTint: Color
) {
    Card(
        modifier  = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = iconTint)
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ── Header kolom tabel ────────────────────────────────────────────────────────

@Composable
private fun SortHeaderCell(
    label: String,
    field: ProductSortField,
    sortField: ProductSortField,
    sortAscending: Boolean,
    onSort: (ProductSortField) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val active = sortField == field
    val color  by animateColorAsState(
        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(200), label = "SortCellColor"
    )
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = { onSort(field) }
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = if (textAlign == TextAlign.End) Arrangement.End else Arrangement.Start
    ) {
        if (textAlign == TextAlign.End) {
            // Reserved space for alignment — icon visible only when active
            Box(modifier = Modifier.size(if (active) 13.dp else 0.dp).then(Modifier.padding(end = if (active) 2.dp else 0.dp))) {}
            AnimatedVisibility(
                visible = active,
                enter   = fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.6f),
                exit    = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.6f)
            ) {
                Row {
                    AnimatedContent(
                        targetState  = sortAscending,
                        transitionSpec = { (fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.7f)) togetherWith (fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.7f)) },
                        label        = "SortIconEnd"
                    ) { asc ->
                        Icon(
                            imageVector = if (asc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint     = color,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                }
            }
        }
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelMedium,
            color      = color,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            textAlign  = textAlign
        )
        if (textAlign != TextAlign.End) {
            Spacer(Modifier.width(2.dp))
            // Always reserve the icon space; animate icon in/out + direction flip
            Box(modifier = Modifier.size(13.dp)) {
                 SortArrow(active = active, ascending = sortAscending, color = color)
            }
        }
    }
}

@Composable
private fun SortArrow(active: Boolean, ascending: Boolean, color: Color) {
    AnimatedVisibility(
        visible = active,
        enter   = fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.6f),
        exit    = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.6f)
    ) {
        AnimatedContent(
            targetState    = ascending,
            transitionSpec = { (fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.7f)) togetherWith (fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.7f)) },
            label          = "SortIconArrow"
        ) { asc ->
            Icon(
                imageVector        = if (asc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint               = color,
                modifier           = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun ProductTableHeader(
    sortField: ProductSortField,
    sortAscending: Boolean,
    onSort: (ProductSortField) -> Unit
) {
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

        SortHeaderCell(
            label         = "Produk",
            field         = ProductSortField.NAME,
            sortField     = sortField,
            sortAscending = sortAscending,
            onSort        = onSort,
            modifier      = Modifier.weight(1f)
        )
        SortHeaderCell(
            label         = "Stok",
            field         = ProductSortField.STOCK,
            sortField     = sortField,
            sortAscending = sortAscending,
            onSort        = onSort,
            modifier      = Modifier.width(110.dp)
        )
        SortHeaderCell(
            label         = "Harga",
            field         = ProductSortField.PRICE,
            sortField     = sortField,
            sortAscending = sortAscending,
            onSort        = onSort,
            modifier      = Modifier.width(100.dp),
            textAlign     = TextAlign.End
        )
        // Kebab placeholder
        Spacer(Modifier.width(4.dp + 36.dp))
    }
}

// ── Filter row: stock status + price range ────────────────────────────────────

@Composable
private fun ProductFilterRow(
    stockFilter: StockFilter,
    priceFilter: PriceFilter,
    onStockFilter: (StockFilter) -> Unit,
    onPriceFilter: (PriceFilter) -> Unit
) {
    val sem = RancakColors.semantic
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Stock filter group
        Icon(Icons.Default.Inventory2, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(2.dp))
        listOf(
            StockFilter.ALL       to "Semua Stok",
            StockFilter.LOW       to "Stok Kritis",
            StockFilter.OUT       to "Habis",
            StockFilter.MARKED_86 to "Produk 86"
        ).forEach { (value, label) ->
            FilterChip(
                selected = stockFilter == value,
                onClick  = { onStockFilter(value) },
                label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (value) {
                        StockFilter.LOW       -> sem.warning.copy(alpha = 0.15f)
                        StockFilter.OUT       -> MaterialTheme.colorScheme.errorContainer
                        StockFilter.MARKED_86 -> MaterialTheme.colorScheme.secondaryContainer
                        else                  -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    selectedLabelColor = when (value) {
                        StockFilter.LOW       -> sem.warning
                        StockFilter.OUT       -> MaterialTheme.colorScheme.onErrorContainer
                        else                  -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            )
        }

        VerticalDivider(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Price filter group
        Icon(Icons.Default.Sell, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(2.dp))
        listOf(
            PriceFilter.ALL     to "Semua Harga",
            PriceFilter.BUDGET  to "< Rp10rb",
            PriceFilter.MID     to "Rp10–50rb",
            PriceFilter.HIGH    to "Rp50–100rb",
            PriceFilter.PREMIUM to "> Rp100rb"
        ).forEach { (value, label) ->
            FilterChip(
                selected = priceFilter == value,
                onClick  = { onPriceFilter(value) },
                label    = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
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
    isLoading: Boolean = false,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                if (targetState) {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(130))
                } else {
                    fadeIn(tween(320, delayMillis = 80)) togetherWith fadeOut(tween(180))
                }
            },
            label    = "phone_product_list",
            modifier = Modifier.fillMaxSize()
        ) { loading ->
            if (loading) {
                ShimmerCardContent()
            } else if (uiState.filteredProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null)
                                "Tidak ada produk ditemukan"
                            else
                                "Belum ada produk",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null) {
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onSearchChange(""); onCategorySelect(null) }) {
                                    Text("Reset filter")
                                }
                                Button(onClick = onAddProduct) {
                                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        if (uiState.selectedCategory != null)
                                            "Tambah di ${uiState.selectedCategory.name}"
                                        else
                                            "Tambah Produk"
                                    )
                                }
                            }
                        } else {
                            Spacer(Modifier.height(4.dp))
                            Button(onClick = onAddProduct) {
                                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Tambah Produk Pertama")
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Shimmer skeleton — digunakan saat loading kategori berlangsung
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun rememberShimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.85f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    return alpha
}

@Composable
private fun ShimmerTableContent() {
    val alpha = rememberShimmerAlpha()
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        repeat(8) {
            ShimmerTableRow(alpha = alpha, shimmerColor = color)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun ShimmerTableRow(alpha: Float, shimmerColor: Color) {
    Row(
        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.small)
                .alpha(alpha)
                .background(shimmerColor)
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(Modifier.fillMaxWidth(0.52f).height(13.dp).clip(RoundedCornerShape(3.dp)).alpha(alpha).background(shimmerColor))
            Box(Modifier.fillMaxWidth(0.33f).height(10.dp).clip(RoundedCornerShape(3.dp)).alpha(alpha * 0.6f).background(shimmerColor))
        }
        Row(
            modifier              = Modifier.width(110.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).alpha(alpha).background(shimmerColor))
            Box(Modifier.width(38.dp).height(11.dp).clip(RoundedCornerShape(3.dp)).alpha(alpha).background(shimmerColor))
        }
        Box(Modifier.width(68.dp).height(13.dp).clip(RoundedCornerShape(3.dp)).alpha(alpha).background(shimmerColor))
        Spacer(Modifier.width(40.dp))
    }
}

@Composable
private fun ShimmerCardContent() {
    val alpha = rememberShimmerAlpha()
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) { ShimmerProductCard(alpha = alpha, shimmerColor = color) }
    }
}

@Composable
private fun ShimmerProductCard(alpha: Float, shimmerColor: Color) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).alpha(alpha).background(shimmerColor))
                    Box(Modifier.fillMaxWidth(0.4f).height(11.dp).clip(RoundedCornerShape(3.dp)).alpha(alpha * 0.6f).background(shimmerColor))
                    Box(Modifier.fillMaxWidth(0.28f).height(11.dp).clip(RoundedCornerShape(3.dp)).alpha(alpha * 0.6f).background(shimmerColor))
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.width(80.dp).height(22.dp).clip(RoundedCornerShape(11.dp)).alpha(alpha).background(shimmerColor))
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(72.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).alpha(alpha).background(shimmerColor))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(36.dp).clip(CircleShape).alpha(alpha).background(shimmerColor))
                        Box(Modifier.size(36.dp).clip(CircleShape).alpha(alpha).background(shimmerColor))
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(4.dp)).alpha(alpha * 0.55f).background(shimmerColor))
                Box(Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(4.dp)).alpha(alpha * 0.55f).background(shimmerColor))
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val previewCategories = persistentListOf(
    Category("c1", "Makanan", null),
    Category("c2", "Minuman", null)
)
private val previewProducts = persistentListOf(
    Product("1", "NGS-4821", null, "Nasi Goreng Spesial", null, previewCategories[0], 25000L, 10.0, "porsi", null, true, false, null),
    Product("2", null, null, "Es Teh Manis", null, previewCategories[1], 5000L, 0.0, "gelas", null, true, false, null)
)

@Preview(name = "ProductListContent – Phone")
@Composable
private fun ProductListContentPhonePreview() {
    RancakTheme {
        ProductListContent(
            uiState          = ProductManagementUiState(
                products = previewProducts,
                categories = previewCategories
            ),
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
