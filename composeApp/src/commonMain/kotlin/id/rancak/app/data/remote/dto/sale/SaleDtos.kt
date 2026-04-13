package id.rancak.app.data.remote.dto.sale

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Request DTOs ──

@Serializable
data class CreateSaleRequest(
    val items: List<SaleItemRequest>,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_amount") val paidAmount: Long? = null,
    val payments: List<SplitPaymentRequest>? = null,
    @SerialName("order_type") val orderType: String? = "dine_in",
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("table_uuid") val tableUuid: String? = null,
    val discount: Long? = null,
    val tax: Long? = null,
    val tip: Long? = null,
    @SerialName("admin_fee") val adminFee: Long? = null,
    @SerialName("delivery_fee") val deliveryFee: Long? = null,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("use_queue") val useQueue: Boolean? = null,
    val note: String? = null,
    val hold: Boolean? = false,
    @SerialName("device_created_at") val deviceCreatedAt: String? = null,
    @SerialName("device_id") val deviceId: String? = null
)

@Serializable
data class SaleItemRequest(
    @SerialName("product_uuid") val productUuid: String? = null,
    @SerialName("bundle_uuid") val bundleUuid: String? = null,
    val qty: Int,
    @SerialName("variant_uuid") val variantUuid: String? = null,
    val price: Long? = null,
    val discount: Long? = null,
    @SerialName("discount_pct") val discountPct: Int? = null,
    @SerialName("override_reason") val overrideReason: String? = null,
    val note: String? = null,
    val addons: List<AddonRequest>? = null
)

@Serializable
data class AddonRequest(
    val name: String,
    val price: Long,
    val qty: Int = 1
)

@Serializable
data class SplitPaymentRequest(
    val method: String,
    val amount: Long
)

// ── Response DTOs ──

@Serializable
data class SaleDto(
    val uuid: String,
    @SerialName("invoice_no") val invoiceNo: String? = null,
    @SerialName("order_type") val orderType: String? = null,
    @SerialName("queue_number") val queueNumber: Int? = null,
    val status: String,
    val subtotal: Long = 0,
    val discount: Long = 0,
    val surcharge: Long = 0,
    @SerialName("voucher_discount") val voucherDiscount: Long = 0,
    @SerialName("auto_discount") val autoDiscount: Long = 0,
    val tax: Long = 0,
    @SerialName("delivery_fee") val deliveryFee: Long = 0,
    val tip: Long = 0,
    @SerialName("admin_fee") val adminFee: Long = 0,
    val total: Long = 0,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_amount") val paidAmount: Long = 0,
    @SerialName("change_amount") val changeAmount: Long = 0,
    @SerialName("cashier_uuid") val cashierUuid: String? = null,
    @SerialName("table_uuid") val tableUuid: String? = null,
    val items: List<SaleItemDto> = emptyList(),
    val payments: List<SalePaymentDto> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SaleItemDto(
    val uuid: String,
    @SerialName("product_name") val productName: String,
    val qty: String,
    val price: Long,
    val subtotal: Long,
    @SerialName("variant_name") val variantName: String? = null,
    val note: String? = null
)

@Serializable
data class SalePaymentDto(
    val method: String,
    val amount: Long
)

@Serializable
data class BatchSalesRequest(
    val sales: List<BatchSaleItem>
)

@Serializable
data class BatchSaleItem(
    @SerialName("idempotency_key") val idempotencyKey: String,
    @SerialName("device_created_at") val deviceCreatedAt: String,
    @SerialName("device_id") val deviceId: String,
    val items: List<SaleItemRequest>,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("paid_amount") val paidAmount: Long
)

@Serializable
data class BatchSalesResponse(
    val total: Int,
    val created: Int,
    val duplicate: Int,
    val errors: Int,
    val results: List<BatchSaleResult>
)

@Serializable
data class BatchSaleResult(
    @SerialName("idempotency_key") val idempotencyKey: String,
    val status: String,
    @SerialName("invoice_no") val invoiceNo: String? = null,
    @SerialName("sale_uuid") val saleUuid: String? = null
)
