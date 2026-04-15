package id.rancak.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.rancak.app.data.local.db.entity.TableEntity

@Dao
interface TableDao {

    @Query("SELECT * FROM `tables` ORDER BY sortOrder ASC")
    suspend fun getAll(): List<TableEntity>

    @Upsert
    suspend fun upsertAll(tables: List<TableEntity>)

    @Query("DELETE FROM `tables`")
    suspend fun deleteAll()
}
