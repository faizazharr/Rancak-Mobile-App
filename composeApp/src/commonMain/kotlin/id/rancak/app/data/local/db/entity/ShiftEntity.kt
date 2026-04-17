package id.rancak.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.rancak.app.domain.model.Shift
import id.rancak.app.domain.model.ShiftStatus

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val uuid: String,
    val openedAt: String?,
    val closedAt: String?,
    val status: String,
    val openingCash: String,
    val closingCash: String?,
    val expectedCash: String?,
    val cashDifference: String?,
    val cashierName: String?,
    val totalSales: Long?,
    val totalTransactions: Int?,
    val totalExpenses: Long?,
    val totalCashIn: Long?,
    val cachedAt: Long = 0L
)

fun ShiftEntity.toDomain() = Shift(
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

fun Shift.toEntity(cachedAt: Long = 0L) = ShiftEntity(
    uuid = uuid,
    openedAt = openedAt,
    closedAt = closedAt,
    status = status.value,
    openingCash = openingCash,
    closingCash = closingCash,
    expectedCash = expectedCash,
    cashDifference = cashDifference,
    cashierName = cashierName,
    totalSales = totalSales,
    totalTransactions = totalTransactions,
    totalExpenses = totalExpenses,
    totalCashIn = totalCashIn,
    cachedAt = cachedAt
)
