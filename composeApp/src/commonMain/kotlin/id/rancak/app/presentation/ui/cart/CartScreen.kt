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
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartUiState
import id.rancak.app.presentation.viewmodel.CartViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    cartViewModel: CartViewModel
) {
    val uiState by cartViewModel.uiState.collectAsState()

    CartScreenContent(
        uiState    = uiState,
        onBack     = onBack,
        onCheckout = onCheckout,
        onClearCart = cartViewModel::clearCart,
        onSetOrderType = { cartViewModel.setOrderType(it) },
        onUpdateQty = { productUuid, variantUuid, qty ->
            cartViewModel.updateQuantity(productUuid, variantUuid, qty)
        },
        onRemoveItem = { productUuid, variantUuid ->
            cartViewModel.removeItem(productUuid, variantUuid)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreenContent(
    uiState: CartUiState,
    onBack: () -> Unit = {},
    onCheckout: () -> Unit = {},
    onClearCart: () -> Unit = {},
    onSetOrderType: (OrderType) -> Unit = {},
    onUpdateQty: (String, String?, Int) -> Unit = { _, _, _ -> },
    onRemoveItem: (String, String?) -> Unit = { _, _ -> }
) {
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
                        TextButton(onClick = onClearCart) {
                            Text("Hapus Semua", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!uiState.isEmpty) {
                Surface(
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${uiState.itemCount} item",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatRupiah(uiState.subtotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        RancakButton(
                            text = "Bayar",
                            onClick = onCheckout,
                            modifier = Modifier.width(120.dp)
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
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Order Type
                item {
                    Text(
                        "Tipe Pesanan",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OrderType.entries.forEach { type ->
                            FilterChip(
                                selected = uiState.orderType == type,
                                onClick = { onSetOrderType(type) },
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                }

                // Cart Items
                items(uiState.items, key = { "${it.productUuid}-${it.variantUuid}" }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrement = {
                            onUpdateQty(item.productUuid, item.variantUuid, item.qty + 1)
                        },
                        onDecrement = {
                            onUpdateQty(item.productUuid, item.variantUuid, item.qty - 1)
                        },
                        onRemove = {
                            onRemoveItem(item.productUuid, item.variantUuid)
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (item.variantName != null) {
                    Text(
                        text = item.variantName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatRupiah(item.price),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Qty Controls — compact
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
                }
                Text(
                    text = "${item.qty}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.width(6.dp))

            Text(
                text = formatRupiah(item.subtotal),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(72.dp)
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ── Previews — call actual CartScreenContent ──

@Preview
@Composable
private fun CartScreenEmptyPreview() {
    RancakTheme {
        CartScreenContent(
            uiState = CartUiState()
        )
    }
}

@Preview
@Composable
private fun CartScreenWithItemsPreview() {
    RancakTheme {
        CartScreenContent(
            uiState = CartUiState(
                items = listOf(
                    CartItem(
                        productUuid = "1",
                        productName = "Nasi Goreng Spesial",
                        qty = 2,
                        price = 25000,
                        variantName = "Pedas"
                    ),
                    CartItem(
                        productUuid = "2",
                        productName = "Es Teh Manis",
                        qty = 3,
                        price = 8000
                    )
                ),
                orderType = OrderType.DINE_IN
            )
        )
    }
}
