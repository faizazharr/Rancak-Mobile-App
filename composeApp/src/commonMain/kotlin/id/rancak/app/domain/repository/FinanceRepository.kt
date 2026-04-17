package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface FinanceRepository {
    suspend fun getCashIns(dateFrom: String? = null, dateTo: String? = null): Resource<List<CashIn>>
    suspend fun createCashIn(amount: Long, source: String, description: String, note: String? = null): Resource<CashIn>
    suspend fun deleteCashIn(cashInId: String): Resource<Unit>

    suspend fun getExpenses(dateFrom: String? = null, dateTo: String? = null): Resource<List<Expense>>
    suspend fun createExpense(amount: Long, description: String, note: String? = null): Resource<Expense>
    suspend fun deleteExpense(expenseId: String): Resource<Unit>

    suspend fun getShiftSummary(): Resource<ShiftSummary>
    suspend fun getMySalesToday(): Resource<MySalesReport>
    suspend fun getStockReport(): Resource<List<StockReport>>
    suspend fun getLowStock(): Resource<List<LowStock>>
    suspend fun getStockAlerts(): Resource<List<StockAlert>>
    suspend fun getExpiringBatches(days: Int = 7): Resource<List<ExpiringBatch>>
    suspend fun getDailyByCategory(date: String? = null): Resource<List<DailyCategoryReport>>
}
