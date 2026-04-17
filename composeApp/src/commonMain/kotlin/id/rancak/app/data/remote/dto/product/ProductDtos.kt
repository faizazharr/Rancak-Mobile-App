package id.rancak.app.data.remote.dto.product

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val uuid: String,
    val sku: String? = null,
    val barcode: String? = null,
    val name: String,
    val description: String? = null,
    val category: CategoryRefDto? = null,
    val price: Long = 0,
    val stock: Double = 0.0,
    val unit: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("has_expiry") val hasExpiry: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CategoryRefDto(
    val uuid: String,
    val name: String
)

@Serializable
data class CategoryDto(
    val uuid: String,
    val name: String,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class VariantGroupDto(
    val uuid: String,
    val name: String,
    @SerialName("is_required") val isRequired: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    val variants: List<VariantDto> = emptyList()
)

@Serializable
data class VariantDto(
    val uuid: String,
    val name: String,
    @SerialName("price_adjustment") val priceAdjustment: String = "0",
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

/** Batch for products with has_expiry = true. */
@Serializable
data class ProductBatchDto(
    val uuid: String,
    @SerialName("quantity_initial") val quantityInitial: Double = 0.0,
    @SerialName("quantity_remaining") val quantityRemaining: Double = 0.0,
    @SerialName("quantity_used") val quantityUsed: Double = 0.0,
    @SerialName("cost_price") val costPrice: Long? = null,
    @SerialName("expiry_date") val expiryDate: String? = null,
    @SerialName("batch_number") val batchNumber: String? = null,
    val note: String? = null,
    @SerialName("received_at") val receivedAt: String? = null,
    @SerialName("is_exhausted") val isExhausted: Boolean = false,
    @SerialName("is_expired") val isExpired: Boolean = false
)

/** Favorite product shortcut for cashier. */
@Serializable
data class FavoriteProductDto(
    val uuid: String,
    val name: String,
    val sku: String? = null,
    val price: String = "0",
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val stock: String = "0",
    @SerialName("sold_count") val soldCount: Int = 0,
    @SerialName("is_low_stock") val isLowStock: Boolean = false
)

/** Product marked as unavailable (86) for today. */
@Serializable
data class Product86Dto(
    val uuid: String,
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    val sku: String? = null,
    val reason: String? = null,
    val date: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class StockAdjustmentResponseDto(
    @SerialName("product_uuid") val productUuid: String,
    @SerialName("product_name") val productName: String,
    @SerialName("stock_before") val stockBefore: Double = 0.0,
    @SerialName("stock_after") val stockAfter: Double = 0.0,
    @SerialName("adjustment_type") val adjustmentType: String,
    val quantity: Double = 0.0
)
