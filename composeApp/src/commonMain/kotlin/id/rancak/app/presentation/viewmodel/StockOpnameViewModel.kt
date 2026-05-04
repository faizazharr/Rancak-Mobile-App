package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.OpnameItemEntry
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.domain.repository.InventoryRepository
import id.rancak.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class StockOpnameUiState(
    val opnames: List<StockOpname> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    // Detail flow
    val detail: StockOpnameDetail? = null,
    val isLoadingDetail: Boolean = false,
    val products: List<Product> = emptyList(),
    val showCreateDialog: Boolean = false,
    val showFinalizeConfirm: Boolean = false,
    val isSubmitting: Boolean = false,
    val filterStatus: String? = null
)

class StockOpnameViewModel(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockOpnameUiState())
    val uiState: StateFlow<StockOpnameUiState> = _uiState.asStateFlow()

    init {
        loadOpnames()
    }

    fun loadOpnames(status: String? = _uiState.value.filterStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = inventoryRepository.getStockOpnames(status)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, opnames = result.data) }
                is Resource.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun setFilter(status: String?) {
        _uiState.update { it.copy(filterStatus = status) }
        loadOpnames(status)
    }

    fun openCreateDialog() = _uiState.update { it.copy(showCreateDialog = true) }
    fun closeCreateDialog() = _uiState.update { it.copy(showCreateDialog = false) }

    fun createOpname(note: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = inventoryRepository.createStockOpname(note?.ifBlank { null })) {
                is Resource.Success -> {
                    val newOpname = result.data
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            showCreateDialog = false,
                            opnames = listOf(newOpname) + state.opnames,
                            successMessage = "Sesi opname #${newOpname.opnameNo} berhasil dibuat"
                        )
                    }
                    loadDetail(newOpname.uuid)
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadDetail(opnameId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            // Also load products for the item picker
            if (_uiState.value.products.isEmpty()) {
                when (val p = productRepository.getProducts()) {
                    is Resource.Success -> _uiState.update { it.copy(products = p.data) }
                    else -> {}
                }
            }
            when (val result = inventoryRepository.getStockOpnameDetail(opnameId)) {
                is Resource.Success -> _uiState.update { it.copy(isLoadingDetail = false, detail = result.data) }
                is Resource.Error   -> _uiState.update { it.copy(isLoadingDetail = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun closeDetail() = _uiState.update { it.copy(detail = null) }

    fun saveItems(items: List<OpnameItemEntry>) {
        val opnameId = _uiState.value.detail?.opname?.uuid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = inventoryRepository.upsertOpnameItems(opnameId, items)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Item berhasil disimpan") }
                    loadDetail(opnameId)
                }
                is Resource.Error   -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun openFinalizeConfirm() = _uiState.update { it.copy(showFinalizeConfirm = true) }
    fun closeFinalizeConfirm() = _uiState.update { it.copy(showFinalizeConfirm = false) }

    fun finalizeOpname() {
        val opnameId = _uiState.value.detail?.opname?.uuid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, showFinalizeConfirm = false) }
            when (val result = inventoryRepository.finalizeStockOpname(opnameId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Opname berhasil difinalisasi. Stok telah disesuaikan.") }
                    // Reload detail to get finalized state, then refresh the list
                    loadDetail(opnameId)
                    loadOpnames()
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun cancelOpname(opname: StockOpname) {
        viewModelScope.launch {
            when (val result = inventoryRepository.cancelStockOpname(opname.uuid)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            opnames = state.opnames.filter { it.uuid != opname.uuid },
                            detail  = if (state.detail?.opname?.uuid == opname.uuid) null else state.detail,
                            successMessage = "Opname #${opname.opnameNo} dihapus"
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteItem(productUuid: String) {
        val opnameId = _uiState.value.detail?.opname?.uuid ?: return
        viewModelScope.launch {
            when (val result = inventoryRepository.deleteOpnameItem(opnameId, productUuid)) {
                is Resource.Success -> loadDetail(opnameId)
                is Resource.Error   -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
