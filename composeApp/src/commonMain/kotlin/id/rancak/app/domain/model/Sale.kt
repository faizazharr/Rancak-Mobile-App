package id.rancak.app.domain.model

data class Sale(
    val uuid: String,
    val invoiceNo: String?,
    val orderType: OrderType,
    val queueNumber: Int?,
    val status: SaleStatus,
    val subtotal: Long,
    val discount: Long,
    val surcharge: Long,
    val tax: Long,
    val total: Long,
    val paymentMethod: PaymentMethod?,
    val paidAmount: Long,
    val changeAmount: Long,
    val items: List<SaleItem>,
    val createdAt: String?
)

data class SaleItem(
    val uuid: String,
    val productName: String,
    val qty: String,
    val price: Long,
    val subtotal: Long,
    val variantName: String?,
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
    SERVED("served"),
    CANCELLED("cancelled");

    companion object {
        fun from(value: String?): SaleStatus =
            entries.firstOrNull { it.value == value } ?: HELD
    }
}

/** Domain model untuk QRIS payment dari Xendit. */
data class QrPayment(
    val uuid: String,
    val saleUuid: String,
    val qrString: String,
    val amount: Long,
    val status: QrPaymentStatus,
    val usingWebhook: Boolean
)

enum class QrPaymentStatus(val value: String) {
    PENDING("pending"),
    SUCCEEDED("succeeded"),
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
    OTHER("other");

    companion object {
        fun from(value: String?): PaymentMethod? =
            entries.firstOrNull { it.value == value }
    }
}
