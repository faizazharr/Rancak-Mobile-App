package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.DiscountRule
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun DiscountFormDialog(
    editing: DiscountRule?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, discountValue: Double, discountType: String,
        ruleType: String, isActive: Boolean, description: String?,
        maxDiscount: Double?, minPurchaseAmount: Double?
    ) -> Unit
) {
    var name          by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountValue by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var discountType  by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var ruleType      by remember(editing) { mutableStateOf(editing?.ruleType ?: "always") }
    var isActive      by remember(editing) { mutableStateOf(editing?.isActive ?: true) }
    var description   by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount   by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase   by remember(editing) { mutableStateOf(editing?.minPurchaseAmount?.toString() ?: "") }

    val isPct = discountType == "pct"
    val discountNum = discountValue.toDoubleOrNull()
    val discountError = when {
        discountValue.isBlank() -> null
        discountNum == null -> "Nilai tidak valid"
        discountNum <= 0 -> "Nilai harus lebih dari 0"
        isPct && discountNum > 100 -> "Persen tidak boleh melebihi 100"
        else -> null
    }
    val maxDiscountNum = maxDiscount.toDoubleOrNull()
    val maxDiscountError = when {
        maxDiscount.isBlank() -> null
        maxDiscountNum == null -> "Nilai tidak valid"
        maxDiscountNum <= 0 -> "Harus lebih dari 0"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        name.isNotBlank() &&
        discountValue.isNotBlank() && discountError == null &&
        maxDiscountError == null

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Aturan Diskon" else "Edit Aturan Diskon") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pct" to "Persen (%)", "flat" to "Nominal (Rp)").forEach { (value, label) ->
                        FilterChip(
                            selected = discountType == value,
                            onClick  = { discountType = value },
                            label    = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isPct) "Nilai Diskon (%) *" else "Nilai Diskon (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = discountError != null,
                    supportingText = discountError?.let { err -> { Text(err) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Jenis Aturan", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("always" to "Selalu", "time_based" to "Berbasis Waktu").forEach { (value, label) ->
                        FilterChip(
                            selected = ruleType == value,
                            onClick  = { ruleType = value },
                            label    = { Text(label) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minPurchase,
                        onValueChange = { minPurchase = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Min. Pembelian (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("0 = tanpa minimum") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Maks. Diskon (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = maxDiscountError != null,
                        supportingText = maxDiscountError?.let { err -> { Text(err) } },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Aktif", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name.trim(), discountValue.toDoubleOrNull() ?: 0.0, discountType,
                        ruleType, isActive, description.ifBlank { null },
                        maxDiscount.toDoubleOrNull(), minPurchase.toDoubleOrNull()
                    )
                },
                enabled = canConfirm
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun DiscountFormDialogAddPreview() {
    RancakTheme {
        DiscountFormDialog(
            editing      = null,
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun DiscountFormDialogEditPreview() {
    RancakTheme {
        DiscountFormDialog(
            editing      = DiscountRule("1", "Diskon Happy Hour", null, "time_based", "pct", 10.0, null, null, null, null, 0, false, null, true),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _, _, _, _ -> }
        )
    }
}
