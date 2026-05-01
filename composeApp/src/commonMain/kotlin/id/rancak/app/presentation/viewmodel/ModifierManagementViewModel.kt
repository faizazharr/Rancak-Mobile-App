package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Modifier
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ModifierTab { GLOBAL, PER_PRODUCT }

data class ModifierManagementUiState(
    val modifiers: List<Modifier> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    /** Modifier yang sedang diedit/dihapus, null = tidak ada dialog terbuka. */
    val selectedModifier: Modifier? = null,
    val showFormDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    /** Nilai field form. */
    val formName: String = "",
    val formSortOrder: Int = 0,
    val formIsActive: Boolean = true,
    val isSaving: Boolean = false,
    // ── Tab & per-produk ──────────────────────────────────────────────────
    val activeTab: ModifierTab = ModifierTab.GLOBAL,
    /** Daftar semua produk untuk picker di tab Per Produk. */
    val products: List<Product> = emptyList(),
    val isLoadingProducts: Boolean = false,
    /** Produk yang dipilih di tab Per Produk. */
    val selectedProduct: Product? = null,
    /** Modifier per-produk yang sedang ditampilkan. */
    val productModifiers: List<Modifier> = emptyList(),
    val isLoadingProductModifiers: Boolean = false
)

class ModifierManagementViewModel(
    private val adminRepository: AdminRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModifierManagementUiState())
    val uiState: StateFlow<ModifierManagementUiState> = _uiState.asStateFlow()

    init {
        loadModifiers()
        loadProducts()
    }

    // ── Tab ──────────────────────────────────────────────────────────────────

    fun selectTab(tab: ModifierTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // ── Global modifiers ─────────────────────────────────────────────────────

    fun loadModifiers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = adminRepository.getModifiers()) {
                is Resource.Success -> _uiState.update { it.copy(modifiers = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Products (untuk picker) ───────────────────────────────────────────────

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProducts = true) }
            when (val result = productRepository.getProducts()) {
                is Resource.Success -> _uiState.update { it.copy(products = result.data, isLoadingProducts = false) }
                is Resource.Error   -> _uiState.update { it.copy(isLoadingProducts = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun selectProduct(product: Product?) {
        _uiState.update { it.copy(selectedProduct = product, productModifiers = emptyList()) }
        if (product != null) loadProductModifiers(product.uuid)
    }

    private fun loadProductModifiers(productUuid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProductModifiers = true) }
            when (val result = productRepository.getModifiers(productUuid)) {
                is Resource.Success -> _uiState.update { it.copy(productModifiers = result.data, isLoadingProductModifiers = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoadingProductModifiers = false) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Form ─────────────────────────────────────────────────────────────────

    fun openCreateForm() {
        _uiState.update { it.copy(
            selectedModifier = null,
            formName         = "",
            formSortOrder    = 0,
            formIsActive     = true,
            showFormDialog   = true
        ) }
    }

    fun openEditForm(modifier: Modifier) {
        _uiState.update { it.copy(
            selectedModifier = modifier,
            formName         = modifier.name,
            formSortOrder    = modifier.sortOrder,
            formIsActive     = modifier.isActive,
            showFormDialog   = true
        ) }
    }

    fun closeFormDialog() {
        _uiState.update { it.copy(showFormDialog = false) }
    }

    fun onFormNameChange(name: String) {
        _uiState.update { it.copy(formName = name) }
    }

    fun onFormSortOrderChange(order: Int) {
        _uiState.update { it.copy(formSortOrder = order) }
    }

    fun onFormIsActiveChange(isActive: Boolean) {
        _uiState.update { it.copy(formIsActive = isActive) }
    }

    fun saveModifier() {
        val state = _uiState.value
        if (state.formName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val existing    = state.selectedModifier
            val productUuid = if (state.activeTab == ModifierTab.PER_PRODUCT) state.selectedProduct?.uuid else null

            val result = when {
                existing != null -> adminRepository.updateModifier(
                    modifierId = existing.uuid,
                    name       = state.formName.trim(),
                    sortOrder  = state.formSortOrder,
                    isActive   = state.formIsActive
                )
                productUuid != null -> adminRepository.createProductModifier(
                    productUuid = productUuid,
                    name        = state.formName.trim(),
                    sortOrder   = state.formSortOrder,
                    isActive    = state.formIsActive
                )
                else -> adminRepository.createModifier(
                    name      = state.formName.trim(),
                    sortOrder = state.formSortOrder,
                    isActive  = state.formIsActive
                )
            }

            when (result) {
                is Resource.Success -> {
                    if (productUuid != null && existing == null) {
                        loadProductModifiers(productUuid)
                    } else {
                        loadModifiers()
                        if (productUuid != null) loadProductModifiers(productUuid)
                    }
                    _uiState.update { it.copy(
                        isSaving       = false,
                        showFormDialog = false,
                        successMessage = if (existing == null) "Modifier berhasil dibuat" else "Modifier berhasil diperbarui"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    fun openDeleteDialog(modifier: Modifier) {
        _uiState.update { it.copy(selectedModifier = modifier, showDeleteDialog = true) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        val state  = _uiState.value
        val target = state.selectedModifier ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (adminRepository.deleteModifier(target.uuid)) {
                is Resource.Success -> {
                    loadModifiers()
                    val productUuid = state.selectedProduct?.uuid
                    if (productUuid != null) loadProductModifiers(productUuid)
                    _uiState.update { it.copy(
                        isSaving         = false,
                        showDeleteDialog = false,
                        successMessage   = "Modifier berhasil dihapus"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = null) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    fun clearSuccessMessage() { _uiState.update { it.copy(successMessage = null) } }
    fun clearError()          { _uiState.update { it.copy(error = null) } }
}

