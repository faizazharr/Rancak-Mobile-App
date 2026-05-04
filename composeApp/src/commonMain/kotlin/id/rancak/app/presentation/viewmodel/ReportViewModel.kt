package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.CashierShiftSummary
import id.rancak.app.domain.model.DailyCategoryReport
import id.rancak.app.domain.model.MySalesReport
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ReportUiState(
    val summary: ShiftSummary? = null,
    val mySalesToday: MySalesReport? = null,
    val dailyByCategory: List<DailyCategoryReport> = emptyList(),
    val cashierShifts: List<CashierShiftSummary> = emptyList(),
    val isCashierShiftsLoading: Boolean = false,
    val cashierShiftDate: String = "",
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
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = financeRepository.getShiftSummary()) {
                is Resource.Success -> _uiState.update { it.copy(summary = result.data) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
            when (val result = financeRepository.getMySalesToday()) {
                is Resource.Success -> _uiState.update { it.copy(mySalesToday = result.data) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
            when (val result = financeRepository.getDailyByCategory()) {
                is Resource.Success -> _uiState.update { it.copy(dailyByCategory = result.data) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loadCashierShifts(date: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCashierShiftsLoading = true) }
            when (val result = financeRepository.getShiftByCashier(date)) {
                is Resource.Success -> _uiState.update {
                    it.copy(cashierShifts = result.data, isCashierShiftsLoading = false, cashierShiftDate = date ?: "")
                }
                is Resource.Error -> _uiState.update {
                    it.copy(error = result.message, isCashierShiftsLoading = false)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
