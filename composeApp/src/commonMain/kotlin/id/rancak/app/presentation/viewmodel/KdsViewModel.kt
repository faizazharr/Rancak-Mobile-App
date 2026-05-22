package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.KdsOrder
import id.rancak.app.domain.model.KdsStatus
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.OperationsRepository
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
data class KdsUiState(
    val activeOrders: ImmutableList<KdsOrder> = persistentListOf(),
    val completedOrders: ImmutableList<KdsOrder> = persistentListOf(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Precomputed agar tidak diulang di setiap rekomposisi.
    val displayOrders: ImmutableList<KdsOrder> = persistentListOf()
) {
    suspend fun recompute() = withContext(Dispatchers.Default) {
        copy(displayOrders = if (showCompleted) completedOrders else activeOrders)
    }
}

class KdsViewModel(
    private val operationsRepository: OperationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KdsUiState())
    val uiState: StateFlow<KdsUiState> = _uiState.asStateFlow()

    fun toggleTab(showCompleted: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showCompleted = showCompleted).recompute()
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = operationsRepository.getKdsOrders()) {
                is Resource.Success -> {
                    val orders = result.data
                    withContext(Dispatchers.Default) {
                        val active = orders.filter { it.status != KdsStatus.DONE }.toImmutableList()
                        val completed = orders.filter { it.status == KdsStatus.DONE }.toImmutableList()
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

    fun updateOrderStatus(kdsUuid: String, status: KdsStatus) {
        viewModelScope.launch {
            when (val result = operationsRepository.updateKdsStatus(kdsUuid, status)) {
                is Resource.Success -> loadOrders()
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
