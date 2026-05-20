package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.components.RancakFormDialog
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val GradientEnd = Color(0xFF0B7A60)

// ── Shared form field groups ──────────────────────────────────────────────────

@Composable
private fun FormSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier         = Modifier.size(24.dp)
                    .background(Primary.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(13.dp)) }
            Text(
                title,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp
            )
        }
        content()
    }
}

// ── State holder shared by both Dialog and Panel ──────────────────────────────

private class ProductFormState(editingProduct: Product?, initialCategoryUuid: String?) {
    var name             by mutableStateOf(editingProduct?.name ?: "")
    var priceText        by mutableStateOf(editingProduct?.price?.toString() ?: "")
    var description      by mutableStateOf(editingProduct?.description ?: "")
    var sku              by mutableStateOf(editingProduct?.sku ?: "")
    var barcode          by mutableStateOf(editingProduct?.barcode ?: "")
    var unit             by mutableStateOf(editingProduct?.unit ?: "")
    var stockText        by mutableStateOf(if (editingProduct == null) "0" else editingProduct.stock.toStockDisplay())
    var hasExpiry        by mutableStateOf(editingProduct?.hasExpiry ?: false)
    var categoryUuid     by mutableStateOf(editingProduct?.category?.uuid ?: initialCategoryUuid)
    var categoryExpanded by mutableStateOf(false)

    val price      get() = priceText.toLongOrNull()
    val stock      get() = stockText.toDoubleOrNull() ?: 0.0
    fun canConfirm(isSubmitting: Boolean) = !isSubmitting && name.isNotBlank() && price != null && price!! > 0

    fun buildConfirmArgs() = Triple(
        Triple(name.trim(), price!!, description.ifBlank { null }),
        Triple(sku.ifBlank { null }, barcode.ifBlank { null }, categoryUuid),
        Triple(unit.ifBlank { null }, stock, hasExpiry)
    )
}

// ── Shared form body ──────────────────────────────────────────────────────────

@Composable
private fun SkuField(state: ProductFormState, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value         = state.sku,
        onValueChange = { state.sku = it },
        label         = { Text("SKU") },
        placeholder   = { Text("Otomatis") },
        modifier      = modifier,
        singleLine    = true,
        shape         = MaterialTheme.shapes.medium,
        trailingIcon  = {
            if (state.sku.isBlank()) {
                IconButton(onClick = { state.sku = generateSku(state.name) }) {
                    Icon(Icons.Default.Refresh, "Generate SKU", Modifier.size(16.dp))
                }
            } else {
                IconButton(onClick = { state.sku = "" }) {
                    Icon(Icons.Default.Clear, "Hapus SKU", Modifier.size(16.dp))
                }
            }
        }
    )
}

