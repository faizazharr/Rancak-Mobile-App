package id.rancak.app.data.remote.api

import io.ktor.client.HttpClient

/**
 * Facade over the Ktor [HttpClient] exposing every Rancak backend call as an
 * extension function on this class. Endpoints are grouped per-domain in
 * sibling files (AuthApi, ProductApi, SaleApi, SyncApi, OperationsApi,
 * PricingApi, FinanceApi, ReportApi).
 *
 * The [client] is internal so extension functions in the same module can use
 * it without exposing it to the public API.
 */
class RancakApiService(internal val client: HttpClient)
