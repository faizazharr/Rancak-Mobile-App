package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.FavoriteProduct
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Product86
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PosUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val favoriteProducts: List<FavoriteProduct> = emptyList(),
    val products86: List<Product86> = emptyList(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    // Precomputed di ViewModel agar tidak mengulang filter di setiap rekomposisi.
    val products86Uuids: Set<String> = emptySet(),
    val filteredProducts: List<Product> = emptyList()
)

class PosViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    /** Recompute derived fields setiap kali data sumber berubah. */
    private fun PosUiState.recompute(): PosUiState {
        val set = products86.mapTo(mutableSetOf()) { it.productUuid }
        var filtered = products.filter { it.isActive }
        if (selectedCategory != null) {
            filtered = filtered.filter { it.category?.uuid == selectedCategory.uuid }
        }
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.sku?.lowercase()?.contains(query) == true ||
                it.barcode?.contains(query) == true
            }
        }
        return copy(products86Uuids = set, filteredProducts = filtered)
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = productRepository.getProducts()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(products = result.data, isLoading = false).recompute() }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            when (val result = productRepository.getCategories()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(categories = result.data) }
                }
                is Resource.Error -> { /* silent fail for categories */ }
                is Resource.Loading -> {}
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).recompute() }
    }

    fun onCategorySelected(category: Category?) {
        _uiState.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category).recompute()
        }
    }

    fun refresh() {
        loadProducts()
        loadCategories()
        loadFavorites()
        load86Products()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            when (val result = productRepository.getFavoriteProducts()) {
                is Resource.Success -> _uiState.update { it.copy(favoriteProducts = result.data) }
                is Resource.Error -> { /* silent fail for favorites */ }
                is Resource.Loading -> {}
            }
        }
    }

    fun load86Products() {
        viewModelScope.launch {
            when (val result = productRepository.get86Products()) {
                is Resource.Success -> _uiState.update { it.copy(products86 = result.data).recompute() }
                is Resource.Error -> { /* silent fail for 86 */ }
                is Resource.Loading -> {}
            }
        }
    }
}
