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
        hold: Boolean
    ): Resource<Sale>

    suspend fun getSales(dateFrom: String? = null, dateTo: String? = null): Resource<List<Sale>>
    suspend fun getSaleDetail(saleUuid: String): Resource<Sale>
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
