package id.rancak.app.presentation.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    cartViewModel: CartViewModel = koinViewModel()
) {
    val uiState by cartViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (!uiState.isEmpty) {
                        TextButton(onClick = cartViewModel::clearCart) {
                            Text("Hapus Semua", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!uiState.isEmpty) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow(
                            label = "Subtotal (${uiState.itemCount} item)",
                            value = formatRupiah(uiState.subtotal),
                            isBold = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        RancakButton(
                            text = "Bayar ${formatRupiah(uiState.subtotal)}",
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isEmpty) {
            EmptyScreen(
                message = "Keranjang kosong.\nTambahkan produk dari layar kasir.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Order Type
                item {
                    Text(
                        "Tipe Pesanan",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OrderType.entries.forEach { type ->
                            FilterChip(
                                selected = uiState.orderType == type,
                                onClick = { cartViewModel.setOrderType(type) },
                                label = {
                                    Text(
                                        when (type) {
                                            OrderType.DINE_IN -> "Dine In"
                                            OrderType.TAKEAWAY -> "Takeaway"
                                            OrderType.DELIVERY -> "Delivery"
                                        }
                                    )
                                }
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }

                // Cart Items
                items(uiState.items, key = { "${it.productUuid}-${it.variantUuid}" }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrement = {
                            cartViewModel.updateQuantity(item.productUuid, item.variantUuid, item.qty + 1)
                        },
                        onDecrement = {
                            cartViewModel.updateQuantity(item.productUuid, item.variantUuid, item.qty - 1)
                        },
                        onRemove = {
                            cartViewModel.removeItem(item.productUuid, item.variantUuid)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall
                )
                if (item.variantName != null) {
                    Text(
                        text = item.variantName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatRupiah(item.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Qty Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "${item.qty}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = formatRupiah(item.subtotal),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
