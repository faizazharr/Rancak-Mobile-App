package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun CreateOpnameDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Buat Sesi Opname Baru") },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan (opsional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(note.ifBlank { null }) }, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Buat")
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
private fun CreateOpnameDialogPreview() {
    RancakTheme {
        CreateOpnameDialog(isSubmitting = false, onDismiss = {}, onConfirm = {})
    }
}
