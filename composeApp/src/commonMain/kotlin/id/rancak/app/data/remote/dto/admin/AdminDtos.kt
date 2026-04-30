package id.rancak.app.data.remote.dto.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Surcharges ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateSurchargeRequest(
    @SerialName("order_type") val orderType: String,
    val name: String,
    val amount: String,
    @SerialName("is_percentage") val isPercentage: Boolean = false,
    @SerialName("max_amount") val maxAmount: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class UpdateSurchargeRequest(
    val name: String? = null,
    val amount: String? = null,
    @SerialName("is_percentage") val isPercentage: Boolean? = null,
    @SerialName("max_amount") val maxAmount: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("sort_order") val sortOrder: Int? = null
)

// ── Vouchers ───────────────────────────────────────────────────────────────────

@Serializable
data class CreateVoucherRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    @SerialName("discount_type") val discountType: String,
    @SerialName("discount_value") val discountValue: String,
    @SerialName("max_discount") val maxDiscount: String? = null,
    @SerialName("min_purchase") val minPurchase: String = "0",
    @SerialName("usage_limit") val usageLimit: Int? = null,
    @SerialName("valid_from") val validFrom: String,
    @SerialName("valid_until") val validUntil: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class UpdateVoucherRequest(
    val name: String? = null,
    val description: String? = null,
    @SerialName("discount_value") val discountValue: String? = null,
    @SerialName("max_discount") val maxDiscount: String? = null,
    @SerialName("min_purchase") val minPurchase: String? = null,
    @SerialName("usage_limit") val usageLimit: Int? = null,
    @SerialName("valid_from") val validFrom: String? = null,
    @SerialName("valid_until") val validUntil: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

// ── Tax Configs ────────────────────────────────────────────────────────────────

@Serializable
data class CreateTaxConfigRequest(
    val name: String,
    val rate: String,
    @SerialName("apply_to") val applyTo: String = "after_discount",
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class UpdateTaxConfigRequest(
    val name: String? = null,
    val rate: String? = null,
    @SerialName("apply_to") val applyTo: String? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

// ── Tables ─────────────────────────────────────────────────────────────────────

@Serializable
data class CreateTableRequest(
    val name: String,
    val area: String? = null,
    val capacity: Int = 2,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class UpdateTableRequest(
    val name: String? = null,
    val area: String? = null,
    val capacity: Int? = null,
    val status: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("sort_order") val sortOrder: Int? = null
)

// ── Bundles ────────────────────────────────────────────────────────────────────

@Serializable
data class BundleItemRequest(
    @SerialName("product_uuid") val productUuid: String,
    val qty: String = "1"
)

@Serializable
data class CreateBundleRequest(
    val name: String,
    val price: String,
    val items: List<BundleItemRequest>,
    val description: String? = null,
    val sku: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class UpdateBundleRequest(
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val sku: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
    val items: List<BundleItemRequest>? = null
)

// ── Modifiers ──────────────────────────────────────────────────────────────────

@Serializable
data class CreateModifierRequest(
    val name: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class UpdateModifierRequest(
    val name: String? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

// ── Discount Rules ─────────────────────────────────────────────────────────────

@Serializable
data class CreateDiscountRuleRequest(
    val name: String,
    val description: String? = null,
    @SerialName("rule_type") val ruleType: String = "always",
    @SerialName("discount_type") val discountType: String = "pct",
    @SerialName("discount_value") val discountValue: Double,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("applicable_days") val applicableDays: List<Int>? = null,
    @SerialName("min_purchase_amount") val minPurchaseAmount: Double? = null,
    val priority: Int = 0,
    val stackable: Boolean = false,
    @SerialName("max_discount") val maxDiscount: Double? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class UpdateDiscountRuleRequest(
    val name: String? = null,
    val description: String? = null,
    @SerialName("rule_type") val ruleType: String? = null,
    @SerialName("discount_type") val discountType: String? = null,
    @SerialName("discount_value") val discountValue: Double? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("applicable_days") val applicableDays: List<Int>? = null,
    @SerialName("min_purchase_amount") val minPurchaseAmount: Double? = null,
    val priority: Int? = null,
    val stackable: Boolean? = null,
    @SerialName("max_discount") val maxDiscount: Double? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

// ── Variant Groups & Variants ──────────────────────────────────────────────────

@Serializable
data class CreateVariantGroupRequest(
    val name: String,
    @SerialName("is_required") val isRequired: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class UpdateVariantGroupRequest(
    val name: String? = null,
    @SerialName("is_required") val isRequired: Boolean? = null,
    @SerialName("sort_order") val sortOrder: Int? = null
)

@Serializable
data class CreateVariantRequest(
    val name: String,
    @SerialName("price_adjustment") val priceAdjustment: String = "0",
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class UpdateVariantRequest(
    val name: String? = null,
    @SerialName("price_adjustment") val priceAdjustment: String? = null,
    @SerialName("is_default") val isDefault: Boolean? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("sort_order") val sortOrder: Int? = null
)

// ── Product create / update ────────────────────────────────────────────────────

@Serializable
data class CreateProductRequest(
    val name: String,
    val price: Long,
    val description: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    @SerialName("category_uuid") val categoryUuid: String? = null,
    val unit: String? = null,
    val stock: Double = 0.0,
    @SerialName("has_expiry") val hasExpiry: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val price: Long? = null,
    val description: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    @SerialName("category_uuid") val categoryUuid: String? = null,
    val unit: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

// ── Category create / update ───────────────────────────────────────────────────

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val description: String? = null
)

// ── Product batch (restock) ───────────────────────────────────────────────────

@Serializable
data class CreateProductBatchRequest(
    val quantity: Double,
    @SerialName("expiry_date") val expiryDate: String? = null,
    @SerialName("cost_price") val costPrice: Long? = null,
    @SerialName("batch_number") val batchNumber: String? = null,
    val note: String? = null,
    @SerialName("received_at") val receivedAt: String? = null
)

// ── Stock adjustment ───────────────────────────────────────────────────────────

@Serializable
data class StockAdjustmentRequest(
    @SerialName("adjustment_type") val adjustmentType: String,
    val quantity: Double,
    val note: String? = null
)

@Serializable
data class StockAdjustmentResponseDto(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    @SerialName("stock_before") val stockBefore: Double,
    @SerialName("stock_after") val stockAfter: Double,
    @SerialName("adjustment_type") val adjustmentType: String,
    val quantity: Double
)

// ── Receipt settings update ────────────────────────────────────────────────────

@Serializable
data class UpdateReceiptSettingsRequest(
    @SerialName("logo_url") val logoUrl: String? = null,
    val email: String? = null,
    val website: String? = null,
    val npwp: String? = null,
    @SerialName("receipt_header") val receiptHeader: String? = null,
    @SerialName("receipt_footer") val receiptFooter: String? = null,
    @SerialName("receipt_footer2") val receiptFooter2: String? = null,
    @SerialName("logo_position") val logoPosition: String? = null,
    @SerialName("logo_size_pct") val logoSizePct: Int? = null,
    @SerialName("receipt_name_size") val receiptNameSize: String? = null,
    @SerialName("separator_style") val separatorStyle: String? = null,
    @SerialName("separator_count") val separatorCount: Int? = null,
    @SerialName("footer_position") val footerPosition: String? = null,
    @SerialName("receipt_instagram") val receiptInstagram: String? = null,
    @SerialName("receipt_facebook") val receiptFacebook: String? = null
)
