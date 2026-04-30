package id.rancak.app.domain.repository

import id.rancak.app.domain.model.Bundle
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.FavoriteProduct
import id.rancak.app.domain.model.Modifier
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.Product86
import id.rancak.app.domain.model.Resource

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
