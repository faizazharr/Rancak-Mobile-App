package id.rancak.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
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

@Immutable
data class Category(
    val uuid: String,
    val name: String,
    val description: String?
)

@Immutable
data class VariantGroup(
    val uuid: String,
    val name: String,
    val isRequired: Boolean = false,
    val sortOrder: Int = 0,
    val variants: List<Variant>
)

@Immutable
data class Variant(
    val uuid: String,
    val name: String,
    val priceAdjustment: Long,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

@Immutable
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

@Immutable
data class FavoriteProduct(
    val uuid: String,
    val name: String,
    val sku: String?,
    val price: Long,
    val categoryName: String?,
    val imageUrl: String?,
    val stock: Double,
    val soldCount: Int,
    val isLowStock: Boolean
)

@Immutable
data class Product86(
    val uuid: String,
    val productUuid: String,
    val productName: String,
    val sku: String?,
    val reason: String?,
    val date: String,
    val createdAt: String?
)

@Immutable
data class StockReport(
    val productUuid: String,
    val sku: String?,
    val name: String,
    val stock: Double,
    val stockAlertThreshold: Double?
)

@Immutable
data class LowStock(
    val productUuid: String,
    val productName: String,
    val sku: String?,
    val currentStock: Double,
    val threshold: Double
)

@Immutable
data class StockAlert(
    val productUuid: String,
    val productName: String,
    val sku: String?,
    val alertType: String,
    val currentStock: Double,
    val threshold: Double?
)

@Immutable
data class ExpiringBatch(
    val batchUuid: String,
    val productUuid: String,
    val productName: String,
    val batchNumber: String?,
    val expiryDate: String,
    val quantityRemaining: Double,
    val daysUntilExpiry: Int
)
