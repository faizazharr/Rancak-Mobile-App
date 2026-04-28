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
import id.rancak.app.domain.model.Voucher
import id.rancak.app.presentation.components.DatePickerField
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun VoucherFormDialog(
    editing: Voucher?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        code: String, name: String, discountType: String, discountValue: String,
        validFrom: String, description: String?, maxDiscount: String?,
        minPurchase: String, usageLimit: Int?, validUntil: String?, isActive: Boolean
    ) -> Unit
) {
    var code           by remember(editing) { mutableStateOf(editing?.code ?: "") }
    var name           by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountType   by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var discountValue  by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var validFrom      by remember(editing) { mutableStateOf(editing?.validFrom?.take(10) ?: "") }
    var validUntil     by remember(editing) { mutableStateOf(editing?.validUntil?.take(10) ?: "") }
    var description    by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount    by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase    by remember(editing) { mutableStateOf(editing?.minPurchase?.toString() ?: "0") }
    var usageLimitText by remember(editing) { mutableStateOf(editing?.usageLimit?.toString() ?: "") }
    var isActive       by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

    val canConfirm = !isSubmitting && code.isNotBlank() && name.isNotBlank() &&
                     discountValue.isNotBlank() && validFrom.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Voucher" else "Edit Voucher") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Kode *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nama *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)

                Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pct" to "Persen (%)", "flat" to "Nominal (Rp)").forEach { (value, label) ->
                        FilterChip(selected = discountType == value,
                            onClick = { discountType = value }, label = { Text(label) })
                    }
                }

                OutlinedTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Nilai Diskon *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        label = "Berlaku Dari *",
                        value = validFrom,
                        onDateSelected = { validFrom = it },
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label = "Berlaku Sampai",
                        value = validUntil,
                        onDateSelected = { validUntil = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minPurchase,
                        onValueChange = { minPurchase = it.filter { c -> c.isDigit() } },
                        label = { Text("Min. Pembelian") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() } },
                        label = { Text("Maks. Diskon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                }

                OutlinedTextField(
                    value = usageLimitText,
                    onValueChange = { usageLimitText = it.filter { c -> c.isDigit() } },
                    label = { Text("Batas Penggunaan") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Aktif", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        code.trim(), name.trim(), discountType, discountValue, validFrom,
                        description.ifBlank { null }, maxDiscount.ifBlank { null },
                        minPurchase.ifBlank { "0" }, usageLimitText.toIntOrNull(),
                        validUntil.ifBlank { null }, isActive
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
private fun VoucherFormDialogAddPreview() {
    RancakTheme {
        VoucherFormDialog(editing = null, isSubmitting = false, onDismiss = {},
            onConfirm = { _, _, _, _, _, _, _, _, _, _, _ -> })
    }
}
