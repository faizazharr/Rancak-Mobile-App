package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Voucher
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

@Composable
fun VoucherCard(
    voucher: Voucher,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeLabel = if (voucher.discountType == "pct") "${voucher.discountValue}%"
                   else formatRupiah(voucher.discountValue)

    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(voucher.code, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (voucher.isActive) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            if (voucher.isActive) "Aktif" else "Nonaktif",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (voucher.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Text(voucher.name, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Diskon: $typeLabel · Min: ${formatRupiah(voucher.minPurchase)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary)
                if (voucher.validFrom != null || voucher.validUntil != null) {
                    Text("${voucher.validFrom?.take(10) ?: "–"} s/d ${voucher.validUntil?.take(10) ?: "∞"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (voucher.usageLimit != null) {
                    Text("Dipakai: ${voucher.usageCount} / ${voucher.usageLimit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val sampleVoucher = Voucher(
    uuid = "1", code = "HEMAT20", name = "Hemat 20%", description = null,
    discountType = "pct", discountValue = 20L, maxDiscount = 50000L,
    minPurchase = 100000L, usageLimit = 100, usageCount = 45,
    validFrom = "2024-01-01", validUntil = "2024-12-31", isActive = true
)

@Preview
@Composable
private fun VoucherCardActivePreview() {
    RancakTheme {
        VoucherCard(voucher = sampleVoucher, onEdit = {}, onDelete = {})
    }
}

@Preview
@Composable
private fun VoucherCardInactivePreview() {
    RancakTheme {
        VoucherCard(voucher = sampleVoucher.copy(isActive = false), onEdit = {}, onDelete = {})
    }
}
