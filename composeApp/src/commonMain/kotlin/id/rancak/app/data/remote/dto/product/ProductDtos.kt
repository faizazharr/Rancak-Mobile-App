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
    val price: Long,
    val stock: String? = null,
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
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class VariantGroupDto(
    val uuid: String,
    val name: String,
    val variants: List<VariantDto> = emptyList()
)

@Serializable
data class VariantDto(
    val uuid: String,
    val name: String,
    @SerialName("price_adjustment") val priceAdjustment: Long = 0
)
