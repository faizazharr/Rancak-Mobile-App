package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Modifier
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val isSaving: Boolean = false
)

class ModifierManagementViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModifierManagementUiState())
    val uiState: StateFlow<ModifierManagementUiState> = _uiState.asStateFlow()

    init {
        loadModifiers()
    }

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
            val existing = state.selectedModifier
            val result = if (existing == null) {
                adminRepository.createModifier(
                    name      = state.formName.trim(),
                    sortOrder = state.formSortOrder,
                    isActive  = state.formIsActive
                )
            } else {
                adminRepository.updateModifier(
                    modifierId = existing.uuid,
                    name       = state.formName.trim(),
                    sortOrder  = state.formSortOrder,
                    isActive   = state.formIsActive
                )
            }
            when (result) {
                is Resource.Success -> {
                    loadModifiers()
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

    fun openDeleteDialog(modifier: Modifier) {
        _uiState.update { it.copy(selectedModifier = modifier, showDeleteDialog = true) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        val target = _uiState.value.selectedModifier ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = adminRepository.deleteModifier(target.uuid)) {
                is Resource.Success -> {
                    loadModifiers()
                    _uiState.update { it.copy(
                        isSaving         = false,
                        showDeleteDialog = false,
                        successMessage   = "Modifier berhasil dihapus"
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
