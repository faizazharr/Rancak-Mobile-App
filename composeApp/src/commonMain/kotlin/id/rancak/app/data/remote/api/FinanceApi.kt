package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.operations.CashInDto
import id.rancak.app.data.remote.dto.operations.ExpenseDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
    dateTo: String? = null
): ApiResponse<List<CashInDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.CASH_INS) {
        dateFrom?.let { parameter("date_from", it) }
        dateTo?.let { parameter("date_to", it) }
    }.body()

suspend fun RancakApiService.createCashIn(
    tenantUuid: String,
    amount: Long,
    source: String,
    description: String,
    note: String? = null
): ApiResponse<CashInDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.CASH_INS) {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            put("amount", amount)
            put("source", source)
            put("description", description)
            note?.let { put("note", it) }
        })
    }.body()

suspend fun RancakApiService.deleteCashIn(tenantUuid: String, cashInId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.CASH_INS}/$cashInId").body()

// ── Expenses ──

suspend fun RancakApiService.getExpenses(
    tenantUuid: String,
    dateFrom: String? = null,
    dateTo: String? = null
): ApiResponse<List<ExpenseDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.EXPENSES) {
        dateFrom?.let { parameter("date_from", it) }
        dateTo?.let { parameter("date_to", it) }
    }.body()

suspend fun RancakApiService.createExpense(
    tenantUuid: String,
    amount: Long,
    description: String,
    note: String? = null
): ApiResponse<ExpenseDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.EXPENSES) {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            put("amount", amount)
            put("description", description)
            note?.let { put("note", it) }
        })
    }.body()

suspend fun RancakApiService.deleteExpense(tenantUuid: String, expenseId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.EXPENSES}/$expenseId").body()
