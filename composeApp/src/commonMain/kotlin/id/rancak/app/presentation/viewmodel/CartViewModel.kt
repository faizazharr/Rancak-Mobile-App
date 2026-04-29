package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val orderType: OrderType = OrderType.DINE_IN,
    val tableUuid: String? = null,
    val customerName: String = "",
    val note: String = "",
    val pax: Int = 1,
    // ── Komponen biaya (dihitung di frontend) ────────────────────────────
    /** Nilai input diskon: nominal Rp jika !discountIsPercent, atau angka 0–100 jika discountIsPercent. */
    val discountInput: Long = 0,
    /** True = diskon dihitung sebagai persen dari subtotal; False = nominal Rp tetap. */
    val discountIsPercent: Boolean = false,
    /** Nilai input pajak: nominal Rp atau angka 0–100 jika taxIsPercent. */
    val taxInput: Long = 0,
    /** True = pajak dihitung sebagai persen dari subtotal. */
    val taxIsPercent: Boolean = false,
    /** Nilai input biaya admin: nominal Rp atau angka 0–100 jika adminFeeIsPercent. */
    val adminFeeInput: Long = 0,
    /** True = biaya admin dihitung sebagai persen dari subtotal. */
    val adminFeeIsPercent: Boolean = false,
    /** Ongkos kirim (hanya relevan saat orderType = DELIVERY). */
    val deliveryFee: Long = 0,
    /** Tip opsional dari pelanggan. */
    val tip: Long = 0,
    /** Kode voucher/promo yang diaplikasikan (validasi di backend). */
    val voucherCode: String = ""
) {
    val subtotal: Long get() = items.sumOf { it.subtotal }
    val itemCount: Int get() = items.sumOf { it.qty }
    val isEmpty: Boolean get() = items.isEmpty()

    /** Diskon nominal Rp aktual — computed dari discountInput + discountIsPercent. */
    val discount: Long get() = if (discountIsPercent)
        (subtotal * discountInput / 100L).coerceIn(0L, subtotal)
    else discountInput

    /** Pajak nominal Rp aktual — computed dari taxInput + taxIsPercent. */
    val tax: Long get() = if (taxIsPercent)
        ((subtotal - discount) * taxInput / 100L).coerceAtLeast(0L)
    else taxInput

    /** Biaya admin nominal Rp aktual — computed dari adminFeeInput + adminFeeIsPercent. */
    val adminFee: Long get() = if (adminFeeIsPercent)
        ((subtotal - discount) * adminFeeInput / 100L).coerceAtLeast(0L)
    else adminFeeInput

    /** Total akhir yang harus dibayar pelanggan. */
    val total: Long get() = subtotal - discount + tax + adminFee + deliveryFee + tip
}

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private data class CartExtras(
        val orderType: OrderType = OrderType.DINE_IN,
        val tableUuid: String? = null,
        val customerName: String = "",
        val note: String = "",
        val pax: Int = 1,
        val discountInput: Long = 0,
        val discountIsPercent: Boolean = false,
        val taxInput: Long = 0,
        val taxIsPercent: Boolean = false,
        val adminFeeInput: Long = 0,
        val adminFeeIsPercent: Boolean = false,
        val deliveryFee: Long = 0,
        val tip: Long = 0,
        val voucherCode: String = ""
    )

    private val _extras = MutableStateFlow(CartExtras())

    // Repository emits whenever the cart_items table changes — UI auto-updates
    val uiState: StateFlow<CartUiState> = cartRepository.observeItems()
        .combine(_extras) { items, extras ->
            CartUiState(
                items = items,
                orderType = extras.orderType,
                tableUuid = extras.tableUuid,
                customerName = extras.customerName,
                note = extras.note,
                pax = extras.pax,
                discountInput = extras.discountInput,
                discountIsPercent = extras.discountIsPercent,
                taxInput = extras.taxInput,
                taxIsPercent = extras.taxIsPercent,
                adminFeeInput = extras.adminFeeInput,
                adminFeeIsPercent = extras.adminFeeIsPercent,
                deliveryFee = extras.deliveryFee,
                tip = extras.tip,
                voucherCode = extras.voucherCode
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CartUiState())

    fun addProduct(product: Product, variantUuid: String? = null, variantName: String? = null) {
        viewModelScope.launch {
            cartRepository.addOrIncrement(product, variantUuid, variantName)
        }
    }

    fun updateQuantity(productUuid: String, variantUuid: String?, qty: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productUuid, variantUuid, qty)
        }
    }

    fun removeItem(productUuid: String, variantUuid: String?) {
        viewModelScope.launch {
            cartRepository.removeItem(productUuid, variantUuid)
        }
    }

    fun updateItemNote(productUuid: String, variantUuid: String?, note: String) {
        viewModelScope.launch {
            cartRepository.updateNote(productUuid, variantUuid, note)
        }
    }

    fun setOrderType(orderType: OrderType) = _extras.update { it.copy(orderType = orderType) }
    fun setTable(tableUuid: String?) = _extras.update { it.copy(tableUuid = tableUuid) }
    fun setCustomerName(name: String) = _extras.update { it.copy(customerName = name) }
    fun setNote(note: String) = _extras.update { it.copy(note = note) }
    fun setPax(pax: Int) { if (pax >= 1) _extras.update { it.copy(pax = pax) } }
    fun setDiscount(value: Long, isPercent: Boolean = false) = _extras.update {
        it.copy(
            discountInput     = value.coerceIn(0L, if (isPercent) 100L else Long.MAX_VALUE),
            discountIsPercent = isPercent
        )
    }
    fun setTax(value: Long, isPercent: Boolean = false) = _extras.update {
        it.copy(
            taxInput     = value.coerceIn(0L, if (isPercent) 100L else Long.MAX_VALUE),
            taxIsPercent = isPercent
        )
    }
    fun setAdminFee(value: Long, isPercent: Boolean = false) = _extras.update {
        it.copy(
            adminFeeInput     = value.coerceIn(0L, if (isPercent) 100L else Long.MAX_VALUE),
            adminFeeIsPercent = isPercent
        )
    }
    fun setDeliveryFee(deliveryFee: Long) = _extras.update { it.copy(deliveryFee = deliveryFee.coerceAtLeast(0L)) }
    fun setTip(tip: Long) = _extras.update { it.copy(tip = tip.coerceAtLeast(0L)) }
    fun setVoucherCode(code: String) = _extras.update { it.copy(voucherCode = code) }

    fun clearCart() {
        viewModelScope.launch { cartRepository.clearAll() }
        _extras.value = CartExtras()
    }
}
