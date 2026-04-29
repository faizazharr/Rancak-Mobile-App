package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.cancelInvoice
import id.rancak.app.data.remote.api.createInvoice
import id.rancak.app.data.remote.api.getBillingPlans
import id.rancak.app.data.remote.api.getInvoice
import id.rancak.app.data.remote.api.getInvoices
import id.rancak.app.data.remote.api.getSubscription
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit
import id.rancak.app.domain.model.Invoice
import id.rancak.app.domain.model.Plan
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.SubscriptionState
import id.rancak.app.domain.repository.BillingRepository

class BillingRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : BillingRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getBillingPlans(): Resource<List<Plan>> = safe(
        block = { api.getBillingPlans() },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat daftar paket"
    )

    override suspend fun getSubscription(): Resource<SubscriptionState> = safe(
        block = { api.getSubscription(tenantUuid) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat status berlangganan"
    )

    override suspend fun getInvoices(): Resource<List<Invoice>> = safe(
        block = { api.getInvoices(tenantUuid) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat riwayat invoice"
    )

    override suspend fun getInvoice(invoiceUuid: String): Resource<Invoice> = safe(
        block = { api.getInvoice(tenantUuid, invoiceUuid) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat detail invoice"
    )

    override suspend fun createInvoice(planCode: String): Resource<Invoice> = safe(
        block = { api.createInvoice(tenantUuid, planCode) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat invoice"
    )

    override suspend fun cancelInvoice(invoiceUuid: String): Resource<Unit> = safeUnit(
        block = { api.cancelInvoice(tenantUuid, invoiceUuid) },
        errorMsg = "Gagal membatalkan invoice"
    )
}
