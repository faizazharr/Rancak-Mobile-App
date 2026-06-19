package id.rancak.app.presentation.util

fun localizeApiError(message: String): String = when {
    message.contains("subscription inactive", ignoreCase = true) ||
    message.contains("subscription status is 'expired'", ignoreCase = true) ||
    message.contains("subscription expired", ignoreCase = true) ->
        "Langganan telah berakhir. Perbarui paket di menu Billing untuk melanjutkan."

    message.contains("subscription", ignoreCase = true) &&
    message.contains("inactive", ignoreCase = true) ->
        "Langganan tidak aktif. Silakan perbarui paket langganan Anda."

    message.contains("shift already open", ignoreCase = true) ||
    message.contains("shift is already open", ignoreCase = true) ->
        "Shift sudah dibuka. Tutup shift yang ada sebelum membuka yang baru."

    message.contains("no open shift", ignoreCase = true) ||
    message.contains("no active shift", ignoreCase = true) ||
    message.contains("shift not found", ignoreCase = true) ->
        "Tidak ada shift aktif saat ini. Buka shift terlebih dahulu."

    message.contains("unauthorized", ignoreCase = true) ||
    message.contains("unauthenticated", ignoreCase = true) ->
        "Sesi Anda telah berakhir. Silakan masuk kembali."

    message.contains("forbidden", ignoreCase = true) ->
        "Anda tidak memiliki akses untuk melakukan tindakan ini."

    message.contains("not found", ignoreCase = true) ->
        "Data tidak ditemukan."

    message.contains("timeout", ignoreCase = true) ||
    message.contains("timed out", ignoreCase = true) ->
        "Koneksi timeout. Periksa jaringan Anda dan coba lagi."

    message.contains("network", ignoreCase = true) ||
    message.contains("connect", ignoreCase = true) ||
    message.contains("unable to resolve", ignoreCase = true) ->
        "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."

    else -> message
}
