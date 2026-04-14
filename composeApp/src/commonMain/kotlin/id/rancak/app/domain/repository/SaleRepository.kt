package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface SaleRepository {
    suspend fun createSale(
        items: List<CartItem>,
        paymentMethod: PaymentMethod,
        paidAmount: Long,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String?,
        note: String?,
        hold: Boolean,
        // ── Info tamu ──────────────────────────────
        pax: Int = 1,
        // ── Komponen biaya (kalkulasi dari frontend) ──
        discount: Long = 0,
        tax: Long = 0,
        adminFee: Long = 0,
        deliveryFee: Long = 0,
        tip: Long = 0,
        voucherCode: String? = null
    ): Resource<Sale>

    suspend fun getSales(dateFrom: String? = null, dateTo: String? = null): Resource<List<Sale>>
    suspend fun getSaleDetail(saleUuid: String): Resource<Sale>
    suspend fun paySale(saleUuid: String, paymentMethod: PaymentMethod, paidAmount: Long): Resource<Sale>
    suspend fun serveSale(saleUuid: String): Resource<Sale>
    suspend fun voidSale(saleUuid: String, reason: String? = null): Resource<Sale>
    suspend fun cancelSale(saleUuid: String, reason: String? = null): Resource<Sale>
    suspend fun refundSale(saleUuid: String, amount: Long? = null, reason: String? = null): Resource<Sale>
    suspend fun moveTable(saleUuid: String, tableUuid: String): Resource<Sale>
}

data class CartItem(
    val productUuid: String,
    val productName: String,
    val qty: Int,
    val price: Long,
    val variantUuid: String? = null,
    val variantName: String? = null,
    val note: String? = null,
    val imageUrl: String? = null
) {
    val subtotal: Long get() = price * qty
}
