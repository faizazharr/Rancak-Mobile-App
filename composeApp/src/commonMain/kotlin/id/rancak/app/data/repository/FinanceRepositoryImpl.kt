package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
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
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.map {
                    CashIn(it.uuid, it.amount, it.source, it.description, it.note, it.createdAt)
                })
            } else Resource.Error(response.message ?: "Failed to load cash ins")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun createCashIn(amount: Long, source: String, description: String, note: String?): Resource<CashIn> {
        return try {
            val response = api.createCashIn(tenantUuid, amount, source, description, note)
            if (response.status == "ok" && response.data != null) {
                val d = response.data
                Resource.Success(CashIn(d.uuid, d.amount, d.source, d.description, d.note, d.createdAt))
            } else Resource.Error(response.message ?: "Failed to create cash in")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun deleteCashIn(cashInId: String): Resource<Unit> {
        return try {
            val response = api.deleteCashIn(tenantUuid, cashInId)
            if (response.status == "ok") Resource.Success(Unit)
            else Resource.Error(response.message ?: "Failed to delete")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun getExpenses(dateFrom: String?, dateTo: String?): Resource<List<Expense>> {
        return try {
            val response = api.getExpenses(tenantUuid, dateFrom, dateTo)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.map {
                    Expense(it.uuid, it.amount, it.description, it.note, it.categoryUuid, it.expenseDate, it.createdAt)
                })
            } else Resource.Error(response.message ?: "Failed to load expenses")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun createExpense(amount: Long, description: String, note: String?): Resource<Expense> {
        return try {
            val response = api.createExpense(tenantUuid, amount, description, note)
            if (response.status == "ok" && response.data != null) {
                val d = response.data
                Resource.Success(Expense(d.uuid, d.amount, d.description, d.note, d.categoryUuid, d.expenseDate, d.createdAt))
            } else Resource.Error(response.message ?: "Failed to create expense")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun deleteExpense(expenseId: String): Resource<Unit> {
        return try {
            val response = api.deleteExpense(tenantUuid, expenseId)
            if (response.status == "ok") Resource.Success(Unit)
            else Resource.Error(response.message ?: "Failed to delete")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun getReportSummary(dateFrom: String, dateTo: String): Resource<ReportSummary> {
        return try {
            val response = api.getReportSummary(tenantUuid, dateFrom, dateTo)
            if (response.status == "ok" && response.data != null) {
                val d = response.data
                Resource.Success(ReportSummary(
                    totalSales = d.totalSales, totalTransactions = d.totalTransactions,
                    totalDiscount = d.totalDiscount, totalTax = d.totalTax, totalNet = d.totalNet,
                    paymentMethods = d.paymentMethods.map { PaymentMethodReport(it.method, it.total, it.count) }
                ))
            } else Resource.Error(response.message ?: "Failed to load report")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    override suspend fun getProductReport(dateFrom: String, dateTo: String): Resource<List<ProductReport>> {
        return try {
            val response = api.getReportProducts(tenantUuid, dateFrom, dateTo)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.map {
                    ProductReport(it.productUuid, it.productName, it.qtySold, it.totalRevenue)
                })
            } else Resource.Error(response.message ?: "Failed to load report")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }
}
