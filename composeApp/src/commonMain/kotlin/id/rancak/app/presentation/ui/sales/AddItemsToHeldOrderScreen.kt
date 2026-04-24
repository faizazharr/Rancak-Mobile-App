package id.rancak.app.presentation.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.AddItemsToHeldOrderViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar **Tambah Item ke Open Bill**.
 *
 * Memuat daftar produk aktif, kasir memilih item + qty, lalu menambahkan ke
 * pesanan yang sedang ditahan (held) via `POST /sales/:id/items`.
 *
 * Stok baru dipotong saat pesanan dibayar.
 */
@Composable
fun AddItemsToHeldOrderScreen(
    saleUuid: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddItemsToHeldOrderViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    LaunchedEffect(uiState.successSale) {
        if (uiState.successSale != null) onSuccess()
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Tambah Item",
                icon     = Icons.Default.PlaylistAdd,
                subtitle = "Tambahkan ke open bill",
                onBack   = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            BottomSubmitBar(
                totalQty   = uiState.totalSelectedQty,
                totalPrice = uiState.totalSelectedPrice,
                isLoading  = uiState.isSubmitting,
                onSubmit   = { viewModel.submit(saleUuid) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            SearchBar(
                query    = uiState.searchQuery,
                onChange = viewModel::setSearchQuery
            )

            when {
                uiState.isLoading -> LoadingScreen(Modifier.weight(1f).fillMaxWidth())
                uiState.products.isEmpty() && !uiState.isLoading ->
                    EmptyScreen("Belum ada produk", Modifier.weight(1f).fillMaxWidth())
                uiState.filteredProducts.isEmpty() ->
                    EmptyScreen("Produk tidak ditemukan", Modifier.weight(1f).fillMaxWidth())
                else -> ProductGrid(
                    products    = uiState.filteredProducts,
                    selectedQty = { uuid -> uiState.selected[uuid]?.qty ?: 0 },
                    onIncrement = viewModel::increment,
                    onDecrement = viewModel::decrement,
                    modifier    = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value         = query,
        onValueChange = onChange,
        leadingIcon   = { Icon(Icons.Default.Search, null) },
        placeholder   = { Text("Cari produk…") },
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth().padding(12.dp)
    )
}

@Composable
private fun ProductGrid(
    products: List<Product>,
    selectedQty: (String) -> Int,
    onIncrement: (Product) -> Unit,
    onDecrement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns             = GridCells.Adaptive(minSize = 160.dp),
        modifier            = modifier,
        contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.uuid }) { product ->
            ProductCard(
                product     = product,
                qty         = selectedQty(product.uuid),
                onIncrement = { onIncrement(product) },
                onDecrement = { onDecrement(product.uuid) }
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    qty: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (qty > 0) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                product.name,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2
            )
            Text(
                formatRupiah(product.price),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (qty > 0) {
                    QtyButton(icon = Icons.Default.Remove, onClick = onDecrement)
                    Text(
                        qty.toString(),
                        modifier   = Modifier.padding(horizontal = 10.dp),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                QtyButton(icon = Icons.Default.Add, onClick = onIncrement)
            }
        }
    }
}

@Composable
private fun QtyButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        shape    = CircleShape,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp),
        onClick  = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimary,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BottomSubmitBar(
    totalQty: Int,
    totalPrice: Long,
    isLoading: Boolean,
    onSubmit: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$totalQty item",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatRupiah(totalPrice),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
            RancakButton(
                text      = "Tambahkan",
                onClick   = onSubmit,
                isLoading = isLoading,
                enabled   = totalQty > 0 && !isLoading
            )
        }
    }
}
