package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface ProductRepository {
    suspend fun getProducts(query: String? = null, categoryId: String? = null): Resource<List<Product>>
    suspend fun getProductByBarcode(barcode: String): Resource<Product>
    suspend fun getCategories(): Resource<List<Category>>
}
