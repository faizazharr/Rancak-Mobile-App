package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.db.dao.ShiftDao
import id.rancak.app.data.local.db.dao.TableDao
import id.rancak.app.data.local.db.entity.toDomain
import id.rancak.app.data.local.db.entity.toEntity
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.OperationsRepository

class OperationsRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager,
    private val tableDao: TableDao,
    private val shiftDao: ShiftDao
) : OperationsRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("No tenant selected")

    override suspend fun getTables(): Resource<List<Table>> {
        return try {
            val response = api.getTables(tenantUuid)
            if (response.status == "ok" && response.data != null) {
                val tables = response.data.map { it.toDomain() }
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                tableDao.upsertAll(tables.map { it.toEntity(now) })
                Resource.Success(tables)
            } else {
                Resource.Error(response.message ?: "Failed to load tables")
            }
        } catch (e: Exception) {
            val cached = tableDao.getAll()
            if (cached.isNotEmpty()) {
                Resource.Success(cached.map { it.toDomain() })
            } else {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    override suspend fun getCurrentShift(): Resource<Shift?> {
        return try {
            val response = api.getCurrentShift(tenantUuid)
            if (response.status == "ok") {
                val shift = response.data?.toDomain()
                if (shift != null) {
                    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    shiftDao.upsert(shift.toEntity(now))
                }
                Resource.Success(shift)
            } else {
                Resource.Success(null)
            }
        } catch (e: Exception) {
            val cached = shiftDao.getOpenShift()
            if (cached != null) {
                Resource.Success(cached.toDomain())
            } else {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    override suspend fun openShift(openingCash: Long): Resource<Shift> {
        return try {
            val response = api.openShift(tenantUuid, openingCash)
            if (response.status == "ok" && response.data != null) {
                val shift = response.data.toDomain()
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                shiftDao.upsert(shift.toEntity(now))
                Resource.Success(shift)
            } else {
                Resource.Error(response.message ?: "Failed to open shift")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun closeShift(closingCash: Long, note: String?): Resource<Shift> {
        return try {
            val response = api.closeShift(tenantUuid, closingCash, note)
            if (response.status == "ok" && response.data != null) {
                val shift = response.data.toDomain()
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                shiftDao.upsert(shift.toEntity(now))
                Resource.Success(shift)
            } else {
                Resource.Error(response.message ?: "Failed to close shift")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getKdsOrders(): Resource<List<KdsOrder>> {
        return try {
            val response = api.getKdsOrders(tenantUuid)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Failed to load KDS orders")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateKdsStatus(kdsUuid: String, status: KdsStatus): Resource<Unit> {
        return try {
            val response = api.updateKdsStatus(tenantUuid, kdsUuid, status.value)
            if (response.status == "ok") {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message ?: "Failed to update status")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
