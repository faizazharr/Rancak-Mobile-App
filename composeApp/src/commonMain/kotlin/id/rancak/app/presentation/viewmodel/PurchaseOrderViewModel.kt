package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.POItemEntry
import id.rancak.app.domain.model.PurchaseOrder
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Supplier
import id.rancak.app.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseOrderUiState(
    val orders: List<PurchaseOrder> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val selectedOrder: PurchaseOrder? = null,
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val showCancelDialog: Boolean = false,
    /** Filter status: null = semua. */
    val statusFilter: String? = null,
    // Create PO form fields
    val formSupplierUuid: String? = null,
    val formOrderDate: String = "",
    val formExpectedDate: String = "",
    val formNotes: String = ""
)

class PurchaseOrderViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseOrderUiState())
    val uiState: StateFlow<PurchaseOrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
        loadSuppliers()
    }

    fun loadOrders(status: String? = _uiState.value.statusFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = inventoryRepository.getPurchaseOrders(status = status)) {
                is Resource.Success -> _uiState.update { it.copy(orders = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            when (val result = inventoryRepository.getSuppliers()) {
                is Resource.Success -> _uiState.update { it.copy(suppliers = result.data) }
                is Resource.Error   -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun setStatusFilter(status: String?) {
        _uiState.update { it.copy(statusFilter = status) }
        loadOrders(status)
    }

    fun selectOrder(order: PurchaseOrder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            when (val result = inventoryRepository.getPurchaseOrderDetail(order.uuid)) {
                is Resource.Success -> _uiState.update { it.copy(selectedOrder = result.data, isLoadingDetail = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoadingDetail = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun closeDetail() {
        _uiState.update { it.copy(selectedOrder = null) }
    }

    fun openCreateDialog() {
        _uiState.update { it.copy(
            showCreateDialog  = true,
            formSupplierUuid  = null,
            formOrderDate     = "",
            formExpectedDate  = "",
            formNotes         = ""
        ) }
    }

    fun closeCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun onFormSupplierChange(supplierUuid: String?) {
        _uiState.update { it.copy(formSupplierUuid = supplierUuid) }
    }

    fun onFormOrderDateChange(date: String) {
        _uiState.update { it.copy(formOrderDate = date) }
    }

    fun onFormExpectedDateChange(date: String) {
        _uiState.update { it.copy(formExpectedDate = date) }
    }

    fun onFormNotesChange(notes: String) {
        _uiState.update { it.copy(formNotes = notes) }
    }

    fun createPurchaseOrder() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = inventoryRepository.createPurchaseOrder(
                supplierUuid  = state.formSupplierUuid,
                orderDate     = state.formOrderDate.ifBlank { null },
                expectedDate  = state.formExpectedDate.ifBlank { null },
                notes         = state.formNotes.ifBlank { null }
            )
            when (result) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving         = false,
                        showCreateDialog = false,
                        successMessage   = "Purchase order berhasil dibuat"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun sendOrder(poId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.sendPurchaseOrder(poId)) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving       = false,
                        selectedOrder  = result.data,
                        successMessage = "PO berhasil dikirim ke supplier"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun openCancelDialog() {
        _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun closeCancelDialog() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    fun cancelOrder() {
        val order = _uiState.value.selectedOrder ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.cancelPurchaseOrder(order.uuid)) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving         = false,
                        showCancelDialog = false,
                        selectedOrder    = result.data,
                        successMessage   = "PO berhasil dibatalkan"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
