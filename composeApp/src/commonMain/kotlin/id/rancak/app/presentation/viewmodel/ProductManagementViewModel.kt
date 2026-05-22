package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Product86
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.ProductRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ProductSortField { NAME, STOCK, PRICE }
enum class StockFilter { ALL, LOW, OUT, MARKED_86 }
enum class PriceFilter { ALL, BUDGET, MID, HIGH, PREMIUM }

@Immutable
data class ProductManagementUiState(
    val products: ImmutableList<Product> = persistentListOf(),
    val categories: ImmutableList<Category> = persistentListOf(),
    val products86: ImmutableList<Product86> = persistentListOf(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val sortField: ProductSortField = ProductSortField.NAME,
    val sortAscending: Boolean = true,
    val stockFilter: StockFilter = StockFilter.ALL,
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionProduct: Product? = null,
    val showAdjustDialog: Boolean = false,
    val showBatchDialog: Boolean = false,
    val showProductFormDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showCategoryFormDialog: Boolean = false,
    val editingCategory: Category? = null,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    // Precomputed fields
    val filteredProducts: ImmutableList<Product> = persistentListOf(),
    val products86Uuids: Set<String> = emptySet()
) {
    fun is86(productUuid: String) = products86Uuids.contains(productUuid)
}

class ProductManagementViewModel(
    private val productRepository: ProductRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductManagementUiState())
    val uiState: StateFlow<ProductManagementUiState> = _uiState.asStateFlow()

    private suspend fun ProductManagementUiState.recompute(): ProductManagementUiState = withContext(Dispatchers.Default) {
        val p86Uuids = products86.mapTo(mutableSetOf()) { it.productUuid }
        
        var base = if (searchQuery.isBlank()) products else {
            val q = searchQuery.lowercase()
            products.filter {
                it.name.lowercase().contains(q) ||
                it.sku?.lowercase()?.contains(q) == true ||
                it.barcode?.contains(q) == true
            }
        }
        base = when (stockFilter) {
            StockFilter.ALL       -> base
            StockFilter.LOW       -> base.filter { it.stock in 1.0..5.0 && !p86Uuids.contains(it.uuid) }
            StockFilter.OUT       -> base.filter { it.stock <= 0.0 && !p86Uuids.contains(it.uuid) }
            StockFilter.MARKED_86 -> base.filter { p86Uuids.contains(it.uuid) }
        }
        base = when (priceFilter) {
            PriceFilter.ALL     -> base
            PriceFilter.BUDGET  -> base.filter { it.price < 10_000L }
            PriceFilter.MID     -> base.filter { it.price in 10_000L..50_000L }
            PriceFilter.HIGH    -> base.filter { it.price in 50_001L..100_000L }
            PriceFilter.PREMIUM -> base.filter { it.price > 100_000L }
        }
        val comparator: Comparator<Product> = when (sortField) {
            ProductSortField.NAME  -> compareBy { it.name.lowercase() }
            ProductSortField.STOCK -> compareBy { it.stock }
            ProductSortField.PRICE -> compareBy { it.price }
        }
        val sorted = if (sortAscending) base.sortedWith(comparator) else base.sortedWith(comparator.reversed())
        
        copy(
            filteredProducts = sorted.toImmutableList(),
            products86Uuids = p86Uuids
        )
    }

    fun loadAll() {
        viewModelScope.launch {
            // Langkah 1: Sajikan data dari Room cache secara instan (tidak ada loading flicker).
            val categoryId = _uiState.value.selectedCategory?.uuid
            val cachedProducts    = productRepository.getProductsFromCache(categoryId = categoryId)
            val cachedCategories  = productRepository.getCategoriesFromCache()

            val hasCachedData =
                (cachedProducts   as? Resource.Success)?.data?.isNotEmpty() == true ||
                (cachedCategories as? Resource.Success)?.data?.isNotEmpty() == true

            val intermediateState = _uiState.value.copy(
                isLoading = !hasCachedData,
                error = null,
                products = (cachedProducts as? Resource.Success)?.data?.toImmutableList() ?: _uiState.value.products,
                categories = (cachedCategories as? Resource.Success)?.data?.toImmutableList() ?: _uiState.value.categories
            ).recompute()
            _uiState.value = intermediateState

            // Langkah 2: Refresh dari network secara silent di background.
            val categoriesResult = productRepository.getCategories()
            val products86Result = productRepository.get86Products()
            val productsResult   = productRepository.getProducts(categoryId = categoryId)

            val finalState = _uiState.value.copy(
                isLoading = false,
                products = (productsResult as? Resource.Success)?.data?.toImmutableList() ?: _uiState.value.products,
                categories = (categoriesResult as? Resource.Success)?.data?.toImmutableList() ?: _uiState.value.categories,
                products86 = (products86Result as? Resource.Success)?.data?.toImmutableList() ?: _uiState.value.products86,
                error = if (!hasCachedData) when {
                    productsResult   is Resource.Error -> productsResult.message
                    categoriesResult is Resource.Error -> categoriesResult.message
                    else -> null
                } else null
            ).recompute()
            _uiState.value = finalState
        }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchQuery = query).recompute()
        }
    }

    fun setSort(field: ProductSortField) {
        viewModelScope.launch {
            val current = _uiState.value
            val newState = if (current.sortField == field) {
                current.copy(sortAscending = !current.sortAscending)
            } else {
                current.copy(sortField = field, sortAscending = true)
            }
            _uiState.value = newState.recompute()
        }
    }

    fun setStockFilter(f: StockFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(stockFilter = f).recompute()
        }
    }

    fun setPriceFilter(f: PriceFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(priceFilter = f).recompute()
        }
    }

    fun setCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            // Langkah 1: Sajikan dari Room cache secara instan — tidak ada loading indicator.
            val cachedResult = productRepository.getProductsFromCache(categoryId = category?.uuid)
            if (cachedResult is Resource.Success) {
                _uiState.value = _uiState.value.copy(products = cachedResult.data.toImmutableList()).recompute()
            }

            // Langkah 2: Refresh dari network secara silent — update data tanpa flicker.
            val result = productRepository.getProducts(categoryId = category?.uuid)
            if (result is Resource.Success) {
                _uiState.value = _uiState.value.copy(products = result.data.toImmutableList()).recompute()
            }
        }
    }
    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    // ── Action dialog controls ────────────────────────────────────────────────

    fun openAdjustDialog(product: Product) =
        _uiState.update { it.copy(actionProduct = product, showAdjustDialog = true) }

    fun closeAdjustDialog() =
        _uiState.update { it.copy(showAdjustDialog = false, actionProduct = null) }

    fun openBatchDialog(product: Product) =
        _uiState.update { it.copy(actionProduct = product, showBatchDialog = true) }

    fun closeBatchDialog() =
        _uiState.update { it.copy(showBatchDialog = false, actionProduct = null) }

    fun openProductForm(product: Product? = null) =
        _uiState.update { it.copy(actionProduct = product, showProductFormDialog = true) }

    fun closeProductForm() =
        _uiState.update { it.copy(showProductFormDialog = false, actionProduct = null) }

    fun openDeleteConfirm(product: Product) =
        _uiState.update { it.copy(actionProduct = product, showDeleteConfirmDialog = true) }

    fun closeDeleteConfirm() =
        _uiState.update { it.copy(showDeleteConfirmDialog = false, actionProduct = null) }

    fun openCategoryForm(category: Category? = null) =
        _uiState.update { it.copy(editingCategory = category, showCategoryFormDialog = true) }

    fun closeCategoryForm() =
        _uiState.update { it.copy(showCategoryFormDialog = false, editingCategory = null) }

    // ── Stock adjustment ──────────────────────────────────────────────────────

    fun adjustStock(productId: String, type: String, quantity: Double, note: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = adminRepository.adjustStock(productId, type, quantity, note)) {
                is Resource.Success -> {
                    val data = result.data
                    val currentState = _uiState.value
                    _uiState.value = currentState.copy(
                            isSubmitting = false,
                            showAdjustDialog = false,
                            actionProduct = null,
                            successMessage = "Stok ${data.productName}: ${data.stockBefore.toStockDisplay()} → ${data.stockAfter.toStockDisplay()}",
                            products = currentState.products.map { p ->
                                if (p.uuid == productId) p.copy(stock = data.stockAfter) else p
                            }.toImmutableList()
                        ).recompute()
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Batch restock ─────────────────────────────────────────────────────────

    fun createBatch(
        productId: String,
        quantity: Double,
        expiryDate: String?,
        costPrice: Long?,
        batchNumber: String?,
        note: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = adminRepository.createProductBatch(
                productId, quantity, expiryDate, costPrice, batchNumber, note, null
            )) {
                is Resource.Success -> {
                    val batch = result.data
                    val currentState = _uiState.value
                    _uiState.value = currentState.copy(
                            isSubmitting = false,
                            showBatchDialog = false,
                            actionProduct = null,
                            successMessage = "Batch ditambahkan: +${batch.quantityInitial.toStockDisplay()} unit",
                            products = currentState.products.map { p ->
                                if (p.uuid == productId) p.copy(stock = p.stock + batch.quantityInitial) else p
                            }.toImmutableList()
                        ).recompute()
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Product CRUD ──────────────────────────────────────────────────────────

    fun saveProduct(
        name: String, price: Long, description: String?, sku: String?, barcode: String?,
        categoryUuid: String?, unit: String?, stock: Double, hasExpiry: Boolean
    ) {
        val existing = _uiState.value.actionProduct
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (existing == null) {
                adminRepository.createProduct(name, price, description, sku, barcode, categoryUuid, unit, stock, hasExpiry)
            } else {
                adminRepository.updateProduct(existing.uuid, name, price, description, sku, barcode, categoryUuid, unit)
            }
            when (result) {
                is Resource.Success -> {
                    val saved = result.data
                    val currentState = _uiState.value
                    // Server may return the product without the nested category object
                    // (only category_uuid). Patch it from the local categories list.
                    val savedWithCategory = if (saved.category == null && categoryUuid != null) {
                        val cat = currentState.categories.find { it.uuid == categoryUuid }
                        if (cat != null) saved.copy(category = cat) else saved
                    } else {
                        saved
                    }
                    val updated = if (existing == null) {
                        currentState.products + savedWithCategory
                    } else {
                        currentState.products.map { if (it.uuid == savedWithCategory.uuid) savedWithCategory else it }
                    }
                    _uiState.value = currentState.copy(
                            isSubmitting = false,
                            showProductFormDialog = false,
                            actionProduct = null,
                            products = updated.toImmutableList(),
                            successMessage = if (existing == null) "Produk \"${savedWithCategory.name}\" berhasil ditambahkan"
                                             else "Produk \"${savedWithCategory.name}\" berhasil diperbarui"
                        ).recompute()
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteProduct() {
        val product = _uiState.value.actionProduct ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = adminRepository.deleteProduct(product.uuid)) {
                is Resource.Success -> {
                    val state = _uiState.value
                    _uiState.value = state.copy(
                        isSubmitting = false,
                        showDeleteConfirmDialog = false,
                        actionProduct = null,
                        products = state.products.filter { it.uuid != product.uuid }.toImmutableList(),
                        successMessage = "Produk \"${product.name}\" berhasil dihapus"
                    ).recompute()
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Category CRUD ─────────────────────────────────────────────────────────

    fun saveCategory(name: String, description: String?) {
        val existing = _uiState.value.editingCategory
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (existing == null) {
                adminRepository.createCategory(name, description)
            } else {
                adminRepository.updateCategory(existing.uuid, name, description)
            }
            when (result) {
                is Resource.Success -> {
                    val saved = result.data
                    val currentState = _uiState.value
                    val updated = if (existing == null) {
                        currentState.categories + saved
                    } else {
                        currentState.categories.map { if (it.uuid == saved.uuid) saved else it }
                    }
                    _uiState.value = currentState.copy(
                            isSubmitting = false,
                            showCategoryFormDialog = false,
                            editingCategory = null,
                            categories = updated.toImmutableList(),
                            successMessage = if (existing == null) "Kategori \"${saved.name}\" berhasil ditambahkan"
                                             else "Kategori \"${saved.name}\" berhasil diperbarui"
                        ).recompute()
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteCategory(category.uuid)) {
                is Resource.Success -> {
                    val state = _uiState.value
                    _uiState.value = state.copy(
                        categories = state.categories.filter { it.uuid != category.uuid }.toImmutableList(),
                        selectedCategory = if (state.selectedCategory?.uuid == category.uuid) null else state.selectedCategory,
                        successMessage = "Kategori \"${category.name}\" berhasil dihapus"
                    ).recompute()
                }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── 86 toggle ─────────────────────────────────────────────────────────────

    fun toggle86(product: Product) {
        val currently86 = _uiState.value.is86(product.uuid)
        // Use the 86-record's own UUID for DELETE (backend primary key lookup);
        // fall back to product UUID if record not found (shouldn't happen).
        val record86Uuid = _uiState.value.products86
            .firstOrNull { it.productUuid == product.uuid }?.uuid ?: product.uuid
        viewModelScope.launch {
            val result = if (currently86) productRepository.unmark86(record86Uuid)
                         else productRepository.mark86(product.uuid)
            when (result) {
                is Resource.Success -> {
                    if (currently86) {
                        // Remove locally — no reload needed
                        _uiState.value = _uiState.value.copy(
                            products86 = _uiState.value.products86.filter { it.productUuid != product.uuid }.toImmutableList(),
                            successMessage = "${product.name} kembali aktif"
                        ).recompute()
                    } else {
                        // Reload from server to get the real 86-record UUID for future DELETE
                        _uiState.update { it.copy(successMessage = "${product.name} ditandai 86 (habis hari ini)") }
                        val refreshed = productRepository.get86Products()
                        if (refreshed is Resource.Success) {
                            _uiState.value = _uiState.value.copy(products86 = refreshed.data.toImmutableList()).recompute()
                        }
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}

private fun Double.toStockDisplay(): String =
    if (this == toLong().toDouble()) toLong().toString() else toString()
