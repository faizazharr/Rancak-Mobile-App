package id.rancak.app.data.mapper

import id.rancak.app.data.remote.api.KdsItemDto
import id.rancak.app.data.remote.api.KdsOrderDto
import id.rancak.app.data.remote.dto.operations.OrderBoardItemDto
import id.rancak.app.data.remote.dto.operations.OrderBoardOrderDto
import id.rancak.app.data.remote.dto.sync.ShiftDto
import id.rancak.app.data.remote.dto.sync.TableDto
import id.rancak.app.domain.model.KdsItem
import id.rancak.app.domain.model.KdsItemStatus
import id.rancak.app.domain.model.KdsOrder
import id.rancak.app.domain.model.KdsStatus
import id.rancak.app.domain.model.OrderBoardItem
import id.rancak.app.domain.model.OrderBoardOrder
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.model.Shift
import id.rancak.app.domain.model.ShiftStatus
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus

/**
 * DTO → domain mappers for Tables, Shifts, KDS, and Order Board.
 */

fun TableDto.toDomain() = Table(
    uuid = uuid,
    name = name,
    area = area,
    capacity = capacity,
    status = TableStatus.from(status),
    isActive = isActive,
    sortOrder = sortOrder,
    activeSaleUuid = activeSaleUuid
)

fun ShiftDto.toDomain() = Shift(
    uuid = uuid,
    openedAt = openedAt,
    closedAt = closedAt,
    status = ShiftStatus.from(status),
    openingCash = openingCash,
    closingCash = closingCash,
    expectedCash = expectedCash,
    cashDifference = cashDifference,
    cashierName = cashierName,
    totalSales = totalSales,
    totalTransactions = totalTransactions,
    totalExpenses = totalExpenses,
    totalCashIn = totalCashIn
)

fun KdsOrderDto.toDomain() = KdsOrder(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = OrderType.from(orderType),
    tableName = tableName,
    queueNumber = queueNumber,
    customerName = customerName,
    note = note,
    status = KdsStatus.from(status),
    items = items.map { it.toDomain() },
    createdAt = createdAt
)

fun KdsItemDto.toDomain() = KdsItem(
    uuid = uuid,
    productName = productName,
    qty = qty,
    variantName = variantName,
    note = note,
    status = KdsItemStatus.from(status)
)

fun OrderBoardOrderDto.toDomain() = OrderBoardOrder(
    uuid = uuid,
    invoiceNo = invoiceNo,
    queueNumber = queueNumber,
    orderType = OrderType.from(orderType),
    customerName = customerName,
    status = SaleStatus.from(status),
    createdAt = createdAt,
    servedAt = servedAt,
    items = items.map { it.toDomain() }
)

fun OrderBoardItemDto.toDomain() = OrderBoardItem(
    productName = productName,
    qty = qty,
    note = note
)
