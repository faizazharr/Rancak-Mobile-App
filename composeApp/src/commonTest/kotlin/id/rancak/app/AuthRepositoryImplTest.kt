package id.rancak.app

import com.russhwolf.settings.MapSettings
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.repository.AuthRepositoryImpl
import id.rancak.app.domain.model.Resource
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AuthRepositoryImpl] against the real implementation class.
 *
 * Dependencies are replaced with test doubles:
 * - [RancakApiService] → backed by Ktor [MockEngine] (controlled JSON responses)
 * - [TokenManager] → backed by [MapSettings] (in-memory, no platform context)
 */
class AuthRepositoryImplTest {

    // ── Reusable JSON fixtures ──────────────────────────────────────────────

    private val loginSuccessJson = """
        {
          "status_code": 200,
          "message": "Login berhasil",
          "data": {
            "access_token": "tok-access",
            "refresh_token": "tok-refresh",
            "token_type": "bearer",
            "expires_in": 3600,
            "refresh_expires_at": "2026-05-22T10:00:00Z",
            "user": {
              "uuid": "user-001",
              "name": "Budi Santoso",
              "email": "budi@rancak.id",
              "tenants": []
            }
          }
        }
    """.trimIndent()

    private val errorJson = """
        {"status_code":401,"message":"Email atau password salah","data":null}
    """.trimIndent()

    private val tenantListJson = """
        {
          "status_code": 200,
          "message": "OK",
          "data": [
            {"uuid":"t-1","name":"Warung Rancak","subscription_status":"active"},
            {"uuid":"t-2","name":"Kafe Daun","subscription_status":"trial"}
          ]
        }
    """.trimIndent()

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun makeRepo(
        responseBody: String,
        tokenManager: TokenManager = TokenManager(MapSettings())
    ): AuthRepositoryImpl {
        val api = mockApiService(responseBody)
        return AuthRepositoryImpl(api, tokenManager)
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login - success returns Resource_Success with tokens and user`() = kotlinx.coroutines.test.runTest {
        val settings = MapSettings()
        val tokenManager = TokenManager(settings)
        val repo = AuthRepositoryImpl(mockApiService(loginSuccessJson), tokenManager)

        val result = repo.login("budi@rancak.id", "secret123")

        assertTrue(result is Resource.Success)
        val data = result.data
        assertEquals("tok-access",   data.tokens.accessToken)
        assertEquals("tok-refresh",  data.tokens.refreshToken)
        assertEquals("user-001",     data.user.uuid)
        assertEquals("Budi Santoso", data.user.name)
    }

    @Test
    fun `login - success saves tokens in TokenManager`() = kotlinx.coroutines.test.runTest {
        val settings = MapSettings()
        val tokenManager = TokenManager(settings)
        val repo = AuthRepositoryImpl(mockApiService(loginSuccessJson), tokenManager)

        repo.login("budi@rancak.id", "secret123")

        assertTrue(tokenManager.isLoggedIn)
        assertEquals("tok-refresh", tokenManager.refreshToken)
    }

    @Test
    fun `login - 401 error returns Resource_Error with message`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(errorJson).login("bad@email.com", "wrong")

        assertTrue(result is Resource.Error)
        assertEquals("Email atau password salah", result.message)
    }

    @Test
    fun `login - network exception returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val api = mockApiService { throw Exception("UnknownHostException: Unable to resolve host") }
        val repo = AuthRepositoryImpl(api, TokenManager(MapSettings()))

        val result = repo.login("budi@rancak.id", "x")

        assertTrue(result is Resource.Error)
        assertTrue(result.message.contains("UnknownHostException") || result.message.contains("host") || result.message.isNotBlank())
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    fun `logout - clears TokenManager even when API succeeds`() = kotlinx.coroutines.test.runTest {
        val settings = MapSettings()
        val tokenManager = TokenManager(settings)
        // Prime with a token
        tokenManager.saveTokens("old-access", "old-refresh")
        assertTrue(tokenManager.isLoggedIn)

        val repo = AuthRepositoryImpl(
            mockApiService("""{"status_code":200,"message":"OK","data":null}"""),
            tokenManager
        )
        val result = repo.logout()

        assertTrue(result is Resource.Success)
        assertFalse(tokenManager.isLoggedIn)
        assertNull(tokenManager.refreshToken)
    }

    @Test
    fun `logout - clears TokenManager even when API throws`() = kotlinx.coroutines.test.runTest {
        val settings = MapSettings()
        val tokenManager = TokenManager(settings)
        tokenManager.saveTokens("tok", "ref")

        val api = mockApiService { throw Exception("Network error") }
        val repo = AuthRepositoryImpl(api, tokenManager)

        val result = repo.logout()

        // logout always succeeds from caller perspective
        assertTrue(result is Resource.Success)
        assertFalse(tokenManager.isLoggedIn)
    }

    @Test
    fun `logout - with no refresh token returns success immediately without API call`() = kotlinx.coroutines.test.runTest {
        var apiCallCount = 0
        val api = mockApiService { _ ->
            apiCallCount++
            respond(
                content = "",
                status  = io.ktor.http.HttpStatusCode.OK,
                headers = io.ktor.http.headersOf(io.ktor.http.HttpHeaders.ContentType, "application/json")
            )
        }
        val repo = AuthRepositoryImpl(api, TokenManager(MapSettings()))

        val result = repo.logout()

        assertTrue(result is Resource.Success)
        assertEquals(0, apiCallCount)
    }

    // ── getMyTenants ──────────────────────────────────────────────────────────

    @Test
    fun `getMyTenants - success returns tenant list`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo(tenantListJson).getMyTenants()

        assertTrue(result is Resource.Success)
        assertEquals(2, result.data.size)
        assertEquals("t-1", result.data[0].uuid)
        assertEquals("Warung Rancak", result.data[0].name)
        assertEquals("active", result.data[0].subscriptionStatus)
        assertEquals("t-2", result.data[1].uuid)
    }

