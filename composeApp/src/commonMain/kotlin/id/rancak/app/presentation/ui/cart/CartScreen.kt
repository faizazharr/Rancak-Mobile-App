package id.rancak.app.presentation.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.CartItem
import id.rancak.app.presentation.ui.cart.components.CartItemCard
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakElevation
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.navigation.LocalCartViewModel
import id.rancak.app.presentation.viewmodel.CartUiState
import id.rancak.app.presentation.viewmodel.ShiftViewModel
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val cartViewModel  = LocalCartViewModel.current
    val shiftViewModel: ShiftViewModel = koinViewModel()
    val uiState    by cartViewModel.uiState.collectAsStateWithLifecycle()
    val shiftState by shiftViewModel.uiState.collectAsStateWithLifecycle()
    val hasOpenShift = shiftState.currentShift != null

    LaunchedEffect(Unit) { shiftViewModel.loadCurrentShift() }

    CartScreenContent(
        uiState    = uiState,
        onBack     = onBack,
        onCheckout = onCheckout,
        hasOpenShift = hasOpenShift,
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

@Composable
fun CartScreenContent(
    uiState: CartUiState,
    onBack: () -> Unit = {},
    onCheckout: () -> Unit = {},
    hasOpenShift: Boolean = true,
    onClearCart: () -> Unit = {},
    onSetOrderType: (OrderType) -> Unit = {},
    onUpdateQty: (String, String?, Int) -> Unit = { _, _, _ -> },
    onRemoveItem: (String, String?) -> Unit = { _, _ -> }
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Hapus semua pesanan?") },
            text  = { Text("Tindakan ini akan menghapus ${uiState.itemCount} item dari keranjang.") },
            confirmButton = {
                TextButton(onClick = { onClearCart(); showClearConfirm = false }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Batalkan") }
            }
        )
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Keranjang",
                icon = Icons.Default.ShoppingCart,
                subtitle = "Item pesanan dipilih",
                onBack = onBack,
                actions = {
                    if (!uiState.isEmpty) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Hapus semua pesanan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!uiState.isEmpty) {
                Surface(
                    shadowElevation = RancakElevation.current.raised,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
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
                            if (!hasOpenShift) {
                                Text(
                                    text = "Buka shift terlebih dahulu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        RancakButton(
                            text = if (hasOpenShift) "Bayar" else "Shift Tutup",
                            onClick = onCheckout,
                            enabled = hasOpenShift,
                            modifier = Modifier.width(140.dp)
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
                items = persistentListOf(
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
