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
    /** Jumlah tamu — digunakan sebagai label & info kapasitas meja */
    val pax: Int? = null,
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
    @SerialName("tenant_uuid") val tenantUuid: String? = null,
    @SerialName("cashier_uuid") val cashierUuid: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("order_type") val orderType: String? = null,
    @SerialName("queue_number") val queueNumber: Int? = null,
    val status: String,
    val subtotal: Long = 0,
    val discount: Long = 0,
    val surcharge: Long = 0,
    @SerialName("voucher_discount") val voucherDiscount: Long = 0,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("auto_discount") val autoDiscount: Long = 0,
    @SerialName("auto_discount_label") val autoDiscountLabel: String? = null,
    val tax: Long = 0,
    @SerialName("delivery_fee") val deliveryFee: Long = 0,
    val tip: Long = 0,
    @SerialName("admin_fee") val adminFee: Long = 0,
    val total: Long = 0,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_amount") val paidAmount: Long = 0,
    @SerialName("change_amount") val changeAmount: Long = 0,
    @SerialName("table_uuid") val tableUuid: String? = null,
    val items: List<SaleItemDto> = emptyList(),
    val payments: List<SalePaymentDto> = emptyList(),
    val delivery: DeliveryResponseDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("served_at") val servedAt: String? = null
)

@Serializable
data class SaleItemDto(
    val uuid: String,
    @SerialName("product_uuid") val productUuid: String? = null,
    @SerialName("product_name") val productName: String,
    val sku: String? = null,
    val qty: String,
    val price: Long = 0,
    val discount: Long = 0,
    val subtotal: Long = 0,
    @SerialName("variant_name") val variantName: String? = null,
    val note: String? = null,
    val addons: List<SaleItemAddonDto> = emptyList()
)

@Serializable
data class SaleItemAddonDto(
    val name: String,
    val price: Long = 0,
    val qty: Double = 1.0,
    val subtotal: Long = 0
)

@Serializable
data class SalePaymentDto(
    val uuid: String? = null,
    val method: String,
    val amount: Long = 0,
    val note: String? = null
)

@Serializable
data class DeliveryResponseDto(
    val uuid: String? = null,
    @SerialName("courier_name") val courierName: String? = null,
    @SerialName("recipient_name") val recipientName: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val note: String? = null
)

@Serializable
data class DeliveryInputDto(
    @SerialName("courier_name") val courierName: String,
    @SerialName("recipient_name") val recipientName: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val note: String? = null
)

/** Pay a held order. */
@Serializable
data class PayHeldOrderRequest(
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_amount") val paidAmount: Long? = null,
    val payments: List<SplitPaymentRequest>? = null
)

/** Refund request (per-item). */
// ── Split Bill ──

@Serializable
data class SplitBillRequest(
    @SerialName("item_ids") val itemIds: List<String>
)

@Serializable
data class SplitBillResponseDto(
    val original: SaleDto,
    @SerialName("new_sale") val newSale: SaleDto
)

@Serializable
data class RefundItemRequest(
    @SerialName("sale_item_uuid") val saleItemUuid: String,
    val qty: Int
)

@Serializable
data class RefundRequest(
    val items: List<RefundItemRequest>,
    val reason: String? = null
)

@Serializable
data class RefundItemResponseDto(
    @SerialName("sale_item_uuid") val saleItemUuid: String,
    @SerialName("product_name") val productName: String,
    val qty: Double = 0.0,
    @SerialName("refund_amount") val refundAmount: Long = 0
)

@Serializable
data class RefundResponseDto(
    val uuid: String,
    @SerialName("sale_uuid") val saleUuid: String,
    @SerialName("refund_amount") val refundAmount: Long = 0,
    val reason: String? = null,
    val items: List<RefundItemResponseDto> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
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
    @SerialName("paid_amount") val paidAmount: Long,
    @SerialName("order_type") val orderType: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val pax: Int? = null,
    val note: String? = null,
    val hold: Boolean? = null,
    val discount: Long? = null,
    val tax: Long? = null,
    @SerialName("admin_fee") val adminFee: Long? = null,
    @SerialName("delivery_fee") val deliveryFee: Long? = null,
    val tip: Long? = null,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("table_uuid") val tableUuid: String? = null
)

// ── QRIS / Xendit ──

@Serializable
data class QrPaymentDto(
    val uuid: String,
    @SerialName("sale_uuid")    val saleUuid: String,
    @SerialName("xendit_qr_id") val xenditQrId: String? = null,
    @SerialName("qr_string")    val qrString: String,
    val amount: Long,
    val status: String,          // "pending" | "succeeded" | "expired"
    @SerialName("expires_at")   val expiresAt: String? = null,
    @SerialName("using_webhook") val usingWebhook: Boolean = false
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
