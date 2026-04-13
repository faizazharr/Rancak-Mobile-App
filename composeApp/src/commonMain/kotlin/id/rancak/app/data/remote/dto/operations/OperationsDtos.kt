package id.rancak.app.data.remote.dto.operations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Cash In ──

@Serializable
data class CashInDto(
    val uuid: String,
    val amount: Long,
    val source: String? = null,
    val description: String? = null,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// ── Expense ──

@Serializable
data class ExpenseDto(
    val uuid: String,
    val amount: Long,
    val description: String? = null,
    val note: String? = null,
    @SerialName("category_uuid") val categoryUuid: String? = null,
    @SerialName("expense_date") val expenseDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// ── Bundle ──

@Serializable
data class BundleDto(
    val uuid: String,
    val name: String,
    val price: Long,
    @SerialName("is_active") val isActive: Boolean = true,
    val items: List<BundleItemDto> = emptyList()
)

@Serializable
data class BundleItemDto(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String? = null,
    val qty: Int
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
data class VoucherValidationDto(
    val code: String,
    @SerialName("discount_applied") val discountApplied: Long,
    val valid: Boolean = true,
    val message: String? = null
)

// ── Discount Preview ──

@Serializable
data class DiscountPreviewDto(
    @SerialName("total_discount") val totalDiscount: Long = 0,
    val rules: List<AppliedRuleDto> = emptyList()
)

@Serializable
data class AppliedRuleDto(
    val name: String,
    val discount: Long
)

// ── Reports ──

@Serializable
data class ReportSummaryDto(
    @SerialName("total_sales") val totalSales: Long = 0,
    @SerialName("total_transactions") val totalTransactions: Int = 0,
    @SerialName("total_discount") val totalDiscount: Long = 0,
    @SerialName("total_tax") val totalTax: Long = 0,
    @SerialName("total_net") val totalNet: Long = 0,
    @SerialName("payment_methods") val paymentMethods: List<PaymentMethodReportDto> = emptyList()
)

@Serializable
data class ProductReportDto(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    @SerialName("qty_sold") val qtySold: Int = 0,
    @SerialName("total_revenue") val totalRevenue: Long = 0
)

@Serializable
data class PaymentMethodReportDto(
    val method: String,
    val total: Long = 0,
    val count: Int = 0
)

// ── Shift Summary ──

@Serializable
data class ShiftSummaryDto(
    val uuid: String,
    @SerialName("opened_at") val openedAt: String? = null,
    @SerialName("closed_at") val closedAt: String? = null,
    val status: String,
    @SerialName("opening_cash") val openingCash: Long = 0,
    @SerialName("payment_summary") val paymentSummary: List<PaymentMethodReportDto> = emptyList(),
    @SerialName("total_sales") val totalSales: Long = 0,
    @SerialName("total_expenses") val totalExpenses: Long = 0
)

// ── Receipt ──

@Serializable
data class ReceiptDto(
    @SerialName("tenant_name") val tenantName: String? = null,
    @SerialName("invoice_no") val invoiceNo: String? = null,
    @SerialName("cashier_name") val cashierName: String? = null,
    val items: List<ReceiptItemDto> = emptyList(),
    val subtotal: Long = 0,
    val discount: Long = 0,
    val tax: Long = 0,
    val total: Long = 0,
    @SerialName("paid_amount") val paidAmount: Long = 0,
    @SerialName("change_amount") val changeAmount: Long = 0,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ReceiptItemDto(
    val name: String,
    val qty: String,
    val price: Long,
    val subtotal: Long
)

// ── Session ──

@Serializable
data class SessionDto(
    val uuid: String,
    @SerialName("device_name") val deviceName: String? = null,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_current") val isCurrent: Boolean = false
)
