package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.operations.CashInDto
import id.rancak.app.data.remote.dto.operations.CreateExpenseCategoryRequest
import id.rancak.app.data.remote.dto.operations.ExpenseCategoryDto
import id.rancak.app.data.remote.dto.operations.ExpenseDto
import id.rancak.app.data.remote.dto.operations.UpdateExpenseCategoryRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Cash-in and expense tracking (cashier/owner finance endpoints).
 */

// ── Cash ins ──

suspend fun RancakApiService.getCashIns(
    tenantUuid: String,
    dateFrom: String? = null,
    dateTo: String? = null,
    shiftUuid: String? = null,
    page: Int = 1,
    limit: Int = 50
): ApiResponse<List<CashInDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.CASH_INS) {
        dateFrom?.let { parameter("date_from", it) }
        dateTo?.let { parameter("date_to", it) }
        shiftUuid?.let { parameter("shift_uuid", it) }
        parameter("page", page)
        parameter("limit", limit)
    }.body()

suspend fun RancakApiService.getCashIn(tenantUuid: String, cashInId: String): ApiResponse<CashInDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.CASH_INS}/$cashInId").body()

suspend fun RancakApiService.createCashIn(
    tenantUuid: String,
    amount: Long,
    source: String,
    description: String,
    note: String? = null,
    cashInDate: String? = null
): ApiResponse<CashInDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.CASH_INS) {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            put("amount", amount)
            put("source", source)
            put("description", description)
            note?.let { put("note", it) }
            cashInDate?.let { put("cash_in_date", it) }
        })
    }.body()

suspend fun RancakApiService.deleteCashIn(tenantUuid: String, cashInId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.CASH_INS}/$cashInId").body()

// ── Expenses ──

suspend fun RancakApiService.getExpenses(
    tenantUuid: String,
    dateFrom: String? = null,
    dateTo: String? = null,
    categoryUuid: String? = null,
    page: Int = 1,
    limit: Int = 50
): ApiResponse<List<ExpenseDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.EXPENSES) {
        dateFrom?.let { parameter("date_from", it) }
        dateTo?.let { parameter("date_to", it) }
        categoryUuid?.let { parameter("category_uuid", it) }
        parameter("page", page)
        parameter("limit", limit)
    }.body()

suspend fun RancakApiService.getExpense(tenantUuid: String, expenseId: String): ApiResponse<ExpenseDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/$expenseId").body()

suspend fun RancakApiService.createExpense(
    tenantUuid: String,
    amount: Long,
    description: String,
    note: String? = null,
    categoryUuid: String? = null,
    expenseDate: String? = null
): ApiResponse<ExpenseDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.EXPENSES) {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            put("amount", amount)
            put("description", description)
            note?.let { put("note", it) }
            categoryUuid?.let { put("category_uuid", it) }
            expenseDate?.let { put("expense_date", it) }
        })
    }.body()

suspend fun RancakApiService.updateExpense(
    tenantUuid: String,
    expenseId: String,
    amount: Long? = null,
    description: String? = null,
    note: String? = null,
    categoryUuid: String? = null,
    expenseDate: String? = null
): ApiResponse<ExpenseDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/$expenseId") {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            amount?.let { put("amount", it) }
            description?.let { put("description", it) }
            note?.let { put("note", it) }
            categoryUuid?.let { put("category_uuid", it) }
            expenseDate?.let { put("expense_date", it) }
        })
    }.body()

suspend fun RancakApiService.deleteExpense(tenantUuid: String, expenseId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/$expenseId").body()

// ── Expense categories CRUD ─────────────────────────────────────────────────

suspend fun RancakApiService.getExpenseCategories(tenantUuid: String): ApiResponse<List<ExpenseCategoryDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/categories").body()

suspend fun RancakApiService.getExpenseCategory(tenantUuid: String, categoryId: String): ApiResponse<ExpenseCategoryDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/categories/$categoryId").body()

suspend fun RancakApiService.createExpenseCategory(
    tenantUuid: String,
    request: CreateExpenseCategoryRequest
): ApiResponse<ExpenseCategoryDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/categories") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateExpenseCategory(
    tenantUuid: String,
    categoryId: String,
    request: UpdateExpenseCategoryRequest
): ApiResponse<ExpenseCategoryDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/categories/$categoryId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteExpenseCategory(
    tenantUuid: String,
    categoryId: String
): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/categories/$categoryId").body()
