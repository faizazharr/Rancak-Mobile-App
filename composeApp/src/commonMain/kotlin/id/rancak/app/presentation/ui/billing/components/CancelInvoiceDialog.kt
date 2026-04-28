package id.rancak.app.presentation.ui.billing.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Invoice
import id.rancak.app.presentation.designsystem.*

@Composable
fun CancelInvoiceDialog(
    invoice: Invoice,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = Error) },
        title = { Text("Batalkan Invoice") },
        text = { Text("Apakah Anda yakin ingin membatalkan invoice ${invoice.invoiceNo}? Tindakan ini tidak dapat dibatalkan.") },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Error)) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Ya, Batalkan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Tidak") } }
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun CancelInvoiceDialogPreview() {
    RancakTheme {
        CancelInvoiceDialog(
            invoice = Invoice("1", "INV-001", "pro", "Pro Plan", 30, 100000.0, 0.11, 11000.0, 111000.0,
                "pending", "2024-01-01", "2024-01-08", null, null, null, null, null, null, null, false),
            isSubmitting = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
