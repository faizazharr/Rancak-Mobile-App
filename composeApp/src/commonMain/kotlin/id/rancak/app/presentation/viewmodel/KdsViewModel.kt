package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.KdsOrder
import id.rancak.app.domain.model.KdsStatus
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.OperationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KdsUiState(
    val orders: List<KdsOrder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class KdsViewModel(
    private val operationsRepository: OperationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KdsUiState())
    val uiState: StateFlow<KdsUiState> = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = operationsRepository.getKdsOrders()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(orders = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateOrderStatus(kdsUuid: String, status: KdsStatus) {
        viewModelScope.launch {
            when (operationsRepository.updateKdsStatus(kdsUuid, status)) {
                is Resource.Success -> loadOrders()
                is Resource.Error -> { /* show toast */ }
                is Resource.Loading -> {}
            }
        }
    }
}
