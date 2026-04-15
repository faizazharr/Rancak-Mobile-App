package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesHistoryUiState(
    val sales: List<Sale> = emptyList(),
    val selectedSale: Sale? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SalesHistoryViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesHistoryUiState())
    val uiState: StateFlow<SalesHistoryUiState> = _uiState.asStateFlow()

    fun selectSale(sale: Sale?) {
        _uiState.update { it.copy(selectedSale = sale) }
    }

    fun loadSales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = saleRepository.getSales()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(sales = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
