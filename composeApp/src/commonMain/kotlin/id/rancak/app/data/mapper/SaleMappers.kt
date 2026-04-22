package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.sale.DeliveryResponseDto
import id.rancak.app.data.remote.dto.sale.QrPaymentDto
import id.rancak.app.data.remote.dto.sale.RefundItemResponseDto
import id.rancak.app.data.remote.dto.sale.RefundResponseDto
import id.rancak.app.data.remote.dto.sale.SaleDto
import id.rancak.app.data.remote.dto.sale.SaleItemAddonDto
import id.rancak.app.data.remote.dto.sale.SaleItemDto
import id.rancak.app.data.remote.dto.sale.SalePaymentDto
import id.rancak.app.domain.model.Delivery
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.QrPayment
import id.rancak.app.domain.model.QrPaymentStatus
import id.rancak.app.domain.model.Refund
import id.rancak.app.domain.model.RefundItem
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.model.SaleItemAddon
import id.rancak.app.domain.model.SalePayment
import id.rancak.app.domain.model.SaleStatus

/**
 * DTO → domain mappers for Sale, SaleItem, Payment, QR, Refund, Delivery.
 */

fun SaleDto.toDomain() = Sale(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = OrderType.from(orderType),
    queueNumber = queueNumber,
    status = SaleStatus.from(status),
    customerName = customerName,
    subtotal = subtotal,
    discount = discount,
    surcharge = surcharge,
    voucherDiscount = voucherDiscount,
    voucherCode = voucherCode,
    autoDiscount = autoDiscount,
    autoDiscountLabel = autoDiscountLabel,
    tax = tax,
    deliveryFee = deliveryFee,
    tip = tip,
    adminFee = adminFee,
    total = total,
    paymentMethod = PaymentMethod.from(paymentMethod),
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    items = items.map { it.toDomain() },
    payments = payments.map { it.toDomain() },
    delivery = delivery?.toDomain(),
    createdAt = createdAt,
    servedAt = servedAt
)

fun SaleItemDto.toDomain() = SaleItem(
    uuid = uuid,
    productUuid = productUuid,
    productName = productName,
    qty = qty,
    price = price,
    discount = discount,
    subtotal = subtotal,
    variantName = variantName,
    note = note,
    addons = addons.map { it.toDomain() }
)

fun SaleItemAddonDto.toDomain() = SaleItemAddon(
    name = name,
    price = price,
    qty = qty,
    subtotal = subtotal
)

fun SalePaymentDto.toDomain() = SalePayment(
    uuid = uuid,
    method = method,
    amount = amount,
    note = note
)

fun DeliveryResponseDto.toDomain() = Delivery(
    uuid = uuid,
    courierName = courierName,
    recipientName = recipientName,
    address = address,
    lat = lat,
    lng = lng,
    note = note
)

fun QrPaymentDto.toDomain() = QrPayment(
    uuid         = uuid,
    saleUuid     = saleUuid,
    qrString     = qrString,
    amount       = amount,
    status       = QrPaymentStatus.from(status),
    expiresAt    = expiresAt,
    usingWebhook = usingWebhook
)

fun RefundResponseDto.toDomain() = Refund(
    uuid         = uuid,
    saleUuid     = saleUuid,
    refundAmount = refundAmount,
    reason       = reason,
    items        = items.map { it.toDomain() },
    createdAt    = createdAt
)

fun RefundItemResponseDto.toDomain() = RefundItem(
    saleItemUuid = saleItemUuid,
    productName  = productName,
    qty          = qty,
    refundAmount = refundAmount
)
