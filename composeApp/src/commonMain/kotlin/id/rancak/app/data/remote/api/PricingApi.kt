package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.admin.CreateBundleRequest
import id.rancak.app.data.remote.dto.admin.CreateDiscountRuleRequest
import id.rancak.app.data.remote.dto.admin.CreateModifierRequest
import id.rancak.app.data.remote.dto.admin.CreateSurchargeRequest
import id.rancak.app.data.remote.dto.admin.CreateTaxConfigRequest
import id.rancak.app.data.remote.dto.admin.CreateVoucherRequest
import id.rancak.app.data.remote.dto.admin.UpdateBundleRequest
import id.rancak.app.data.remote.dto.admin.UpdateDiscountRuleRequest
import id.rancak.app.data.remote.dto.admin.UpdateModifierRequest
import id.rancak.app.data.remote.dto.admin.UpdateSurchargeRequest
import id.rancak.app.data.remote.dto.admin.UpdateTaxConfigRequest
import id.rancak.app.data.remote.dto.admin.UpdateVoucherRequest
import id.rancak.app.data.remote.dto.operations.BundleDto
import id.rancak.app.data.remote.dto.operations.DiscountPreviewDto
import id.rancak.app.data.remote.dto.operations.ModifierDto
import id.rancak.app.data.remote.dto.operations.VoucherDto
import id.rancak.app.data.remote.dto.operations.VoucherValidationDto
import id.rancak.app.data.remote.dto.sync.DiscountRuleDto
import id.rancak.app.data.remote.dto.sync.SurchargeDto
import id.rancak.app.data.remote.dto.sync.TaxConfigDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Pricing-related catalogue: surcharges, tax configs, vouchers, discount
 * rules, bundles, and modifiers.
 */

// ── Surcharges ─────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getSurcharges(tenantUuid: String): ApiResponse<List<SurchargeDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SURCHARGES).body()

suspend fun RancakApiService.getSurcharge(tenantUuid: String, surchargeId: String): ApiResponse<SurchargeDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SURCHARGES}/$surchargeId").body()

suspend fun RancakApiService.createSurcharge(
    tenantUuid: String,
    request: CreateSurchargeRequest
): ApiResponse<SurchargeDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SURCHARGES) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateSurcharge(
    tenantUuid: String,
    surchargeId: String,
    request: UpdateSurchargeRequest
): ApiResponse<SurchargeDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SURCHARGES}/$surchargeId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteSurcharge(tenantUuid: String, surchargeId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SURCHARGES}/$surchargeId").body()

// ── Tax Configs ────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getTaxConfigs(tenantUuid: String): ApiResponse<List<TaxConfigDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.TAX_CONFIGS).body()

suspend fun RancakApiService.getTaxConfig(tenantUuid: String, configId: String): ApiResponse<TaxConfigDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.TAX_CONFIGS}/$configId").body()

suspend fun RancakApiService.createTaxConfig(
    tenantUuid: String,
    request: CreateTaxConfigRequest
): ApiResponse<TaxConfigDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.TAX_CONFIGS) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateTaxConfig(
    tenantUuid: String,
    configId: String,
    request: UpdateTaxConfigRequest
): ApiResponse<TaxConfigDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.TAX_CONFIGS}/$configId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteTaxConfig(tenantUuid: String, configId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.TAX_CONFIGS}/$configId").body()

// ── Vouchers ───────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getVouchers(
    tenantUuid: String,
    isActive: Boolean? = null,
    page: Int = 1,
    limit: Int = 50
): ApiResponse<List<VoucherDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.VOUCHERS) {
        isActive?.let { parameter("is_active", it) }
        parameter("page", page)
        parameter("limit", limit)
    }.body()

suspend fun RancakApiService.getVoucher(tenantUuid: String, voucherId: String): ApiResponse<VoucherDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.VOUCHERS}/$voucherId").body()

suspend fun RancakApiService.validateVoucher(
    tenantUuid: String,
    code: String,
    subtotal: Long
): ApiResponse<VoucherValidationDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.VOUCHERS}/validate") {
        parameter("code", code)
        parameter("subtotal", subtotal)
    }.body()

suspend fun RancakApiService.createVoucher(
    tenantUuid: String,
    request: CreateVoucherRequest
): ApiResponse<VoucherDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.VOUCHERS) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateVoucher(
    tenantUuid: String,
    voucherId: String,
    request: UpdateVoucherRequest
): ApiResponse<VoucherDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.VOUCHERS}/$voucherId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteVoucher(tenantUuid: String, voucherId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.VOUCHERS}/$voucherId").body()

// ── Discount Rules ─────────────────────────────────────────────────────────────

suspend fun RancakApiService.getDiscountRules(tenantUuid: String): ApiResponse<List<DiscountRuleDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.DISCOUNT_RULES).body()

suspend fun RancakApiService.getDiscountRule(tenantUuid: String, ruleId: String): ApiResponse<DiscountRuleDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.DISCOUNT_RULES}/$ruleId").body()

suspend fun RancakApiService.previewDiscount(
    tenantUuid: String,
    total: Long
): ApiResponse<DiscountPreviewDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.DISCOUNT_RULES}/preview") {
        parameter("total", total)
    }.body()

suspend fun RancakApiService.createDiscountRule(
    tenantUuid: String,
    request: CreateDiscountRuleRequest
): ApiResponse<DiscountRuleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.DISCOUNT_RULES) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateDiscountRule(
    tenantUuid: String,
    ruleId: String,
    request: UpdateDiscountRuleRequest
): ApiResponse<DiscountRuleDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.DISCOUNT_RULES}/$ruleId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteDiscountRule(tenantUuid: String, ruleId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.DISCOUNT_RULES}/$ruleId").body()

// ── Bundles ────────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getBundles(tenantUuid: String): ApiResponse<List<BundleDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.BUNDLES).body()

suspend fun RancakApiService.getBundle(tenantUuid: String, bundleId: String): ApiResponse<BundleDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.BUNDLES}/$bundleId").body()

suspend fun RancakApiService.createBundle(
    tenantUuid: String,
    request: CreateBundleRequest
): ApiResponse<BundleDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.BUNDLES) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateBundle(
    tenantUuid: String,
    bundleId: String,
    request: UpdateBundleRequest
): ApiResponse<BundleDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.BUNDLES}/$bundleId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteBundle(tenantUuid: String, bundleId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.BUNDLES}/$bundleId").body()

// ── Modifiers ──────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getModifiers(tenantUuid: String): ApiResponse<List<ModifierDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.MODIFIERS).body()

suspend fun RancakApiService.getModifier(tenantUuid: String, modifierId: String): ApiResponse<ModifierDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.MODIFIERS}/$modifierId").body()

suspend fun RancakApiService.getProductModifiers(
    tenantUuid: String,
    productUuid: String
): ApiResponse<List<ModifierDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.MODIFIERS}/product/$productUuid").body()

suspend fun RancakApiService.createModifier(
    tenantUuid: String,
    request: CreateModifierRequest
): ApiResponse<ModifierDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.MODIFIERS) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.createProductModifier(
    tenantUuid: String,
    productUuid: String,
    request: CreateModifierRequest
): ApiResponse<ModifierDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.MODIFIERS}/product/$productUuid") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateModifier(
    tenantUuid: String,
    modifierId: String,
    request: UpdateModifierRequest
): ApiResponse<ModifierDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.MODIFIERS}/$modifierId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteModifier(tenantUuid: String, modifierId: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.MODIFIERS}/$modifierId").body()
