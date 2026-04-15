package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.repository.SaleRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class DateFilter(val label: String) {
    ALL("Semua"),
    TODAY("Hari Ini"),
    YESTERDAY("Kemarin"),
    WEEK("7 Hari"),
    CUSTOM("Pilih Tanggal")
}

private data class FilterParams(
    val query: String,
    val dateFilter: DateFilter,
    val statusFilter: SaleStatus?,
    val customDateRange: Pair<String, String>?
)

data class SalesHistoryUiState(
    val allSales: List<Sale> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val selectedSale: Sale? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val statusFilter: SaleStatus? = null,
    /** Tanggal mulai rentang kustom — format "YYYY-MM-DD" */
    val customDateFrom: String? = null,
    /** Tanggal akhir rentang kustom — format "YYYY-MM-DD" */
    val customDateTo: String? = null
)

class SalesHistoryViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _allSales       = MutableStateFlow<List<Sale>>(emptyList())
    private val _selectedSale   = MutableStateFlow<Sale?>(null)
    private val _isLoading      = MutableStateFlow(false)
    private val _error          = MutableStateFlow<String?>(null)
    private val _searchQuery    = MutableStateFlow("")
    private val _dateFilter     = MutableStateFlow(DateFilter.ALL)
    private val _statusFilter   = MutableStateFlow<SaleStatus?>(null)
    private val _customDateRange = MutableStateFlow<Pair<String, String>?>(null)

    private val _filterParams = combine(
        _searchQuery, _dateFilter, _statusFilter, _customDateRange
    ) { q, d, s, c -> FilterParams(q, d, s, c) }

    val uiState: StateFlow<SalesHistoryUiState> = combine(
        _allSales,
        _selectedSale,
        _isLoading,
        _error,
        _filterParams
    ) { allSales, selected, loading, error, filters ->
        SalesHistoryUiState(
            allSales       = allSales,
            sales          = applyFilters(allSales, filters),
            selectedSale   = selected,
            isLoading      = loading,
            error          = error,
            searchQuery    = filters.query,
            dateFilter     = filters.dateFilter,
            statusFilter   = filters.statusFilter,
            customDateFrom = filters.customDateRange?.first,
            customDateTo   = filters.customDateRange?.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesHistoryUiState())

    fun selectSale(sale: Sale?)              { _selectedSale.value  = sale }
    fun setSearchQuery(query: String)        { _searchQuery.value   = query }
    fun setDateFilter(filter: DateFilter)    { _dateFilter.value    = filter }
    fun setStatusFilter(status: SaleStatus?) { _statusFilter.value  = status }

    /**
     * Set rentang tanggal kustom dan aktifkan [DateFilter.CUSTOM].
     * @param fromMillis  epoch-millis dari tanggal mulai (UTC midnight)
     * @param toMillis    epoch-millis dari tanggal akhir (UTC midnight)
     */
    fun setCustomDateRange(fromMillis: Long, toMillis: Long) {
        val from = Instant.fromEpochMilliseconds(fromMillis)
            .toLocalDateTime(TimeZone.UTC).date.toString()
        val to = Instant.fromEpochMilliseconds(toMillis)
            .toLocalDateTime(TimeZone.UTC).date.toString()
        _customDateRange.value = from to to
        _dateFilter.value = DateFilter.CUSTOM
    }

    fun clearFilters() {
        _searchQuery.value    = ""
        _dateFilter.value     = DateFilter.ALL
        _statusFilter.value   = null
        _customDateRange.value = null
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

    private fun applyFilters(sales: List<Sale>, filters: FilterParams): List<Sale> {
        val today        = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStr     = today.toString()
        val yesterdayStr = today.minus(DatePeriod(days = 1)).toString()
        val weekStartStr = today.minus(DatePeriod(days = 6)).toString()

        return sales
            .filter { sale ->
                if (filters.query.isBlank()) true
                else {
                    val q = filters.query.trim().lowercase()
                    sale.invoiceNo?.lowercase()?.contains(q) == true ||
                    sale.items.any { it.productName.lowercase().contains(q) }
                }
            }
            .filter { sale ->
                val d = sale.createdAt?.take(10)
                    ?: return@filter filters.dateFilter == DateFilter.ALL
                when (filters.dateFilter) {
                    DateFilter.ALL       -> true
                    DateFilter.TODAY     -> d == todayStr
                    DateFilter.YESTERDAY -> d == yesterdayStr
                    DateFilter.WEEK      -> d in weekStartStr..todayStr
                    DateFilter.CUSTOM    -> {
                        val (from, to) = filters.customDateRange ?: return@filter true
                        d in from..to
                    }
                }
            }
            .filter { sale ->
                filters.statusFilter == null || sale.status == filters.statusFilter
            }
    }
}
