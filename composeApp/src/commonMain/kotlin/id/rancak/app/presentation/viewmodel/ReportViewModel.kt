package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportUiState(
    val summary: ShiftSummary? = null,
    val mySalesToday: MySalesReport? = null,
    val dailyByCategory: List<DailyCategoryReport> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val dateFrom: String = "",
    val dateTo: String = ""
)

class ReportViewModel(
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun setDateRange(from: String, to: String) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to) }
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = financeRepository.getShiftSummary()) {
                is Resource.Success -> _uiState.update { it.copy(summary = result.data, isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
            // Also load my sales today
            when (val result = financeRepository.getMySalesToday()) {
                is Resource.Success -> _uiState.update { it.copy(mySalesToday = result.data) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
            // Also load daily by category
            when (val result = financeRepository.getDailyByCategory()) {
                is Resource.Success -> _uiState.update { it.copy(dailyByCategory = result.data) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
