package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Tenant
import id.rancak.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TenantPickerUiState(
    val tenants: List<Tenant> = emptyList(),
    val selectedTenant: Tenant? = null,
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false,
    val error: String? = null
)

class TenantPickerViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantPickerUiState())
    val uiState: StateFlow<TenantPickerUiState> = _uiState.asStateFlow()

    fun loadTenants() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.getMyTenants()) {
                is Resource.Success -> {
                    val tenants = result.data
                    _uiState.update { it.copy(tenants = tenants, isLoading = false) }
                    if (tenants.size == 1) {
                        selectTenant(tenants.first())
                        confirm()
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun selectTenant(tenant: Tenant) {
        _uiState.update { it.copy(selectedTenant = tenant) }
    }

    fun confirm() {
        val tenant = _uiState.value.selectedTenant ?: return
        authRepository.setTenant(tenant.uuid, tenant.name)
        _uiState.update { it.copy(isConfirmed = true) }
    }
}
