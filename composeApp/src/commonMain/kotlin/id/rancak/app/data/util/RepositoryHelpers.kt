package id.rancak.app.data.util

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.domain.model.Resource

/**
 * Mendeteksi apakah exception berasal dari koneksi jaringan yang tidak tersedia.
 * Digunakan sebelum memutuskan apakah transaksi perlu di-queue offline.
 */
internal fun isNetworkError(e: Exception): Boolean {
    val msg = e.message ?: return false
    return msg.contains("UnknownHostException", ignoreCase = true) ||
           msg.contains("ConnectException", ignoreCase = true) ||
           msg.contains("SocketTimeoutException", ignoreCase = true) ||
           msg.contains("Network is unreachable", ignoreCase = true) ||
           msg.contains("Unable to resolve host", ignoreCase = true) ||
           msg.contains("failed to connect", ignoreCase = true)
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
    Resource.Error(e.message ?: "Kesalahan jaringan")
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
    Resource.Error(e.message ?: "Kesalahan jaringan")
}

internal suspend fun safeUnit(
    block: suspend () -> ApiResponse<Unit>,
    errorMsg: String
): Resource<Unit> = try {
    val response = block()
    if (response.isSuccess) Resource.Success(Unit)
    else Resource.Error(response.message ?: errorMsg)
} catch (e: Exception) {
    Resource.Error(e.message ?: "Kesalahan jaringan")
}
