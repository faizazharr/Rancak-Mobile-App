package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.CashierShiftSummary
import id.rancak.app.domain.model.DailyCategoryReport
import id.rancak.app.domain.model.ExpiringBatch
import id.rancak.app.domain.model.LowStock
import id.rancak.app.domain.model.MySalesReport
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.domain.model.StockAlert
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
    val dateTo: String = "",
    // Stock alerts
    val stockAlerts: List<StockAlert> = emptyList(),
    val lowStockItems: List<LowStock> = emptyList(),
    val expiringBatches: List<ExpiringBatch> = emptyList(),
    val isStockLoading: Boolean = false,
    val stockError: String? = null
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun loadStockAlerts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isStockLoading = true, stockError = null) }
            val alertsResult   = financeRepository.getStockAlerts()
            val lowStockResult = financeRepository.getLowStock()
            val expiringResult = financeRepository.getExpiringBatches(days = 30)

            if (alertsResult is Resource.Success) {
                _uiState.update { it.copy(stockAlerts = alertsResult.data) }
            } else if (alertsResult is Resource.Error) {
                _uiState.update { it.copy(stockError = alertsResult.message) }
            }

            if (lowStockResult is Resource.Success) {
                _uiState.update { it.copy(lowStockItems = lowStockResult.data) }
            }

            if (expiringResult is Resource.Success) {
                _uiState.update { it.copy(expiringBatches = expiringResult.data) }
            }

            _uiState.update { it.copy(isStockLoading = false) }
        }
    }

    fun markAlertRead(alertId: String) {
        viewModelScope.launch {
            financeRepository.markStockAlertRead(alertId)
            _uiState.update { state ->
                state.copy(stockAlerts = state.stockAlerts.filterNot { it.productUuid == alertId })
            }
        }
    }

    fun markAllAlertsRead() {
        viewModelScope.launch {
            financeRepository.markAllStockAlertsRead()
            _uiState.update { it.copy(stockAlerts = emptyList()) }
        }
    }
}
