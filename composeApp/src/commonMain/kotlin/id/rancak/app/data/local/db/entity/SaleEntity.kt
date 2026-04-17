package id.rancak.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val uuid: String,
    val invoiceNo: String?,
    val orderType: String,
    val queueNumber: Int?,
    val status: String,
    val customerName: String?,
    val subtotal: Long,
    val discount: Long,
    val surcharge: Long,
    val tax: Long,
    val total: Long,
    val paymentMethod: String?,
    val paidAmount: Long,
    val changeAmount: Long,
    val createdAt: String?,
    val cachedAt: Long = 0L
)

fun SaleEntity.toDomain(items: List<SaleItemEntity>) = Sale(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = OrderType.from(orderType),
    queueNumber = queueNumber,
    status = SaleStatus.from(status),
    customerName = customerName,
    subtotal = subtotal,
    discount = discount,
    surcharge = surcharge,
    tax = tax,
    total = total,
    paymentMethod = PaymentMethod.from(paymentMethod),
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    items = items.map { it.toDomain() },
    createdAt = createdAt
)

fun Sale.toEntity(cachedAt: Long = 0L) = SaleEntity(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = orderType.value,
    queueNumber = queueNumber,
    status = status.value,
    customerName = customerName,
    subtotal = subtotal,
    discount = discount,
    surcharge = surcharge,
    tax = tax,
    total = total,
    paymentMethod = paymentMethod?.value,
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    createdAt = createdAt,
    cachedAt = cachedAt
)
