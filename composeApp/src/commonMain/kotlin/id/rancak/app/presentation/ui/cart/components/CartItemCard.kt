package id.rancak.app.presentation.ui.cart.components

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
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape  = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (item.variantName != null) {
                    Text(item.variantName, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(formatRupiah(item.price), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
                }
                Text("${item.qty}", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.width(6.dp))

            Text(formatRupiah(item.subtotal), style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))

            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun CartItemCardPreview() {
    RancakTheme {
        CartItemCard(
            item = CartItem(
                productUuid = "p1", productName = "Nasi Goreng Spesial",
                qty = 2, price = 25000L, variantName = "Pedas"
            ),
            onIncrement = {}, onDecrement = {}, onRemove = {}
        )
    }
}
