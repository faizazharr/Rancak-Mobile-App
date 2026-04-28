package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductManagementUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val products86Uuids: Set<String> = emptySet(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
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
    val filteredProducts: List<Product>
        get() {
            var filtered = products
            if (selectedCategory != null) {
                filtered = filtered.filter { it.category?.uuid == selectedCategory.uuid }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.lowercase()
                filtered = filtered.filter {
                    it.name.lowercase().contains(q) ||
                    it.sku?.lowercase()?.contains(q) == true ||
                    it.barcode?.contains(q) == true
                }
            }
            return filtered
        }

    fun is86(productUuid: String) = products86Uuids.contains(productUuid)
}

class ProductManagementViewModel(
    private val productRepository: ProductRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductManagementUiState())
    val uiState: StateFlow<ProductManagementUiState> = _uiState.asStateFlow()

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val productsResult = productRepository.getProducts()
            val categoriesResult = productRepository.getCategories()
            val products86Result = productRepository.get86Products()

            _uiState.update { state ->
                var s = state.copy(isLoading = false)
                if (productsResult is Resource.Success) s = s.copy(products = productsResult.data)
                if (categoriesResult is Resource.Success) s = s.copy(categories = categoriesResult.data)
                if (products86Result is Resource.Success) {
                    s = s.copy(products86Uuids = products86Result.data.map { it.productUuid }.toSet())
                }
                s.copy(
                    error = when {
                        productsResult is Resource.Error -> productsResult.message
                        categoriesResult is Resource.Error -> categoriesResult.message
                        else -> null
                    }
                )
            }
        }
    }

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }
    fun setCategory(category: Category?) = _uiState.update { it.copy(selectedCategory = category) }
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
                        val updated = if (existing == null) {
                            state.products + saved
                        } else {
                            state.products.map { if (it.uuid == saved.uuid) saved else it }
                        }
                        state.copy(
                            isSubmitting = false,
                            showProductFormDialog = false,
                            actionProduct = null,
                            products = updated,
                            successMessage = if (existing == null) "Produk \"${saved.name}\" berhasil ditambahkan"
                                             else "Produk \"${saved.name}\" berhasil diperbarui"
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
        viewModelScope.launch {
            val result = if (currently86) productRepository.unmark86(product.uuid)
                         else productRepository.mark86(product.uuid)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        val updated86 = if (currently86)
                            state.products86Uuids - product.uuid
                        else
                            state.products86Uuids + product.uuid
                        state.copy(
                            products86Uuids = updated86,
                            successMessage = if (currently86) "${product.name} kembali aktif"
                                             else "${product.name} ditandai 86 (habis hari ini)"
                        )
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
