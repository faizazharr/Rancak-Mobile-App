package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.sync.CatalogSyncDto
import id.rancak.app.data.remote.dto.sync.SyncStatusDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Offline-first catalog synchronisation endpoints.
 */

suspend fun RancakApiService.syncCatalog(
    tenantUuid: String,
    updatedAfter: String? = null
): ApiResponse<CatalogSyncDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SYNC_CATALOG) {
        updatedAfter?.let { parameter("updated_after", it) }
    }.body()

suspend fun RancakApiService.syncStatus(tenantUuid: String): ApiResponse<SyncStatusDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SYNC_STATUS).body()
