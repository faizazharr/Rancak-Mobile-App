package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Voucher
import id.rancak.app.presentation.components.StatusChip
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

@Composable
fun VoucherCard(
    voucher: Voucher,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sem      = RancakColors.semantic
    val isPct    = voucher.discountType == "pct"
    val badgeColor = if (isPct) MaterialTheme.colorScheme.primary else sem.warning
    val typeLabel  = if (isPct) "${voucher.discountValue}%" else formatRupiah(voucher.discountValue)

    Card(
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Icon box ───────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalOffer, null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }

            // ── Info column ────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        voucher.code,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f, fill = false)
                    )
                    // Discount type badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            typeLabel,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = badgeColor
                        )
                    }
                    StatusChip(
                        text  = if (voucher.isActive) "Aktif" else "Nonaktif",
                        color = if (voucher.isActive) sem.success else MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    voucher.name,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Min purchase + max cap (if pct)
                val detailLine = buildString {
                    append("Min: ${formatRupiah(voucher.minPurchase)}")
                    if (isPct && voucher.maxDiscount != null && voucher.maxDiscount > 0)
                        append(" · Maks: ${formatRupiah(voucher.maxDiscount)}")
                }
                Text(detailLine, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Valid period
                if (voucher.validFrom != null || voucher.validUntil != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${voucher.validFrom?.take(10) ?: "–"} s/d ${voucher.validUntil?.take(10) ?: "∞"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Usage progress bar
                if (voucher.usageLimit != null && voucher.usageLimit > 0) {
                    val progress = (voucher.usageCount.toFloat() / voucher.usageLimit).coerceIn(0f, 1f)
                    val barColor = when {
                        progress >= 1f    -> MaterialTheme.colorScheme.error
                        progress >= 0.8f  -> sem.warning
                        else              -> sem.success
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Dipakai: ${voucher.usageCount} / ${voucher.usageLimit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress         = { progress },
                            modifier         = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                            color            = barColor,
                            trackColor       = barColor.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // ── Actions ────────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, "Edit", Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Hapus", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val sampleVoucher = Voucher(
    uuid = "1", code = "HEMAT20", name = "Hemat 20%", description = null,
    discountType = "percentage", discountValue = 20L, maxDiscount = 50000L,
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
