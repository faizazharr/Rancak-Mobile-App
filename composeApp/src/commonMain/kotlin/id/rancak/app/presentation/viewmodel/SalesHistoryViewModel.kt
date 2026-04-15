package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class DateFilter(val label: String) {
    ALL("Semua"),
    TODAY("Hari Ini"),
    YESTERDAY("Kemarin"),
    WEEK("7 Hari")
}

data class SalesHistoryUiState(
    val allSales: List<Sale> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val selectedSale: Sale? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val statusFilter: SaleStatus? = null
)

class SalesHistoryViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _allSales     = MutableStateFlow<List<Sale>>(emptyList())
    private val _selectedSale = MutableStateFlow<Sale?>(null)
    private val _isLoading    = MutableStateFlow(false)
    private val _error        = MutableStateFlow<String?>(null)
    private val _searchQuery  = MutableStateFlow("")
    private val _dateFilter   = MutableStateFlow(DateFilter.ALL)
    private val _statusFilter = MutableStateFlow<SaleStatus?>(null)

    val uiState: StateFlow<SalesHistoryUiState> = combine(
        _allSales,
        _selectedSale,
        _isLoading,
        _error,
        combine(_searchQuery, _dateFilter, _statusFilter) { q, d, s -> Triple(q, d, s) }
    ) { allSales, selected, loading, error, (query, dateFilter, statusFilter) ->
        SalesHistoryUiState(
            allSales     = allSales,
            sales        = applyFilters(allSales, query, dateFilter, statusFilter),
            selectedSale = selected,
            isLoading    = loading,
            error        = error,
            searchQuery  = query,
            dateFilter   = dateFilter,
            statusFilter = statusFilter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesHistoryUiState())

    fun selectSale(sale: Sale?)              { _selectedSale.value = sale }
    fun setSearchQuery(query: String)        { _searchQuery.value  = query }
    fun setDateFilter(filter: DateFilter)    { _dateFilter.value   = filter }
    fun setStatusFilter(status: SaleStatus?) { _statusFilter.value = status }

    fun clearFilters() {
        _searchQuery.value  = ""
        _dateFilter.value   = DateFilter.ALL
        _statusFilter.value = null
    }

    fun loadSales() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null
            when (val result = saleRepository.getSales()) {
                is Resource.Success -> {
                    _allSales.value  = result.data
                    _isLoading.value = false
                }
                is Resource.Error -> {
                    _error.value     = result.message
                    _isLoading.value = false
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun applyFilters(
        sales: List<Sale>,
        query: String,
        dateFilter: DateFilter,
        statusFilter: SaleStatus?
    ): List<Sale> {
        val today        = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStr     = today.toString()
        val yesterdayStr = today.minus(DatePeriod(days = 1)).toString()
        val weekStartStr = today.minus(DatePeriod(days = 6)).toString()

        return sales
            .filter { sale ->
                if (query.isBlank()) true
                else {
                    val q = query.trim().lowercase()
                    sale.invoiceNo?.lowercase()?.contains(q) == true ||
                    sale.items.any { it.productName.lowercase().contains(q) }
                }
            }
            .filter { sale ->
                val d = sale.createdAt?.take(10) ?: return@filter dateFilter == DateFilter.ALL
                when (dateFilter) {
                    DateFilter.ALL       -> true
                    DateFilter.TODAY     -> d == todayStr
                    DateFilter.YESTERDAY -> d == yesterdayStr
                    DateFilter.WEEK      -> d in weekStartStr..todayStr
                }
            }
            .filter { sale ->
                statusFilter == null || sale.status == statusFilter
            }
    }
}
