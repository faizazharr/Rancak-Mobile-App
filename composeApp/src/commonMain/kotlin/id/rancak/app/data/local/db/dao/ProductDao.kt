package id.rancak.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.rancak.app.data.local.db.entity.ProductEntity

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    suspend fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE (:query = '' OR name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%') AND (:categoryUuid = '' OR categoryUuid = :categoryUuid)")
    suspend fun search(query: String, categoryUuid: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
