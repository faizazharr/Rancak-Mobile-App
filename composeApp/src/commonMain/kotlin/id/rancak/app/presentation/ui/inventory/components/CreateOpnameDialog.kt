package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.presentation.components.RancakFormDialog
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun CreateOpnameDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var note by remember { mutableStateOf("") }

    RancakFormDialog(
        icon             = Icons.Default.Inventory,
        title            = "Buat Sesi Opname Baru",
        subtitle         = "Mulai sesi penghitungan stok fisik",
        onDismissRequest = onDismiss,
        confirmLabel     = "Buat Sesi",
        onConfirm        = { onConfirm(note.ifBlank { null }) },
        confirmEnabled   = !isSubmitting,
        isSubmitting     = isSubmitting
    ) {
        OutlinedTextField(
            value         = note,
            onValueChange = { note = it },
            label         = { Text("Catatan (opsional)") },
            placeholder   = { Text("Contoh: Opname bulanan Mei 2026") },
            modifier      = Modifier.fillMaxWidth(),
            maxLines      = 3,
            shape         = MaterialTheme.shapes.medium
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun CreateOpnameDialogPreview() {
    RancakTheme {
        CreateOpnameDialog(isSubmitting = false, onDismiss = {}, onConfirm = {})
    }
}
