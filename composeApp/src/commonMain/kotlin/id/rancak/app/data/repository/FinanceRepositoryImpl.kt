package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.FinanceRepository

class FinanceRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : FinanceRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("No tenant selected")

    override suspend fun getCashIns(dateFrom: String?, dateTo: String?): Resource<List<CashIn>> {
        return try {
            val response = api.getCashIns(tenantUuid, dateFrom, dateTo)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else Resource.Error(response.message ?: "Gagal memuat cash in")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun createCashIn(amount: Long, source: String, description: String, note: String?): Resource<CashIn> {
        return try {
            val response = api.createCashIn(tenantUuid, amount, source, description, note)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else Resource.Error(response.message ?: "Gagal membuat cash in")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun deleteCashIn(cashInId: String): Resource<Unit> {
        return try {
            val response = api.deleteCashIn(tenantUuid, cashInId)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal menghapus")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun getExpenses(dateFrom: String?, dateTo: String?): Resource<List<Expense>> {
        return try {
            val response = api.getExpenses(tenantUuid, dateFrom, dateTo)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else Resource.Error(response.message ?: "Gagal memuat pengeluaran")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun createExpense(amount: Long, description: String, note: String?): Resource<Expense> {
        return try {
            val response = api.createExpense(tenantUuid, amount, description, note)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else Resource.Error(response.message ?: "Gagal membuat pengeluaran")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun deleteExpense(expenseId: String): Resource<Unit> {
        return try {
            val response = api.deleteExpense(tenantUuid, expenseId)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal menghapus")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun getShiftSummary(): Resource<ShiftSummary> {
        return try {
            val response = api.getShiftSummary(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else Resource.Error(response.message ?: "Gagal memuat ringkasan shift")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }
}

