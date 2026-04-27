package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.deviceconfig.AppConfigDto
import id.rancak.app.data.remote.dto.deviceconfig.CreatePrinterConfigRequest
import id.rancak.app.data.remote.dto.deviceconfig.PrinterConfigDto
import id.rancak.app.data.remote.dto.deviceconfig.UpdatePrinterConfigRequest
import id.rancak.app.data.remote.dto.deviceconfig.UpsertAppConfigRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Device-config endpoints — printers (per device JWT-scoped) + app config (key-value).
 * `device_id` diambil otomatis dari JWT, tidak perlu dikirim manual.
 */

private fun base(tenantUuid: String) =
    ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.DEVICE_CONFIG

// ── Printers ────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getPrinters(tenantUuid: String): ApiResponse<List<PrinterConfigDto>> =
    client.get(base(tenantUuid) + "/printers").body()

suspend fun RancakApiService.getAllPrinters(tenantUuid: String): ApiResponse<List<PrinterConfigDto>> =
    client.get(base(tenantUuid) + "/printers/all").body()

suspend fun RancakApiService.getPrinter(
    tenantUuid: String,
    printerId: String
): ApiResponse<PrinterConfigDto> =
    client.get(base(tenantUuid) + "/printers/$printerId").body()

suspend fun RancakApiService.createPrinter(
    tenantUuid: String,
    request: CreatePrinterConfigRequest
): ApiResponse<PrinterConfigDto> =
    client.post(base(tenantUuid) + "/printers") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updatePrinter(
    tenantUuid: String,
    printerId: String,
    request: UpdatePrinterConfigRequest
): ApiResponse<PrinterConfigDto> =
    client.patch(base(tenantUuid) + "/printers/$printerId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deletePrinter(
    tenantUuid: String,
    printerId: String
): ApiResponse<Unit> =
    client.delete(base(tenantUuid) + "/printers/$printerId").body()

// ── App config (key-value) ──────────────────────────────────────────────────

suspend fun RancakApiService.getAppConfig(tenantUuid: String): ApiResponse<List<AppConfigDto>> =
    client.get(base(tenantUuid) + "/app").body()

suspend fun RancakApiService.upsertAppConfig(
    tenantUuid: String,
    key: String,
    value: String
): ApiResponse<AppConfigDto> =
    client.post(base(tenantUuid) + "/app/$key") {
        contentType(ContentType.Application.Json)
        setBody(UpsertAppConfigRequest(value))
    }.body()

suspend fun RancakApiService.deleteAppConfig(
    tenantUuid: String,
    key: String
): ApiResponse<Unit> =
    client.delete(base(tenantUuid) + "/app/$key").body()
