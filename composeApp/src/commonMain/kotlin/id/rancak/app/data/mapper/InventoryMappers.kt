package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.inventory.OpnameDetailDto
import id.rancak.app.data.remote.dto.inventory.OpnameDto
import id.rancak.app.data.remote.dto.inventory.OpnameItemDto
import id.rancak.app.data.remote.dto.inventory.PurchaseOrderDto
import id.rancak.app.data.remote.dto.inventory.PurchaseOrderItemDto
import id.rancak.app.data.remote.dto.inventory.SupplierDto
import id.rancak.app.domain.model.OpnameItem
import id.rancak.app.domain.model.PurchaseOrder
import id.rancak.app.domain.model.PurchaseOrderItem
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.domain.model.Supplier

fun OpnameDto.toDomain(): StockOpname = StockOpname(
    uuid = uuid,
    opnameNo = opnameNo,
    status = status,
    note = note,
    itemCount = itemCount,
    createdBy = createdBy,
    finalizedBy = finalizedBy,
    finalizedAt = finalizedAt,
    createdAt = createdAt
)

fun OpnameItemDto.toDomain(): OpnameItem = OpnameItem(
    productUuid = productUuid,
    productName = productName,
    sku = sku,
    systemStock = systemStock,
    actualStock = actualStock,
    difference = difference,
    note = note
)

fun OpnameDetailDto.toDomain(): StockOpnameDetail = StockOpnameDetail(
    opname = StockOpname(
        uuid = uuid,
        opnameNo = opnameNo,
        status = status,
        note = note,
        itemCount = itemCount,
        createdBy = createdBy,
        finalizedBy = finalizedBy,
        finalizedAt = finalizedAt,
        createdAt = createdAt
    ),
    items = items.map { it.toDomain() },
    shortageCount = shortageCount,
    surplusCount = surplusCount
)

fun SupplierDto.toDomain(): Supplier = Supplier(
    uuid = uuid,
    name = name,
    contactName = contactName,
    phone = phone,
    email = email,
    address = address,
    npwp = npwp,
    notes = notes,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PurchaseOrderItemDto.toDomain(): PurchaseOrderItem = PurchaseOrderItem(
    uuid = uuid,
    productUuid = productUuid,
    productName = productName,
    qtyOrdered = qtyOrdered.toDoubleOrZero(),
    qtyReceived = qtyReceived.toDoubleOrZero(),
    unitCost = unitCost.toDoubleOrZero(),
    subtotal = subtotal.toDoubleOrZero(),
    notes = notes
)

fun PurchaseOrderDto.toDomain(): PurchaseOrder = PurchaseOrder(
    uuid = uuid,
    poNumber = poNumber,
    supplierUuid = supplierUuid,
    supplierName = supplierName,
    status = status,
    orderDate = orderDate,
    expectedDate = expectedDate,
    receivedDate = receivedDate,
    subtotal = subtotal.toDoubleOrZero(),
    taxAmount = taxAmount.toDoubleOrZero(),
    shippingCost = shippingCost.toDoubleOrZero(),
    total = total.toDoubleOrZero(),
    notes = notes,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
    items = items.map { it.toDomain() }
)

private fun String.toDoubleOrZero(): Double = toDoubleOrNull() ?: 0.0
