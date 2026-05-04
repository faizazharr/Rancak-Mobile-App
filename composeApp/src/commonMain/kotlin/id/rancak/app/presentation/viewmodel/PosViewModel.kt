package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.FavoriteProduct
import id.rancak.app.domain.model.Modifier
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Product86
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.ProductRepository
import id.rancak.app.domain.repository.UserSessionProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class PosUiState(
    val outletName: String = "",
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
    val filteredProducts: List<Product> = emptyList(),
    /**
     * Cache modifier per produk: productUuid → list modifier (global + per-produk).
     * Di-load lazy saat pertama kali item di-tap di OrderPanel.
     */
    val modifierCache: Map<String, List<Modifier>> = emptyMap(),
    /** Set productUuid yang sedang dalam proses load modifier — cegah double request. */
    val loadingModifierUuids: Set<String> = emptySet()
)

class PosViewModel(
    private val productRepository: ProductRepository,
    private val sessionProvider: UserSessionProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState(
        outletName = sessionProvider.getCurrentTenantName() ?: ""
    ))
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

    /**
     * Load modifier untuk satu produk secara lazy — dipanggil saat note dialog
     * pertama kali dibuka untuk produk tersebut. Jika sudah ada di cache, skip.
     * Menyimpan gabungan modifier global (productUuid = null) + per-produk.
     */
    fun loadModifiersForProduct(productUuid: String) {
        val state = _uiState.value
        // Sudah di-cache atau sedang loading — tidak perlu request ulang
        if (productUuid in state.modifierCache || productUuid in state.loadingModifierUuids) return

        viewModelScope.launch {
            _uiState.update { it.copy(loadingModifierUuids = it.loadingModifierUuids + productUuid) }
            when (val result = productRepository.getModifiers(productUuid)) {
                is Resource.Success -> _uiState.update { s ->
                    s.copy(
                        modifierCache       = s.modifierCache + (productUuid to result.data),
                        loadingModifierUuids = s.loadingModifierUuids - productUuid
                    )
                }
                is Resource.Error -> _uiState.update { s ->
                    s.copy(loadingModifierUuids = s.loadingModifierUuids - productUuid)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
