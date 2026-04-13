package id.rancak.app.presentation.util

import kotlin.math.abs

fun formatRupiah(amount: Long): String {
    val prefix = if (amount < 0) "-" else ""
    val absAmount = abs(amount)
    val formatted = absAmount.toString().reversed().chunked(3).joinToString(".").reversed()
    return "${prefix}Rp$formatted"
}
