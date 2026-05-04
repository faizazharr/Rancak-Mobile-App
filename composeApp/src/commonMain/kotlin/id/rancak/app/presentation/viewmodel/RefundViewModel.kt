package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Refund
import id.rancak.app.domain.model.RefundItemInput
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Satu baris item yang dapat di-refund di dialog. */
@Immutable
data class RefundLine(
    val saleItemUuid: String,
    val productName: String,
    val variantName: String?,
    val maxQty: Int,
    val unitPrice: Long,
    val qtyToRefund: Int = 0
) {
    val lineRefund: Long get() = unitPrice * qtyToRefund
}

@Immutable
data class RefundUiState(
    val saleUuid: String? = null,
    val invoiceNo: String? = null,
    val lines: List<RefundLine> = emptyList(),
    val reason: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val completed: Refund? = null,
    // Derived
    val totalRefund: Long = 0,
    val totalQty: Int = 0,
    val canSubmit: Boolean = false
)

/**
 * ViewModel untuk dialog refund — gunakan Pattern C (recompute derived fields).
 *
 * Alur:
 *  1. [openFor] → buka untuk satu [Sale] yang status-nya `paid`
 *  2. [setQty] / [setReason] / [refundFull] → update input
 *  3. [submit] → POST ke backend, hasilnya di [RefundUiState.completed]
 *  4. [reset] setelah dismissed
 */
class RefundViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RefundUiState())
    val uiState: StateFlow<RefundUiState> = _uiState.asStateFlow()

    /** Buka refund flow untuk [sale]. Akan mengisi [RefundUiState.lines]. */
    fun openFor(sale: Sale) {
        _uiState.value = RefundUiState(
            saleUuid  = sale.uuid,
            invoiceNo = sale.invoiceNo,
            lines     = sale.items.map { it.toRefundLine() }
        ).recompute()
    }

    fun setQty(saleItemUuid: String, qty: Int) {
        _uiState.update { state ->
            state.copy(
                lines = state.lines.map { line ->
                    if (line.saleItemUuid == saleItemUuid)
                        line.copy(qtyToRefund = qty.coerceIn(0, line.maxQty))
                    else line
                }
            ).recompute()
        }
    }

    /** Set semua qty ke maxQty (refund seluruh item). */
    fun refundFull() {
        _uiState.update { state ->
            state.copy(
                lines = state.lines.map { it.copy(qtyToRefund = it.maxQty) }
            ).recompute()
        }
    }

    /** Reset semua qty ke 0. */
    fun clearQty() {
        _uiState.update { state ->
            state.copy(lines = state.lines.map { it.copy(qtyToRefund = 0) }).recompute()
        }
    }

    fun setReason(value: String) {
        _uiState.update { it.copy(reason = value) }
    }

    fun submit() {
        val state = _uiState.value
        val saleUuid = state.saleUuid ?: return
        val items = state.lines
            .filter { it.qtyToRefund > 0 }
            .map { RefundItemInput(saleItemUuid = it.saleItemUuid, qty = it.qtyToRefund) }
        if (items.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            when (val result = saleRepository.refundSale(
                saleUuid = saleUuid,
                items    = items,
                reason   = state.reason.trim().ifEmpty { null }
            )) {
                is Resource.Success -> _uiState.update {
                    it.copy(isProcessing = false, completed = result.data)
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isProcessing = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun reset() {
        _uiState.value = RefundUiState()
    }
}

private fun SaleItem.toRefundLine(): RefundLine = RefundLine(
    saleItemUuid = uuid,
    productName  = productName,
    variantName  = variantName,
    maxQty       = qty.toDoubleOrNull()?.toInt() ?: 1,
    unitPrice    = price
)

private fun RefundUiState.recompute(): RefundUiState {
    val totalRefund = lines.sumOf { it.lineRefund }
    val totalQty    = lines.sumOf { it.qtyToRefund }
    return copy(
        totalRefund = totalRefund,
        totalQty    = totalQty,
        canSubmit   = totalQty > 0 && !isProcessing
    )
}
