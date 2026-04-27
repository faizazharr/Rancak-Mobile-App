package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.operations.OrderBoardOrderDto
import id.rancak.app.data.remote.dto.operations.ShiftSummaryDto
import id.rancak.app.data.remote.dto.sync.ShiftDto
import id.rancak.app.data.remote.dto.sync.TableDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Operational endpoints: shifts, tables, KDS, order board.
 */

// ── Shifts ──

suspend fun RancakApiService.openShift(tenantUuid: String, openingCash: String): ApiResponse<ShiftDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/open") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("opening_cash" to openingCash))
    }.body()

suspend fun RancakApiService.closeShift(
    tenantUuid: String,
    closingCash: String,
    note: String? = null
): ApiResponse<ShiftDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/close") {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            put("closing_cash", closingCash)
            note?.let { put("note", it) }
        })
    }.body()

suspend fun RancakApiService.getCurrentShift(tenantUuid: String): ApiResponse<ShiftDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/current").body()

suspend fun RancakApiService.getShiftSummary(tenantUuid: String): ApiResponse<ShiftSummaryDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/current/summary").body()

suspend fun RancakApiService.getShiftSummaryById(
    tenantUuid: String,
    shiftUuid: String
): ApiResponse<ShiftSummaryDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/$shiftUuid/summary").body()

// ── Tables ──

suspend fun RancakApiService.getTables(tenantUuid: String): ApiResponse<List<TableDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.TABLES).body()

// ── KDS ──

suspend fun RancakApiService.getKdsOrders(
    tenantUuid: String,
    status: String = "active"
): ApiResponse<List<KdsOrderDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.KDS) {
        parameter("status", status)
    }.body()

suspend fun RancakApiService.updateKdsStatus(
    tenantUuid: String,
    kdsUuid: String,
    status: String
): ApiResponse<Unit> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.KDS}/$kdsUuid") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("status" to status))
    }.body()

// ── Order Board ──

suspend fun RancakApiService.getOrderBoard(
    tenantUuid: String,
    date: String? = null,
    includeDone: Boolean = false
): ApiResponse<List<OrderBoardOrderDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.ORDER_BOARD) {
        date?.let { parameter("date", it) }
        if (includeDone) parameter("include_done", true)
    }.body()

// ── Cash count (rekonsiliasi kas) ───────────────────────────────────────────

/** Riwayat cash count untuk shift tertentu. */
suspend fun RancakApiService.getCashCounts(
    tenantUuid: String,
    shiftUuid: String
): ApiResponse<List<id.rancak.app.data.remote.dto.operations.CashCountDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/$shiftUuid/cash-count")
        .body()

/** Submit hasil hitung kas fisik. */
suspend fun RancakApiService.submitCashCount(
    tenantUuid: String,
    shiftUuid: String,
    request: id.rancak.app.data.remote.dto.operations.SubmitCashCountRequest
): ApiResponse<id.rancak.app.data.remote.dto.operations.CashCountDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/$shiftUuid/cash-count") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

// ── KDS: detail + item-level update ─────────────────────────────────────────

suspend fun RancakApiService.getKdsDetail(
    tenantUuid: String,
    kdsUuid: String
): ApiResponse<KdsOrderDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.KDS}/$kdsUuid").body()

/** Update status item dapur — order status di-recalculate otomatis. */
suspend fun RancakApiService.updateKdsItemStatus(
    tenantUuid: String,
    kdsUuid: String,
    itemUuid: String,
    status: String
): ApiResponse<Unit> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.KDS}/$kdsUuid/items/$itemUuid") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("status" to status))
    }.body()
