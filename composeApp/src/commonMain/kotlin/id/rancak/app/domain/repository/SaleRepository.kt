package id.rancak.app.domain.repository

import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderBoardOrder
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.QrPayment
import id.rancak.app.domain.model.Receipt
import id.rancak.app.domain.model.Refund
import id.rancak.app.domain.model.RefundItemInput
import id.rancak.app.domain.model.ReprintResult
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SplitBillResult
import id.rancak.app.domain.model.SplitPaymentEntry

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

    /** Tambah item ke held/open-bill order (POST /sales/:id/items). */
    suspend fun addItemsToHeldOrder(saleUuid: String, items: List<CartItem>): Resource<Sale>

    /** Hapus satu item dari held/open-bill order (DELETE /sales/:id/items/:item_id). */
    suspend fun removeHeldOrderItem(saleUuid: String, itemUuid: String): Resource<Sale>

    suspend fun serveSale(saleUuid: String): Resource<Sale>
    suspend fun voidSale(saleUuid: String, reason: String? = null): Resource<Sale>
    suspend fun cancelSale(saleUuid: String, reason: String? = null): Resource<Sale>

    /**
     * Partial / full refund: kembalikan sebagian atau seluruh item.
     * Backend akan mengembalikan stok otomatis dan men-set status sale ke
     * `refunded` jika seluruh item sudah di-refund.
     */
    suspend fun refundSale(
        saleUuid: String,
        items: List<RefundItemInput>,
        reason: String? = null
    ): Resource<Refund>
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

    /** Gabung dua hold order — semua item dari [sourceUuid] pindah ke [targetUuid]. */
    suspend fun mergeSale(targetUuid: String, sourceUuid: String): Resource<Sale>

    /** Struk nomor antrian (font besar 58mm). */
    suspend fun getReceiptQueue(saleUuid: String): Resource<ByteArray>

    /**
     * Cetak ulang struk — server mencatat audit log.
     * @param printType "receipt" | "kitchen" | "queue"
     */
    suspend fun reprintSale(saleUuid: String, printType: String = "receipt", reason: String? = null): Resource<ReprintResult>

    /** Buka laci kas — kembalikan ESC/POS bytes untuk dikirim ke printer. */
    suspend fun openCashDrawer(): Resource<ByteArray>
}
