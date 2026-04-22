package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.*
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.FinanceRepository

class FinanceRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : FinanceRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getCashIns(dateFrom: String?, dateTo: String?): Resource<List<CashIn>> {
        return try {
            val response = api.getCashIns(tenantUuid, dateFrom, dateTo)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else Resource.Error(response.message ?: "Gagal memuat cash in")
        } catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
    }

    override suspend fun createCashIn(amount: Long, source: String, description: String, note: String?): Resource<CashIn> {
        return try {
            val response = api.createCashIn(tenantUuid, amount, source, description, note)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else Resource.Error(response.message ?: "Gagal membuat cash in")
        } catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
    }

    override suspend fun deleteCashIn(cashInId: String): Resource<Unit> {
        return try {
            val response = api.deleteCashIn(tenantUuid, cashInId)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal menghapus")
        } catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
    }

    override suspend fun getExpenses(dateFrom: String?, dateTo: String?): Resource<List<Expense>> {
        return try {
            val response = api.getExpenses(tenantUuid, dateFrom, dateTo)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else Resource.Error(response.message ?: "Gagal memuat pengeluaran")
        } catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
    }

    override suspend fun createExpense(amount: Long, description: String, note: String?): Resource<Expense> {
        return try {
            val response = api.createExpense(tenantUuid, amount, description, note)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else Resource.Error(response.message ?: "Gagal membuat pengeluaran")
        } catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
    }

    override suspend fun deleteExpense(expenseId: String): Resource<Unit> {
        return try {
            val response = api.deleteExpense(tenantUuid, expenseId)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal menghapus")
        } catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
    }

    override suspend fun getShiftSummary(): Resource<ShiftSummary> {
        return try {
            val response = api.getShiftSummary(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else Resource.Error(response.message ?: "Gagal memuat ringkasan shift")
        } catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
    }

    override suspend fun getMySalesToday(): Resource<MySalesReport> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getMySalesToday(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal mengambil laporan penjualan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getStockReport(): Resource<List<StockReport>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getStockReport(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil laporan stok")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getLowStock(): Resource<List<LowStock>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getLowStock(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil data stok rendah")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getStockAlerts(): Resource<List<StockAlert>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getStockAlerts(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil alert stok")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getExpiringBatches(days: Int): Resource<List<ExpiringBatch>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getExpiringBatches(tenantUuid, days)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil batch kadaluarsa")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getDailyByCategory(date: String?): Resource<List<DailyCategoryReport>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getDailyByCategory(tenantUuid, date)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil laporan kategori")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }
}