@Composable
private fun BarcodeField(state: ProductFormState, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value           = state.barcode,
        onValueChange   = { state.barcode = it.filter { c -> c.isDigit() } },
        label           = { Text("Barcode") },
        placeholder     = { Text("Otomatis") },
        modifier        = modifier,
        singleLine      = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape           = MaterialTheme.shapes.medium,
        trailingIcon    = {
            if (state.barcode.isBlank()) {
                IconButton(onClick = { state.barcode = generateBarcode() }) {
                    Icon(Icons.Default.Refresh, "Generate Barcode", Modifier.size(16.dp))
                }
            } else {
                IconButton(onClick = { state.barcode = "" }) {
                    Icon(Icons.Default.Clear, "Hapus Barcode", Modifier.size(16.dp))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormBody(
    state: ProductFormState,
    editingProduct: Product?,
    categories: ImmutableList<Category>,
    isCompact: Boolean = false
) {
    // ── Informasi Utama ───────────────────────────────────────────────────
    FormSection("INFORMASI UTAMA", Icons.Default.ShoppingBag) {
        OutlinedTextField(
            value         = state.name,
            onValueChange = { state.name = it },
            label         = { Text("Nama Produk *") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            isError       = state.name.isBlank(),
            shape         = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value           = state.priceText,
            onValueChange   = { state.priceText = it.filter { c -> c.isDigit() } },
            label           = { Text("Harga (Rp) *") },
            prefix          = { Text("Rp ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            isError         = state.priceText.isNotBlank() && (state.price == null || state.price!! <= 0),
            supportingText  = if (state.priceText.isNotBlank() && (state.price == null || state.price!! <= 0))
                { { Text("Harga harus lebih dari 0") } } else null,
            shape           = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value         = state.description,
            onValueChange = { state.description = it },
            label         = { Text("Deskripsi") },
            placeholder   = { Text("Deskripsi singkat produk (opsional)") },
            modifier      = Modifier.fillMaxWidth(),
            maxLines      = 3,
            shape         = MaterialTheme.shapes.medium
        )
    }

    // ── Identifikasi ──────────────────────────────────────────────────────
    FormSection("IDENTIFIKASI", Icons.Default.Tag) {
        if (isCompact) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkuField(state, Modifier.fillMaxWidth())
                BarcodeField(state, Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkuField(state, Modifier.weight(1f))
                BarcodeField(state, Modifier.weight(1f))
            }
        }
    }

    // ── Inventaris ────────────────────────────────────────────────────────
    FormSection("INVENTARIS", Icons.Default.Inventory) {
        if (isCompact) {
            OutlinedTextField(
                value         = state.unit,
                onValueChange = { state.unit = it },
                label         = { Text("Satuan") },
                placeholder   = { Text("pcs, kg, botol…") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = MaterialTheme.shapes.medium
            )
            if (editingProduct == null) {
                OutlinedTextField(
                    value           = state.stockText,
                    onValueChange   = { state.stockText = it.filter { c -> c.isDigit() || c == '.' } },
                    label           = { Text("Stok Awal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                    singleLine      = true,
                    shape           = MaterialTheme.shapes.medium
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = state.unit,
                    onValueChange = { state.unit = it },
                    label         = { Text("Satuan") },
                    placeholder   = { Text("pcs, kg, botol…") },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = MaterialTheme.shapes.medium
                )
                if (editingProduct == null) {
                    OutlinedTextField(
                        value           = state.stockText,
                        onValueChange   = { state.stockText = it.filter { c -> c.isDigit() || c == '.' } },
                        label           = { Text("Stok Awal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier        = Modifier.weight(1f),
                        singleLine      = true,
                        shape           = MaterialTheme.shapes.medium
                    )
                }
            }
        }

        // Dropdown kategori
        ExposedDropdownMenuBox(
            expanded         = state.categoryExpanded,
            onExpandedChange = { state.categoryExpanded = !state.categoryExpanded }
        ) {
            OutlinedTextField(
                value         = categories.find { it.uuid == state.categoryUuid }?.name ?: "Tanpa kategori",
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Kategori") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.categoryExpanded) },
                modifier      = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape         = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(
                expanded         = state.categoryExpanded,
                onDismissRequest = { state.categoryExpanded = false }
            ) {
                DropdownMenuItem(
                    text    = { Text("Tanpa kategori") },
                    onClick = { state.categoryUuid = null; state.categoryExpanded = false }
                )
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text    = { Text(cat.name) },
                        onClick = { state.categoryUuid = cat.uuid; state.categoryExpanded = false }
                    )
                }
            }
        }
    }

    // ── Pengaturan ────────────────────────────────────────────────────────
    FormSection("PENGATURAN", Icons.Default.Settings) {
        Surface(
            shape  = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color  = Color.Transparent
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Produk kadaluarsa (FIFO)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        "Stok masuk pertama akan keluar pertama",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.hasExpiry, onCheckedChange = { state.hasExpiry = it })
            }
        }
    }
}

// ── Gradient confirm button ───────────────────────────────────────────────────

@Composable
private fun GradientSaveButton(canConfirm: Boolean, isSubmitting: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (canConfirm) Brush.horizontalGradient(listOf(Primary, GradientEnd))
                else Brush.horizontalGradient(listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surfaceVariant
                ))
            )
            .clickable(
                enabled           = canConfirm,
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text(
                "Simpan",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = if (canConfirm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Dialog (phone) ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    editingProduct: Product?,
    categories: ImmutableList<Category>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Long, description: String?, sku: String?, barcode: String?,
                categoryUuid: String?, unit: String?, stock: Double, hasExpiry: Boolean) -> Unit,
    initialCategoryUuid: String? = null
) {
    val state = remember(editingProduct) { ProductFormState(editingProduct, initialCategoryUuid) }

    RancakFormDialog(
        icon             = Icons.Default.ShoppingBag,
        title            = if (editingProduct == null) "Tambah Produk" else "Edit Produk",
        subtitle         = if (editingProduct == null) "Isi detail produk baru" else "Perbarui informasi produk",
        onDismissRequest = onDismiss,
        confirmLabel     = "Simpan",
        onConfirm        = {
            val (a, b, c) = state.buildConfirmArgs()
            onConfirm(a.first, a.second, a.third, b.first, b.second, b.third, c.first, c.second, c.third)
        },
        confirmEnabled   = state.canConfirm(isSubmitting),
        isSubmitting     = isSubmitting,
        maxWidth         = 560.dp
    ) {
        ProductFormBody(state, editingProduct, categories, isCompact = true)
    }
}

// ── Inline panel (tablet) ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormPanel(
    editingProduct: Product?,
    initialCategoryUuid: String?,
    categories: ImmutableList<Category>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Long, description: String?, sku: String?, barcode: String?,
                categoryUuid: String?, unit: String?, stock: Double, hasExpiry: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = remember(editingProduct) { ProductFormState(editingProduct, initialCategoryUuid) }

    Column(modifier = modifier) {
        // ── Gradient header ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Primary, GradientEnd)))
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { if (!isSubmitting) onDismiss() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = if (editingProduct == null) "Tambah Produk" else "Edit Produk",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        text  = if (editingProduct == null) "Isi detail produk baru" else "Perbarui informasi produk",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // ── Form fields ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .widthIn(max = 560.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ProductFormBody(state, editingProduct, categories)
        }

        // ── Action buttons ────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            TextButton(onClick = { if (!isSubmitting) onDismiss() }, enabled = !isSubmitting) {
                Text("Batal")
            }
            Spacer(Modifier.width(10.dp))
            GradientSaveButton(
                canConfirm   = state.canConfirm(isSubmitting),
                isSubmitting = isSubmitting,
                modifier     = Modifier.width(140.dp)
            ) {
                val (a, b, c) = state.buildConfirmArgs()
                onConfirm(a.first, a.second, a.third, b.first, b.second, b.third, c.first, c.second, c.third)
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ProductFormDialogAddPreview() {
    RancakTheme {
        ProductFormDialog(
            editingProduct = null,
            categories     = listOf(
                Category("c1", "Makanan", null),
                Category("c2", "Minuman", null)
            ).toImmutableList(),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun ProductFormDialogEditPreview() {
    RancakTheme {
        ProductFormDialog(
            editingProduct = Product(
                uuid = "1", sku = "NGS-4821", barcode = null, name = "Nasi Goreng Spesial",
                description = "Nasi goreng dengan bumbu rahasia", category = Category("c1", "Makanan", null),
                price = 25000L, stock = 10.0, unit = "porsi",
                imageUrl = null, isActive = true, hasExpiry = false, updatedAt = null
            ),
            categories   = listOf(Category("c1", "Makanan", null), Category("c2", "Minuman", null)).toImmutableList(),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}
