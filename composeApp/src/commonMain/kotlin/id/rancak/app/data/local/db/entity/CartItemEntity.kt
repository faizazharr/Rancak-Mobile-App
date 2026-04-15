package id.rancak.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.rancak.app.domain.repository.CartItem

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,          // "$productUuid:${variantUuid ?: "_"}"
    val productUuid: String,
    val productName: String,
    val qty: Int,
    val price: Long,
    val variantUuid: String?,
    val variantName: String?,
    val note: String?,
    val imageUrl: String?
)

fun CartItemEntity.toDomain() = CartItem(
    productUuid = productUuid,
    productName = productName,
    qty = qty,
    price = price,
    variantUuid = variantUuid,
    variantName = variantName,
    note = note,
    imageUrl = imageUrl
)

fun CartItem.toEntity() = CartItemEntity(
    id = "$productUuid:${variantUuid ?: "_"}",
    productUuid = productUuid,
    productName = productName,
    qty = qty,
    price = price,
    variantUuid = variantUuid,
    variantName = variantName,
    note = note,
    imageUrl = imageUrl
)
