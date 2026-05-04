package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Voucher
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.VoucherUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class VoucherManagementUiState(
    val vouchers: List<Voucher> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showFormDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val editingVoucher: Voucher? = null,
    val isSubmitting: Boolean = false,
    val filterActive: Boolean? = null
)

class VoucherManagementViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoucherManagementUiState())
    val uiState: StateFlow<VoucherManagementUiState> = _uiState.asStateFlow()

    init { load() }

    fun load(isActive: Boolean? = _uiState.value.filterActive) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = adminRepository.getVouchers(isActive)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, vouchers = r.data) }
                is Resource.Error   -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun setFilter(isActive: Boolean?) {
        _uiState.update { it.copy(filterActive = isActive) }
        load(isActive)
    }

    fun openForm(voucher: Voucher? = null) = _uiState.update { it.copy(editingVoucher = voucher, showFormDialog = true) }
    fun closeForm() = _uiState.update { it.copy(showFormDialog = false, editingVoucher = null) }
    fun openDeleteConfirm(voucher: Voucher) = _uiState.update { it.copy(editingVoucher = voucher, showDeleteConfirm = true) }
    fun closeDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false, editingVoucher = null) }

    fun save(
        code: String, name: String, discountType: String, discountValue: String,
        validFrom: String, description: String?, maxDiscount: String?, minPurchase: String,
        usageLimit: Int?, validUntil: String?, isActive: Boolean
    ) {
        val existing = _uiState.value.editingVoucher
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (existing == null) {
                adminRepository.createVoucher(code, name, discountType, discountValue, validFrom, description, maxDiscount, minPurchase, usageLimit, validUntil, isActive)
            } else {
                adminRepository.updateVoucher(existing.uuid, VoucherUpdate(name, description, discountValue, maxDiscount, minPurchase, usageLimit, validFrom, validUntil, isActive))
            }
            when (result) {
                is Resource.Success -> {
                    val saved = result.data
                    _uiState.update { state ->
                        val updated = if (existing == null) state.vouchers + saved
                                      else state.vouchers.map { if (it.uuid == saved.uuid) saved else it }
                        state.copy(isSubmitting = false, showFormDialog = false, editingVoucher = null, vouchers = updated,
                            successMessage = if (existing == null) "Voucher \"${saved.code}\" berhasil ditambahkan"
                                             else "Voucher \"${saved.code}\" berhasil diperbarui")
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun delete() {
        val voucher = _uiState.value.editingVoucher ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = adminRepository.deleteVoucher(voucher.uuid)) {
                is Resource.Success -> _uiState.update { state ->
                    state.copy(isSubmitting = false, showDeleteConfirm = false, editingVoucher = null,
                        vouchers = state.vouchers.filter { it.uuid != voucher.uuid },
                        successMessage = "Voucher \"${voucher.code}\" berhasil dihapus")
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, error = r.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
