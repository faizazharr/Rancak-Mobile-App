package id.rancak.app.presentation.ui.auth.components

import androidx.compose.ui.graphics.Color

/**
 * Mengembalikan pasangan (label, warna) untuk status billing tenant.
 * Mengembalikan (null, Color.Unspecified) jika status tidak bermasalah
 * sehingga caller tidak perlu menampilkan chip sama sekali.
 */
internal fun billingStatusInfo(status: String?): Pair<String?, Color> = when (status?.lowercase()) {
    "expired", "past_due" -> "Kedaluwarsa" to Color(0xFFE8772E)
    "inactive"            -> "Belum Aktif"  to Color(0xFFF59E0B)
    else                  -> null            to Color.Unspecified
}
