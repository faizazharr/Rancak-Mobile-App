package id.rancak.app.data.repository.fake

import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.FinanceRepository

class FakeFinanceRepository : FinanceRepository {

    private var cashInCounter  = 4
    private var expenseCounter = 6

    override suspend fun getCashIns(dateFrom: String?, dateTo: String?): Resource<List<CashIn>> =
        Resource.Success(demoCashIns.toList())

    override suspend fun createCashIn(
        amount: Long,
        source: String,
        description: String,
        note: String?
    ): Resource<CashIn> {
        val cashIn = CashIn(
            uuid        = "ci-%03d".format(cashInCounter++),
            amount      = amount,
            source      = source,
            description = description,
            note        = note,
            cashierUuid = null,
            cashierName = null,
            shiftUuid   = null,
            cashInDate  = null,
            createdAt   = "2025-06-14 ${(10..20).random()}:00:00"
        )
        demoCashIns.add(0, cashIn)
        return Resource.Success(cashIn)
    }

    override suspend fun deleteCashIn(cashInId: String): Resource<Unit> {
        demoCashIns.removeAll { it.uuid == cashInId }
        return Resource.Success(Unit)
    }

    override suspend fun getExpenses(dateFrom: String?, dateTo: String?): Resource<List<Expense>> =
        Resource.Success(demoExpenses.toList())

    override suspend fun createExpense(
        amount: Long,
        description: String,
        note: String?
    ): Resource<Expense> {
        val expense = Expense(
            uuid         = "exp-%03d".format(expenseCounter++),
            amount       = amount,
            description  = description,
            note         = note,
            categoryUuid = null,
            categoryName = null,
            cashierUuid  = null,
            cashierName  = null,
            expenseDate  = "2025-06-14",
            createdAt    = "2025-06-14 ${(10..20).random()}:00:00",
            updatedAt    = null
        )
        demoExpenses.add(0, expense)
        return Resource.Success(expense)
    }

    override suspend fun deleteExpense(expenseId: String): Resource<Unit> {
        demoExpenses.removeAll { it.uuid == expenseId }
        return Resource.Success(Unit)
    }

    override suspend fun getShiftSummary(): Resource<ShiftSummary> =
        Resource.Success(demoShiftSummary)
}
