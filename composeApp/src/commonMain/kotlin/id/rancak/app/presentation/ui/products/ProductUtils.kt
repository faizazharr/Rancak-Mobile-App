package id.rancak.app.presentation.ui.products

import kotlin.random.Random

/** Menampilkan stok tanpa desimal jika nilai bulat (mis. 10.0 → "10", 10.5 → "10.5"). */
internal fun Double.toStockDisplay(): String =
    if (this == toLong().toDouble()) toLong().toString() else toString()

/**
 * Generate SKU dari inisial nama produk + 4 digit acak.
 * Contoh: "Nasi Goreng Spesial" → "NGS-4821"
 */
internal fun generateSku(name: String): String {
    val prefix = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(3)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "PRD" }
    val suffix = Random.nextInt(1000, 9999)
    return "$prefix-$suffix"
}

/** Generate barcode numerik 12 digit (EAN-12 style). */
internal fun generateBarcode(): String =
    Random.nextLong(100_000_000_000L, 999_999_999_999L).toString()
