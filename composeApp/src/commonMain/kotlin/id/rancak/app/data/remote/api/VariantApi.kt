package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.admin.CreateVariantGroupRequest
import id.rancak.app.data.remote.dto.admin.CreateVariantRequest
import id.rancak.app.data.remote.dto.admin.UpdateVariantGroupRequest
import id.rancak.app.data.remote.dto.admin.UpdateVariantRequest
import id.rancak.app.data.remote.dto.product.VariantDto
import id.rancak.app.data.remote.dto.product.VariantGroupDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Product variant groups and variant options CRUD.
 */

private fun variantGroupsUrl(tenantUuid: String, productId: String) =
    ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productId/variant-groups"

// ── Variant Groups ─────────────────────────────────────────────────────────────

suspend fun RancakApiService.getVariantGroups(
    tenantUuid: String,
    productId: String
): ApiResponse<List<VariantGroupDto>> =
    client.get(variantGroupsUrl(tenantUuid, productId)).body()

suspend fun RancakApiService.createVariantGroup(
    tenantUuid: String,
    productId: String,
    request: CreateVariantGroupRequest
): ApiResponse<VariantGroupDto> =
    client.post(variantGroupsUrl(tenantUuid, productId)) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateVariantGroup(
    tenantUuid: String,
    productId: String,
    groupId: String,
    request: UpdateVariantGroupRequest
): ApiResponse<VariantGroupDto> =
    client.patch("${variantGroupsUrl(tenantUuid, productId)}/$groupId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteVariantGroup(
    tenantUuid: String,
    productId: String,
    groupId: String
): ApiResponse<Unit> =
    client.delete("${variantGroupsUrl(tenantUuid, productId)}/$groupId").body()

// ── Variants ───────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getVariants(
    tenantUuid: String,
    productId: String,
    groupId: String
): ApiResponse<List<VariantDto>> =
    client.get("${variantGroupsUrl(tenantUuid, productId)}/$groupId/variants").body()

suspend fun RancakApiService.createVariant(
    tenantUuid: String,
    productId: String,
    groupId: String,
    request: CreateVariantRequest
): ApiResponse<VariantDto> =
    client.post("${variantGroupsUrl(tenantUuid, productId)}/$groupId/variants") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateVariant(
    tenantUuid: String,
    productId: String,
    groupId: String,
    variantId: String,
    request: UpdateVariantRequest
): ApiResponse<VariantDto> =
    client.patch("${variantGroupsUrl(tenantUuid, productId)}/$groupId/variants/$variantId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteVariant(
    tenantUuid: String,
    productId: String,
    groupId: String,
    variantId: String
): ApiResponse<Unit> =
    client.delete("${variantGroupsUrl(tenantUuid, productId)}/$groupId/variants/$variantId").body()
