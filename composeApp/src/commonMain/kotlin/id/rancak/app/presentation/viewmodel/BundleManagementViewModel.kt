package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Bundle
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.BundleItemEntry
import id.rancak.app.domain.repository.BundleUpdate
import id.rancak.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class BundleManagementUiState(
    val bundles: List<Bundle> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Form state
    val showCreateDialog: Boolean = false,
    val editingBundle: Bundle? = null,
    val deletingBundle: Bundle? = null,
    // Form fields
    val formName: String = "",
    val formPrice: String = "",
    val formSku: String = "",
    val formIsActive: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null
)

class BundleManagementViewModel(
    private val productRepository: ProductRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BundleManagementUiState())
    val uiState: StateFlow<BundleManagementUiState> = _uiState.asStateFlow()

    init { loadBundles() }

    fun loadBundles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = productRepository.getBundles()) {
                is Resource.Success -> _uiState.update { it.copy(bundles = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Dialog controls ───────────────────────────────────────────────────────

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                editingBundle    = null,
                formName         = "",
                formPrice        = "",
                formSku          = "",
                formIsActive     = true,
                saveError        = null
            )
        }
    }

    fun openEditDialog(bundle: Bundle) {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                editingBundle    = bundle,
                formName         = bundle.name,
                formPrice        = bundle.price.toString(),
                formSku          = "",
                formIsActive     = bundle.isActive,
                saveError        = null
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(showCreateDialog = false, editingBundle = null, saveError = null) }
    }

    fun showDeleteConfirm(bundle: Bundle) = _uiState.update { it.copy(deletingBundle = bundle) }
    fun dismissDeleteConfirm() = _uiState.update { it.copy(deletingBundle = null) }

    // ── Form field changes ────────────────────────────────────────────────────

    fun onNameChange(v: String)    = _uiState.update { it.copy(formName = v, saveError = null) }
    fun onPriceChange(v: String)   = _uiState.update { it.copy(formPrice = v.filter { c -> c.isDigit() }, saveError = null) }
    fun onSkuChange(v: String)     = _uiState.update { it.copy(formSku = v, saveError = null) }
    fun onIsActiveChange(v: Boolean) = _uiState.update { it.copy(formIsActive = v) }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun saveBundle() {
        val state = _uiState.value
        if (state.formName.isBlank()) {
            _uiState.update { it.copy(saveError = "Nama bundle tidak boleh kosong.") }
            return
        }
        if (state.formPrice.isBlank()) {
            _uiState.update { it.copy(saveError = "Harga bundle tidak boleh kosong.") }
            return
        }
        val editing = state.editingBundle
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            val result = if (editing == null) {
                adminRepository.createBundle(
                    name      = state.formName.trim(),
                    price     = state.formPrice,
                    items     = emptyList(),
                    sku       = state.formSku.takeIf { it.isNotBlank() },
                    isActive  = state.formIsActive
                )
            } else {
                adminRepository.updateBundle(
                    bundleId = editing.uuid,
                    update   = BundleUpdate(
                        name     = state.formName.trim(),
                        price    = state.formPrice,
                        sku      = state.formSku.takeIf { it.isNotBlank() },
                        isActive = state.formIsActive
                    )
                )
            }
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false, showCreateDialog = false, editingBundle = null) }
                    loadBundles()
                }
                is Resource.Error   -> _uiState.update { it.copy(isSaving = false, saveError = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteBundle() {
        val bundle = _uiState.value.deletingBundle ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingBundle = null) }
            when (val result = adminRepository.deleteBundle(bundle.uuid)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(bundles = it.bundles.filter { b -> b.uuid != bundle.uuid }) }
                }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleActive(bundle: Bundle) {
        viewModelScope.launch {
            adminRepository.updateBundle(bundle.uuid, BundleUpdate(isActive = !bundle.isActive)).let { result ->
                if (result is Resource.Success) {
                    _uiState.update { state ->
                        state.copy(bundles = state.bundles.map { if (it.uuid == bundle.uuid) result.data else it })
                    }
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
