package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface ProductRepository {
    suspend fun getProducts(query: String? = null, categoryId: String? = null): Resource<List<Product>>
    suspend fun getProductByUuid(productUuid: String): Resource<Product>
    suspend fun getProductByBarcode(barcode: String): Resource<Product>
    suspend fun getCategories(): Resource<List<Category>>
    suspend fun getFavoriteProducts(): Resource<List<FavoriteProduct>>
    suspend fun get86Products(): Resource<List<Product86>>
    suspend fun mark86(productUuid: String): Resource<Unit>
    suspend fun unmark86(productUuid: String): Resource<Unit>
    suspend fun getBundles(): Resource<List<Bundle>>
    suspend fun getModifiers(productUuid: String): Resource<List<Modifier>>
}
