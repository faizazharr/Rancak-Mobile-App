package id.rancak.app.data.util

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.domain.model.Resource

// ── Cause-chain helpers ───────────────────────────────────────────────────────

/**
 * Menghasilkan seluruh rantai cause dari exception ini (termasuk dirinya sendiri).
 * Dipakai agar deteksi network error tetap bekerja saat Ktor membungkus
 * java.net.UnknownHostException di dalam CIORequestException atau sejenisnya.
 */
private fun Throwable.causeChain(): Sequence<Throwable> =
    generateSequence(this) { it.cause?.takeIf { cause -> cause !== it } }

/**
 * Gabungkan seluruh rantai cause menjadi satu string untuk pencocokan pola.
 * Format: "<ClassName1> <message1> | <ClassName2> <message2> | ..."
 */
private fun Throwable.causeTokens(): String =
    causeChain().joinToString(" | ") { "${it::class.simpleName ?: ""} ${it.message ?: ""}" }

// ── Network message translation ───────────────────────────────────────────────

/**
 * Mengubah exception jaringan menjadi pesan yang ramah pengguna dalam Bahasa Indonesia.
 *
 * [fallback] dipakai bila exception bukan error jaringan (misal: deserialisasi gagal,
 * error bisnis yang tidak terduga). Ini membuat pesan tetap kontekstual di call site.
 *
 * Pola yang ditangani (ditelusuri ke seluruh cause chain):
 * - DNS gagal / offline / ISP blokir domain        → UnknownHostException, nodename nor servname
 * - Koneksi timeout / ISP throttle                 → HttpRequestTimeoutException, SocketTimeoutException
 * - Koneksi ditolak / ISP TCP-RST                  → ConnectException, ECONNREFUSED
 * - Koneksi putus tiba-tiba / ISP drop             → EOFException, Connection reset, ECONNRESET
 * - SSL/TLS gagal / ISP MITM / captive portal      → SSLException, SSLHandshakeException, CertPathValidatorException
 * - Jaringan tidak tersedia / airplane mode         → Network is unreachable
 * - IO generik                                     → IOException
 */
internal fun Exception.toNetworkMessage(fallback: String = "Terjadi kesalahan. Coba lagi."): String {
    val tokens = causeTokens()
    return when {
        tokens.contains("UnknownHostException", ignoreCase = true) ||
        tokens.contains("Unable to resolve", ignoreCase = true) ||
        tokens.contains("No address associated", ignoreCase = true) ||
        tokens.contains("nodename nor servname", ignoreCase = true) ->
            "Tidak ada koneksi internet. Periksa jaringan Anda."

        // Ktor: HttpRequestTimeoutException; JVM: SocketTimeoutException / TimeoutException
        tokens.contains("HttpRequestTimeoutException", ignoreCase = true) ||
        tokens.contains("SocketTimeoutException", ignoreCase = true) ||
        tokens.contains("TimeoutException", ignoreCase = true) ||
        tokens.contains("timed out", ignoreCase = true) ||
        tokens.contains("Request timeout", ignoreCase = true) ->
            "Koneksi timeout. Coba lagi."

        tokens.contains("ConnectException", ignoreCase = true) ||
        tokens.contains("Connection refused", ignoreCase = true) ||
        tokens.contains("ECONNREFUSED", ignoreCase = true) ||
        tokens.contains("Failed to connect", ignoreCase = true) ->
            "Gagal terhubung ke server. Coba lagi."

        tokens.contains("EOFException", ignoreCase = true) ||
        tokens.contains("Unexpected EOF", ignoreCase = true) ||
        tokens.contains("Connection reset", ignoreCase = true) ||
        tokens.contains("ECONNRESET", ignoreCase = true) ||
        tokens.contains("Broken pipe", ignoreCase = true) ->
            "Koneksi terputus. Coba lagi."

        tokens.contains("SSLException", ignoreCase = true) ||
        tokens.contains("SSLHandshakeException", ignoreCase = true) ||
        tokens.contains("CertPathValidatorException", ignoreCase = true) ||
        tokens.contains("certificate", ignoreCase = true) ||
        tokens.contains("handshake", ignoreCase = true) ->
            "Masalah keamanan koneksi. Coba lagi."

        tokens.contains("Network is unreachable", ignoreCase = true) ||
        tokens.contains("NetworkOnMainThreadException", ignoreCase = true) ->
            "Jaringan tidak tersedia. Periksa koneksi Anda."

        tokens.contains("IOException", ignoreCase = true) ->
            "Kesalahan koneksi. Coba lagi."

        else -> fallback
    }
}

