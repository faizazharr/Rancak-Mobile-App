package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.data.local.db.dao.CartDao
import id.rancak.app.data.local.db.entity.CartItemEntity
import id.rancak.app.data.local.db.entity.toDomain
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.repository.CartItem
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
    /** Diskon nominal keseluruhan (Rp). */
    val discount: Long = 0,
    /** PPN/pajak nominal. 0 = tidak ada pajak. */
    val tax: Long = 0,
    /** Biaya admin (mis. biaya platform/grab, dll). */
    val adminFee: Long = 0,
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

    /** Total akhir yang harus dibayar pelanggan. */
    val total: Long get() = subtotal - discount + tax + adminFee + deliveryFee + tip
}

class CartViewModel(private val cartDao: CartDao) : ViewModel() {

    private data class CartExtras(
        val orderType: OrderType = OrderType.DINE_IN,
        val tableUuid: String? = null,
        val customerName: String = "",
        val note: String = "",
        val pax: Int = 1,
        val discount: Long = 0,
        val tax: Long = 0,
        val adminFee: Long = 0,
        val deliveryFee: Long = 0,
        val tip: Long = 0,
        val voucherCode: String = ""
    )

    private val _extras = MutableStateFlow(CartExtras())

    // Room emits whenever the cart_items table changes — UI auto-updates
    val uiState: StateFlow<CartUiState> = cartDao.observeAll()
        .combine(_extras) { entities, extras ->
            CartUiState(
                items = entities.map { it.toDomain() },
                orderType = extras.orderType,
                tableUuid = extras.tableUuid,
                customerName = extras.customerName,
                note = extras.note,
                pax = extras.pax,
                discount = extras.discount,
                tax = extras.tax,
                adminFee = extras.adminFee,
                deliveryFee = extras.deliveryFee,
                tip = extras.tip,
                voucherCode = extras.voucherCode
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CartUiState())

    fun addProduct(product: Product, variantUuid: String? = null, variantName: String? = null) {
        viewModelScope.launch {
            val id = cartItemId(product.uuid, variantUuid)
            val existing = cartDao.findById(id)
            cartDao.upsert(
                if (existing != null) {
                    existing.copy(qty = existing.qty + 1)
                } else {
                    CartItemEntity(
                        id = id,
                        productUuid = product.uuid,
                        productName = product.name,
                        qty = 1,
                        price = product.price,
                        variantUuid = variantUuid,
                        variantName = variantName,
                        note = null,
                        imageUrl = product.imageUrl
                    )
                }
            )
        }
    }

    fun updateQuantity(productUuid: String, variantUuid: String?, qty: Int) {
        viewModelScope.launch {
            val id = cartItemId(productUuid, variantUuid)
            if (qty <= 0) cartDao.deleteById(id) else cartDao.updateQty(id, qty)
        }
    }

    fun removeItem(productUuid: String, variantUuid: String?) {
        updateQuantity(productUuid, variantUuid, 0)
    }

    fun updateItemNote(productUuid: String, variantUuid: String?, note: String) {
        viewModelScope.launch {
            cartDao.updateNote(cartItemId(productUuid, variantUuid), note.ifBlank { null })
        }
    }

    fun setOrderType(orderType: OrderType) = _extras.update { it.copy(orderType = orderType) }
    fun setTable(tableUuid: String?) = _extras.update { it.copy(tableUuid = tableUuid) }
    fun setCustomerName(name: String) = _extras.update { it.copy(customerName = name) }
    fun setNote(note: String) = _extras.update { it.copy(note = note) }
    fun setPax(pax: Int) { if (pax >= 1) _extras.update { it.copy(pax = pax) } }
    fun setDiscount(discount: Long) = _extras.update { it.copy(discount = discount.coerceAtLeast(0L)) }
    fun setTax(tax: Long) = _extras.update { it.copy(tax = tax.coerceAtLeast(0L)) }
    fun setAdminFee(adminFee: Long) = _extras.update { it.copy(adminFee = adminFee.coerceAtLeast(0L)) }
    fun setDeliveryFee(deliveryFee: Long) = _extras.update { it.copy(deliveryFee = deliveryFee.coerceAtLeast(0L)) }
    fun setTip(tip: Long) = _extras.update { it.copy(tip = tip.coerceAtLeast(0L)) }
    fun setVoucherCode(code: String) = _extras.update { it.copy(voucherCode = code) }

    fun clearCart() {
        viewModelScope.launch { cartDao.deleteAll() }
        _extras.value = CartExtras()
    }

    private fun cartItemId(productUuid: String, variantUuid: String?) =
        "$productUuid:${variantUuid ?: "_"}"
}
