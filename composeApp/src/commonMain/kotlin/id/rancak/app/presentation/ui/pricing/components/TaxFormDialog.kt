package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.presentation.designsystem.RancakTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxFormDialog(
    editing: TaxConfig?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, rate: String, applyTo: String, sortOrder: Int) -> Unit
) {
    var name      by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var rate      by remember(editing) { mutableStateOf(editing?.rate?.toString() ?: "") }
    var applyTo   by remember(editing) { mutableStateOf(editing?.applyTo ?: "after_discount") }
    var sortOrder by remember(editing) { mutableStateOf(editing?.sortOrder?.toString() ?: "0") }
    var expanded  by remember { mutableStateOf(false) }

    val applyToOptions = listOf("after_discount" to "Setelah Diskon", "before_discount" to "Sebelum Diskon")

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Pajak" else "Edit Pajak") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Tarif (%) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = applyToOptions.firstOrNull { it.first == applyTo }?.second ?: applyTo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Diterapkan Pada") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        applyToOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { applyTo = value; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = sortOrder,
                    onValueChange = { sortOrder = it.filter { c -> c.isDigit() } },
                    label = { Text("Urutan Tampil") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), rate, applyTo, sortOrder.toIntOrNull() ?: 0) },
                enabled = !isSubmitting && name.isNotBlank() && rate.isNotBlank()
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
private fun TaxFormDialogAddPreview() {
    RancakTheme {
        TaxFormDialog(
            editing      = null,
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun TaxFormDialogEditPreview() {
    RancakTheme {
        TaxFormDialog(
            editing      = TaxConfig("1", "PPN", 11.0, "after_discount", 1, true),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _ -> }
        )
    }
}
