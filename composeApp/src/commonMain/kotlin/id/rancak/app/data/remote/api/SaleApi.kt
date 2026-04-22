package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.operations.ReceiptDto
import id.rancak.app.data.remote.dto.sale.BatchSalesRequest
import id.rancak.app.data.remote.dto.sale.BatchSalesResponse
import id.rancak.app.data.remote.dto.sale.CreateSaleRequest
import id.rancak.app.data.remote.dto.sale.PayHeldOrderRequest
import id.rancak.app.data.remote.dto.sale.QrPaymentDto
import id.rancak.app.data.remote.dto.sale.RefundRequest
import id.rancak.app.data.remote.dto.sale.RefundResponseDto
import id.rancak.app.data.remote.dto.sale.SaleDto
import id.rancak.app.data.remote.dto.sale.SplitBillRequest
import id.rancak.app.data.remote.dto.sale.SplitBillResponseDto
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Sales lifecycle — create, list, hold-pay, serve, cancel, void, refund,
 * move-table, QR payment, and receipt/ESC-POS download endpoints.
 */

suspend fun RancakApiService.createSale(
    tenantUuid: String,
    request: CreateSaleRequest,
    idempotencyKey: String
): ApiResponse<SaleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SALES) {
        contentType(ContentType.Application.Json)
        header("X-Idempotency-Key", idempotencyKey)
        setBody(request)
    }.body()

suspend fun RancakApiService.getSales(
    tenantUuid: String,
    dateFrom: String? = null,
    dateTo: String? = null,
    status: String? = null,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<List<SaleDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SALES) {
        parameter("page", page)
        parameter("limit", limit)
        dateFrom?.let { parameter("date_from", it) }
        dateTo?.let { parameter("date_to", it) }
        status?.let { parameter("status", it) }
    }.body()

suspend fun RancakApiService.getSaleDetail(tenantUuid: String, saleUuid: String): ApiResponse<SaleDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid").body()

suspend fun RancakApiService.batchSales(tenantUuid: String, request: BatchSalesRequest): ApiResponse<BatchSalesResponse> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/batch") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.payHeldOrder(
    tenantUuid: String,
    saleUuid: String,
    request: PayHeldOrderRequest
): ApiResponse<SaleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/pay") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.serveSale(tenantUuid: String, saleUuid: String): ApiResponse<SaleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/serve").body()

suspend fun RancakApiService.cancelSale(tenantUuid: String, saleUuid: String, reason: String? = null): ApiResponse<SaleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/cancel") {
        contentType(ContentType.Application.Json)
        setBody(buildMap { reason?.let { put("reason", it) } })
    }.body()

suspend fun RancakApiService.voidSale(tenantUuid: String, saleUuid: String, reason: String? = null): ApiResponse<SaleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/void") {
        contentType(ContentType.Application.Json)
        setBody(buildMap { reason?.let { put("reason", it) } })
    }.body()

suspend fun RancakApiService.refundSale(
    tenantUuid: String,
    saleUuid: String,
    request: RefundRequest
): ApiResponse<RefundResponseDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/refund") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.moveTable(tenantUuid: String, saleUuid: String, tableUuid: String): ApiResponse<SaleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/move-table") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("table_uuid" to tableUuid))
    }.body()

// ── Split Bill ──

/** Pisahkan beberapa item dari held order ke transaksi baru (POST /sales/:id/split). */
suspend fun RancakApiService.splitBill(
    tenantUuid: String,
    saleUuid: String,
    request: SplitBillRequest
): ApiResponse<SplitBillResponseDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/split") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

// ── QR payments ──
suspend fun RancakApiService.createQrPayment(tenantUuid: String, saleUuid: String): ApiResponse<QrPaymentDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.qrPayment(saleUuid)) {
        contentType(ContentType.Application.Json)
    }.body()

/** Poll current QR payment status for a sale. */
suspend fun RancakApiService.getQrPaymentStatus(tenantUuid: String, saleUuid: String): ApiResponse<QrPaymentDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.qrPayment(saleUuid)).body()

// ── Receipt & ESC/POS ──

suspend fun RancakApiService.getSaleReceipt(tenantUuid: String, saleUuid: String): ApiResponse<ReceiptDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/receipt").body()

/**
 * Get pre-built ESC/POS cashier receipt bytes from server.
 * Returns raw binary data ready to send to printer.
 */
suspend fun RancakApiService.getReceiptEscpos(tenantUuid: String, saleUuid: String): ByteArray =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.receiptEscpos(saleUuid)) {
        accept(ContentType.Application.OctetStream)
    }.body()

/**
 * Get pre-built ESC/POS kitchen ticket (KOT) bytes from server.
 * Returns raw binary data ready to send to kitchen printer.
 */
suspend fun RancakApiService.getReceiptKitchen(tenantUuid: String, saleUuid: String): ByteArray =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.receiptKitchen(saleUuid)) {
        accept(ContentType.Application.OctetStream)
    }.body()

/**
 * Get combined KOT + cashier receipt bytes for single-printer mode.
 * @param kotFirst true = KOT first then cashier, false = cashier first then KOT
 */
suspend fun RancakApiService.getReceiptCombined(tenantUuid: String, saleUuid: String, kotFirst: Boolean = true): ByteArray =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.receiptCombined(saleUuid)) {
        accept(ContentType.Application.OctetStream)
        parameter("kot_first", kotFirst)
    }.body()
