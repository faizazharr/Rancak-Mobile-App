package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.createCashIn
import id.rancak.app.data.remote.api.createExpense
import id.rancak.app.data.remote.api.createExpenseCategory
import id.rancak.app.data.remote.api.deleteCashIn
import id.rancak.app.data.remote.api.deleteExpense
import id.rancak.app.data.remote.api.deleteExpenseCategory
import id.rancak.app.data.remote.api.getDailyByCategory
import id.rancak.app.data.remote.api.getExpense
import id.rancak.app.data.remote.api.getExpenseCategories
import id.rancak.app.data.remote.api.getExpenseCategory
import id.rancak.app.data.remote.api.getExpenses
import id.rancak.app.data.remote.api.getExpiringBatches
import id.rancak.app.data.remote.api.getCashIn
import id.rancak.app.data.remote.api.getCashIns
import id.rancak.app.data.remote.api.getLowStock
import id.rancak.app.data.remote.api.getMySalesToday
import id.rancak.app.data.remote.api.getShiftByCashier
import id.rancak.app.data.remote.api.getShiftSummary
import id.rancak.app.data.remote.api.getStockAlerts
import id.rancak.app.data.remote.api.getStockReport
import id.rancak.app.data.remote.api.markAllStockAlertsRead
import id.rancak.app.data.remote.api.markStockAlertRead
import id.rancak.app.data.remote.api.updateExpense
import id.rancak.app.data.remote.api.updateExpenseCategory
import id.rancak.app.data.remote.dto.operations.CreateExpenseCategoryRequest
import id.rancak.app.data.remote.dto.operations.UpdateExpenseCategoryRequest
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit
import id.rancak.app.domain.model.CashIn
import id.rancak.app.domain.model.CashierShiftSummary
import id.rancak.app.domain.model.DailyCategoryReport
import id.rancak.app.domain.model.Expense
import id.rancak.app.domain.model.ExpenseCategory
import id.rancak.app.domain.model.ExpiringBatch
import id.rancak.app.domain.model.LowStock
import id.rancak.app.domain.model.MySalesReport
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.domain.model.StockAlert
import id.rancak.app.domain.model.StockReport
import id.rancak.app.domain.repository.FinanceRepository

class FinanceRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : FinanceRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getCashIns(dateFrom: String?, dateTo: String?, shiftUuid: String?, page: Int, limit: Int): Resource<List<CashIn>> = safe(
        block    = { api.getCashIns(tenantUuid, dateFrom, dateTo, shiftUuid, page, limit) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat kas masuk"
    )

    override suspend fun getCashIn(cashInId: String): Resource<CashIn> = safe(
        block    = { api.getCashIn(tenantUuid, cashInId) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memuat kas masuk"
    )

    override suspend fun createCashIn(amount: Long, source: String, description: String, note: String?, cashInDate: String?): Resource<CashIn> = safe(
        block    = { api.createCashIn(tenantUuid, amount, source, description, note, cashInDate) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mencatat kas masuk"
    )

    override suspend fun deleteCashIn(cashInId: String): Resource<Unit> = safeUnit(
        block    = { api.deleteCashIn(tenantUuid, cashInId) },
        errorMsg = "Gagal menghapus kas masuk"
    )

    override suspend fun getExpenses(dateFrom: String?, dateTo: String?, categoryUuid: String?, page: Int, limit: Int): Resource<List<Expense>> = safe(
        block    = { api.getExpenses(tenantUuid, dateFrom, dateTo, categoryUuid, page, limit) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat pengeluaran"
    )

    override suspend fun getExpense(expenseId: String): Resource<Expense> = safe(
        block    = { api.getExpense(tenantUuid, expenseId) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memuat pengeluaran"
    )

    override suspend fun createExpense(amount: Long, description: String, note: String?, categoryUuid: String?, expenseDate: String?): Resource<Expense> = safe(
        block    = { api.createExpense(tenantUuid, amount, description, note, categoryUuid, expenseDate) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mencatat pengeluaran"
    )

    override suspend fun updateExpense(expenseId: String, amount: Long?, description: String?, note: String?, categoryUuid: String?, expenseDate: String?): Resource<Expense> = safe(
        block    = { api.updateExpense(tenantUuid, expenseId, amount, description, note, categoryUuid, expenseDate) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memperbarui pengeluaran"
    )

    override suspend fun deleteExpense(expenseId: String): Resource<Unit> = safeUnit(
        block    = { api.deleteExpense(tenantUuid, expenseId) },
        errorMsg = "Gagal menghapus pengeluaran"
    )

    override suspend fun getShiftSummary(): Resource<ShiftSummary> = safe(
        block    = { api.getShiftSummary(tenantUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memuat ringkasan shift"
    )

    override suspend fun getMySalesToday(): Resource<MySalesReport> = safe(
        block    = { api.getMySalesToday(tenantUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengambil laporan penjualan"
    )

    override suspend fun getStockReport(): Resource<List<StockReport>> = safe(
        block    = { api.getStockReport(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil laporan stok"
    )

    override suspend fun getLowStock(): Resource<List<LowStock>> = safe(
        block    = { api.getLowStock(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil data stok rendah"
    )

    override suspend fun getStockAlerts(): Resource<List<StockAlert>> = safe(
        block    = { api.getStockAlerts(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil alert stok"
    )

    override suspend fun getExpiringBatches(days: Int): Resource<List<ExpiringBatch>> = safe(
        block    = { api.getExpiringBatches(tenantUuid, days) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil batch kadaluarsa"
    )

    override suspend fun getDailyByCategory(date: String?): Resource<List<DailyCategoryReport>> = safe(
        block    = { api.getDailyByCategory(tenantUuid, date) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil laporan kategori"
    )

    override suspend fun markStockAlertRead(alertId: String): Resource<Unit> = safeUnit(
        block    = { api.markStockAlertRead(tenantUuid, alertId) },
        errorMsg = "Gagal menandai alert"
    )

    override suspend fun markAllStockAlertsRead(): Resource<Int> = safe(
        block    = { api.markAllStockAlertsRead(tenantUuid) },
        map      = { it.dismissedCount },
        errorMsg = "Gagal menandai semua alert"
    )

    override suspend fun getShiftByCashier(date: String?): Resource<List<CashierShiftSummary>> = safe(
        block    = { api.getShiftByCashier(tenantUuid, date) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat rekap kasir"
    )

    override suspend fun getExpenseCategories(): Resource<List<ExpenseCategory>> = safe(
        block    = { api.getExpenseCategories(tenantUuid) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat kategori pengeluaran"
    )

    override suspend fun getExpenseCategory(categoryId: String): Resource<ExpenseCategory> = safe(
        block    = { api.getExpenseCategory(tenantUuid, categoryId) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memuat kategori"
    )

    override suspend fun createExpenseCategory(
        name: String,
        isActive: Boolean,
        sortOrder: Int
    ): Resource<ExpenseCategory> = safe(
        block    = { api.createExpenseCategory(tenantUuid, CreateExpenseCategoryRequest(name, isActive, sortOrder)) },
        map      = { it.toDomain() },
        errorMsg = "Gagal membuat kategori"
    )

    override suspend fun updateExpenseCategory(
        categoryId: String,
        name: String?,
        isActive: Boolean?,
        sortOrder: Int?
    ): Resource<ExpenseCategory> = safe(
        block    = { api.updateExpenseCategory(tenantUuid, categoryId, UpdateExpenseCategoryRequest(name, isActive, sortOrder)) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memperbarui kategori"
    )

    override suspend fun deleteExpenseCategory(categoryId: String): Resource<Unit> = safeUnit(
        block    = { api.deleteExpenseCategory(tenantUuid, categoryId) },
        errorMsg = "Gagal menghapus kategori"
    )
}

