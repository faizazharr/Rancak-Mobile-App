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

    /** Split payment: kirim beberapa metode bayar sekaligus. */
    suspend fun createSaleWithSplitPayment(
        items: List<CartItem>,
        payments: List<SplitPaymentEntry>,
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
    ): Resource<Sale>

    suspend fun getSales(dateFrom: String? = null, dateTo: String? = null): Resource<List<Sale>>
    suspend fun getSaleDetail(saleUuid: String): Resource<Sale>
    suspend fun paySale(saleUuid: String, paymentMethod: PaymentMethod, paidAmount: Long): Resource<Sale>

    /** Bayar held order dengan split payment. */
    suspend fun paySaleWithSplitPayment(saleUuid: String, payments: List<SplitPaymentEntry>): Resource<Sale>

    /** Pisahkan item dari held order ke transaksi baru (split bill). */
    suspend fun splitBill(saleUuid: String, itemIds: List<String>): Resource<SplitBillResult>

    suspend fun serveSale(saleUuid: String): Resource<Sale>
    suspend fun voidSale(saleUuid: String, reason: String? = null): Resource<Sale>
    suspend fun cancelSale(saleUuid: String, reason: String? = null): Resource<Sale>
    suspend fun refundSale(saleUuid: String, amount: Long? = null, reason: String? = null): Resource<Sale>
    suspend fun moveTable(saleUuid: String, tableUuid: String): Resource<Sale>

    /** Create (or retrieve) a Xendit QRIS QR code for the given sale. */
    suspend fun createQrPayment(saleUuid: String): Resource<QrPayment>

    /** Poll the current status of the QRIS payment for the given sale. */
    suspend fun getQrPaymentStatus(saleUuid: String): Resource<QrPayment>

    suspend fun getSaleReceipt(saleUuid: String): Resource<Receipt>
    suspend fun getReceiptEscpos(saleUuid: String): Resource<ByteArray>
    suspend fun getReceiptKitchen(saleUuid: String): Resource<ByteArray>
    suspend fun getReceiptCombined(saleUuid: String, kotFirst: Boolean = true): Resource<ByteArray>
    suspend fun batchSales(sales: List<CartItem>): Resource<Unit>
    suspend fun getOrderBoard(date: String? = null, includeDone: Boolean = false): Resource<List<OrderBoardOrder>>
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

/** Satu metode bayar dalam split payment. */
data class SplitPaymentEntry(
    val method: PaymentMethod,
    val amount: Long,
    val note: String? = null
)

/** Hasil split bill — transaksi asal + transaksi baru. */
data class SplitBillResult(
    val original: Sale,
    val newSale: Sale
)
