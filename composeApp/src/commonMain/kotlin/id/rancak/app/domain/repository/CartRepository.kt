package id.rancak.app.domain.repository

import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak domain untuk keranjang belanja.
 *
 * Semua operasi Cart mengalir melalui antarmuka ini sehingga lapisan presentasi
 * tidak pernah bergantung langsung pada Room DAO maupun entitas database.
 */
interface CartRepository {
    /** Stream reaktif seluruh item keranjang — diperbarui otomatis saat data berubah. */
    fun observeItems(): Flow<List<CartItem>>

    /**
     * Tambah [product] ke keranjang. Jika sudah ada (same productUuid + variantUuid),
     * qty-nya dinaikkan 1.
     */
    suspend fun addOrIncrement(product: Product, variantUuid: String? = null, variantName: String? = null)

    /**
     * Set qty item secara eksplisit. Jika [qty] ≤ 0, item dihapus dari keranjang.
     */
    suspend fun updateQuantity(productUuid: String, variantUuid: String?, qty: Int)

    /** Perbarui catatan (note) untuk satu item. String kosong disimpan sebagai null. */
    suspend fun updateNote(productUuid: String, variantUuid: String?, note: String)

    /** Hapus satu item dari keranjang. */
    suspend fun removeItem(productUuid: String, variantUuid: String?)

    /** Kosongkan seluruh keranjang. */
    suspend fun clearAll()

    /** Ganti semua item keranjang dengan [items] yang diberikan secara atomik. */
    suspend fun replaceAll(items: List<CartItem>)
}
