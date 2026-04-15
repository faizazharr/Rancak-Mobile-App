package id.rancak.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.rancak.app.data.local.db.entity.ShiftEntity

@Dao
interface ShiftDao {

    @Query("SELECT * FROM shifts WHERE status = 'open' LIMIT 1")
    suspend fun getOpenShift(): ShiftEntity?

    @Upsert
    suspend fun upsert(shift: ShiftEntity)

    @Query("DELETE FROM shifts")
    suspend fun deleteAll()
}
