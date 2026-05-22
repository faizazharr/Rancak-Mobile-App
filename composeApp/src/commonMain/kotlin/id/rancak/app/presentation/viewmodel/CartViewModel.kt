package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.data.local.LocalOpenBill
import id.rancak.app.data.local.PricingConfigStore
import id.rancak.app.data.local.toDomain
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.domain.repository.CartRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class CartUiState(
    val items: ImmutableList<CartItem> = persistentListOf(),
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
    val voucherCode: String = "",
    /** ID open bill lokal yang sedang diubah — null jika ini adalah pesanan baru. */
    val activeOpenBillId: String? = null,
    /** Nama open bill yang sedang aktif, untuk ditampilkan di UI. */
    val activeOpenBillName: String = "",
    /** UUID sale di backend (HELD) yang sedang aktif — non-null saat keranjang berasal dari open bill yang sudah tersinkron ke KDS. */
    val activeOpenBillSaleUuid: String? = null,
    // ── Auto-applied dari Pricing Settings ───────────────────────────────
    /** Daftar TaxConfig aktif yang otomatis dipakai (mis. PPN 11%). */
    val activeTaxConfigs: ImmutableList<TaxConfig> = persistentListOf(),
    /** Daftar Surcharge aktif yang cocok dengan orderType saat ini. */
    val activeSurcharges: ImmutableList<Surcharge> = persistentListOf(),
    // ── Precomputed values ──
    val subtotal: Long = 0,
    val itemCount: Int = 0,
    val discount: Long = 0,
    val tax: Long = 0,
    val autoTax: Long = 0,
    val totalTax: Long = 0,
    val adminFee: Long = 0,
    val autoSurcharge: Long = 0,
    val totalSurcharge: Long = 0,
    val total: Long = 0
) {
    val isEmpty: Boolean get() = items.isEmpty()
}

class CartViewModel(
    private val cartRepository: CartRepository,
    private val pricingStore: PricingConfigStore
) : ViewModel() {

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
        val voucherCode: String = "",
        val activeOpenBillId: String? = null,
        val activeOpenBillName: String = "",
        val activeOpenBillSaleUuid: String? = null
    )

    private val _extras = MutableStateFlow(CartExtras())

    init {
        // Pastikan pricing configs sudah ter-load minimal sekali (idempotent).
        viewModelScope.launch { pricingStore.ensureLoaded() }
    }

    /**
     * Memaksa reload dari server. Biasanya tidak perlu dipanggil manual karena
     * [PricingConfigStore] otomatis menyiarkan perubahan ke semua observer; ini
     * disediakan untuk kasus pull-to-refresh.
     */
    fun loadPricingConfigs() {
        viewModelScope.launch { pricingStore.refresh() }
    }

    // Repository emits whenever the cart_items table changes — UI auto-updates
    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.observeItems(),
        _extras,
        pricingStore.taxConfigs,
        pricingStore.surcharges
    ) { items, extras, taxConfigs, surcharges ->
        // Hanya konfigurasi yang `isActive` yang ikut diperhitungkan di kasir.
        val activeTax = taxConfigs.filter { it.isActive }.toImmutableList()
        val activeSurcharges = surcharges.filter { it.isActive }
        // Surcharge yang berlaku: yang orderType-nya null (semua), atau cocok dengan orderType saat ini.
        val orderTypeKey = extras.orderType.name.lowercase()
        val applicableSurcharges = activeSurcharges.filter { sc ->
            sc.orderType.isNullOrBlank() || sc.orderType.equals(orderTypeKey, ignoreCase = true)
        }.toImmutableList()

        val subtotal = items.sumOf { it.subtotal }
        val itemCount = items.sumOf { it.qty }
        val discount = if (extras.discountIsPercent)
            (subtotal * extras.discountInput / 100L).coerceIn(0L, subtotal)
        else extras.discountInput

        val adminFee = if (extras.adminFeeIsPercent)
            ((subtotal - discount) * extras.adminFeeInput / 100L).coerceAtLeast(0L)
        else extras.adminFeeInput

        val autoSurcharge = applicableSurcharges.sumOf { sc ->
            val raw = if (sc.isPercentage) {
                val basis = (subtotal - discount).coerceAtLeast(0L)
                (basis * sc.amount / 100L).coerceAtLeast(0L)
            } else sc.amount
            sc.maxAmount?.let { cap -> raw.coerceAtMost(cap) } ?: raw
        }
        val totalSurcharge = adminFee + autoSurcharge

        val tax = if (extras.taxIsPercent)
            ((subtotal - discount) * extras.taxInput / 100L).coerceAtLeast(0L)
        else extras.taxInput

        val autoTax = activeTax.sumOf { cfg ->
            val basis = if (cfg.applyTo == "subtotal") subtotal
            else (subtotal - discount + totalSurcharge).coerceAtLeast(0L)
            ((basis * (cfg.rate * 100).toLong()) / 10_000L).coerceAtLeast(0L)
        }
        val totalTax = tax + autoTax

        val total = subtotal - discount + totalTax + totalSurcharge + extras.deliveryFee + extras.tip

        CartUiState(
            items = items.toImmutableList(),
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
            voucherCode = extras.voucherCode,
            activeOpenBillId       = extras.activeOpenBillId,
            activeOpenBillName     = extras.activeOpenBillName,
            activeOpenBillSaleUuid = extras.activeOpenBillSaleUuid,
            activeTaxConfigs       = activeTax,
            activeSurcharges       = applicableSurcharges,
            subtotal = subtotal,
            itemCount = itemCount,
            discount = discount,
            tax = tax,
            autoTax = autoTax,
            totalTax = totalTax,
            adminFee = adminFee,
            autoSurcharge = autoSurcharge,
            totalSurcharge = totalSurcharge,
            total = total
        )
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

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

    /**
     * Muat open bill lokal ke dalam keranjang secara atomik.
     * Item yang ada sebelumnya dihapus dan diganti dengan item dari [bill].
     * [CartUiState.activeOpenBillId] di-set agar UI tahu bill mana yang sedang diubah.
     */
    fun loadOpenBill(bill: LocalOpenBill) {
        viewModelScope.launch {
            cartRepository.replaceAll(bill.items.map { it.toDomain() })
            _extras.update {
                it.copy(
                    orderType          = runCatching { OrderType.valueOf(bill.orderType) }
                                            .getOrDefault(OrderType.DINE_IN),
                    tableUuid          = bill.tableUuid,
                    customerName       = bill.customerName,
                    note               = bill.note,
                    pax                = bill.pax,
                    discountInput      = bill.discountInput,
                    discountIsPercent  = bill.discountIsPercent,
                    taxInput           = bill.taxInput,
                    taxIsPercent       = bill.taxIsPercent,
                    adminFeeInput      = bill.adminFeeInput,
                    adminFeeIsPercent  = bill.adminFeeIsPercent,
                    deliveryFee        = bill.deliveryFee,
                    tip                = bill.tip,
                    voucherCode        = bill.voucherCode,
                    activeOpenBillId       = bill.id,
                    activeOpenBillName     = bill.name,
                    activeOpenBillSaleUuid = bill.remoteSaleUuid
                )
            }
        }
    }
}
