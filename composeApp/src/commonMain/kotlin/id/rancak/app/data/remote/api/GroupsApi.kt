package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.auth.MyTenantDto
import id.rancak.app.data.remote.dto.groups.BranchReportDto
import id.rancak.app.data.remote.dto.groups.GroupDto
import id.rancak.app.data.remote.dto.groups.GroupOverviewDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement

// ── Groups CRUD ───────────────────────────────────────────────────────────────

suspend fun RancakApiService.getGroups(): ApiResponse<List<GroupDto>> =
    client.get(ApiConstants.BASE_URL + "/groups").body()

suspend fun RancakApiService.getGroup(groupUuid: String): ApiResponse<GroupDto> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid").body()

suspend fun RancakApiService.createGroup(name: String, description: String? = null): ApiResponse<GroupDto> =
    client.post(ApiConstants.BASE_URL + "/groups") {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            put("name", name)
            description?.let { put("description", it) }
        })
    }.body()

suspend fun RancakApiService.updateGroup(groupUuid: String, name: String? = null, description: String? = null): ApiResponse<GroupDto> =
    client.put(ApiConstants.BASE_URL + "/groups/$groupUuid") {
        contentType(ContentType.Application.Json)
        setBody(buildMap {
            name?.let { put("name", it) }
            description?.let { put("description", it) }
        })
    }.body()

suspend fun RancakApiService.deleteGroup(groupUuid: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + "/groups/$groupUuid").body()

// ── Group tenant assignment ────────────────────────────────────────────────────

suspend fun RancakApiService.getGroupTenants(groupUuid: String): ApiResponse<List<MyTenantDto>> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/tenants").body()

suspend fun RancakApiService.assignTenantToGroup(groupUuid: String, tenantUuid: String): ApiResponse<Unit> =
    client.post(ApiConstants.BASE_URL + "/groups/$groupUuid/tenants") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("tenant_uuid" to tenantUuid))
    }.body()

suspend fun RancakApiService.removeTenantFromGroup(groupUuid: String, tenantUuid: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + "/groups/$groupUuid/tenants/$tenantUuid").body()

// ── Group reports ─────────────────────────────────────────────────────────────

suspend fun RancakApiService.getGroupOverview(groupUuid: String, start: String? = null, end: String? = null): ApiResponse<GroupOverviewDto> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/overview") {
        start?.let { parameter("start", it) }
        end?.let { parameter("end", it) }
    }.body()

suspend fun RancakApiService.getGroupBranches(groupUuid: String, start: String? = null, end: String? = null): ApiResponse<List<BranchReportDto>> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/branches") {
        start?.let { parameter("start", it) }
        end?.let { parameter("end", it) }
    }.body()

suspend fun RancakApiService.getGroupRevenueSeries(groupUuid: String, start: String? = null, end: String? = null): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/revenue") {
        start?.let { parameter("start", it) }
        end?.let { parameter("end", it) }
    }.body()

suspend fun RancakApiService.getGroupTopProducts(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/products").body()

suspend fun RancakApiService.getGroupPaymentMethods(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/payment-methods").body()

suspend fun RancakApiService.getGroupPeakHours(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/peak-hours").body()

suspend fun RancakApiService.getGroupPnl(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/pnl").body()

suspend fun RancakApiService.getGroupCategories(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/categories").body()

suspend fun RancakApiService.getGroupCashiers(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/cashiers").body()

suspend fun RancakApiService.getGroupDiscounts(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/discounts").body()

suspend fun RancakApiService.getGroupVoidRefund(groupUuid: String): ApiResponse<JsonElement> =
    client.get(ApiConstants.BASE_URL + "/groups/$groupUuid/reports/void-refund").body()
