package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import id.rancak.app.domain.model.Tenant
import id.rancak.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TenantPickerUiState(
    val tenants: List<Tenant> = emptyList(),
    val selectedTenant: Tenant? = null,
    val isConfirmed: Boolean = false
)

class TenantPickerViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantPickerUiState())
    val uiState: StateFlow<TenantPickerUiState> = _uiState.asStateFlow()

    fun setTenants(tenants: List<Tenant>) {
        _uiState.update { it.copy(tenants = tenants) }
        if (tenants.size == 1) {
            selectTenant(tenants.first())
            confirm()
        }
    }

    fun selectTenant(tenant: Tenant) {
        _uiState.update { it.copy(selectedTenant = tenant) }
    }

    fun confirm() {
        val tenant = _uiState.value.selectedTenant ?: return
        authRepository.setTenant(tenant.uuid)
        _uiState.update { it.copy(isConfirmed = true) }
    }
}
