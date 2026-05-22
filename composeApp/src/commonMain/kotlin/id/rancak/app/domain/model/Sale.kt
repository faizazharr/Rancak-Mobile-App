package id.rancak.app.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class Sale(
    val uuid: String,
    val invoiceNo: String?,
    val orderType: OrderType,
    val queueNumber: Int?,
    val status: SaleStatus,
    val customerName: String?,
    val subtotal: Long,
    val discount: Long,
    val surcharge: Long,
    val voucherDiscount: Long = 0,
    val voucherCode: String? = null,
    val autoDiscount: Long = 0,
    val autoDiscountLabel: String? = null,
    val tax: Long,
    val deliveryFee: Long = 0,
    val tip: Long = 0,
    val adminFee: Long = 0,
    val total: Long,
    val paymentMethod: PaymentMethod?,
    val paidAmount: Long,
    val changeAmount: Long,
    val items: ImmutableList<SaleItem>,
    val payments: ImmutableList<SalePayment> = persistentListOf(),
    val delivery: Delivery? = null,
    val createdAt: String?,
    val servedAt: String? = null
)

@Immutable
data class SaleItem(
    val uuid: String,
    val productUuid: String?,
    val productName: String,
    val qty: String,
    val price: Long,
    val discount: Long = 0,
    val subtotal: Long,
    val variantName: String?,
    val note: String?,
    val addons: ImmutableList<SaleItemAddon> = persistentListOf()
)

@Immutable
data class SaleItemAddon(
    val name: String,
    val price: Long,
    val qty: Double,
    val subtotal: Long
)

@Immutable
data class SalePayment(
    val uuid: String?,
    val method: String,
    val amount: Long,
    val note: String?
)

@Immutable
data class Delivery(
    val uuid: String?,
    val courierName: String?,
    val recipientName: String?,
    val address: String?,
    val lat: Double?,
    val lng: Double?,
    val note: String?
)

enum class OrderType(val value: String) {
    DINE_IN("dine_in"),
    TAKEAWAY("takeaway"),
    DELIVERY("delivery");

    companion object {
        fun from(value: String?): OrderType =
            entries.firstOrNull { it.value == value } ?: DINE_IN
    }
}

enum class SaleStatus(val value: String) {
    HELD("held"),
    PAID("paid"),
    VOID("void"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    companion object {
        fun from(value: String?): SaleStatus =
            entries.firstOrNull { it.value == value } ?: HELD
    }
}

/** Domain model untuk QRIS payment dari Xendit. */
@Immutable
data class QrPayment(
    val uuid: String,
    val saleUuid: String,
    val qrString: String,
    val amount: Long,
    val status: QrPaymentStatus,
    val expiresAt: String?,
    val usingWebhook: Boolean
)

enum class QrPaymentStatus(val value: String) {
    PENDING("pending"),
    SUCCEEDED("succeeded"),
    FAILED("failed"),
    EXPIRED("expired");

    companion object {
        fun from(value: String?): QrPaymentStatus =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}

enum class PaymentMethod(val value: String) {
    CASH("cash"),
    CARD("card"),
    QRIS("qris"),
    TRANSFER("transfer"),
    OTHER("other"),
    MIXED("mixed");

    companion object {
        fun from(value: String?): PaymentMethod? =
            entries.firstOrNull { it.value == value }
    }
}

@Immutable
data class Refund(
    val uuid: String,
    val saleUuid: String,
    val refundAmount: Long,
    val reason: String?,
    val items: ImmutableList<RefundItem>,
    val createdAt: String?
)

@Immutable
data class RefundItem(
    val saleItemUuid: String,
    val productName: String,
    val qty: Double,
    val refundAmount: Long
)

/** Input untuk request refund: refund [qty] unit dari [saleItemUuid]. */
@Immutable
data class RefundItemInput(
    val saleItemUuid: String,
    val qty: Int
)

/** Item yang akan ditambahkan ke keranjang belanja. */
@Immutable
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
@Immutable
data class SplitPaymentEntry(
    val method: PaymentMethod,
    val amount: Long,
    val note: String? = null
)

/** Hasil split bill — transaksi asal + transaksi baru. */
@Immutable
data class SplitBillResult(
    val original: Sale,
    val newSale: Sale
)

/** Hasil reprint — tipe cetak + sale lengkap. */
@Immutable
data class ReprintResult(
    val printType: String,
    val sale: Sale
)
