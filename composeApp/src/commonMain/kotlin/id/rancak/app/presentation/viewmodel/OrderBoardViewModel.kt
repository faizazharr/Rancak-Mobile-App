package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.OrderBoardOrder
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class OrderBoardUiState(
    val activeOrders: ImmutableList<OrderBoardOrder> = persistentListOf(),
    val completedOrders: ImmutableList<OrderBoardOrder> = persistentListOf(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Precomputed agar tidak diulang di setiap rekomposisi.
    val displayOrders: ImmutableList<OrderBoardOrder> = persistentListOf()
) {
    suspend fun recompute() = withContext(Dispatchers.Default) {
        copy(displayOrders = if (showCompleted) completedOrders else activeOrders)
    }
}

class OrderBoardViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderBoardUiState())
    val uiState: StateFlow<OrderBoardUiState> = _uiState.asStateFlow()

    fun toggleTab(showCompleted: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showCompleted = showCompleted).recompute()
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = saleRepository.getOrderBoard(includeDone = true)) {
                is Resource.Success -> {
                    val orders = result.data
                    withContext(Dispatchers.Default) {
                        val active = orders.filter { it.status == SaleStatus.HELD }.toImmutableList()
                        val completed = orders.filter { it.status == SaleStatus.PAID }.toImmutableList()
                        _uiState.value = _uiState.value.copy(
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
