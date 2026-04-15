package id.rancak.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val area: String?,
    val capacity: Int?,
    val status: String,
    val isActive: Boolean,
    val sortOrder: Int,
    val activeSaleUuid: String?,
    val cachedAt: Long = 0L
)

fun TableEntity.toDomain() = Table(
    uuid = uuid,
    name = name,
    area = area,
    capacity = capacity,
    status = TableStatus.from(status),
    isActive = isActive,
    sortOrder = sortOrder,
    activeSaleUuid = activeSaleUuid
)

fun Table.toEntity(cachedAt: Long = 0L) = TableEntity(
    uuid = uuid,
    name = name,
    area = area,
    capacity = capacity,
    status = status.value,
    isActive = isActive,
    sortOrder = sortOrder,
    activeSaleUuid = activeSaleUuid,
    cachedAt = cachedAt
)
