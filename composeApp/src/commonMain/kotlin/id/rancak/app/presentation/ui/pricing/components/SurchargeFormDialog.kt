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
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.presentation.designsystem.RancakTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurchargeFormDialog(
    editing: Surcharge?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (orderType: String, name: String, amount: String, isPercentage: Boolean, maxAmount: String?) -> Unit
) {
    var orderType    by remember(editing) { mutableStateOf(editing?.orderType ?: "all") }
    var name         by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var amount       by remember(editing) { mutableStateOf(editing?.amount?.toString() ?: "") }
    var isPercentage by remember(editing) { mutableStateOf(editing?.isPercentage ?: false) }
    var maxAmount    by remember(editing) { mutableStateOf(editing?.maxAmount?.toString() ?: "") }

    val amountNum = amount.toDoubleOrNull()
    val amountError = when {
        amount.isBlank() -> null
        amountNum == null -> "Nilai tidak valid"
        amountNum <= 0 -> "Nilai harus lebih dari 0"
        isPercentage && amountNum > 100 -> "Persen tidak boleh melebihi 100"
        else -> null
    }
    val maxAmountNum = maxAmount.toDoubleOrNull()
    val maxAmountError = when {
        !isPercentage || maxAmount.isBlank() -> null
        maxAmountNum == null -> "Nilai tidak valid"
        maxAmountNum <= 0 -> "Harus lebih dari 0"
        else -> null
    }
    val canConfirm = !isSubmitting &&
        name.isNotBlank() &&
        amount.isNotBlank() && amountError == null &&
        maxAmountError == null

    val orderTypes = listOf("all" to "Semua", "dine_in" to "Dine In", "takeaway" to "Takeaway", "delivery" to "Delivery")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Surcharge" else "Edit Surcharge") },
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

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = orderTypes.firstOrNull { it.first == orderType }?.second ?: orderType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipe Pesanan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        orderTypes.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { orderType = value; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isPercentage) "Jumlah (%) *" else "Jumlah (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError != null,
                    supportingText = amountError?.let { err -> { Text(err) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Berbasis Persentase", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isPercentage, onCheckedChange = { isPercentage = it })
                }

                if (isPercentage) {
                    OutlinedTextField(
                        value = maxAmount,
                        onValueChange = { maxAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Maks. Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = maxAmountError != null,
                        supportingText = maxAmountError?.let { err -> { Text(err) } } ?: { Text("Batasi nominal meski persennya besar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(orderType, name.trim(), amount, isPercentage, maxAmount.ifBlank { null }) },
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
private fun SurchargeFormDialogAddPreview() {
    RancakTheme {
        SurchargeFormDialog(
            editing      = null,
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun SurchargeFormDialogEditPreview() {
    RancakTheme {
        SurchargeFormDialog(
            editing      = Surcharge("1", "all", "Biaya Layanan", 5L, true, null, true, 0),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _ -> }
        )
    }
}
