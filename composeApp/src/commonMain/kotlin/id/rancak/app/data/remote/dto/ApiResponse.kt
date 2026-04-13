package id.rancak.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null,
    val code: Int? = null
)

@Serializable
data class PaginatedData<T>(
    val items: List<T>,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)