// ── Offline-queue guard ───────────────────────────────────────────────────────

/**
 * Mendeteksi apakah exception berasal dari koneksi jaringan yang tidak tersedia.
 * Digunakan di [SaleRepositoryImpl] untuk memutuskan apakah penjualan perlu
 * di-queue offline.
 *
 * Menelusuri seluruh cause chain agar deteksi tetap bekerja saat Ktor membungkus
 * exception asli (mis. CIORequestException wrapping UnknownHostException).
 */
internal fun isNetworkError(e: Exception): Boolean {
    val tokens = e.causeTokens()
    return tokens.contains("UnknownHostException", ignoreCase = true) ||
           tokens.contains("Unable to resolve", ignoreCase = true) ||
           tokens.contains("ConnectException", ignoreCase = true) ||
           tokens.contains("Connection refused", ignoreCase = true) ||
           tokens.contains("ECONNREFUSED", ignoreCase = true) ||
           tokens.contains("HttpRequestTimeoutException", ignoreCase = true) ||
           tokens.contains("SocketTimeoutException", ignoreCase = true) ||
           tokens.contains("TimeoutException", ignoreCase = true) ||
           tokens.contains("timed out", ignoreCase = true) ||
           tokens.contains("Request timeout", ignoreCase = true) ||
           tokens.contains("Network is unreachable", ignoreCase = true) ||
           tokens.contains("Failed to connect", ignoreCase = true) ||
           tokens.contains("EOFException", ignoreCase = true) ||
           tokens.contains("Unexpected EOF", ignoreCase = true) ||
           tokens.contains("Connection reset", ignoreCase = true) ||
           tokens.contains("ECONNRESET", ignoreCase = true) ||
           tokens.contains("Broken pipe", ignoreCase = true) ||
           tokens.contains("SSLException", ignoreCase = true) ||
           tokens.contains("SSLHandshakeException", ignoreCase = true) ||
           tokens.contains("CertPathValidatorException", ignoreCase = true)
}

internal suspend fun <T, R> safe(
    block: suspend () -> ApiResponse<T>,
    map: (T) -> R,
    errorMsg: String
): Resource<R> = try {
    val response = block()
    if (response.isSuccess && response.data != null) {
        Resource.Success(map(response.data))
    } else {
        Resource.Error(response.message ?: errorMsg)
    }
} catch (e: Exception) {
    Resource.Error(e.toNetworkMessage())
}

internal suspend fun <T, R> safeList(
    block: suspend () -> ApiResponse<List<T>>,
    errorMsg: String,
    map: (T) -> R
): Resource<List<R>> = try {
    val response = block()
    if (response.isSuccess) {
        // Backend kadang mengembalikan null (bukan []) untuk list kosong — perlakukan sebagai empty list
        Resource.Success(response.data?.map(map) ?: emptyList())
    } else {
        Resource.Error(response.message ?: errorMsg)
    }
} catch (e: Exception) {
    Resource.Error(e.toNetworkMessage())
}

internal suspend fun safeUnit(
    block: suspend () -> ApiResponse<Unit>,
    errorMsg: String
): Resource<Unit> = try {
    val response = block()
    if (response.isSuccess) Resource.Success(Unit)
    else Resource.Error(response.message ?: errorMsg)
} catch (e: Exception) {
    Resource.Error(e.toNetworkMessage())
}
