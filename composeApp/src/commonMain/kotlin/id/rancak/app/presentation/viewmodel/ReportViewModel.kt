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
import id.rancak.app.domain.model.StockReport
import id.rancak.app.domain.repository.FinanceRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ReportUiState(
    val summary: ShiftSummary? = null,
    val mySalesToday: MySalesReport? = null,
    val dailyByCategory: ImmutableList<DailyCategoryReport> = persistentListOf(),
    val cashierShifts: ImmutableList<CashierShiftSummary> = persistentListOf(),
    val isCashierShiftsLoading: Boolean = false,
    val cashierShiftDate: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val dateFrom: String = "",
    val dateTo: String = "",
    // Stock alerts
    val stockAlerts: ImmutableList<StockAlert> = persistentListOf(),
    val lowStockItems: ImmutableList<LowStock> = persistentListOf(),
    val expiringBatches: ImmutableList<ExpiringBatch> = persistentListOf(),
    val stockReport: ImmutableList<StockReport> = persistentListOf(),
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

            val summaryDeferred   = async { financeRepository.getShiftSummary() }
            val mySalesDeferred   = async { financeRepository.getMySalesToday() }
            val dailyCatDeferred = async { financeRepository.getDailyByCategory() }

            val summaryRes  = summaryDeferred.await()
            val mySalesRes  = mySalesDeferred.await()
            val dailyCatRes = dailyCatDeferred.await()

            _uiState.update { state ->
                var s = state.copy(isLoading = false)
                if (summaryRes is Resource.Success) s = s.copy(summary = summaryRes.data)
                if (mySalesRes is Resource.Success) s = s.copy(mySalesToday = mySalesRes.data)
                if (dailyCatRes is Resource.Success) s = s.copy(dailyByCategory = dailyCatRes.data.toImmutableList())
                
                // Show first error found, if any
                s.copy(
                    error = when {
                        summaryRes is Resource.Error -> summaryRes.message
                        mySalesRes is Resource.Error -> mySalesRes.message
                        dailyCatRes is Resource.Error -> dailyCatRes.message
                        else -> null
                    }
                )
            }
        }
    }

    fun loadCashierShifts(date: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCashierShiftsLoading = true) }
            when (val result = financeRepository.getShiftByCashier(date)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        cashierShifts = result.data.toImmutableList(),
                        isCashierShiftsLoading = false,
                        cashierShiftDate = date ?: ""
                    )
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
            
            val alertsDeferred   = async { financeRepository.getStockAlerts() }
            val lowStockDeferred = async { financeRepository.getLowStock() }
            val expiringDeferred = async { financeRepository.getExpiringBatches(days = 30) }
            val stockRptDeferred = async { financeRepository.getStockReport() }

            val alertsRes   = alertsDeferred.await()
            val lowStockRes = lowStockDeferred.await()
            val expiringRes = expiringDeferred.await()
            val stockRptRes = stockRptDeferred.await()

            _uiState.update { state ->
                state.copy(
                    isStockLoading  = false,
                    stockAlerts     = (alertsRes   as? Resource.Success)?.data?.toImmutableList() ?: state.stockAlerts,
                    lowStockItems   = (lowStockRes as? Resource.Success)?.data?.toImmutableList() ?: state.lowStockItems,
                    expiringBatches = (expiringRes as? Resource.Success)?.data?.toImmutableList() ?: state.expiringBatches,
                    stockReport     = (stockRptRes as? Resource.Success)?.data?.toImmutableList() ?: state.stockReport,
                    stockError      = (alertsRes as? Resource.Error)?.message
                )
            }
        }
    }

    fun markAlertRead(alertId: String) {
        viewModelScope.launch {
            financeRepository.markStockAlertRead(alertId)
            _uiState.update { state ->
                state.copy(stockAlerts = state.stockAlerts.filterNot { it.productUuid == alertId }.toImmutableList())
            }
        }
    }

    fun markAllAlertsRead() {
        viewModelScope.launch {
            financeRepository.markAllStockAlertsRead()
            _uiState.update { it.copy(stockAlerts = persistentListOf()) }
        }
    }
}
