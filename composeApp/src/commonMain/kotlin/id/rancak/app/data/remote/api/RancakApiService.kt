package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.auth.*
import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.ProductDto
import id.rancak.app.data.remote.dto.sale.*
import id.rancak.app.data.remote.dto.sync.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RancakApiService(private val client: HttpClient) {

    // ── Auth ──

    suspend fun login(request: LoginRequest): ApiResponse<LoginResponse> =
        client.post(ApiConstants.BASE_URL + ApiConstants.LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun googleLogin(request: GoogleLoginRequest): ApiResponse<LoginResponse> =
        client.post(ApiConstants.BASE_URL + ApiConstants.GOOGLE_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<LoginResponse> =
        client.post(ApiConstants.BASE_URL + ApiConstants.REFRESH) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun logout(request: LogoutRequest): ApiResponse<Unit> =
        client.post(ApiConstants.BASE_URL + ApiConstants.LOGOUT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getMe(): ApiResponse<UserDto> =
        client.get(ApiConstants.BASE_URL + ApiConstants.ME).body()

    // ── Products ──

    suspend fun getProducts(
        tenantUuid: String,
        query: String? = null,
        categoryId: String? = null,
        page: Int = 1,
        limit: Int = 50
    ): ApiResponse<List<ProductDto>> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.PRODUCTS) {
            parameter("page", page)
            parameter("limit", limit)
            query?.let { parameter("q", it) }
            categoryId?.let { parameter("category_id", it) }
        }.body()

    suspend fun getProductByUuid(tenantUuid: String, productUuid: String): ApiResponse<ProductDto> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productUuid").body()

    suspend fun getProductByBarcode(tenantUuid: String, barcode: String): ApiResponse<ProductDto> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/barcode/$barcode").body()

    // ── Categories ──

    suspend fun getCategories(tenantUuid: String): ApiResponse<List<CategoryDto>> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.CATEGORIES).body()

    // ── Sales ──

    suspend fun createSale(
        tenantUuid: String,
        request: CreateSaleRequest,
        idempotencyKey: String
    ): ApiResponse<SaleDto> =
        client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SALES) {
            contentType(ContentType.Application.Json)
            header("X-Idempotency-Key", idempotencyKey)
            setBody(request)
        }.body()

    suspend fun getSales(
        tenantUuid: String,
        dateFrom: String? = null,
        dateTo: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): ApiResponse<List<SaleDto>> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SALES) {
            parameter("page", page)
            parameter("limit", limit)
            dateFrom?.let { parameter("date_from", it) }
            dateTo?.let { parameter("date_to", it) }
            status?.let { parameter("status", it) }
        }.body()

    suspend fun getSaleDetail(tenantUuid: String, saleUuid: String): ApiResponse<SaleDto> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid").body()

    suspend fun batchSales(tenantUuid: String, request: BatchSalesRequest): ApiResponse<BatchSalesResponse> =
        client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/batch") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Sync ──

    suspend fun syncCatalog(tenantUuid: String, updatedAfter: String? = null): ApiResponse<CatalogSyncDto> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SYNC_CATALOG) {
            updatedAfter?.let { parameter("updated_after", it) }
        }.body()

    suspend fun syncStatus(tenantUuid: String): ApiResponse<SyncStatusDto> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SYNC_STATUS).body()

    // ── Shifts ──

    suspend fun openShift(tenantUuid: String, openingCash: Long): ApiResponse<ShiftDto> =
        client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/open") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("opening_cash" to openingCash))
        }.body()

    suspend fun closeShift(tenantUuid: String, closingCash: Long, note: String? = null): ApiResponse<ShiftDto> =
        client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/close") {
            contentType(ContentType.Application.Json)
            setBody(buildMap {
                put("closing_cash", closingCash)
                note?.let { put("note", it) }
            })
        }.body()

    suspend fun getCurrentShift(tenantUuid: String): ApiResponse<ShiftDto> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SHIFTS}/current").body()

    // ── Tables ──

    suspend fun getTables(tenantUuid: String): ApiResponse<List<TableDto>> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.TABLES).body()

    // ── KDS ──

    suspend fun getKdsOrders(tenantUuid: String, status: String = "active"): ApiResponse<List<KdsOrderDto>> =
        client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.KDS) {
            parameter("status", status)
        }.body()

    suspend fun updateKdsStatus(tenantUuid: String, kdsUuid: String, status: String): ApiResponse<Unit> =
        client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.KDS}/$kdsUuid") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }.body()

    // ── Sale Actions ──

    suspend fun serveSale(tenantUuid: String, saleUuid: String): ApiResponse<SaleDto> =
        client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/serve").body()

    suspend fun voidSale(tenantUuid: String, saleUuid: String, reason: String? = null): ApiResponse<SaleDto> =
        client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/void") {
            contentType(ContentType.Application.Json)
            setBody(buildMap { reason?.let { put("reason", it) } })
        }.body()

    suspend fun cancelSale(tenantUuid: String, saleUuid: String, reason: String? = null): ApiResponse<SaleDto> =
        client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.SALES}/$saleUuid/cancel") {
            contentType(ContentType.Application.Json)
            setBody(buildMap { reason?.let { put("reason", it) } })
        }.body()
}

@kotlinx.serialization.Serializable
data class KdsOrderDto(
    val uuid: String,
    @kotlinx.serialization.SerialName("invoice_no") val invoiceNo: String? = null,
    @kotlinx.serialization.SerialName("order_type") val orderType: String? = null,
    @kotlinx.serialization.SerialName("table_name") val tableName: String? = null,
    @kotlinx.serialization.SerialName("queue_number") val queueNumber: Int? = null,
    val status: String,
    val items: List<KdsItemDto> = emptyList(),
    @kotlinx.serialization.SerialName("created_at") val createdAt: String? = null
)

@kotlinx.serialization.Serializable
data class KdsItemDto(
    val uuid: String,
    @kotlinx.serialization.SerialName("product_name") val productName: String,
    val qty: String,
    @kotlinx.serialization.SerialName("variant_name") val variantName: String? = null,
    val note: String? = null,
    val status: String = "pending"
)
