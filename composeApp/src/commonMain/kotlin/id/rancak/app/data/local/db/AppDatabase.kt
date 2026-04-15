package id.rancak.app.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import id.rancak.app.data.local.db.dao.CartDao
import id.rancak.app.data.local.db.dao.CategoryDao
import id.rancak.app.data.local.db.dao.ProductDao
import id.rancak.app.data.local.db.dao.SaleDao
import id.rancak.app.data.local.db.dao.ShiftDao
import id.rancak.app.data.local.db.dao.TableDao
import id.rancak.app.data.local.db.entity.CartItemEntity
import id.rancak.app.data.local.db.entity.CategoryEntity
import id.rancak.app.data.local.db.entity.ProductEntity
import id.rancak.app.data.local.db.entity.SaleEntity
import id.rancak.app.data.local.db.entity.SaleItemEntity
import id.rancak.app.data.local.db.entity.ShiftEntity
import id.rancak.app.data.local.db.entity.TableEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        CartItemEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ShiftEntity::class,
        TableEntity::class
    ],
    version = 2,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartDao(): CartDao
    abstract fun saleDao(): SaleDao
    abstract fun shiftDao(): ShiftDao
    abstract fun tableDao(): TableDao
}

// KSP fills in the initialize() body for each platform target.
// @Suppress is required because the actual is generated at KSP build time,
// not from a source file — the compiler never sees a declared actual.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
