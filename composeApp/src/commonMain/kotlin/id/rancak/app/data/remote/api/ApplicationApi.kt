package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.auth.SubmitApplicationRequest
import id.rancak.app.data.remote.dto.auth.TenantApplicationDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Pengajuan outlet (Applications) endpoints.
 *
 * Sejak revisi 2026-04 backend, `POST /applications` bersifat **auto-approve** —
 * outlet langsung dibuat dengan demo trial 14 hari.
 */

suspend fun RancakApiService.submitApplication(
    request: SubmitApplicationRequest
): ApiResponse<TenantApplicationDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.APPLICATIONS) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.getMyApplications(): ApiResponse<List<TenantApplicationDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.APPLICATIONS_ME).body()
