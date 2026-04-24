package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.domain.repository.ProductRepository
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State untuk layar **Tambah Item ke Open Bill**.
 *
 * Kasir memuat daftar produk, memilih beberapa item dengan kuantitas, lalu
 * mengirimkannya ke `POST /sales/:id/items` melalui
 * [SaleRepository.addItemsToHeldOrder].
 */
data class AddItemsToHeldOrderUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selected: Map<String, CartItem> = emptyMap(), // key = productUuid
    val error: String? = null,
    val successSale: Sale? = null
) {
    val filteredProducts: List<Product>
        get() = if (searchQuery.isBlank()) products
                else products.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.sku?.contains(searchQuery, ignoreCase = true) == true)
                }

    val totalSelectedQty: Int get() = selected.values.sumOf { it.qty }
    val totalSelectedPrice: Long get() = selected.values.sumOf { it.subtotal }
}

class AddItemsToHeldOrderViewModel(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemsToHeldOrderUiState())
    val uiState: StateFlow<AddItemsToHeldOrderUiState> = _uiState.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = productRepository.getProducts()) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, products = result.data.filter { p -> p.isActive })
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun setSearchQuery(q: String) = _uiState.update { it.copy(searchQuery = q) }

    fun increment(product: Product) {
        _uiState.update { state ->
            val existing = state.selected[product.uuid]
            val updated = existing?.copy(qty = existing.qty + 1)
                ?: CartItem(
                    productUuid = product.uuid,
                    productName = product.name,
                    qty         = 1,
                    price       = product.price,
                    imageUrl    = product.imageUrl
                )
            state.copy(selected = state.selected + (product.uuid to updated))
        }
    }

    fun decrement(productUuid: String) {
        _uiState.update { state ->
            val existing = state.selected[productUuid] ?: return@update state
            val nextQty = existing.qty - 1
            val nextSelected = if (nextQty <= 0) state.selected - productUuid
                              else state.selected + (productUuid to existing.copy(qty = nextQty))
            state.copy(selected = nextSelected)
        }
    }

    fun submit(saleUuid: String) {
        val items = _uiState.value.selected.values.toList()
        if (items.isEmpty()) {
            _uiState.update { it.copy(error = "Pilih minimal satu item") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = saleRepository.addItemsToHeldOrder(saleUuid, items)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSubmitting = false, successSale = result.data)
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
