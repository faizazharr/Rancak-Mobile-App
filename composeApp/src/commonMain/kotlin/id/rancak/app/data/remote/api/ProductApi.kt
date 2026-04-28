package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.admin.CreateCategoryRequest
import id.rancak.app.data.remote.dto.admin.CreateProductBatchRequest
import id.rancak.app.data.remote.dto.admin.CreateProductRequest
import id.rancak.app.data.remote.dto.admin.StockAdjustmentRequest
import id.rancak.app.data.remote.dto.admin.StockAdjustmentResponseDto
import id.rancak.app.data.remote.dto.admin.UpdateCategoryRequest
import id.rancak.app.data.remote.dto.admin.UpdateProductRequest
import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.FavoriteProductDto
import id.rancak.app.data.remote.dto.product.Product86Dto
import id.rancak.app.data.remote.dto.product.ProductBatchDto
import id.rancak.app.data.remote.dto.product.ProductDto
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

suspend fun RancakApiService.getCategory(tenantUuid: String, categoryId: String): ApiResponse<CategoryDto> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.CATEGORIES}/$categoryId").body()

// ── Stock & Batches ──

suspend fun RancakApiService.adjustStock(
    tenantUuid: String,
    productId: String,
    request: StockAdjustmentRequest
): ApiResponse<StockAdjustmentResponseDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productId/stock-adjustment") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.getProductBatches(
    tenantUuid: String,
    productId: String
): ApiResponse<List<ProductBatchDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productId/batches").body()

suspend fun RancakApiService.createProductBatch(
    tenantUuid: String,
    productId: String,
    request: CreateProductBatchRequest
): ApiResponse<ProductBatchDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productId/batches") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

// ── Product CRUD ────────────────────────────────────────────────────────────────

suspend fun RancakApiService.createProduct(
    tenantUuid: String,
    request: CreateProductRequest
): ApiResponse<ProductDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.PRODUCTS) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateProduct(
    tenantUuid: String,
    productId: String,
    request: UpdateProductRequest
): ApiResponse<ProductDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteProduct(
    tenantUuid: String,
    productId: String
): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productId").body()

// ── Category CRUD ───────────────────────────────────────────────────────────────

suspend fun RancakApiService.createCategory(
    tenantUuid: String,
    request: CreateCategoryRequest
): ApiResponse<CategoryDto> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.CATEGORIES) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.updateCategory(
    tenantUuid: String,
    categoryId: String,
    request: UpdateCategoryRequest
): ApiResponse<CategoryDto> =
    client.patch(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.CATEGORIES}/$categoryId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

suspend fun RancakApiService.deleteCategory(
    tenantUuid: String,
    categoryId: String
): ApiResponse<Unit> =
    client.delete(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.CATEGORIES}/$categoryId").body()
