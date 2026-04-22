package id.rancak.app.data.remote

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.remote.api.ApiConstants
import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.auth.LoginResponse
import id.rancak.app.data.remote.dto.auth.RefreshTokenRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(tokenManager: TokenManager): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                prettyPrint = false
                coerceInputValues = true
            })
        }

        install(Logging) {
            logger = platformHttpLogger()
            level  = LogLevel.ALL
            sanitizeHeader { header ->
                header == HttpHeaders.Authorization || header == "X-API-Key"
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            // App-level API key — required on every request by backend
            header("X-API-Key", ApiConstants.API_KEY)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis  = 30_000
        }

        // ── Auto-refresh Bearer tokens ──
        // loadTokens is called per-request (always returns the latest token).
        // refreshTokens is called automatically when a 401 response is received.
        install(Auth) {
            bearer {
                loadTokens {
                    val access  = tokenManager.accessToken.value ?: return@loadTokens null
                    val refresh = tokenManager.refreshToken     ?: ""
                    BearerTokens(access, refresh)
                }

                refreshTokens {
                    val refreshToken = tokenManager.refreshToken
                        ?: return@refreshTokens null

                    return@refreshTokens try {
                        val response = client.post(
                            ApiConstants.BASE_URL + ApiConstants.REFRESH
                        ) {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequest(refreshToken))
                            markAsRefreshTokenRequest()
                        }
                        if (response.status.isSuccess()) {
                            val body: ApiResponse<LoginResponse> = response.body()
                            if (body.isSuccess && body.data != null) {
                                tokenManager.saveTokens(
                                    body.data.accessToken,
                                    body.data.refreshToken
                                )
                                BearerTokens(body.data.accessToken, body.data.refreshToken)
                            } else {
                                // Refresh rejected — force re-login
                                tokenManager.clear()
                                null
                            }
                        } else {
                            tokenManager.clear()
                            null
                        }
                    } catch (e: Exception) {
                        // Network error — keep tokens, retry later
                        null
                    }
                }

                // Do NOT add Bearer header to public auth endpoints
                sendWithoutRequest { request ->
                    val path = request.url.encodedPath
                    !path.contains("/auth/login") &&
                    !path.contains("/auth/refresh") &&
                    !path.contains("/auth/google") &&
                    !path.contains("/auth/forgot-password") &&
                    !path.contains("/auth/reset-password")
                }
            }
        }

        expectSuccess = false
    }
}
