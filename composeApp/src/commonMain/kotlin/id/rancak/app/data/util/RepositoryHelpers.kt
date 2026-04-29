package id.rancak.app.data.util

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.domain.model.Resource

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
    if (response.isSuccess && response.data != null) {
        Resource.Success(response.data.map(map))
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
