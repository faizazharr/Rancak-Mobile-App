package id.rancak.app.data.repository

import id.rancak.app.data.local.db.dao.CartDao
import id.rancak.app.data.local.db.entity.CartItemEntity
import id.rancak.app.data.local.db.entity.toDomain
import id.rancak.app.data.local.db.entity.toEntity
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepositoryImpl(private val cartDao: CartDao) : CartRepository {

    override fun observeItems(): Flow<List<CartItem>> =
        cartDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addOrIncrement(product: Product, variantUuid: String?, variantName: String?) {
        val id = cartItemId(product.uuid, variantUuid)
        val existing = cartDao.findById(id)
        cartDao.upsert(
            if (existing != null) {
                existing.copy(qty = existing.qty + 1)
            } else {
                CartItemEntity(
                    id          = id,
                    productUuid = product.uuid,
                    productName = product.name,
                    qty         = 1,
                    price       = product.price,
                    variantUuid = variantUuid,
                    variantName = variantName,
                    note        = null,
                    imageUrl    = product.imageUrl
                )
            }
        )
    }

    override suspend fun updateQuantity(productUuid: String, variantUuid: String?, qty: Int) {
        val id = cartItemId(productUuid, variantUuid)
        if (qty <= 0) cartDao.deleteById(id) else cartDao.updateQty(id, qty)
    }

    override suspend fun updateNote(productUuid: String, variantUuid: String?, note: String) {
        cartDao.updateNote(cartItemId(productUuid, variantUuid), note.ifBlank { null })
    }

    override suspend fun removeItem(productUuid: String, variantUuid: String?) {
        cartDao.deleteById(cartItemId(productUuid, variantUuid))
    }

    override suspend fun clearAll() {
        cartDao.deleteAll()
    }

    override suspend fun replaceAll(items: List<CartItem>) {
        cartDao.deleteAll()
        cartDao.upsertAll(items.map { it.toEntity() })
    }

    private fun cartItemId(productUuid: String, variantUuid: String?) =
        "$productUuid:${variantUuid ?: "_"}"
}
