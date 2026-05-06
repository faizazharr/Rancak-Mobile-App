package id.rancak.app.presentation.ui.products

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
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.components.StatusChip
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

@Composable
fun ProductManagementCard(
    product: Product,
    is86: Boolean,
    onAdjustStock: () -> Unit,
    onAddBatch: () -> Unit,
    on86Toggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sem = RancakColors.semantic
    val stockColor = when {
        is86               -> MaterialTheme.colorScheme.error
        product.stock <= 0 -> MaterialTheme.colorScheme.error
        product.stock <= 5 -> sem.warning
        else               -> sem.success
    }
    val stockLabel = when {
        is86               -> "86 – Tidak Tersedia"
        product.stock <= 0 -> "Stok habis"
        else               -> "Stok: ${product.stock.toStockDisplay()}"
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = MaterialTheme.shapes.medium,
        colors    = if (is86) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        ) else CardDefaults.cardColors()
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header: nama + meta kiri | harga + aksi kanan ────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                // Kiri: nama, SKU, kategori, stok
                Column(
                    modifier            = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text       = product.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!product.sku.isNullOrBlank()) {
                        Text(
                            text  = "SKU: ${product.sku}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (product.category != null) {
                        Text(
                            text  = product.category.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (product.hasExpiry) {
                        Text(
                            text  = "Kadaluarsa (FIFO)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    StatusChip(text = stockLabel, color = stockColor)
                }

                // Kanan: harga + tombol edit/hapus
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = formatRupiah(product.price),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick  = onEdit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit produk",
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                        FilledTonalIconButton(
                            onClick  = onDelete,
                            modifier = Modifier.size(36.dp),
                            colors   = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor   = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Hapus produk",
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Tombol aksi ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick  = onAdjustStock,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Tune, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sesuaikan Stok", style = MaterialTheme.typography.labelMedium)
                }

                if (product.hasExpiry) {
                    OutlinedButton(
                        onClick  = onAddBatch,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddBox, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tambah Batch", style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedButton(
                    onClick  = on86Toggle,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (is86) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector        = if (is86) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = if (is86) "Aktifkan" else "86",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ProductManagementCardPreview() {
    RancakTheme {
        ProductManagementCard(
            product = Product(
                uuid        = "1",
                sku         = "NGS-4821",
                barcode     = null,
                name        = "Nasi Goreng Spesial",
                description = null,
                category    = Category("c1", "Makanan", null),
                price       = 25000L,
                stock       = 10.0,
                unit        = "porsi",
                imageUrl    = null,
                isActive    = true,
                hasExpiry   = false,
                updatedAt   = null
            ),
            is86         = false,
            onAdjustStock = {},
            onAddBatch   = {},
            on86Toggle   = {},
            onEdit       = {},
            onDelete     = {}
        )
    }
}

@Preview
@Composable
private fun ProductManagementCard86Preview() {
    RancakTheme {
        ProductManagementCard(
            product = Product(
                uuid        = "2",
                sku         = null,
                barcode     = null,
                name        = "Es Teh Manis",
                description = null,
                category    = null,
                price       = 5000L,
                stock       = 0.0,
                unit        = "gelas",
                imageUrl    = null,
                isActive    = true,
                hasExpiry   = false,
                updatedAt   = null
            ),
            is86         = true,
            onAdjustStock = {},
            onAddBatch   = {},
            on86Toggle   = {},
            onEdit       = {},
            onDelete     = {}
        )
    }
}
