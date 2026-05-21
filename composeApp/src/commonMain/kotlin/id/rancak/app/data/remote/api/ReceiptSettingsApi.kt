package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.receipt.ReceiptSettingsDto
import id.rancak.app.data.remote.dto.receipt.UpdateReceiptSettingsDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Konfigurasi tampilan struk per tenant.
 * GET  — baca pengaturan struk (logo, footer, separator, social media, dll.)
 * PATCH — update sebagian atau seluruh field (PATCH semantics).
 */

suspend fun RancakApiService.getReceiptSettings(
    tenantUuid: String
): ApiResponse<ReceiptSettingsDto> =
    client.get(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.RECEIPT_SETTINGS
    ).body()

suspend fun RancakApiService.patchReceiptSettings(
    tenantUuid: String,
    body: UpdateReceiptSettingsDto
): ApiResponse<ReceiptSettingsDto> =
    client.patch(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.RECEIPT_SETTINGS
    ) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }.body()
