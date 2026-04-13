package id.rancak.app.presentation.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PosViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onCartClick: () -> Unit,
    onMenuClick: () -> Unit,
    posViewModel: PosViewModel = koinViewModel(),
    cartViewModel: CartViewModel = koinViewModel()
) {
    val uiState by posViewModel.uiState.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        posViewModel.loadProducts()
        posViewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kasir", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartState.itemCount > 0) {
                                Badge { Text("${cartState.itemCount}") }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = posViewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cari produk...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { posViewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Category Chips
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { posViewModel.onCategorySelected(category) },
                            label = { Text(category.name) },
                            shape = MaterialTheme.shapes.small
                        )
                    }
                }
            }

            // Product Grid
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.error != null -> ErrorScreen(
                    message = uiState.error!!,
                    onRetry = posViewModel::refresh
                )
                uiState.filteredProducts.isEmpty() -> EmptyScreen("Tidak ada produk ditemukan")
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredProducts, key = { it.uuid }) { product ->
                            ProductCard(
                                name = product.name,
                                price = formatRupiah(product.price),
                                category = product.category?.name,
                                imageUrl = product.imageUrl,
                                isAvailable = product.isActive,
                                onClick = { cartViewModel.addProduct(product) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PosScreenPreview() {
    RancakTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Kasir", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.Menu, "Menu") } },
                    actions = {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            IconButton(onClick = {}) { Icon(Icons.Default.ShoppingCart, "Keranjang") }
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Cari produk...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(listOf("Makanan", "Minuman", "Snack")) { cat ->
                        FilterChip(selected = cat == "Makanan", onClick = {}, label = { Text(cat) }, shape = MaterialTheme.shapes.small)
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf("Nasi Goreng" to "Rp 25.000", "Es Teh" to "Rp 8.000", "Ayam Bakar" to "Rp 35.000", "Mie Goreng" to "Rp 22.000")) { (name, price) ->
                        ProductCard(name = name, price = price, category = "Makanan", imageUrl = null, isAvailable = true, onClick = {})
                    }
                }
            }
        }
    }
}
