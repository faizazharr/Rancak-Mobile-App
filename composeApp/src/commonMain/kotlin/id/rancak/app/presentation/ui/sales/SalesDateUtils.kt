package id.rancak.app.presentation.ui.sales

/**
 * Convert `YYYY-MM-DD` into a compact "15 Apr" Bahasa Indonesia label.
 * Returns the input unchanged when it cannot be parsed.
 */
internal fun formatDateShort(dateStr: String): String {
    val p = dateStr.split("-")
    if (p.size != 3) return dateStr
    val day = p[2].trimStart('0').ifEmpty { "0" }
    val month = when (p[1].toIntOrNull()) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "Mei"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Agu"
        9 -> "Sep"; 10 -> "Okt"; 11 -> "Nov"; 12 -> "Des"
        else -> p[1]
    }
    return "$day $month"
}
