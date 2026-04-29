package id.rancak.app.presentation.util

import kotlin.math.abs

fun formatRupiah(amount: Long): String {
    val prefix = if (amount < 0) "-" else ""
    val absAmount = abs(amount)
    val formatted = absAmount.toString().reversed().chunked(3).joinToString(".").reversed()
    return "${prefix}Rp$formatted"
}

/**
 * Formats an ISO-8601 date string (`YYYY-MM-DD` or `YYYY-MM-DDTHH:mm:ssZ`) into
 * a human-friendly Bahasa Indonesia label, e.g. "29 Apr 2026".
 * Returns the original string unchanged when it cannot be parsed.
 */
fun formatDateFriendly(dateStr: String?): String? {
    if (dateStr.isNullOrBlank()) return null
    // Take only the date part (first 10 chars) from an ISO datetime
    val datePart = dateStr.take(10)
    val p = datePart.split("-")
    if (p.size != 3) return dateStr
    val year = p[0]
    val day = p[2].trimStart('0').ifEmpty { "0" }
    val month = when (p[1].toIntOrNull()) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "Mei"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Agu"
        9 -> "Sep"; 10 -> "Okt"; 11 -> "Nov"; 12 -> "Des"
        else -> p[1]
    }
    return "$day $month $year"
}
