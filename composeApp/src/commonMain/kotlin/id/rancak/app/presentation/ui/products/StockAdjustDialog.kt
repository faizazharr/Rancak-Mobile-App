package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun StockAdjustDialog(
    product: Product,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, note: String?) -> Unit
) {
    var adjustType   by remember { mutableStateOf("in") }
    var quantityText by remember { mutableStateOf("") }
    var noteText     by remember { mutableStateOf("") }

    val qty      = quantityText.toDoubleOrNull()
    val qtyError = when {
        quantityText.isBlank() -> null
        qty == null            -> "Angka tidak valid"
        qty <= 0               -> "Harus lebih dari 0"
        else                   -> null
    }
    val canConfirm = !isSubmitting && qty != null && qty > 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Sesuaikan Stok") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Stok saat ini: ${product.stock.toStockDisplay()} ${product.unit ?: ""}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected    = adjustType == "in",
                        onClick     = { adjustType = "in" },
                        label       = { Text("Masuk (+)") },
                        leadingIcon = { if (adjustType == "in") Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected    = adjustType == "out",
                        onClick     = { adjustType = "out" },
                        label       = { Text("Keluar (−)") },
                        leadingIcon = { if (adjustType == "out") Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    )
                }

                OutlinedTextField(
                    value          = quantityText,
                    onValueChange  = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label          = { Text("Jumlah *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError        = qtyError != null,
                    supportingText = qtyError?.let { { Text(it) } },
                    modifier       = Modifier.fillMaxWidth(),
                    singleLine     = true
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
            Button(onClick = { onConfirm(adjustType, qty!!, noteText.ifBlank { null }) }, enabled = canConfirm) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") }
        }
    )
}

// ── Inline panel (tablet) ─────────────────────────────────────────────────────

@Composable
fun StockAdjustPanel(
    product: Product,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, note: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var adjustType   by remember(product) { mutableStateOf("in") }
    var quantityText by remember(product) { mutableStateOf("") }
    var noteText     by remember(product) { mutableStateOf("") }

    val qty      = quantityText.toDoubleOrNull()
    val qtyError = when {
        quantityText.isBlank() -> null
        qty == null            -> "Angka tidak valid"
        qty <= 0               -> "Harus lebih dari 0"
        else                   -> null
    }
    val canConfirm = !isSubmitting && qty != null && qty > 0

    Column(modifier = modifier) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (!isSubmitting) onDismiss() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Column {
                Text(
                    text       = "Sesuaikan Stok",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = product.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider()

        // ── Form fields ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .widthIn(max = 480.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Stok saat ini: ${product.stock.toStockDisplay()} ${product.unit ?: ""}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected    = adjustType == "in",
                    onClick     = { adjustType = "in" },
                    label       = { Text("Masuk (+)") },
                    leadingIcon = { if (adjustType == "in") Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected    = adjustType == "out",
                    onClick     = { adjustType = "out" },
                    label       = { Text("Keluar (−)") },
                    leadingIcon = { if (adjustType == "out") Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                )
            }

            OutlinedTextField(
                value           = quantityText,
                onValueChange   = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                label           = { Text("Jumlah *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError         = qtyError != null,
                supportingText  = qtyError?.let { { Text(it) } },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true
            )

            OutlinedTextField(
                value         = noteText,
                onValueChange = { noteText = it },
                label         = { Text("Catatan (opsional)") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )
        }

        // ── Action buttons ────────────────────────────────────────────────────
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
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onConfirm(adjustType, qty!!, noteText.ifBlank { null }) },
                enabled = canConfirm
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun StockAdjustDialogPreview() {
    RancakTheme {
        StockAdjustDialog(
            product = Product(
                uuid = "1", sku = null, barcode = null, name = "Nasi Goreng",
                description = null, category = Category("c1", "Makanan", null),
                price = 25000L, stock = 8.0, unit = "porsi",
                imageUrl = null, isActive = true, hasExpiry = false, updatedAt = null
            ),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _ -> }
        )
    }
}
