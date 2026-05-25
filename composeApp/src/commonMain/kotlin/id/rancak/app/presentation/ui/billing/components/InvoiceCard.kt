package id.rancak.app.presentation.ui.billing.components

import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Invoice
import id.rancak.app.presentation.designsystem.Error
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.designsystem.Success
import id.rancak.app.presentation.designsystem.Warning
import id.rancak.app.presentation.ui.billing.formatPlanPrice

@Composable
fun InvoiceCard(
    invoice: Invoice,
    onCancel: () -> Unit,
    onShowQr: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusLabel, statusIcon) = when (invoice.status) {
        "paid"      -> Triple(Success,           "Lunas",       Icons.Default.CheckCircle)
        "pending"   -> Triple(Warning,           "Menunggu",    Icons.Default.HourglassTop)
        "cancelled" -> Triple(Color(0xFF9E9E9E), "Dibatalkan",  Icons.Default.Cancel)
        "expired"   -> Triple(Error,             "Kedaluwarsa", Icons.Default.ErrorOutline)
        else        -> Triple(Color(0xFF9E9E9E), invoice.status, Icons.Default.Info)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = MaterialTheme.shapes.large, color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(18.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(invoice.invoiceNo, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(invoice.planName, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SmallBadge(statusLabel, statusColor)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("${invoice.durationDays} hari", style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (invoice.issuedAt != null) {
                        Text("Terbit: ${invoice.issuedAt.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (invoice.dueAt != null) {
                        Text("Jatuh tempo: ${invoice.dueAt.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (invoice.status == "pending") Warning
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(formatPlanPrice(invoice.totalAmount), style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold, color = Primary)
                }
            }

            if (invoice.status == "pending" && invoice.qrString != null) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.QrCode, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("QRIS tersedia — selesaikan pembayaran via e-wallet",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f))
                    }
                }
            }

            if (invoice.status == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (invoice.qrString != null && onShowQr != null) {
                        Button(
                            onClick = onShowQr,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.extraLarge,
                            contentPadding = PaddingValues(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.QrCode, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Lihat QR", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.extraLarge,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Batalkan", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun InvoiceCardPendingPreview() {
    RancakTheme {
        InvoiceCard(
            invoice = Invoice("1", "INV-001", "pro", "Pro Plan", 30, 100000.0, 0.11, 11000.0, 111000.0,
                "pending", "2024-01-01", "2024-01-08", null, null, null, null, null, null, null, false),
            onCancel = {}
        )
    }
}

@Preview
@Composable
private fun InvoiceCardPaidPreview() {
    RancakTheme {
        InvoiceCard(
            invoice = Invoice("2", "INV-002", "pro", "Pro Plan", 30, 100000.0, 0.11, 11000.0, 111000.0,
                "paid", "2024-01-01", "2024-01-08", "2024-01-03", null, null, null, null, null, null, false),
            onCancel = {}
        )
    }
}