    @Test
    fun `getMyTenants - API error returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo("""{"status_code":500,"message":"Server error","data":null}""").getMyTenants()

        assertTrue(result is Resource.Error)
        assertEquals("Server error", result.message)
    }

    @Test
    fun `getMyTenants - empty list returns Resource_Success with empty list`() = kotlinx.coroutines.test.runTest {
        val result = makeRepo("""{"status_code":200,"message":"OK","data":[]}""").getMyTenants()

        assertTrue(result is Resource.Success)
        assertEquals(0, result.data.size)
    }

    // ── isLoggedIn / setTenant / getCurrentTenantUuid ─────────────────────────

    @Test
    fun `isLoggedIn - returns false before login`() = kotlinx.coroutines.test.runTest {
        val repo = makeRepo(loginSuccessJson)
        assertFalse(repo.isLoggedIn())
    }

    @Test
    fun `isLoggedIn - returns true after successful login`() = kotlinx.coroutines.test.runTest {
        val settings = MapSettings()
        val tokenManager = TokenManager(settings)
        val repo = AuthRepositoryImpl(mockApiService(loginSuccessJson), tokenManager)

        assertFalse(repo.isLoggedIn())
        repo.login("a@b.com", "p")
        assertTrue(repo.isLoggedIn())
    }

    @Test
    fun `setTenant persists tenant uuid and name`() = kotlinx.coroutines.test.runTest {
        val settings = MapSettings()
        val tokenManager = TokenManager(settings)
        val repo = AuthRepositoryImpl(mockApiService(loginSuccessJson), tokenManager)

        repo.setTenant("tenant-xyz", "Kedai Nasi")

        assertEquals("tenant-xyz", repo.getCurrentTenantUuid())
        assertEquals("Kedai Nasi", repo.getCurrentTenantName())
    }

    // ── refreshToken ──────────────────────────────────────────────────────────

    @Test
    fun `refreshToken - no refresh token returns Resource_Error`() = kotlinx.coroutines.test.runTest {
        val repo = makeRepo(loginSuccessJson) // no tokens saved
        val result = repo.refreshToken()

        assertTrue(result is Resource.Error)
        assertTrue(result.message.contains("refresh token"))
    }

    @Test
    fun `refreshToken - success updates saved tokens`() = kotlinx.coroutines.test.runTest {
        val settings = MapSettings()
        val tokenManager = TokenManager(settings)
        tokenManager.saveTokens("old-acc", "old-ref")

        val refreshResponseJson = """
            {
              "status_code": 200,
              "data": {
                "access_token": "new-access",
                "refresh_token": "new-refresh",
                "token_type": "bearer",
                "expires_in": 3600,
                "refresh_expires_at": "2026-05-22T10:00:00Z",
                "user": {
                  "uuid": "u1", "name": "A", "email": "a@b.com", "tenants": []
                }
              }
            }
        """.trimIndent()
        val repo = AuthRepositoryImpl(mockApiService(refreshResponseJson), tokenManager)

        val result = repo.refreshToken()

        assertTrue(result is Resource.Success)
        assertEquals("new-refresh", tokenManager.refreshToken)
    }
}
