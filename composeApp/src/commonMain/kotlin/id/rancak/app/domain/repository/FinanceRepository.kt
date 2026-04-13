package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface FinanceRepository {
    suspend fun getCashIns(dateFrom: String? = null, dateTo: String? = null): Resource<List<CashIn>>
    suspend fun createCashIn(amount: Long, source: String, description: String, note: String? = null): Resource<CashIn>
    suspend fun deleteCashIn(cashInId: String): Resource<Unit>

    suspend fun getExpenses(dateFrom: String? = null, dateTo: String? = null): Resource<List<Expense>>
    suspend fun createExpense(amount: Long, description: String, note: String? = null): Resource<Expense>
    suspend fun deleteExpense(expenseId: String): Resource<Unit>

    suspend fun getReportSummary(dateFrom: String, dateTo: String): Resource<ReportSummary>
    suspend fun getProductReport(dateFrom: String, dateTo: String): Resource<List<ProductReport>>
}
