package id.rancak.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.rancak.app.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val description: String?
)

fun CategoryEntity.toDomain() = Category(
    uuid = uuid,
    name = name,
    description = description
)

fun Category.toEntity() = CategoryEntity(
    uuid = uuid,
    name = name,
    description = description
)
