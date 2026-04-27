package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface FinanceRepository {
    suspend fun getCashIns(dateFrom: String? = null, dateTo: String? = null, shiftUuid: String? = null, page: Int = 1, limit: Int = 50): Resource<List<CashIn>>
    suspend fun getCashIn(cashInId: String): Resource<CashIn>
    suspend fun createCashIn(amount: Long, source: String, description: String, note: String? = null, cashInDate: String? = null): Resource<CashIn>
    suspend fun deleteCashIn(cashInId: String): Resource<Unit>

    suspend fun getExpenses(dateFrom: String? = null, dateTo: String? = null, categoryUuid: String? = null, page: Int = 1, limit: Int = 50): Resource<List<Expense>>
    suspend fun getExpense(expenseId: String): Resource<Expense>
    suspend fun createExpense(amount: Long, description: String, note: String? = null, categoryUuid: String? = null, expenseDate: String? = null): Resource<Expense>
    suspend fun updateExpense(expenseId: String, amount: Long? = null, description: String? = null, note: String? = null, categoryUuid: String? = null, expenseDate: String? = null): Resource<Expense>
    suspend fun deleteExpense(expenseId: String): Resource<Unit>

    suspend fun getShiftSummary(): Resource<ShiftSummary>
    suspend fun getMySalesToday(): Resource<MySalesReport>
    suspend fun getStockReport(): Resource<List<StockReport>>
    suspend fun getLowStock(): Resource<List<LowStock>>
    suspend fun getStockAlerts(): Resource<List<StockAlert>>
    suspend fun getExpiringBatches(days: Int = 7): Resource<List<ExpiringBatch>>
    suspend fun getDailyByCategory(date: String? = null): Resource<List<DailyCategoryReport>>

    // ── Stock alert dismissal ───────────────────────────────────────────────
    suspend fun markStockAlertRead(alertId: String): Resource<Unit>
    suspend fun markAllStockAlertsRead(): Resource<Int>

    // ── Shift per kasir ─────────────────────────────────────────────────────
    suspend fun getShiftByCashier(date: String? = null): Resource<List<CashierShiftSummary>>

    // ── Expense categories CRUD ─────────────────────────────────────────────
    suspend fun getExpenseCategories(): Resource<List<ExpenseCategory>>
    suspend fun getExpenseCategory(categoryId: String): Resource<ExpenseCategory>
    suspend fun createExpenseCategory(name: String, isActive: Boolean = true, sortOrder: Int = 0): Resource<ExpenseCategory>
    suspend fun updateExpenseCategory(categoryId: String, name: String? = null, isActive: Boolean? = null, sortOrder: Int? = null): Resource<ExpenseCategory>
    suspend fun deleteExpenseCategory(categoryId: String): Resource<Unit>
}
