package id.rancak.app.data.remote

import id.rancak.app.data.remote.dto.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Safe alternative to `.body<ApiResponse<T>>()`.
 *
 * When the server returns an error response with a non-JSON content type
 * (e.g. 422 with `text/plain`), calling `.body()` throws
 * `NoTransformationFoundException` because Ktor's content negotiation
 * cannot deserialize plain text as `ApiResponse`.
 *
 * This extension reads the body as plain text in that case and wraps it
 * inside a `ApiResponse` error envelope so callers get a consistent type.
 */
internal suspend inline fun <reified T> HttpResponse.safeBody(): ApiResponse<T> {
    val ct = contentType()
    return if (ct != null && ct.match(ContentType.Application.Json)) {
        body()
    } else {
        val text = try { bodyAsText() } catch (_: Exception) { status.description }
        ApiResponse(
            statusCode = status.value,
            message = text.ifBlank { "Terjadi kesalahan (${status.value})" }
        )
    }
}
