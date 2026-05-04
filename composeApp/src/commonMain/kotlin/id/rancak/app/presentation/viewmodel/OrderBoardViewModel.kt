package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.OrderBoardOrder
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class OrderBoardUiState(
    val activeOrders: List<OrderBoardOrder> = emptyList(),
    val completedOrders: List<OrderBoardOrder> = emptyList(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Precomputed agar tidak diulang di setiap rekomposisi.
    val displayOrders: List<OrderBoardOrder> = emptyList()
) {
    fun recompute() = copy(displayOrders = if (showCompleted) completedOrders else activeOrders)
}

class OrderBoardViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderBoardUiState())
    val uiState: StateFlow<OrderBoardUiState> = _uiState.asStateFlow()

    fun toggleTab(showCompleted: Boolean) {
        _uiState.update { it.copy(showCompleted = showCompleted).recompute() }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = saleRepository.getOrderBoard(includeDone = true)) {
                is Resource.Success -> {
                    val active = result.data.filter { it.status == SaleStatus.HELD }
                    val completed = result.data.filter { it.status == SaleStatus.PAID }
                    _uiState.update {
                        it.copy(
                            activeOrders = active,
                            completedOrders = completed,
                            isLoading = false
                        ).recompute()
                    }
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
            when (val result = saleRepository.serveSale(saleUuid)) {
                is Resource.Success -> loadOrders()
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
