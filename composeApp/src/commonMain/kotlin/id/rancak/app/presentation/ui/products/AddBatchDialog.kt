package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun AddBatchDialog(
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
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value          = quantityText,
                    onValueChange  = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label          = { Text("Jumlah *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier       = Modifier.fillMaxWidth(),
                    singleLine     = true,
                    isError        = quantityText.isNotBlank() && (qty == null || qty <= 0),
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
                    value          = costPriceText,
                    onValueChange  = { costPriceText = it.filter { c -> c.isDigit() } },
                    label          = { Text("Harga Beli (opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier       = Modifier.fillMaxWidth(),
                    singleLine     = true
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
private fun AddBatchDialogPreview() {
    RancakTheme {
        AddBatchDialog(
            product = Product(
                uuid = "1", sku = null, barcode = null, name = "Susu UHT Full Cream",
                description = null, category = Category("c1", "Minuman", null),
                price = 12000L, stock = 3.0, unit = "karton",
                imageUrl = null, isActive = true, hasExpiry = true, updatedAt = null
            ),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _ -> }
        )
    }
}
