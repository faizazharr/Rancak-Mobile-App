package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.FavoriteProductDto
import id.rancak.app.data.remote.dto.product.Product86Dto
import id.rancak.app.data.remote.dto.product.ProductDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Product catalog, variants, favorites, 86, and categories.
 */

suspend fun RancakApiService.getProducts(
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

suspend fun RancakApiService.getProductByUuid(tenantUuid: String, productUuid: String): ApiResponse<ProductDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productUuid").body()

suspend fun RancakApiService.getProductByBarcode(tenantUuid: String, barcode: String): ApiResponse<ProductDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/barcode/$barcode").body()

suspend fun RancakApiService.getFavoriteProducts(tenantUuid: String): ApiResponse<List<FavoriteProductDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/favorites").body()

suspend fun RancakApiService.get86Products(tenantUuid: String): ApiResponse<List<Product86Dto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/86").body()

suspend fun RancakApiService.mark86(tenantUuid: String, productUuid: String): ApiResponse<Unit> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/86") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("product_uuid" to productUuid))
    }.body()

suspend fun RancakApiService.unmark86(tenantUuid: String, productUuid: String): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/86/$productUuid").body()

// ── Categories ──

suspend fun RancakApiService.getCategories(tenantUuid: String): ApiResponse<List<CategoryDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.CATEGORIES).body()
