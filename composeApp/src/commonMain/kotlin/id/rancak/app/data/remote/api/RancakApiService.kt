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
 *
 * [clearBearerToken] — invalidate cache token internal Ktor Bearer Auth agar
 * request pertama setelah login ulang selalu memanggil [loadTokens] dan
 * mendapatkan access token terbaru, bukan token lama dari sesi sebelumnya.
 * Dipanggil oleh [AuthRepositoryImpl.clearSessionData] saat logout.
 */
class RancakApiService(
    internal val client: HttpClient,
    private val clearBearerToken: () -> Unit = {}
) {
    fun clearBearerTokenCache() = clearBearerToken()
}
