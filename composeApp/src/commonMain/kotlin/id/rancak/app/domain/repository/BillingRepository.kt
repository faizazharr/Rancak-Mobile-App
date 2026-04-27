package id.rancak.app.domain.repository

import id.rancak.app.domain.model.Invoice
import id.rancak.app.domain.model.Plan
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.SubscriptionState

interface BillingRepository {
    suspend fun getBillingPlans(): Resource<List<Plan>>
    suspend fun getSubscription(): Resource<SubscriptionState>
    suspend fun getInvoices(): Resource<List<Invoice>>
    suspend fun getInvoice(invoiceUuid: String): Resource<Invoice>
    suspend fun createInvoice(planCode: String): Resource<Invoice>
    suspend fun cancelInvoice(invoiceUuid: String): Resource<Unit>
}
