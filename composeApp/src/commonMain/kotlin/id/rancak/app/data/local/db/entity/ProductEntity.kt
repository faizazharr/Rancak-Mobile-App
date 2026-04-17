package id.rancak.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val uuid: String,
    val sku: String?,
    val barcode: String?,
    val name: String,
    val description: String?,
    val categoryUuid: String?,
    val categoryName: String?,
    val price: Long,
    val stock: Double,
    val unit: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val updatedAt: String?
)

fun ProductEntity.toDomain() = Product(
    uuid = uuid,
    sku = sku,
    barcode = barcode,
    name = name,
    description = description,
    category = categoryUuid?.let { Category(uuid = it, name = categoryName ?: "", description = null) },
    price = price,
    stock = stock,
    unit = unit,
    imageUrl = imageUrl,
    isActive = isActive,
    updatedAt = updatedAt
)

fun Product.toEntity() = ProductEntity(
    uuid = uuid,
    sku = sku,
    barcode = barcode,
    name = name,
    description = description,
    categoryUuid = category?.uuid,
    categoryName = category?.name,
    price = price,
    stock = stock,
    unit = unit,
    imageUrl = imageUrl,
    isActive = isActive,
    updatedAt = updatedAt
)
