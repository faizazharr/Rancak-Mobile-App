package id.rancak.app.presentation.ui.billing.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Plan
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.billing.formatPlanPrice

@Composable
fun SubscribeConfirmDialog(
    plan: Plan,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        icon = { Icon(Icons.Default.Stars, contentDescription = null, tint = Primary) },
        title = { Text("Berlangganan Paket") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Anda akan membuat invoice untuk:")
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(plan.name)
                        Text(formatPlanPrice(plan.totalPrice), style = MaterialTheme.typography.titleMedium,
                            color = Primary)
                        Text("${plan.durationDays} hari", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("Invoice akan dikirim. Selesaikan pembayaran sebelum jatuh tempo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Buat Invoice")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SubscribeConfirmDialogPreview() {
    RancakTheme {
        SubscribeConfirmDialog(
            plan = Plan("1", "pro", "Pro Plan", null, 100000.0, 0.11, 30, 5, false, 111000.0),
            isSubmitting = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
