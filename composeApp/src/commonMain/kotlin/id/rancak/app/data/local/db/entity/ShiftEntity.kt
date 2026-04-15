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
    val openingCash: Long,
    val closingCash: Long?,
    val totalSales: Long?,
    val totalExpenses: Long?,
    val cachedAt: Long = 0L
)

fun ShiftEntity.toDomain() = Shift(
    uuid = uuid,
    openedAt = openedAt,
    closedAt = closedAt,
    status = ShiftStatus.from(status),
    openingCash = openingCash,
    closingCash = closingCash,
    totalSales = totalSales,
    totalExpenses = totalExpenses
)

fun Shift.toEntity(cachedAt: Long = 0L) = ShiftEntity(
    uuid = uuid,
    openedAt = openedAt,
    closedAt = closedAt,
    status = status.value,
    openingCash = openingCash,
    closingCash = closingCash,
    totalSales = totalSales,
    totalExpenses = totalExpenses,
    cachedAt = cachedAt
)
