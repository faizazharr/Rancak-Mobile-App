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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock

private const val KDS_POLL_INTERVAL_MS = 15_000L

@Immutable
data class KdsUiState(
    val activeOrders: ImmutableList<KdsOrder> = persistentListOf(),
    val completedOrders: ImmutableList<KdsOrder> = persistentListOf(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Epoch-ms dari polling terakhir berhasil; null jika belum pernah load. */
    val lastUpdatedMs: Long? = null,
    // Precomputed agar tidak diulang di setiap rekomposisi.
    val displayOrders: ImmutableList<KdsOrder> = persistentListOf(),
) {
    suspend fun recompute() =
        withContext(Dispatchers.Default) {
            copy(displayOrders = if (showCompleted) completedOrders else activeOrders)
        }
}

class KdsViewModel(
    private val operationsRepository: OperationsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(KdsUiState())
    val uiState: StateFlow<KdsUiState> = _uiState.asStateFlow()

    init {
        startPolling()
    }

    /** Polling loop: auto-refresh setiap [KDS_POLL_INTERVAL_MS] ms selama ViewModel hidup. */
    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                loadOrdersSilently()
                delay(KDS_POLL_INTERVAL_MS)
            }
        }
    }

    /** Refresh tanpa menampilkan full loading indicator — hanya dipakai polling. */
    private fun loadOrdersSilently() {
        viewModelScope.launch {
            when (val result = operationsRepository.getKdsOrders()) {
                is Resource.Success -> {
                    val orders = result.data
                    withContext(Dispatchers.Default) {
                        val (notDone, done) = orders.partition { it.status != KdsStatus.DONE }
                        val active = notDone.toImmutableList()
                        val completed = done.toImmutableList()
                        _uiState.value =
                            _uiState.value.copy(
                                activeOrders = active,
                                completedOrders = completed,
                                lastUpdatedMs = Clock.System.now().toEpochMilliseconds(),
                            ).recompute()
                    }
                }
                is Resource.Error -> { /* silent — preserve stale data */ }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleTab(showCompleted: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showCompleted = showCompleted).recompute()
        }
    }

    /** Manual reload (tombol refresh) — tampilkan loading indicator. */
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = operationsRepository.getKdsOrders()) {
                is Resource.Success -> {
                    val orders = result.data
                    withContext(Dispatchers.Default) {
                        val (notDone, done) = orders.partition { it.status != KdsStatus.DONE }
                        val active = notDone.toImmutableList()
                        val completed = done.toImmutableList()
                        _uiState.value =
                            _uiState.value.copy(
                                activeOrders = active,
                                completedOrders = completed,
                                isLoading = false,
                                lastUpdatedMs = Clock.System.now().toEpochMilliseconds(),
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

    fun updateOrderStatus(
        kdsUuid: String,
        status: KdsStatus,
    ) {
        viewModelScope.launch {
            when (val result = operationsRepository.updateKdsStatus(kdsUuid, status)) {
                is Resource.Success -> loadOrders()
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
