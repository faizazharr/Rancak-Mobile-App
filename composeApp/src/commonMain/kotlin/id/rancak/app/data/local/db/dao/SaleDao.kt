package id.rancak.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import id.rancak.app.data.local.db.entity.SaleEntity
import id.rancak.app.data.local.db.entity.SaleItemEntity

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    suspend fun getAll(): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE status IN ('held', 'served') ORDER BY createdAt DESC")
    suspend fun getActive(): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE uuid = :uuid")
    suspend fun findByUuid(uuid: String): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleUuid = :saleUuid")
    suspend fun getItemsForSale(saleUuid: String): List<SaleItemEntity>

    @Upsert
    suspend fun upsertSales(sales: List<SaleEntity>)

    @Upsert
    suspend fun upsertItems(items: List<SaleItemEntity>)

    @Transaction
    suspend fun upsertSalesWithItems(sales: List<SaleEntity>, items: List<SaleItemEntity>) {
        upsertSales(sales)
        upsertItems(items)
    }

    @Query("DELETE FROM sales")
    suspend fun deleteAll()

    @Query("DELETE FROM sale_items")
    suspend fun deleteAllItems()
}
