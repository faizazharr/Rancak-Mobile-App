package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.auth.ChangePasswordRequest
import id.rancak.app.data.remote.dto.auth.GoogleLoginRequest
import id.rancak.app.data.remote.dto.auth.LoginRequest
import id.rancak.app.data.remote.dto.auth.LoginResponse
import id.rancak.app.data.remote.dto.auth.LogoutRequest
import id.rancak.app.data.remote.dto.auth.MyTenantDto
import id.rancak.app.data.remote.dto.auth.ReceiptSettingsDto
import id.rancak.app.data.remote.dto.auth.RefreshTokenRequest
import id.rancak.app.data.remote.dto.auth.SessionDto
import id.rancak.app.data.remote.dto.auth.TenantSettingsDto
import id.rancak.app.data.remote.dto.auth.UserDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Auth, current-user, sessions and tenant-settings endpoints.
 */

suspend fun RancakApiService.login(request: LoginRequest): ApiResponse<LoginResponse> =
    client.post(ApiConstants.BASE_URL + ApiConstants.LOGIN) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.googleLogin(request: GoogleLoginRequest): ApiResponse<LoginResponse> =
    client.post(ApiConstants.BASE_URL + ApiConstants.GOOGLE_LOGIN) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.refreshToken(request: RefreshTokenRequest): ApiResponse<LoginResponse> =
    client.post(ApiConstants.BASE_URL + ApiConstants.REFRESH) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.logout(request: LogoutRequest): ApiResponse<Unit> =
    client.post(ApiConstants.BASE_URL + ApiConstants.LOGOUT) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.getMe(): ApiResponse<UserDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.ME).body()

suspend fun RancakApiService.changePassword(request: ChangePasswordRequest): ApiResponse<Unit> =
    client.post(ApiConstants.BASE_URL + ApiConstants.CHANGE_PASSWORD) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.getSessions(): ApiResponse<List<SessionDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.SESSIONS).body()

suspend fun RancakApiService.revokeSession(sessionId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + "${ApiConstants.SESSIONS}/$sessionId").body()

// ── Tenants ──

suspend fun RancakApiService.getMyTenants(): ApiResponse<List<MyTenantDto>> =
    client.get(ApiConstants.BASE_URL + "/tenants").body()

suspend fun RancakApiService.getTenantSettings(tenantUuid: String): ApiResponse<TenantSettingsDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/settings").body()

suspend fun RancakApiService.getReceiptSettings(tenantUuid: String): ApiResponse<ReceiptSettingsDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/receipt-settings").body()
