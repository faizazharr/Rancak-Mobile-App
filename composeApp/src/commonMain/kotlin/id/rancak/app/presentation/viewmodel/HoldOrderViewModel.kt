package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HoldOrderUiState(
    val isHolding: Boolean = false,
    val error: String? = null,
    /** Non-null setelah hold berhasil; consumer harus memanggil [HoldOrderViewModel.clearSuccess] setelah diproses. */
    val successSaleUuid: String? = null
)

/**
 * Mengelola aksi "simpan sebagai open bill" (hold order).
 * Sengaja dibuat kecil — hanya satu tanggung jawab.
 */
class HoldOrderViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HoldOrderUiState())
    val uiState: StateFlow<HoldOrderUiState> = _uiState.asStateFlow()

    fun holdOrder(
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
        if (_uiState.value.isHolding) return     // hindari double-tap
        viewModelScope.launch {
            _uiState.update { it.copy(isHolding = true, error = null, successSaleUuid = null) }
            when (val result = saleRepository.createSale(
                items         = items,
                paymentMethod = PaymentMethod.CASH,   // placeholder; backend abaikan saat hold = true
                paidAmount    = 0L,
                orderType     = orderType,
                tableUuid     = tableUuid,
                customerName  = customerName?.takeIf { it.isNotBlank() },
                note          = note?.takeIf { it.isNotBlank() },
                hold          = true,
                pax           = pax,
                discount      = discount,
                tax           = tax,
                adminFee      = adminFee,
                deliveryFee   = deliveryFee,
                tip           = tip,
                voucherCode   = voucherCode?.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> _uiState.update {
                    it.copy(isHolding = false, successSaleUuid = result.data.uuid)
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isHolding = false, error = result.message)
                }
                is Resource.Loading -> { /* ditangani state isHolding */ }
            }
        }
    }

    fun clearSuccess() = _uiState.update { it.copy(successSaleUuid = null) }
    fun clearError()   = _uiState.update { it.copy(error = null) }
}
