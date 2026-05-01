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
        is86               -> "86"
        product.stock <= 0 -> "Stok habis"
        else               -> "Stok: ${product.stock.toStockDisplay()}"
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape     = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ── Header: nama + harga + badge stok + edit/hapus ────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text       = product.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!product.sku.isNullOrBlank()) {
                        Text(
                            text  = "SKU: ${product.sku}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (product.category != null) {
                        Text(
                            text  = product.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (product.hasExpiry) {
                        Text(
                            text  = "Produk kadaluarsa (FIFO)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = formatRupiah(product.price),
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit produk",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Hapus produk",
                                modifier = Modifier.size(16.dp),
                                tint     = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    StatusChip(text = stockLabel, color = stockColor)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Tombol aksi ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick        = onAdjustStock,
                    modifier       = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Tune, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sesuaikan Stok", style = MaterialTheme.typography.labelSmall)
                }

                if (product.hasExpiry) {
                    OutlinedButton(
                        onClick        = onAddBatch,
                        modifier       = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddBox, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah Batch", style = MaterialTheme.typography.labelSmall)
                    }
                }

                OutlinedButton(
                    onClick        = on86Toggle,
                    modifier       = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    colors         = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (is86) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector        = if (is86) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = if (is86) "Aktifkan" else "86",
                        style = MaterialTheme.typography.labelSmall
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
