package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.RancakTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    editingProduct: Product?,
    categories: List<Category>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Long, description: String?, sku: String?, barcode: String?,
                categoryUuid: String?, unit: String?, stock: Double, hasExpiry: Boolean) -> Unit
) {
    var name             by remember(editingProduct) { mutableStateOf(editingProduct?.name ?: "") }
    var priceText        by remember(editingProduct) { mutableStateOf(editingProduct?.price?.toString() ?: "") }
    var description      by remember(editingProduct) { mutableStateOf(editingProduct?.description ?: "") }
    var sku              by remember(editingProduct) { mutableStateOf(editingProduct?.sku ?: "") }
    var barcode          by remember(editingProduct) { mutableStateOf(editingProduct?.barcode ?: "") }
    var unit             by remember(editingProduct) { mutableStateOf(editingProduct?.unit ?: "") }
    var stockText        by remember(editingProduct) { mutableStateOf(if (editingProduct == null) "0" else editingProduct.stock.toStockDisplay()) }
    var hasExpiry        by remember(editingProduct) { mutableStateOf(editingProduct?.hasExpiry ?: false) }
    var categoryUuid     by remember(editingProduct) { mutableStateOf(editingProduct?.category?.uuid) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val price      = priceText.toLongOrNull()
    val stock      = stockText.toDoubleOrNull() ?: 0.0
    val canConfirm = !isSubmitting && name.isNotBlank() && price != null && price > 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editingProduct == null) "Tambah Produk" else "Edit Produk") },
        text  = {
            Column(
                modifier            = Modifier.verticalScroll(rememberScrollState()),
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
                    value          = priceText,
                    onValueChange  = { priceText = it.filter { c -> c.isDigit() } },
                    label          = { Text("Harga (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier       = Modifier.fillMaxWidth(),
                    singleLine     = true,
                    isError        = priceText.isNotBlank() && (price == null || price <= 0),
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

                // ── SKU + Barcode dengan tombol generate otomatis ─────────────
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
                                    Icon(Icons.Default.Refresh, "Generate SKU", Modifier.size(16.dp))
                                }
                            } else {
                                IconButton(onClick = { sku = "" }) {
                                    Icon(Icons.Default.Clear, "Hapus SKU", Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                    OutlinedTextField(
                        value          = barcode,
                        onValueChange  = { barcode = it.filter { c -> c.isDigit() } },
                        label          = { Text("Barcode") },
                        placeholder    = { Text("Otomatis", style = MaterialTheme.typography.bodySmall) },
                        modifier       = Modifier.weight(1f),
                        singleLine     = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon   = {
                            if (barcode.isBlank()) {
                                IconButton(onClick = { barcode = generateBarcode() }) {
                                    Icon(Icons.Default.Refresh, "Generate Barcode", Modifier.size(16.dp))
                                }
                            } else {
                                IconButton(onClick = { barcode = "" }) {
                                    Icon(Icons.Default.Clear, "Hapus Barcode", Modifier.size(16.dp))
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
                            value          = stockText,
                            onValueChange  = { stockText = it.filter { c -> c.isDigit() || c == '.' } },
                            label          = { Text("Stok Awal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier       = Modifier.weight(1f),
                            singleLine     = true
                        )
                    }
                }

                // ── Dropdown kategori ─────────────────────────────────────────
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
                        modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded         = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text    = { Text("Tanpa kategori") },
                            onClick = { categoryUuid = null; categoryExpanded = false }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat.name) },
                                onClick = { categoryUuid = cat.uuid; categoryExpanded = false }
                            )
                        }
                    }
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
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
                        name.trim(), price!!,
                        description.ifBlank { null },
                        sku.ifBlank { null },
                        barcode.ifBlank { null },
                        categoryUuid,
                        unit.ifBlank { null },
                        stock, hasExpiry
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
            ),
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
            categories   = listOf(Category("c1", "Makanan", null), Category("c2", "Minuman", null)),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}
