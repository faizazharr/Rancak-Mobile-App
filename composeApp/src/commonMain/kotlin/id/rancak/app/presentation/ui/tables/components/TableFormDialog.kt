package id.rancak.app.presentation.ui.tables.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.components.RancakFormDialog
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Dialog form tambah/edit meja. Dipakai admin di [TableMapScreen] saat
 * `adminMode` aktif.
 *
 * Validasi minimal: `name` tidak boleh kosong, `capacity` ≥ 1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableFormDialog(
    editingTable: Table?,
    isSubmitting: Boolean,
    existingAreas: ImmutableList<String> = persistentListOf(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, area: String?, capacity: Int, isActive: Boolean, sortOrder: Int) -> Unit
) {
    var name         by remember(editingTable) { mutableStateOf(editingTable?.name ?: "") }
    var area         by remember(editingTable) { mutableStateOf(editingTable?.area ?: "") }
    var areaMenuOpen by remember { mutableStateOf(false) }
    var capacityStr  by remember(editingTable) { mutableStateOf((editingTable?.capacity ?: 2).toString()) }
    var sortOrderStr by remember(editingTable) { mutableStateOf((editingTable?.sortOrder ?: 0).toString()) }
    var isActive     by remember(editingTable) { mutableStateOf(editingTable?.isActive ?: true) }

    val areaSuggestions = remember(existingAreas, area) {
        existingAreas.filter { it.contains(area, ignoreCase = true) || area.isBlank() }
    }

    val capacity   = capacityStr.toIntOrNull() ?: 0
    val sortOrder  = sortOrderStr.toIntOrNull() ?: 0
    val canConfirm = !isSubmitting && name.isNotBlank() && capacity >= 1

    RancakFormDialog(
        icon             = Icons.Default.TableBar,
        title            = if (editingTable == null) "Tambah Meja" else "Edit Meja",
        subtitle         = if (editingTable == null) "Tambahkan meja baru ke denah" else "Perbarui informasi meja",
        onDismissRequest = onDismiss,
        confirmLabel     = "Simpan",
        onConfirm        = {
            onConfirm(
                name.trim(),
                area.ifBlank { null }?.trim(),
                capacity,
                isActive,
                sortOrder
            )
        },
        confirmEnabled   = canConfirm,
        isSubmitting     = isSubmitting
    ) {
        OutlinedTextField(
            value         = name,
            onValueChange = { name = it },
            label         = { Text("Nama Meja *") },
            placeholder   = { Text("mis. Meja 1, A1, VIP-3") },
            singleLine    = true,
            isError       = name.isBlank(),
            modifier      = Modifier.fillMaxWidth(),
            shape         = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenuBox(
            expanded         = areaMenuOpen && areaSuggestions.isNotEmpty(),
            onExpandedChange = { areaMenuOpen = it }
        ) {
            OutlinedTextField(
                value         = area,
                onValueChange = { area = it; areaMenuOpen = true },
                label         = { Text("Area") },
                placeholder   = { Text("mis. Indoor, Outdoor, Lt. 2") },
                singleLine    = true,
                trailingIcon  = {
                    if (areaSuggestions.isNotEmpty())
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaMenuOpen)
                },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .onFocusChanged { if (it.isFocused) areaMenuOpen = true },
                shape         = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(
                expanded         = areaMenuOpen && areaSuggestions.isNotEmpty(),
                onDismissRequest = { areaMenuOpen = false }
            ) {
                areaSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text    = { Text(suggestion) },
                        onClick = { area = suggestion; areaMenuOpen = false }
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value           = capacityStr,
                onValueChange   = { capacityStr = it.filter { c -> c.isDigit() } },
                label           = { Text("Kapasitas *") },
                suffix          = { Text("kursi") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError         = capacity < 1,
                modifier        = Modifier.weight(1f),
                shape           = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value           = sortOrderStr,
                onValueChange   = { sortOrderStr = it.filter { c -> c.isDigit() } },
                label           = { Text("Urutan") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1f),
                shape           = MaterialTheme.shapes.medium
            )
        }
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth()
        ) {
            Text("Aktif", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = isActive, onCheckedChange = { isActive = it })
        }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TableFormDialogAddPreview() {
    RancakTheme {
        TableFormDialog(
            editingTable = null,
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun TableFormDialogEditPreview() {
    RancakTheme {
        TableFormDialog(
            editingTable = Table(
                uuid = "t1", name = "VIP-1", area = "Lantai 2", capacity = 6,
                status = TableStatus.AVAILABLE, isActive = true, sortOrder = 3, activeSaleUuid = null
            ),
            isSubmitting = false,
            onDismiss    = {},
            onConfirm    = { _, _, _, _, _ -> }
        )
    }
}
