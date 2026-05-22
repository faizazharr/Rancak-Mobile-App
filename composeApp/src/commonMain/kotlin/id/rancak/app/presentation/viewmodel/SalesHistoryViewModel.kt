package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.ReprintResult
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.repository.SaleRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

@Immutable
data class SalesHistoryUiState(
    val allSales: ImmutableList<Sale> = persistentListOf(),
    val sales: ImmutableList<Sale> = persistentListOf(),
    val selectedSale: Sale? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val statusFilter: SaleStatus? = null,
    /** Tanggal mulai rentang kustom — format "YYYY-MM-DD" */
    val customDateFrom: String? = null,
    /** Tanggal akhir rentang kustom — format "YYYY-MM-DD" */
    val customDateTo: String? = null,
    /** Sukses cetak ulang — null jika tidak ada; di-clear setelah ditampilkan. */
    val reprintSuccess: String? = null
)

class SalesHistoryViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesHistoryUiState())
    val uiState: StateFlow<SalesHistoryUiState> = _uiState.asStateFlow()

    private suspend fun SalesHistoryUiState.recompute(): SalesHistoryUiState = withContext(Dispatchers.Default) {
        val today        = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStr     = today.toString()
        val yesterdayStr = today.minus(DatePeriod(days = 1)).toString()
        val weekStartStr = today.minus(DatePeriod(days = 6)).toString()

        val filtered = allSales
            .filter { sale ->
                if (searchQuery.isBlank()) true
                else {
                    val q = searchQuery.trim().lowercase()
                    sale.invoiceNo?.lowercase()?.contains(q) == true ||
                    sale.items.any { it.productName.lowercase().contains(q) }
                }
            }
            .filter { sale ->
                val d = sale.createdAt?.take(10)
                    ?: return@filter dateFilter == DateFilter.ALL
                when (dateFilter) {
                    DateFilter.ALL       -> true
                    DateFilter.TODAY     -> d == todayStr
                    DateFilter.YESTERDAY -> d == yesterdayStr
                    DateFilter.WEEK      -> d in weekStartStr..todayStr
                    DateFilter.CUSTOM    -> {
                        val from = customDateFrom ?: return@filter true
                        val to   = customDateTo   ?: return@filter true
                        d in from..to
                    }
                }
            }
            .filter { sale ->
                statusFilter == null || sale.status == statusFilter
            }
        
        copy(sales = filtered.toImmutableList())
    }

    fun selectSale(sale: Sale?) {
        _uiState.update { it.copy(selectedSale = sale) }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchQuery = query).recompute()
        }
    }

    fun setDateFilter(filter: DateFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(dateFilter = filter).recompute()
        }
    }

    fun setStatusFilter(status: SaleStatus?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(statusFilter = status).recompute()
        }
    }

    /**
     * Select a sale and immediately fetch its full detail (with items).
     * The list API returns sales without items; we need the detail endpoint.
     */
    fun selectSaleAndFetchDetail(sale: Sale) {
        // Show the sale immediately (even without items) so the panel opens instantly
        _uiState.update { it.copy(selectedSale = sale) }
        viewModelScope.launch {
            // Langkah 1: Sajikan dari cache jika ada detail item tersimpan
            val cachedResult = saleRepository.getSaleDetailFromCache(sale.uuid)
            if (cachedResult is Resource.Success && cachedResult.data.items.isNotEmpty()) {
                val cachedSale = cachedResult.data
                _uiState.value = _uiState.value.copy(
                    selectedSale = cachedSale,
                    allSales = _uiState.value.allSales.map {
                        if (it.uuid == sale.uuid) cachedSale else it
                    }.toImmutableList()
                ).recompute()
            }

            // Langkah 2: Refresh dari network
            when (val result = saleRepository.getSaleDetail(sale.uuid)) {
                is Resource.Success -> {
                    val updatedSale = result.data
                    _uiState.value = _uiState.value.copy(
                        selectedSale = updatedSale,
                        allSales = _uiState.value.allSales.map {
                            if (it.uuid == sale.uuid) updatedSale else it
                        }.toImmutableList()
                    ).recompute()
                }
                is Resource.Error   -> { /* keep showing partial data */ }
                is Resource.Loading -> {}
            }
        }
    }

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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                customDateFrom = from,
                customDateTo = to,
                dateFilter = DateFilter.CUSTOM
            ).recompute()
        }
    }

    fun clearFilters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchQuery = "",
                dateFilter = DateFilter.ALL,
                statusFilter = null,
                customDateFrom = null,
                customDateTo = null
            ).recompute()
        }
    }

    fun clearReprintSuccess() { _uiState.update { it.copy(reprintSuccess = null) } }

    fun reprintSale(saleUuid: String, printType: String = "receipt") {
        viewModelScope.launch {
            when (val result = saleRepository.reprintSale(saleUuid, printType = printType)) {
                is Resource.Success -> _uiState.update { it.copy(reprintSuccess = "Struk berhasil dicetak ulang") }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    /** Pindah meja untuk held order. Setelah sukses, reload list & update detail. */
    fun moveTable(saleUuid: String, tableUuid: String) {
        viewModelScope.launch {
            when (val result = saleRepository.moveTable(saleUuid, tableUuid)) {
                is Resource.Success -> {
                    val updatedSale = result.data
                    _uiState.value = _uiState.value.copy(
                        allSales = _uiState.value.allSales.map {
                            if (it.uuid == saleUuid) updatedSale else it
                        }.toImmutableList(),
                        selectedSale = if (_uiState.value.selectedSale?.uuid == saleUuid) updatedSale else _uiState.value.selectedSale,
                        reprintSuccess = "Meja berhasil dipindahkan"
                    ).recompute()
                }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    /** Gabung held order — semua item dari [sourceUuid] dipindah ke [targetUuid]. */
    fun mergeSale(targetUuid: String, sourceUuid: String) {
        viewModelScope.launch {
            when (val result = saleRepository.mergeSale(targetUuid, sourceUuid)) {
                is Resource.Success -> {
                    val mergedSale = result.data
                    _uiState.value = _uiState.value.copy(
                        allSales = _uiState.value.allSales.map {
                            if (it.uuid == targetUuid) mergedSale else it
                        }.filter { it.uuid != sourceUuid }.toImmutableList(),
                        selectedSale = when (_uiState.value.selectedSale?.uuid) {
                            targetUuid, sourceUuid -> mergedSale
                            else -> _uiState.value.selectedSale
                        },
                        reprintSuccess = "Transaksi berhasil digabung"
                    ).recompute()
                }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadSales() {
        viewModelScope.launch {
            // Langkah 1: Muat dari cache untuk instant UI
            val cachedResult = saleRepository.getSalesFromCache()
            val hasCachedData = (cachedResult as? Resource.Success)?.data?.isNotEmpty() == true
            if (hasCachedData) {
                _uiState.value = _uiState.value.copy(
                    allSales = (cachedResult as Resource.Success).data.toImmutableList(),
                    isLoading = false
                ).recompute()
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            // Langkah 2: Refresh dari network
            when (val result = saleRepository.getSales()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        allSales = result.data.toImmutableList(),
                        isLoading = false
                    ).recompute()
                }
                is Resource.Error -> {
                    if (!hasCachedData) {
                        _uiState.update { it.copy(error = result.message, isLoading = false) }
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
