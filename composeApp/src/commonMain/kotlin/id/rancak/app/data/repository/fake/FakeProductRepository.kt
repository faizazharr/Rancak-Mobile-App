package id.rancak.app.data.repository.fake

import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.ProductRepository

class FakeProductRepository : ProductRepository {

    override suspend fun getProducts(query: String?, categoryId: String?): Resource<List<Product>> {
        var result = demoProducts
        if (!query.isNullOrBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                it.sku?.lowercase()?.contains(q) == true
            }
        }
        if (!categoryId.isNullOrBlank()) {
            result = result.filter { it.category?.uuid == categoryId }
        }
        return Resource.Success(result)
    }

    override suspend fun getProductByBarcode(barcode: String): Resource<Product> {
        val product = demoProducts.firstOrNull { it.barcode == barcode }
        return if (product != null)
            Resource.Success(product)
        else
            Resource.Error("Produk dengan barcode $barcode tidak ditemukan")
    }

    override suspend fun getCategories(): Resource<List<Category>> =
        Resource.Success(demoCategories)

    override suspend fun getFavoriteProducts(): Resource<List<FavoriteProduct>> =
        Resource.Success(emptyList())

    override suspend fun get86Products(): Resource<List<Product86>> =
        Resource.Success(emptyList())

    override suspend fun mark86(productUuid: String): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun unmark86(productUuid: String): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun getBundles(): Resource<List<Bundle>> =
        Resource.Success(emptyList())

    override suspend fun getModifiers(productUuid: String): Resource<List<Modifier>> =
        Resource.Success(emptyList())
}
