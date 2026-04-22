package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.repository.SaleRepository
import id.rancak.app.domain.repository.SplitBillResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplitBillUiState(
    val sale: Sale? = null,
    val selectedItemIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Non-null saat split berhasil. */
    val result: SplitBillResult? = null
) {
    val availableItems: List<SaleItem> get() = sale?.items ?: emptyList()
    val canSplit: Boolean get() {
        val total = availableItems.size
        val selected = selectedItemIds.size
        // Harus ada ≥1 item dipilih AND minimal 1 item tersisa di transaksi asal
        return selected >= 1 && selected < total
    }
    val selectedTotal: Long
        get() = availableItems
            .filter { it.uuid in selectedItemIds }
            .sumOf { it.subtotal }
    val remainingTotal: Long
        get() = availableItems
            .filter { it.uuid !in selectedItemIds }
            .sumOf { it.subtotal }
}

class SplitBillViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitBillUiState())
    val uiState: StateFlow<SplitBillUiState> = _uiState.asStateFlow()

    fun loadSale(saleUuid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = saleRepository.getSaleDetail(saleUuid)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, sale = result.data, selectedItemIds = emptySet())
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleItem(itemUuid: String) {
        _uiState.update { state ->
            val updated = if (itemUuid in state.selectedItemIds)
                state.selectedItemIds - itemUuid
            else
                state.selectedItemIds + itemUuid
            state.copy(selectedItemIds = updated)
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            state.copy(selectedItemIds = state.availableItems.map { it.uuid }.toSet())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItemIds = emptySet()) }
    }

    fun confirmSplit() {
        val state = _uiState.value
        val saleUuid = state.sale?.uuid ?: return
        if (!state.canSplit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = saleRepository.splitBill(saleUuid, state.selectedItemIds.toList())) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, result = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun dismissResult() {
        _uiState.update { it.copy(result = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
