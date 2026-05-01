package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Supplier
import id.rancak.app.domain.model.SupplierInput
import id.rancak.app.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupplierUiState(
    val suppliers: List<Supplier> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedSupplier: Supplier? = null,
    val showFormDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isSaving: Boolean = false,
    // Form fields
    val formName: String = "",
    val formContactName: String = "",
    val formPhone: String = "",
    val formEmail: String = "",
    val formAddress: String = "",
    val formNpwp: String = "",
    val formNotes: String = ""
)

class SupplierViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierUiState())
    val uiState: StateFlow<SupplierUiState> = _uiState.asStateFlow()

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = inventoryRepository.getSuppliers()) {
                is Resource.Success -> _uiState.update { it.copy(suppliers = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun openCreateForm() {
        _uiState.update { it.copy(
            selectedSupplier = null,
            formName         = "",
            formContactName  = "",
            formPhone        = "",
            formEmail        = "",
            formAddress      = "",
            formNpwp         = "",
            formNotes        = "",
            showFormDialog   = true
        ) }
    }

    fun openEditForm(supplier: Supplier) {
        _uiState.update { it.copy(
            selectedSupplier = supplier,
            formName         = supplier.name,
            formContactName  = supplier.contactName ?: "",
            formPhone        = supplier.phone ?: "",
            formEmail        = supplier.email ?: "",
            formAddress      = supplier.address ?: "",
            formNpwp         = supplier.npwp ?: "",
            formNotes        = supplier.notes ?: "",
            showFormDialog   = true
        ) }
    }

    fun closeFormDialog() {
        _uiState.update { it.copy(showFormDialog = false) }
    }

    fun onFormChange(field: SupplierFormField, value: String) {
        _uiState.update { state ->
            when (field) {
                SupplierFormField.NAME         -> state.copy(formName = value)
                SupplierFormField.CONTACT_NAME -> state.copy(formContactName = value)
                SupplierFormField.PHONE        -> state.copy(formPhone = value)
                SupplierFormField.EMAIL        -> state.copy(formEmail = value)
                SupplierFormField.ADDRESS      -> state.copy(formAddress = value)
                SupplierFormField.NPWP         -> state.copy(formNpwp = value)
                SupplierFormField.NOTES        -> state.copy(formNotes = value)
            }
        }
    }

    fun saveSupplier() {
        val state = _uiState.value
        if (state.formName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val existing = state.selectedSupplier
            val result = if (existing == null) {
                inventoryRepository.createSupplier(SupplierInput(
                    name        = state.formName.trim(),
                    contactName = state.formContactName.ifBlank { null },
                    phone       = state.formPhone.ifBlank { null },
                    email       = state.formEmail.ifBlank { null },
                    address     = state.formAddress.ifBlank { null },
                    npwp        = state.formNpwp.ifBlank { null },
                    notes       = state.formNotes.ifBlank { null }
                ))
            } else {
                inventoryRepository.updateSupplier(
                    supplierId  = existing.uuid,
                    name        = state.formName.trim(),
                    contactName = state.formContactName.ifBlank { null },
                    phone       = state.formPhone.ifBlank { null },
                    email       = state.formEmail.ifBlank { null },
                    address     = state.formAddress.ifBlank { null },
                    npwp        = state.formNpwp.ifBlank { null },
                    notes       = state.formNotes.ifBlank { null }
                )
            }
            when (result) {
                is Resource.Success -> {
                    loadSuppliers()
                    _uiState.update { it.copy(
                        isSaving       = false,
                        showFormDialog = false,
                        successMessage = if (existing == null) "Supplier berhasil ditambahkan" else "Supplier berhasil diperbarui"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun openDeleteDialog(supplier: Supplier) {
        _uiState.update { it.copy(selectedSupplier = supplier, showDeleteDialog = true) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        val target = _uiState.value.selectedSupplier ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.deleteSupplier(target.uuid)) {
                is Resource.Success -> {
                    loadSuppliers()
                    _uiState.update { it.copy(
                        isSaving         = false,
                        showDeleteDialog = false,
                        successMessage   = "Supplier berhasil dihapus"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
    fun clearError()          = _uiState.update { it.copy(error = null) }
}

enum class SupplierFormField {
    NAME, CONTACT_NAME, PHONE, EMAIL, ADDRESS, NPWP, NOTES
}
