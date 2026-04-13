package id.rancak.app.domain.model

data class Product(
    val uuid: String,
    val sku: String?,
    val barcode: String?,
    val name: String,
    val description: String?,
    val category: Category?,
    val price: Long,
    val stock: String?,
    val unit: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val updatedAt: String?
)

data class Category(
    val uuid: String,
    val name: String,
    val description: String?
)

data class VariantGroup(
    val uuid: String,
    val name: String,
    val variants: List<Variant>
)

data class Variant(
    val uuid: String,
    val name: String,
    val priceAdjustment: Long
)
