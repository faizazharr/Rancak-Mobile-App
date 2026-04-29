package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.db.dao.ShiftDao
import id.rancak.app.data.local.db.dao.TableDao
import id.rancak.app.data.local.db.entity.toDomain
import id.rancak.app.data.local.db.entity.toEntity
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.*
import id.rancak.app.data.remote.dto.operations.SubmitCashCountRequest
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.OperationsRepository

class OperationsRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager,
    private val tableDao: TableDao,
    private val shiftDao: ShiftDao
) : OperationsRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getTables(): Resource<List<Table>> {
        return try {
            val response = api.getTables(tenantUuid)
            if (response.isSuccess && response.data != null) {
                val tables = response.data.map { it.toDomain() }
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                tableDao.upsertAll(tables.map { it.toEntity(now) })
                Resource.Success(tables)
            } else {
                Resource.Error(response.message ?: "Gagal memuat daftar meja")
            }
        } catch (e: Exception) {
            val cached = tableDao.getAll()
            if (cached.isNotEmpty()) {
                Resource.Success(cached.map { it.toDomain() })
            } else {
                Resource.Error(e.message ?: "Kesalahan jaringan")
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
                Resource.Error(e.message ?: "Kesalahan jaringan")
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
                Resource.Error(response.message ?: "Gagal membuka shift")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
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
                Resource.Error(response.message ?: "Gagal menutup shift")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getKdsOrders(): Resource<List<KdsOrder>> = safe(
        block    = { api.getKdsOrders(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat pesanan KDS"
    )

    override suspend fun updateKdsStatus(kdsUuid: String, status: KdsStatus): Resource<Unit> = safeUnit(
        block    = { api.updateKdsStatus(tenantUuid, kdsUuid, status.value) },
        errorMsg = "Gagal memperbarui status"
    )

    override suspend fun getSurcharges(): Resource<List<Surcharge>> = safe(
        block    = { api.getSurcharges(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil surcharge"
    )

    override suspend fun getTaxConfigs(): Resource<List<TaxConfig>> = safe(
        block    = { api.getTaxConfigs(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil konfigurasi pajak"
    )

    override suspend fun getDiscountRules(): Resource<List<DiscountRule>> = safe(
        block    = { api.getDiscountRules(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil aturan diskon"
    )

    override suspend fun validateVoucher(code: String, subtotal: Long): Resource<VoucherValidation> = safe(
        block    = { api.validateVoucher(tenantUuid, code, subtotal) },
        map      = { it.toDomain() },
        errorMsg = "Voucher tidak valid"
    )

    override suspend fun previewDiscount(total: Long): Resource<DiscountPreview> = safe(
        block    = { api.previewDiscount(tenantUuid, total) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengambil preview diskon"
    )

    override suspend fun syncCatalog(updatedAfter: String?): Resource<Unit> = safe(
        block    = { api.syncCatalog(tenantUuid, updatedAfter) },
        map      = { _ -> },
        errorMsg = "Gagal sinkronisasi katalog"
    )

    override suspend fun syncStatus(): Resource<Boolean> = safe(
        block    = { api.syncStatus(tenantUuid) },
        map      = { it.hasOpenShift },
        errorMsg = "Gagal mengecek status sinkronisasi"
    )

    override suspend fun getShiftSummaryById(shiftUuid: String): Resource<ShiftSummary> = safe(
        block    = { api.getShiftSummaryById(tenantUuid, shiftUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengambil ringkasan shift"
    )

    override suspend fun getCashCounts(shiftUuid: String): Resource<List<CashCount>> = safe(
        block    = { api.getCashCounts(tenantUuid, shiftUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil cash count"
    )

    override suspend fun submitCashCount(
        shiftUuid: String,
        actualCash: Double,
        denominations: Map<String, Int>?,
        note: String?
    ): Resource<CashCount> = safe(
        block    = { api.submitCashCount(tenantUuid, shiftUuid, SubmitCashCountRequest(actualCash, denominations, note)) },
        map      = { it.toDomain() },
        errorMsg = "Gagal menyimpan hitungan kas"
    )

    override suspend fun getKdsDetail(kdsUuid: String): Resource<KdsOrder> = safe(
        block    = { api.getKdsDetail(tenantUuid, kdsUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memuat detail KDS"
    )

    override suspend fun updateKdsItemStatus(
        kdsUuid: String,
        itemUuid: String,
        status: String
    ): Resource<Unit> = safeUnit(
        block    = { api.updateKdsItemStatus(tenantUuid, kdsUuid, itemUuid, status) },
        errorMsg = "Gagal update status item"
    )
}

