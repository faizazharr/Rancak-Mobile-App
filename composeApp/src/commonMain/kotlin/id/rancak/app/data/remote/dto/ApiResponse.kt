package id.rancak.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Standard API envelope matching backend format:
 * ```json
 * { "status_code": 200, "message": "Success", "data": { ... } }
 * ```
 * On error, `data` is null and `message` contains the error description.
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("status_code") val statusCode: Int = 200,
    val message: String? = null,
    val data: T? = null
) {
    val isSuccess: Boolean get() = statusCode in 200..299
}

@Serializable
data class PaginatedData<T>(
    val items: List<T>,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)
