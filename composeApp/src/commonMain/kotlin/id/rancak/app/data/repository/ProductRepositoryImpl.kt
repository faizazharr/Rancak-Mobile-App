package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.db.dao.CategoryDao
import id.rancak.app.data.local.db.dao.ProductDao
import id.rancak.app.data.local.db.entity.toDomain
import id.rancak.app.data.local.db.entity.toEntity
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager,
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) : ProductRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("No tenant selected")

    override suspend fun getProducts(query: String?, categoryId: String?): Resource<List<Product>> {
        val isFiltered = !query.isNullOrBlank() || !categoryId.isNullOrBlank()

        return try {
            val response = api.getProducts(tenantUuid, query, categoryId)
            if (response.isSuccess && response.data != null) {
                val products = response.data.map { it.toDomain() }
                // Only cache unfiltered full-list responses
                if (!isFiltered) {
                    productDao.deleteAll()
                    productDao.upsertAll(products.map { it.toEntity() })
                }
                Resource.Success(products)
            } else {
                serveCachedProducts(query, categoryId, response.message)
            }
        } catch (e: Exception) {
            // Network unavailable — serve Room cache so the app works offline
            serveCachedProducts(query, categoryId, e.message)
        }
    }

    private suspend fun serveCachedProducts(
        query: String?,
        categoryId: String?,
        errorMessage: String?
    ): Resource<List<Product>> {
        val isFiltered = !query.isNullOrBlank() || !categoryId.isNullOrBlank()
        val cached = if (isFiltered) {
            productDao.search(query ?: "", categoryId ?: "")
        } else {
            productDao.getAll()
        }
        return if (cached.isNotEmpty()) {
            Resource.Success(cached.map { it.toDomain() })
        } else {
            Resource.Error(errorMessage ?: "Tidak ada koneksi internet")
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Resource<Product> {
        return try {
            val response = api.getProductByBarcode(tenantUuid, barcode)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                // Fallback to local cache
                val local = productDao.findByBarcode(barcode)
                if (local != null) Resource.Success(local.toDomain())
                else Resource.Error(response.message ?: "Produk tidak ditemukan")
            }
        } catch (e: Exception) {
            val local = productDao.findByBarcode(barcode)
            if (local != null) Resource.Success(local.toDomain())
            else Resource.Error(e.message ?: "Tidak ada koneksi internet")
        }
    }

    override suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val response = api.getCategories(tenantUuid)
            if (response.isSuccess && response.data != null) {
                val categories = response.data.map { it.toDomain() }
                categoryDao.deleteAll()
                categoryDao.upsertAll(categories.map { it.toEntity() })
                Resource.Success(categories)
            } else {
                val cached = categoryDao.getAll()
                if (cached.isNotEmpty()) Resource.Success(cached.map { it.toDomain() })
                else Resource.Error(response.message ?: "Gagal memuat kategori")
            }
        } catch (e: Exception) {
            val cached = categoryDao.getAll()
            if (cached.isNotEmpty()) Resource.Success(cached.map { it.toDomain() })
            else Resource.Error(e.message ?: "Tidak ada koneksi internet")
        }
    }

    override suspend fun getFavoriteProducts(): Resource<List<FavoriteProduct>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getFavoriteProducts(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil produk favorit")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun get86Products(): Resource<List<Product86>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.get86Products(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil produk 86")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun mark86(productUuid: String): Resource<Unit> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.mark86(tenantUuid, productUuid)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal menandai produk 86")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun unmark86(productUuid: String): Resource<Unit> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.unmark86(tenantUuid, productUuid)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal membatalkan produk 86")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getBundles(): Resource<List<Bundle>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getBundles(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil data bundle")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getModifiers(productUuid: String): Resource<List<Modifier>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getProductModifiers(tenantUuid, productUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil modifier")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}


