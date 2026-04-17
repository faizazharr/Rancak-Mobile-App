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
            if (response.isSuccess && response.data != null) {
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
            if (response.isSuccess) {
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

    override suspend fun openShift(openingCash: String): Resource<Shift> {
        return try {
            val response = api.openShift(tenantUuid, openingCash)
            if (response.isSuccess && response.data != null) {
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

    override suspend fun closeShift(closingCash: String, note: String?): Resource<Shift> {
        return try {
            val response = api.closeShift(tenantUuid, closingCash, note)
            if (response.isSuccess && response.data != null) {
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
            if (response.isSuccess && response.data != null) {
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
            if (response.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message ?: "Failed to update status")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getSurcharges(): Resource<List<Surcharge>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getSurcharges(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil surcharge")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getTaxConfigs(): Resource<List<TaxConfig>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getTaxConfigs(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil konfigurasi pajak")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getDiscountRules(): Resource<List<DiscountRule>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getDiscountRules(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil aturan diskon")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun validateVoucher(code: String, subtotal: Long): Resource<VoucherValidation> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.validateVoucher(tenantUuid, code, subtotal)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Voucher tidak valid")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun previewDiscount(total: Long): Resource<DiscountPreview> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.previewDiscount(tenantUuid, total)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal mengambil preview diskon")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun syncCatalog(updatedAfter: String?): Resource<Unit> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.syncCatalog(tenantUuid, updatedAfter)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal sinkronisasi katalog")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun syncStatus(): Resource<Boolean> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.syncStatus(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.hasOpenShift)
            } else {
                Resource.Error(response.message ?: "Gagal mengecek status sinkronisasi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}

