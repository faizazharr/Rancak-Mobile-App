package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.operations.DailyCategoryReportDto
import id.rancak.app.data.remote.dto.operations.ExpiringBatchDto
import id.rancak.app.data.remote.dto.operations.LowStockDto
import id.rancak.app.data.remote.dto.operations.MySalesReportDto
import id.rancak.app.data.remote.dto.operations.StockAlertDto
import id.rancak.app.data.remote.dto.operations.StockReportDto
import id.rancak.app.domain.model.DailyCategoryReport
import id.rancak.app.domain.model.ExpiringBatch
import id.rancak.app.domain.model.LowStock
import id.rancak.app.domain.model.MySalesReport
import id.rancak.app.domain.model.StockAlert
import id.rancak.app.domain.model.StockReport
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * DTO → domain mappers for Reports (sales, stock, low-stock, expiring, daily-by-category).
 */

fun MySalesReportDto.toDomain() = MySalesReport(
    totalSales = totalSales.toLongOrNull() ?: 0L,
    totalTransactions = totalTransactions,
    cashTotal = cashTotal.toLongOrNull() ?: 0L
)

fun StockReportDto.toDomain() = StockReport(
    productUuid = productUuid,
    sku = sku,
    name = name,
    stock = stock,
    stockAlertThreshold = stockAlertThreshold
)

fun LowStockDto.toDomain() = LowStock(
    productUuid = productUuid,
    productName = productName,
    sku = sku,
    currentStock = currentStock,
    threshold = threshold
)

fun StockAlertDto.toDomain() = StockAlert(
    productUuid = productUuid,
    productName = productName,
    sku = sku,
    alertType = alertType,
    currentStock = stock,
    threshold = threshold
)

fun ExpiringBatchDto.toDomain(): ExpiringBatch {
    val days = expiryDate?.let {
        try {
            val expiry = LocalDate.parse(it)
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            (expiry.toEpochDays() - today.toEpochDays()).toInt()
        } catch (_: Exception) { 0 }
    } ?: 0
    return ExpiringBatch(
        batchUuid = batchUuid,
        productUuid = productUuid,
        productName = productName,
        batchNumber = batchNumber,
        expiryDate = expiryDate ?: "",
        quantityRemaining = quantityRemaining,
        daysUntilExpiry = days
    )
}

fun DailyCategoryReportDto.toDomain() = DailyCategoryReport(
    categoryName = categoryName,
    totalSales = totalRevenue,
    totalQty = totalQty
)
