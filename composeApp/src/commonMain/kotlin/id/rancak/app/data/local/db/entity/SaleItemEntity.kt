package id.rancak.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.rancak.app.domain.model.SaleItem

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey val uuid: String,
    val saleUuid: String,
    val productUuid: String?,
    val productName: String,
    val qty: String,
    val price: Long,
    val subtotal: Long,
    val variantName: String?,
    val note: String?,
    val discount: Long = 0
)

fun SaleItemEntity.toDomain() = SaleItem(
    uuid = uuid,
    productUuid = productUuid,
    productName = productName,
    qty = qty,
    price = price,
    discount = discount,
    subtotal = subtotal,
    variantName = variantName,
    note = note
)

fun SaleItem.toEntity(saleUuid: String) = SaleItemEntity(
    uuid = uuid,
    saleUuid = saleUuid,
    productUuid = productUuid,
    productName = productName,
    qty = qty,
    price = price,
    subtotal = subtotal,
    variantName = variantName,
    note = note,
    discount = discount
)
