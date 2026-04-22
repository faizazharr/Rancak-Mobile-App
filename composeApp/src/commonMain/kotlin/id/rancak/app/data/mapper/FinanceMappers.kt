package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.operations.CashInDto
import id.rancak.app.data.remote.dto.operations.ExpenseDto
import id.rancak.app.data.remote.dto.operations.PaymentMethodReportDto
import id.rancak.app.data.remote.dto.operations.ShiftSummaryDto
import id.rancak.app.domain.model.CashIn
import id.rancak.app.domain.model.Expense
import id.rancak.app.domain.model.PaymentMethodReport
import id.rancak.app.domain.model.ShiftSummary

/**
 * DTO → domain mappers for Cash-ins, Expenses, and Shift summaries.
 */

fun CashInDto.toDomain() = CashIn(
    uuid = uuid,
    amount = amount,
    source = source,
    description = description,
    note = note,
    cashierUuid = cashierUuid,
    cashierName = cashierName,
    shiftUuid = shiftUuid,
    cashInDate = cashInDate,
    createdAt = createdAt
)

fun ExpenseDto.toDomain() = Expense(
    uuid = uuid,
    amount = amount,
    description = description,
    note = note,
    categoryUuid = categoryUuid,
    categoryName = categoryName,
    cashierUuid = cashierUuid,
    cashierName = cashierName,
    expenseDate = expenseDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ShiftSummaryDto.toDomain() = ShiftSummary(
    uuid = uuid,
    openedAt = openedAt,
    closedAt = closedAt,
    status = status,
    openingCash = openingCash,
    closingCash = closingCash,
    expectedCash = expectedCash,
    cashDifference = cashDifference,
    cashierName = cashierName,
    totalSales = totalSales,
    totalTransactions = totalTransactions,
    totalExpenses = totalExpenses,
    totalCashIn = totalCashIn,
    paymentSummary = payments.map { it.toDomain() }
)

fun PaymentMethodReportDto.toDomain() = PaymentMethodReport(
    method = method,
    total = total,
    count = count
)
