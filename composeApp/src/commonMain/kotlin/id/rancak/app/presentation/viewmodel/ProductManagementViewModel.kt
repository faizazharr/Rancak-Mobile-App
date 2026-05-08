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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProductSortField { NAME, STOCK, PRICE }

@Immutable
data class ProductManagementUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val products86: List<Product86> = emptyList(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val sortField: ProductSortField = ProductSortField.NAME,
    val sortAscending: Boolean = true,
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
    val successMessage: String? = null
) {
    // Products are already filtered by category server-side (via setCategory/loadAll).
    // filteredProducts applies the local search query on top, then sorts.
    val filteredProducts: List<Product>
        get() {
            val base = if (searchQuery.isBlank()) products else {
                val q = searchQuery.lowercase()
                products.filter {
                    it.name.lowercase().contains(q) ||
                    it.sku?.lowercase()?.contains(q) == true ||
                    it.barcode?.contains(q) == true
                }
            }
            val comparator: Comparator<Product> = when (sortField) {
                ProductSortField.NAME  -> compareBy { it.name.lowercase() }
                ProductSortField.STOCK -> compareBy { it.stock }
                ProductSortField.PRICE -> compareBy { it.price }
            }
            return if (sortAscending) base.sortedWith(comparator) else base.sortedWith(comparator.reversed())
        }

    fun is86(productUuid: String) = products86.any { it.productUuid == productUuid }
    val products86Uuids: Set<String> get() = products86.map { it.productUuid }.toSet()
}

class ProductManagementViewModel(
    private val productRepository: ProductRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductManagementUiState())
    val uiState: StateFlow<ProductManagementUiState> = _uiState.asStateFlow()

    fun loadAll() {
        viewModelScope.launch {
            // Langkah 1: Sajikan data dari Room cache secara instan (tidak ada loading flicker).
            val categoryId = _uiState.value.selectedCategory?.uuid
            val cachedProducts    = productRepository.getProductsFromCache(categoryId = categoryId)
            val cachedCategories  = productRepository.getCategoriesFromCache()

            val hasCachedData =
                (cachedProducts   as? Resource.Success)?.data?.isNotEmpty() == true ||
                (cachedCategories as? Resource.Success)?.data?.isNotEmpty() == true

            _uiState.update { state ->
                var s = state.copy(
                    isLoading = !hasCachedData, // loading hanya jika cache benar-benar kosong
                    error = null
                )
                if (cachedProducts   is Resource.Success) s = s.copy(products    = cachedProducts.data)
                if (cachedCategories is Resource.Success) s = s.copy(categories  = cachedCategories.data)
                s
            }

            // Langkah 2: Refresh dari network secara silent di background.
            val categoriesResult = productRepository.getCategories()
            val products86Result = productRepository.get86Products()
            val productsResult   = productRepository.getProducts(categoryId = categoryId)

            _uiState.update { state ->
                var s = state.copy(isLoading = false)
                if (productsResult   is Resource.Success) s = s.copy(products    = productsResult.data)
                if (categoriesResult is Resource.Success) s = s.copy(categories  = categoriesResult.data)
                if (products86Result is Resource.Success) s = s.copy(products86  = products86Result.data)
                // Tampilkan error hanya jika cache memang kosong (pengguna belum punya data sama sekali)
                s.copy(
                    error = if (!hasCachedData) when {
                        productsResult   is Resource.Error -> productsResult.message
                        categoriesResult is Resource.Error -> categoriesResult.message
                        else -> null
                    } else null
                )
            }
        }
    }

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun setSort(field: ProductSortField) = _uiState.update {
        if (it.sortField == field) it.copy(sortAscending = !it.sortAscending)
        else it.copy(sortField = field, sortAscending = true)
    }

    fun setCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            // Langkah 1: Sajikan dari Room cache secara instan — tidak ada loading indicator.
            val cachedResult = productRepository.getProductsFromCache(categoryId = category?.uuid)
            if (cachedResult is Resource.Success) {
                _uiState.update { it.copy(products = cachedResult.data) }
            }

            // Langkah 2: Refresh dari network secara silent — update data tanpa flicker.
            val result = productRepository.getProducts(categoryId = category?.uuid)
            _uiState.update { state ->
                when (result) {
                    is Resource.Success -> state.copy(products = result.data)
                    else                -> state // Jika gagal, tetap tampilkan data cache
                }
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
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            showAdjustDialog = false,
                            actionProduct = null,
                            successMessage = "Stok ${data.productName}: ${data.stockBefore.toStockDisplay()} → ${data.stockAfter.toStockDisplay()}",
                            products = state.products.map { p ->
                                if (p.uuid == productId) p.copy(stock = data.stockAfter) else p
                            }
                        )
                    }
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
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            showBatchDialog = false,
                            actionProduct = null,
                            successMessage = "Batch ditambahkan: +${batch.quantityInitial.toStockDisplay()} unit",
                            products = state.products.map { p ->
                                if (p.uuid == productId) p.copy(stock = p.stock + batch.quantityInitial) else p
                            }
                        )
                    }
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
                    _uiState.update { state ->
                        // Server may return the product without the nested category object
                        // (only category_uuid). Patch it from the local categories list.
                        val savedWithCategory = if (saved.category == null && categoryUuid != null) {
                            val cat = state.categories.find { it.uuid == categoryUuid }
                            if (cat != null) saved.copy(category = cat) else saved
                        } else {
                            saved
                        }
                        val updated = if (existing == null) {
                            state.products + savedWithCategory
                        } else {
                            state.products.map { if (it.uuid == savedWithCategory.uuid) savedWithCategory else it }
                        }
                        state.copy(
                            isSubmitting = false,
                            showProductFormDialog = false,
                            actionProduct = null,
                            products = updated,
                            successMessage = if (existing == null) "Produk \"${savedWithCategory.name}\" berhasil ditambahkan"
                                             else "Produk \"${savedWithCategory.name}\" berhasil diperbarui"
                        )
                    }
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
                is Resource.Success -> _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        showDeleteConfirmDialog = false,
                        actionProduct = null,
                        products = state.products.filter { it.uuid != product.uuid },
                        successMessage = "Produk \"${product.name}\" berhasil dihapus"
                    )
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
                    _uiState.update { state ->
                        val updated = if (existing == null) {
                            state.categories + saved
                        } else {
                            state.categories.map { if (it.uuid == saved.uuid) saved else it }
                        }
                        state.copy(
                            isSubmitting = false,
                            showCategoryFormDialog = false,
                            editingCategory = null,
                            categories = updated,
                            successMessage = if (existing == null) "Kategori \"${saved.name}\" berhasil ditambahkan"
                                             else "Kategori \"${saved.name}\" berhasil diperbarui"
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteCategory(category.uuid)) {
                is Resource.Success -> _uiState.update { state ->
                    state.copy(
                        categories = state.categories.filter { it.uuid != category.uuid },
                        selectedCategory = if (state.selectedCategory?.uuid == category.uuid) null else state.selectedCategory,
                        successMessage = "Kategori \"${category.name}\" berhasil dihapus"
                    )
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
                        _uiState.update { state ->
                            state.copy(
                                products86 = state.products86.filter { it.productUuid != product.uuid },
                                successMessage = "${product.name} kembali aktif"
                            )
                        }
                    } else {
                        // Reload from server to get the real 86-record UUID for future DELETE
                        _uiState.update { it.copy(successMessage = "${product.name} ditandai 86 (habis hari ini)") }
                        val refreshed = productRepository.get86Products()
                        if (refreshed is Resource.Success) {
                            _uiState.update { it.copy(products86 = refreshed.data) }
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
