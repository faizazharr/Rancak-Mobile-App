package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.operations.BundleDto
import id.rancak.app.data.remote.dto.operations.ModifierDto
import id.rancak.app.data.remote.dto.operations.ReceiptDto
import id.rancak.app.data.remote.dto.operations.ReceiptItemDto
import id.rancak.app.domain.model.Bundle
import id.rancak.app.domain.model.BundleItem
import id.rancak.app.domain.model.Modifier
import id.rancak.app.domain.model.Receipt
import id.rancak.app.domain.model.ReceiptItemDomain

/**
 * DTO → domain mappers for Receipt printing, Bundles, and Modifiers.
 */

fun ReceiptDto.toDomain() = Receipt(
    invoiceNo = invoiceNo,
    tenantName = tenantName,
    tenantAddress = tenantAddress,
    tenantPhone = tenantPhone,
    customerName = customerName,
    queueNumber = queueNumber,
    orderType = orderType,
    cashierName = cashierName,
    createdAt = createdAt,
    items = items.map { it.toDomain() },
    subtotal = subtotal,
    discount = discount,
    surcharge = surcharge,
    tax = tax,
    deliveryFee = deliveryFee,
    tip = tip,
    adminFee = adminFee,
    total = total,
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    paymentMethod = paymentMethod
)

fun ReceiptItemDto.toDomain() = ReceiptItemDomain(
    productName = name,
    variantName = variantName,
    qty = qty.toIntOrNull() ?: 1,
    price = price,
    subtotal = subtotal,
    note = note
)

fun BundleDto.toDomain() = Bundle(
    uuid = uuid,
    name = name,
    price = price,
    isActive = isActive,
    items = items.map { BundleItem(it.productUuid, it.productName, it.qty) }
)

fun ModifierDto.toDomain() = Modifier(
    uuid = uuid,
    name = name,
    sortOrder = sortOrder,
    productUuid = productUuid
)
