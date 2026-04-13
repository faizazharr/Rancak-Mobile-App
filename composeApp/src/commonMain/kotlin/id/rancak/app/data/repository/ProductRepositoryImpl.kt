package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : ProductRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("No tenant selected")

    override suspend fun getProducts(query: String?, categoryId: String?): Resource<List<Product>> {
        return try {
            val response = api.getProducts(tenantUuid, query, categoryId)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Failed to load products")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Resource<Product> {
        return try {
            val response = api.getProductByBarcode(tenantUuid, barcode)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Product not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val response = api.getCategories(tenantUuid)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Failed to load categories")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
