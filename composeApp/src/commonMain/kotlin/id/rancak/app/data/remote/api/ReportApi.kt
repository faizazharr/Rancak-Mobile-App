package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.operations.DailyCategoryReportDto
import id.rancak.app.data.remote.dto.operations.ExpiringBatchDto
import id.rancak.app.data.remote.dto.operations.LowStockDto
import id.rancak.app.data.remote.dto.operations.MySalesReportDto
import id.rancak.app.data.remote.dto.operations.StockAlertDto
import id.rancak.app.data.remote.dto.operations.StockReportDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Reporting endpoints — my-sales, stock, low-stock, stock-alerts,
 * expiring batches, daily-by-category.
 */

suspend fun RancakApiService.getMySalesToday(tenantUuid: String): ApiResponse<MySalesReportDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.REPORTS}/my-sales/today").body()

suspend fun RancakApiService.getStockReport(tenantUuid: String): ApiResponse<List<StockReportDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.REPORTS}/stock").body()

suspend fun RancakApiService.getLowStock(tenantUuid: String): ApiResponse<List<LowStockDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.REPORTS}/low-stock").body()

suspend fun RancakApiService.getStockAlerts(tenantUuid: String): ApiResponse<List<StockAlertDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.REPORTS}/stock-alerts").body()

suspend fun RancakApiService.getExpiringBatches(
    tenantUuid: String,
    days: Int = 7
): ApiResponse<List<ExpiringBatchDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.REPORTS}/expiring-batches") {
        parameter("days", days)
    }.body()

suspend fun RancakApiService.getDailyByCategory(
    tenantUuid: String,
    date: String? = null
): ApiResponse<List<DailyCategoryReportDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.REPORTS}/daily-by-category") {
        date?.let { parameter("date", it) }
    }.body()
