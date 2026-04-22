package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.operations.BundleDto
import id.rancak.app.data.remote.dto.operations.DiscountPreviewDto
import id.rancak.app.data.remote.dto.operations.ModifierDto
import id.rancak.app.data.remote.dto.operations.VoucherValidationDto
import id.rancak.app.data.remote.dto.sync.DiscountRuleDto
import id.rancak.app.data.remote.dto.sync.SurchargeDto
import id.rancak.app.data.remote.dto.sync.TaxConfigDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Pricing-related catalogue: surcharges, tax configs, vouchers, discount
 * rules, bundles, and modifiers.
 */

// ── Surcharges & tax ──

suspend fun RancakApiService.getSurcharges(tenantUuid: String): ApiResponse<List<SurchargeDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SURCHARGES).body()

suspend fun RancakApiService.getTaxConfigs(tenantUuid: String): ApiResponse<List<TaxConfigDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.TAX_CONFIGS).body()

// ── Vouchers ──

suspend fun RancakApiService.validateVoucher(
    tenantUuid: String,
    code: String,
    subtotal: Long
): ApiResponse<VoucherValidationDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.VOUCHERS}/validate") {
        parameter("code", code)
        parameter("subtotal", subtotal)
    }.body()

// ── Discount Rules ──

suspend fun RancakApiService.getDiscountRules(tenantUuid: String): ApiResponse<List<DiscountRuleDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.DISCOUNT_RULES).body()

suspend fun RancakApiService.previewDiscount(
    tenantUuid: String,
    total: Long
): ApiResponse<DiscountPreviewDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.DISCOUNT_RULES}/preview") {
        parameter("total", total)
    }.body()

// ── Bundles & Modifiers ──

suspend fun RancakApiService.getBundles(tenantUuid: String): ApiResponse<List<BundleDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.BUNDLES).body()

suspend fun RancakApiService.getModifiers(tenantUuid: String): ApiResponse<List<ModifierDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.MODIFIERS).body()

suspend fun RancakApiService.getProductModifiers(
    tenantUuid: String,
    productUuid: String
): ApiResponse<List<ModifierDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.MODIFIERS}/product/$productUuid").body()
