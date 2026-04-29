package id.rancak.app.data.local

import id.rancak.app.domain.model.CartItem
import kotlinx.serialization.Serializable

/**
 * Open bill yang disimpan secara lokal di perangkat kasir.
 * Tidak memerlukan koneksi internet — hanya disimpan via [OpenBillStore].
 */
@Serializable
data class LocalOpenBill(
    val id: String,
    /** Nama tagihan yang dimasukkan kasir, misal "Meja 5 - Andi". */
    val name: String,
    val items: List<LocalOpenBillItem>,
    val orderType: String = "DINE_IN",
    val tableUuid: String? = null,
    val customerName: String = "",
    val note: String = "",
    val pax: Int = 1,
    val discountInput: Long = 0,
    val discountIsPercent: Boolean = false,
    val taxInput: Long = 0,
    val taxIsPercent: Boolean = false,
    val adminFeeInput: Long = 0,
    val adminFeeIsPercent: Boolean = false,
    val deliveryFee: Long = 0,
    val tip: Long = 0,
    val voucherCode: String = "",
    /** Epoch millis saat open bill pertama kali dibuat. */
    val createdAt: Long = 0L
) {
    val subtotal: Long get() = items.sumOf { it.price * it.qty }
    val itemCount: Int get() = items.sumOf { it.qty }
}

@Serializable
data class LocalOpenBillItem(
    val productUuid: String,
    val productName: String,
    val qty: Int,
    val price: Long,
    val variantUuid: String? = null,
    val variantName: String? = null,
    val note: String? = null,
    val imageUrl: String? = null
)

fun LocalOpenBillItem.toDomain() = CartItem(
    productUuid = productUuid,
    productName = productName,
    qty         = qty,
    price       = price,
    variantUuid = variantUuid,
    variantName = variantName,
    note        = note,
    imageUrl    = imageUrl
)

fun CartItem.toLocalOpenBillItem() = LocalOpenBillItem(
    productUuid = productUuid,
    productName = productName,
    qty         = qty,
    price       = price,
    variantUuid = variantUuid,
    variantName = variantName,
    note        = note,
    imageUrl    = imageUrl
)
