package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Reservation
import id.rancak.app.domain.model.ReservationInput
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.repository.OperationsRepository
import id.rancak.app.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filter status untuk list reservasi. `null` = semua. */
enum class ReservationStatusFilter(val apiValue: String?, val label: String) {
    ALL(null,        "Semua"),
    PENDING("pending",   "Menunggu"),
    CONFIRMED("confirmed", "Dikonfirmasi"),
    SEATED("seated",    "Sedang Hadir"),
    COMPLETED("completed", "Selesai"),
    CANCELLED("cancelled", "Dibatalkan")
}

@Immutable
data class ReservationUiState(
    val reservations: List<Reservation> = emptyList(),
    val tables: List<Table> = emptyList(),
    val statusFilter: ReservationStatusFilter = ReservationStatusFilter.ALL,
    val dateFilter: String? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val showFormDialog: Boolean = false,
    val editingReservation: Reservation? = null,
    val pendingCancel: Reservation? = null,
    val pendingSeat: Reservation? = null,
    val snackbarMessage: String? = null
)

/**
 * ViewModel untuk fitur Reservasi Meja.
 *
 * Lifecycle reservasi: pending → confirmed → seated → completed
 * (atau cancelled di langkah mana saja).
 *
 * - [ReservationRepository] untuk semua operasi reservasi.
 * - [OperationsRepository] dipakai untuk memuat list meja saat user
 *   memilih meja di form atau saat seat reservation.
 */
class ReservationViewModel(
    private val reservationRepository: ReservationRepository,
    private val operationsRepository: OperationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    fun load() {
        loadReservations()
        loadTables()
    }

    fun loadReservations() {
        viewModelScope.launch {
            val s = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = reservationRepository.getReservations(
                status = s.statusFilter.apiValue,
                date   = s.dateFilter
            )) {
                is Resource.Success -> _uiState.update {
                    it.copy(reservations = result.data, isLoading = false)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> { /* not used */ }
            }
        }
    }

    private fun loadTables() {
        viewModelScope.launch {
            when (val result = operationsRepository.getTables()) {
                is Resource.Success -> _uiState.update {
                    it.copy(tables = result.data.sortedBy { t -> t.sortOrder })
                }
                is Resource.Error -> { /* swallow — list meja optional di form */ }
                is Resource.Loading -> { /* not used */ }
            }
        }
    }

    // ── Filters ─────────────────────────────────────────────────────────────

    fun setStatusFilter(filter: ReservationStatusFilter) {
        _uiState.update { it.copy(statusFilter = filter) }
        loadReservations()
    }

    fun setDateFilter(date: String?) {
        _uiState.update { it.copy(dateFilter = date) }
        loadReservations()
    }

    // ── Form dialog ─────────────────────────────────────────────────────────

    fun openCreateDialog() {
        _uiState.update { it.copy(editingReservation = null, showFormDialog = true) }
    }

    fun openEditDialog(reservation: Reservation) {
        _uiState.update { it.copy(editingReservation = reservation, showFormDialog = true) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showFormDialog = false, editingReservation = null) }
    }

    // ── Save (create or update) ────────────────────────────────────────────

    fun saveReservation(input: ReservationInput) {
        val editing = _uiState.value.editingReservation
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (editing == null) {
                reservationRepository.createReservation(input)
            } else {
                reservationRepository.updateReservation(
                    reservationId   = editing.uuid,
                    customerName    = input.customerName,
                    customerPhone   = input.customerPhone,
                    partySize       = input.partySize,
                    reservedAt      = input.reservedAt,
                    durationMinutes = input.durationMinutes,
                    tableUuid       = input.tableUuid,
                    note            = input.note
                )
            }
            handleResult(result, successMsg = if (editing == null) "Reservasi dibuat" else "Reservasi diperbarui") {
                _uiState.update { it.copy(showFormDialog = false, editingReservation = null) }
            }
        }
    }

    // ── State transitions ──────────────────────────────────────────────────

    fun confirm(reservation: Reservation) = transition(
        successMsg = "Reservasi dikonfirmasi"
    ) { reservationRepository.confirmReservation(reservation.uuid) }

    fun complete(reservation: Reservation) = transition(
        successMsg = "Reservasi selesai"
    ) { reservationRepository.completeReservation(reservation.uuid) }

    // Seat — butuh table_uuid
    fun requestSeat(reservation: Reservation) {
        _uiState.update { it.copy(pendingSeat = reservation) }
    }

    fun cancelSeat() {
        _uiState.update { it.copy(pendingSeat = null) }
    }

    fun confirmSeat(tableUuid: String) {
        val target = _uiState.value.pendingSeat ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = reservationRepository.seatReservation(target.uuid, tableUuid)
            _uiState.update { it.copy(pendingSeat = null) }
            handleResult(result, successMsg = "Tamu duduk di meja")
        }
    }

    // Cancel — butuh reason opsional
    fun requestCancel(reservation: Reservation) {
        _uiState.update { it.copy(pendingCancel = reservation) }
    }

    fun dismissCancel() {
        _uiState.update { it.copy(pendingCancel = null) }
    }

    fun confirmCancel(reason: String?) {
        val target = _uiState.value.pendingCancel ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = reservationRepository.cancelReservation(target.uuid, reason)
            _uiState.update { it.copy(pendingCancel = null) }
            handleResult(result, successMsg = "Reservasi dibatalkan")
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun transition(
        successMsg: String,
        block: suspend () -> Resource<Reservation>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            handleResult(block(), successMsg)
        }
    }

    private fun handleResult(
        result: Resource<*>,
        successMsg: String,
        onSuccess: () -> Unit = {}
    ) {
        when (result) {
            is Resource.Success -> {
                _uiState.update {
                    it.copy(isSubmitting = false, snackbarMessage = successMsg)
                }
                onSuccess()
                loadReservations()
            }
            is Resource.Error ->
                _uiState.update {
                    it.copy(isSubmitting = false, snackbarMessage = result.message)
                }
            is Resource.Loading -> { /* not used */ }
        }
    }
}
