package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.billing.InvoiceDto
import id.rancak.app.data.remote.dto.billing.PlanDto
import id.rancak.app.data.remote.dto.billing.SubscriptionStateDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

// ── Billing plans (not tenant-scoped) ─────────────────────────────────────────

suspend fun RancakApiService.getBillingPlans(): ApiResponse<List<PlanDto>> =
    client.get(ApiConstants.BASE_URL + "/billing/plans").body()

// ── Tenant subscription ────────────────────────────────────────────────────────

suspend fun RancakApiService.getSubscription(tenantUuid: String): ApiResponse<SubscriptionStateDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/billing/subscription").body()

// ── Invoices ───────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getInvoices(tenantUuid: String): ApiResponse<List<InvoiceDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/billing/invoices").body()

suspend fun RancakApiService.getInvoice(tenantUuid: String, invoiceUuid: String): ApiResponse<InvoiceDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/billing/invoices/$invoiceUuid").body()

suspend fun RancakApiService.createInvoice(tenantUuid: String, planCode: String): ApiResponse<InvoiceDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/billing/invoices") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("plan_code" to planCode))
    }.body()

suspend fun RancakApiService.cancelInvoice(tenantUuid: String, invoiceUuid: String): ApiResponse<Unit> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/billing/invoices/$invoiceUuid/cancel").body()
