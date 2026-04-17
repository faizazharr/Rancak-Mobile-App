package id.rancak.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val uuid: String,
    val sku: String?,
    val barcode: String?,
    val name: String,
    val description: String?,
    val category: Category?,
    val price: Long,
    val stock: Double,
    val unit: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val hasExpiry: Boolean = false,
    val updatedAt: String?
)

@Serializable
data class Category(
    val uuid: String,
    val name: String,
    val description: String?
)

@Serializable
data class VariantGroup(
    val uuid: String,
    val name: String,
    val isRequired: Boolean = false,
    val sortOrder: Int = 0,
    val variants: List<Variant>
)

@Serializable
data class Variant(
    val uuid: String,
    val name: String,
    val priceAdjustment: Long,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

data class ProductBatch(
    val uuid: String,
    val quantityInitial: Double,
    val quantityRemaining: Double,
    val quantityUsed: Double,
    val costPrice: Long?,
    val expiryDate: String?,
    val batchNumber: String?,
    val note: String?,
    val receivedAt: String?,
    val isExhausted: Boolean,
    val isExpired: Boolean
)
