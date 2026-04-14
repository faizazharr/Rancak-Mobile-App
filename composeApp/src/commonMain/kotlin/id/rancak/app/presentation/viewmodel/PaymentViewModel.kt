package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val selectedMethod: PaymentMethod = PaymentMethod.CASH,
    val paidAmount: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val completedSale: Sale? = null
) {
    val paidAmountLong: Long get() = paidAmount.toLongOrNull() ?: 0L
}

class PaymentViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun selectMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method, error = null) }
    }

    fun setPaidAmount(amount: String) {
        _uiState.update { it.copy(paidAmount = amount.filter { c -> c.isDigit() }, error = null) }
    }

    fun processPayment(
        items: List<CartItem>,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String?,
        note: String?,
        pax: Int = 1,
        discount: Long = 0,
        tax: Long = 0,
        adminFee: Long = 0,
        deliveryFee: Long = 0,
        tip: Long = 0,
        voucherCode: String? = null
    ) {
        val state = _uiState.value
        if (state.paidAmountLong <= 0 && state.selectedMethod == PaymentMethod.CASH) {
            _uiState.update { it.copy(error = "Masukkan jumlah pembayaran") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            when (val result = saleRepository.createSale(
                items         = items,
                paymentMethod = state.selectedMethod,
                paidAmount    = state.paidAmountLong,
                orderType     = orderType,
                tableUuid     = tableUuid,
                customerName  = customerName?.takeIf { it.isNotBlank() },
                note          = note?.takeIf { it.isNotBlank() },
                hold          = false,
                pax           = pax,
                discount      = discount,
                tax           = tax,
                adminFee      = adminFee,
                deliveryFee   = deliveryFee,
                tip           = tip,
                voucherCode   = voucherCode?.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isProcessing = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun reset() {
        _uiState.value = PaymentUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
