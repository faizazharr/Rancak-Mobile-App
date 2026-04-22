package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.FavoriteProductDto
import id.rancak.app.data.remote.dto.product.Product86Dto
import id.rancak.app.data.remote.dto.product.ProductBatchDto
import id.rancak.app.data.remote.dto.product.ProductDto
import id.rancak.app.data.remote.dto.product.VariantDto
import id.rancak.app.data.remote.dto.product.VariantGroupDto
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.FavoriteProduct
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Product86
import id.rancak.app.domain.model.ProductBatch
import id.rancak.app.domain.model.Variant
import id.rancak.app.domain.model.VariantGroup

/**
 * DTO → domain mappers for Product, Category, Variant, Batch, Favorite, Product86.
 */

fun ProductDto.toDomain() = Product(
    uuid = uuid,
    sku = sku,
    barcode = barcode,
    name = name,
    description = description,
    category = category?.let { Category(it.uuid, it.name, null) },
    price = price,
    stock = stock,
    unit = unit,
    imageUrl = imageUrl,
    isActive = isActive,
    hasExpiry = hasExpiry,
    updatedAt = updatedAt
)

fun CategoryDto.toDomain() = Category(
    uuid = uuid,
    name = name,
    description = description
)

fun VariantGroupDto.toDomain() = VariantGroup(
    uuid = uuid,
    name = name,
    isRequired = isRequired,
    sortOrder = sortOrder,
    variants = variants.map { it.toDomain() }
)

fun VariantDto.toDomain() = Variant(
    uuid = uuid,
    name = name,
    priceAdjustment = priceAdjustment.toLongOrNull() ?: 0L,
    isDefault = isDefault,
    isActive = isActive
)

fun ProductBatchDto.toDomain() = ProductBatch(
    uuid = uuid,
    quantityInitial = quantityInitial,
    quantityRemaining = quantityRemaining,
    quantityUsed = quantityUsed,
    costPrice = costPrice,
    expiryDate = expiryDate,
    batchNumber = batchNumber,
    note = note,
    receivedAt = receivedAt,
    isExhausted = isExhausted,
    isExpired = isExpired
)

fun FavoriteProductDto.toDomain() = FavoriteProduct(
    uuid = uuid,
    name = name,
    sku = sku,
    price = price.toLongOrNull() ?: 0L,
    categoryName = categoryName,
    imageUrl = imageUrl,
    stock = stock.toDoubleOrNull() ?: 0.0,
    soldCount = soldCount,
    isLowStock = isLowStock
)

fun Product86Dto.toDomain() = Product86(
    uuid = uuid,
    productUuid = productUuid,
    productName = productName,
    sku = sku,
    reason = reason,
    date = date ?: "",
    createdAt = createdAt
)
