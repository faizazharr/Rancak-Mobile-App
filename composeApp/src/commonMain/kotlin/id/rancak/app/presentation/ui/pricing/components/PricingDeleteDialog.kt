package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun PricingDeleteDialog(
    name: String,
    entity: String,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Hapus ${entity.replaceFirstChar { it.uppercase() }}") },
        text  = { Text("Hapus $entity \"$name\"? Tindakan ini tidak dapat dibatalkan.") },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp))
                else Text("Hapus", color = MaterialTheme.colorScheme.error)
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
private fun PricingDeleteDialogPreview() {
    RancakTheme {
        PricingDeleteDialog(
            name         = "PPN 11%",
            entity       = "pajak",
            isSubmitting = false,
            onConfirm    = {},
            onDismiss    = {}
        )
    }
}
