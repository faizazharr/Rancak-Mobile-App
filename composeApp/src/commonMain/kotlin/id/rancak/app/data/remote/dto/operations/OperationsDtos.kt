package id.rancak.app.data.remote.dto.operations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Cash In ──

@Serializable
data class CashInDto(
    val uuid: String,
    val amount: Long = 0,
    val source: String? = null,
    val description: String? = null,
    val note: String? = null,
    @SerialName("cashier_uuid") val cashierUuid: String? = null,
    @SerialName("cashier_name") val cashierName: String? = null,
    @SerialName("shift_uuid") val shiftUuid: String? = null,
    @SerialName("cash_in_date") val cashInDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// ── Expense ──

@Serializable
data class ExpenseDto(
    val uuid: String,
    val amount: Long = 0,
    val description: String? = null,
    val note: String? = null,
    @SerialName("category_uuid") val categoryUuid: String? = null,
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("cashier_uuid") val cashierUuid: String? = null,
    @SerialName("cashier_name") val cashierName: String? = null,
    @SerialName("expense_date") val expenseDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// ── Bundle ──

@Serializable
data class BundleDto(
    val uuid: String,
    val name: String,
    val price: Long = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    val items: List<BundleItemDto> = emptyList()
)

@Serializable
data class BundleItemDto(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String? = null,
    val qty: Int = 1
)

// ── Modifier ──

@Serializable
data class ModifierDto(
    val uuid: String,
    val name: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("product_uuid") val productUuid: String? = null
)

// ── Voucher ──

@Serializable
data class VoucherDto(
    val uuid: String,
    val code: String,
    val name: String,
    val description: String? = null,
    @SerialName("discount_type") val discountType: String,
    @SerialName("discount_value") val discountValue: String = "0",
    @SerialName("max_discount") val maxDiscount: String? = null,
    @SerialName("min_purchase") val minPurchase: String = "0",
    @SerialName("usage_limit") val usageLimit: Int? = null,
    @SerialName("usage_count") val usageCount: Int = 0,
    @SerialName("valid_from") val validFrom: String? = null,
    @SerialName("valid_until") val validUntil: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class VoucherValidationDto(
    val voucher: VoucherDto,
    @SerialName("discount_applied") val discountApplied: String = "0"
)

// ── Discount Preview ──

@Serializable
data class DiscountPreviewDto(
    @SerialName("applied_rules") val appliedRules: List<AppliedRuleDto> = emptyList(),
    @SerialName("total_discount") val totalDiscount: Long = 0,
    @SerialName("final_total") val finalTotal: Long = 0
)

@Serializable
data class AppliedRuleDto(
    val uuid: String,
    val name: String,
    @SerialName("rule_type") val ruleType: String? = null,
    val discount: Long = 0
)

// ── Order Board ──

@Serializable
data class OrderBoardOrderDto(
    val uuid: String,
    @SerialName("invoice_no") val invoiceNo: String? = null,
    @SerialName("queue_number") val queueNumber: Int? = null,
    @SerialName("order_type") val orderType: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("served_at") val servedAt: String? = null,
    val items: List<OrderBoardItemDto> = emptyList()
)

@Serializable
data class OrderBoardItemDto(
    @SerialName("product_name") val productName: String,
    val qty: Int = 1,
    val note: String? = null
)

// ── Reports ──

@Serializable
data class MySalesReportDto(
    @SerialName("total_sales") val totalSales: String = "0",
    @SerialName("total_transactions") val totalTransactions: Int = 0,
    @SerialName("cash_total") val cashTotal: String = "0"
)

@Serializable
data class StockReportDto(
    @SerialName("product_uuid") val productUuid: String,
    val sku: String? = null,
    val name: String,
    val stock: Double = 0.0,
    @SerialName("stock_alert_threshold") val stockAlertThreshold: Double? = null
)

@Serializable
data class LowStockDto(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    val sku: String? = null,
    @SerialName("current_stock") val currentStock: Double = 0.0,
    val threshold: Double = 0.0
)

@Serializable
data class StockAlertDto(
    val uuid: String,
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    val sku: String? = null,
    @SerialName("alert_type") val alertType: String,
    val stock: Double = 0.0,
    val threshold: Double = 0.0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ExpiringBatchDto(
    @SerialName("batch_uuid") val batchUuid: String,
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    @SerialName("batch_number") val batchNumber: String? = null,
    @SerialName("expiry_date") val expiryDate: String? = null,
    @SerialName("quantity_remaining") val quantityRemaining: Double = 0.0
)

@Serializable
data class DailyCategoryReportDto(
    @SerialName("category_name") val categoryName: String,
    @SerialName("total_qty") val totalQty: Double = 0.0,
    @SerialName("total_revenue") val totalRevenue: Long = 0
)

@Serializable
data class PaymentMethodReportDto(
    @SerialName("payment_method") val method: String,
    @SerialName("transaction_count") val count: Int = 0,
    val total: Long = 0
)

// ── Shift Summary ──

@Serializable
data class ShiftSummaryDto(
    val uuid: String,
    @SerialName("opened_at") val openedAt: String? = null,
    @SerialName("closed_at") val closedAt: String? = null,
    val status: String,
    @SerialName("opening_cash") val openingCash: String = "0",
    @SerialName("closing_cash") val closingCash: String? = null,
    @SerialName("expected_cash") val expectedCash: String? = null,
    @SerialName("cash_difference") val cashDifference: String? = null,
    @SerialName("cashier_name") val cashierName: String? = null,
    @SerialName("total_sales") val totalSales: Long = 0,
    @SerialName("total_transactions") val totalTransactions: Int = 0,
    @SerialName("void_transactions") val voidTransactions: Int = 0,
    @SerialName("gross_total") val grossTotal: Long = 0,
    @SerialName("total_discount") val totalDiscount: Long = 0,
    @SerialName("total_tax") val totalTax: Long = 0,
    @SerialName("net_total") val netTotal: Long = 0,
    @SerialName("total_expenses") val totalExpenses: Long = 0,
    @SerialName("total_cash_in") val totalCashIn: Long = 0,
    @SerialName("payment_breakdown") val paymentBreakdown: List<PaymentMethodReportDto> = emptyList(),
    // Backward compat alias — older endpoints may still return `payment_summary`.
    @SerialName("payment_summary") val paymentSummary: List<PaymentMethodReportDto> = emptyList()
) {
    /** Unified accessor: prefer `payment_breakdown` (new schema) else fallback. */
    val payments: List<PaymentMethodReportDto>
        get() = if (paymentBreakdown.isNotEmpty()) paymentBreakdown else paymentSummary
}

// ── Receipt ──

@Serializable
data class ReceiptDto(
    @SerialName("tenant_name") val tenantName: String? = null,
    @SerialName("tenant_address") val tenantAddress: String? = null,
    @SerialName("tenant_phone") val tenantPhone: String? = null,
    @SerialName("invoice_no") val invoiceNo: String? = null,
    @SerialName("cashier_name") val cashierName: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("queue_number") val queueNumber: Int? = null,
    @SerialName("order_type") val orderType: String? = null,
    val items: List<ReceiptItemDto> = emptyList(),
    val subtotal: Long = 0,
    val discount: Long = 0,
    val surcharge: Long = 0,
    val tax: Long = 0,
    val tip: Long = 0,
    @SerialName("admin_fee") val adminFee: Long = 0,
    @SerialName("delivery_fee") val deliveryFee: Long = 0,
    val total: Long = 0,
    @SerialName("paid_amount") val paidAmount: Long = 0,
    @SerialName("change_amount") val changeAmount: Long = 0,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val settings: id.rancak.app.data.remote.dto.auth.ReceiptSettingsDto? = null
)

@Serializable
data class ReceiptItemDto(
    val name: String,
    val qty: String,
    val price: Long = 0,
    val subtotal: Long = 0,
    @SerialName("variant_name") val variantName: String? = null,
    val note: String? = null
)
