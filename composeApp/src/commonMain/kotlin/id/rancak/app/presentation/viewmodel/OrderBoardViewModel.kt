package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderBoardUiState(
    val orders: List<Sale> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class OrderBoardViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderBoardUiState())
    val uiState: StateFlow<OrderBoardUiState> = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = saleRepository.getSales()) {
                is Resource.Success -> {
                    val active = result.data.filter { it.status == SaleStatus.HELD || it.status == SaleStatus.SERVED }
                    _uiState.update { it.copy(orders = active, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun serveOrder(saleUuid: String) {
        viewModelScope.launch {
            when (saleRepository.serveSale(saleUuid)) {
                is Resource.Success -> loadOrders()
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }
}
