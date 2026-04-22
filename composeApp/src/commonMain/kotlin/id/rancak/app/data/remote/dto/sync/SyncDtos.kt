package id.rancak.app.data.remote.dto.sync

import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.ProductDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogSyncDto(
    @SerialName("server_time") val serverTime: String,
    @SerialName("is_delta") val isDelta: Boolean = false,
    val categories: List<CategoryDto> = emptyList(),
    val products: List<ProductDto> = emptyList(),
    val surcharges: List<SurchargeDto> = emptyList(),
    @SerialName("tax_configs") val taxConfigs: List<TaxConfigDto> = emptyList(),
    @SerialName("discount_rules") val discountRules: List<DiscountRuleDto> = emptyList(),
    val tables: List<TableDto> = emptyList(),
    @SerialName("current_shift") val currentShift: ShiftDto? = null
)

@Serializable
data class SyncStatusDto(
    @SerialName("server_time") val serverTime: String,
    @SerialName("products_updated_at") val productsUpdatedAt: String? = null,
    @SerialName("categories_updated_at") val categoriesUpdatedAt: String? = null,
    @SerialName("has_open_shift") val hasOpenShift: Boolean = false
)

@Serializable
data class SurchargeDto(
    val uuid: String,
    @SerialName("order_type") val orderType: String? = null,
    val name: String,
    val amount: String = "0",
    @SerialName("is_percentage") val isPercentage: Boolean = false,
    @SerialName("max_amount") val maxAmount: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class TaxConfigDto(
    val uuid: String,
    val name: String,
    val rate: String,
    @SerialName("apply_to") val applyTo: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class DiscountRuleDto(
    val uuid: String,
    val name: String,
    val description: String? = null,
    @SerialName("rule_type") val ruleType: String? = null,
    @SerialName("discount_type") val discountType: String? = null,
    @SerialName("discount_value") val discountValue: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("applicable_days") val applicableDays: List<Int>? = null,
    @SerialName("min_purchase_amount") val minPurchaseAmount: String? = null,
    val priority: Int = 0,
    val stackable: Boolean = false,
    @SerialName("max_discount") val maxDiscount: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class TableDto(
    val uuid: String,
    val name: String,
    val area: String? = null,
    val capacity: Int? = null,
    val status: String = "available",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("active_sale_uuid") val activeSaleUuid: String? = null
)

@Serializable
data class ShiftDto(
    val uuid: String,
    @SerialName("opened_at") val openedAt: String? = null,
    @SerialName("closed_at") val closedAt: String? = null,
    val status: String = "open",
    @SerialName("opening_cash") val openingCash: String = "0",
    @SerialName("closing_cash") val closingCash: String? = null,
    @SerialName("expected_cash") val expectedCash: String? = null,
    @SerialName("cash_difference") val cashDifference: String? = null,
    @SerialName("cashier_name") val cashierName: String? = null,
    @SerialName("payment_summary") val paymentSummary: List<PaymentSummaryDto>? = null,
    @SerialName("total_sales") val totalSales: Long? = null,
    @SerialName("total_transactions") val totalTransactions: Int? = null,
    @SerialName("total_expenses") val totalExpenses: Long? = null,
    @SerialName("total_cash_in") val totalCashIn: Long? = null
)

@Serializable
data class PaymentSummaryDto(
    @SerialName("payment_method") val method: String,
    @SerialName("transaction_count") val transactionCount: Int = 0,
    val total: Long = 0
)
