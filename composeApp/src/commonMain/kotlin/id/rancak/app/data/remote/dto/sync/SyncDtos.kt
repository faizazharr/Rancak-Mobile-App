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
    val amount: Long,
    @SerialName("is_percentage") val isPercentage: Boolean = false,
    @SerialName("max_amount") val maxAmount: Long? = null
)

@Serializable
data class TaxConfigDto(
    val uuid: String,
    val name: String,
    val rate: String,
    @SerialName("apply_to") val applyTo: String? = null
)

@Serializable
data class DiscountRuleDto(
    val uuid: String,
    val name: String,
    val type: String? = null,
    val value: Long? = null,
    @SerialName("min_purchase") val minPurchase: Long? = null,
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
    @SerialName("opening_cash") val openingCash: Long = 0,
    @SerialName("closing_cash") val closingCash: Long? = null,
    @SerialName("payment_summary") val paymentSummary: List<PaymentSummaryDto>? = null,
    @SerialName("total_sales") val totalSales: Long? = null,
    @SerialName("total_expenses") val totalExpenses: Long? = null
)

@Serializable
data class PaymentSummaryDto(
    val method: String,
    val total: Long
)
