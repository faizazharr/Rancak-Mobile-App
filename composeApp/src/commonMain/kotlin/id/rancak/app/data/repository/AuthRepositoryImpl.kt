package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.mapper.toLoginResult
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.*
import id.rancak.app.data.remote.dto.auth.GoogleLoginRequest
import id.rancak.app.data.remote.dto.auth.LoginRequest
import id.rancak.app.data.remote.dto.auth.LogoutRequest
import id.rancak.app.data.remote.dto.auth.RefreshTokenRequest
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<LoginResult> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccess && response.data != null) {
                val result = response.data.toLoginResult()
                tokenManager.saveTokens(result.tokens.accessToken, result.tokens.refreshToken)
                Resource.Success(result)
            } else {
                val msg = when (response.statusCode) {
                    401 -> "Email atau password salah. Pastikan akun Anda sudah terdaftar di sistem."
                    403 -> "Akun Anda tidak memiliki akses ke aplikasi ini."
                    else -> response.message ?: "Login gagal. Coba lagi."
                }
                Resource.Error(msg, response.statusCode)
            }
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage())
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Resource<LoginResult> {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(idToken))
            if (response.isSuccess && response.data != null) {
                val result = response.data.toLoginResult()
                tokenManager.saveTokens(result.tokens.accessToken, result.tokens.refreshToken)
                Resource.Success(result)
            } else {
                val msg = when (response.statusCode) {
                    401 -> "Akun Google Anda belum terdaftar di sistem. Hubungi admin untuk mendaftarkan email Anda."
                    403 -> "Akun Anda tidak memiliki akses ke aplikasi ini."
                    else -> response.message ?: "Login dengan Google gagal. Coba lagi."
                }
                Resource.Error(msg, response.statusCode)
            }
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage())
        }
    }

    override suspend fun refreshToken(): Resource<LoginResult> {
        val refreshToken = tokenManager.refreshToken ?: return Resource.Error("Tidak ada refresh token")
        return try {
            val response = api.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccess && response.data != null) {
                val result = response.data.toLoginResult()
                tokenManager.saveTokens(result.tokens.accessToken, result.tokens.refreshToken)
                Resource.Success(result)
            } else {
                Resource.Error(response.message ?: "Refresh token gagal")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        val refreshToken = tokenManager.refreshToken ?: return Resource.Success(Unit)
        return try {
            api.logout(LogoutRequest(refreshToken))
            tokenManager.clear()
            Resource.Success(Unit)
        } catch (e: Exception) {
            tokenManager.clear()
            Resource.Success(Unit)
        }
    }

    override suspend fun getMe(): Resource<User> {
        return try {
            val response = api.getMe()
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal mengambil data pengguna")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn

    override fun getCurrentTenantUuid(): String? = tokenManager.tenantUuid

    override fun getCurrentTenantName(): String? = tokenManager.tenantName

    override fun setTenant(uuid: String, name: String) = tokenManager.setTenant(uuid, name)

    override fun setUserRole(role: String) = tokenManager.setUserRole(role)

    override suspend fun getMyTenants(): Resource<List<Tenant>> {
        return try {
            val response = api.getMyTenants()
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil data tenant")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getTenantSettings(): Resource<TenantSettings> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getTenantSettings(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal mengambil pengaturan tenant")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getReceiptSettings(): Resource<ReceiptSettings> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getReceiptSettings(tenantUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal mengambil pengaturan struk")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> {
        return try {
            val response = api.changePassword(
                id.rancak.app.data.remote.dto.auth.ChangePasswordRequest(currentPassword, newPassword)
            )
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal mengubah password")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getSessions(): Resource<List<Session>> {
        return try {
            val response = api.getSessions()
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil daftar sesi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun revokeSession(sessionId: String): Resource<Unit> {
        return try {
            val response = api.revokeSession(sessionId)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.message ?: "Gagal menghapus sesi")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }
}

/**
 * Mengubah exception jaringan menjadi pesan yang ramah pengguna dalam Bahasa Indonesia.
 * Menghindari pesan raw seperti "java.net.UnknownHostException: Unable to resolve host…"
 */
private fun Exception.toNetworkMessage(): String {
    val msg = message ?: ""
    return when {
        msg.contains("UnknownHostException", ignoreCase = true) ||
        msg.contains("Unable to resolve", ignoreCase = true) ||
        msg.contains("No address associated", ignoreCase = true) ||
        msg.contains("nodename nor servname", ignoreCase = true) ->
            "Tidak ada koneksi internet. Periksa jaringan Anda."
        msg.contains("SocketTimeoutException", ignoreCase = true) ||
        msg.contains("TimeoutException", ignoreCase = true) ||
        msg.contains("timed out", ignoreCase = true) ||
        msg.contains("timeout", ignoreCase = true) ->
            "Koneksi timeout. Coba lagi."
        msg.contains("ConnectException", ignoreCase = true) ||
        msg.contains("Connection refused", ignoreCase = true) ||
        msg.contains("Failed to connect", ignoreCase = true) ->
            "Gagal terhubung ke server. Coba lagi."
        msg.contains("SSLException", ignoreCase = true) ||
        msg.contains("certificate", ignoreCase = true) ->
            "Masalah keamanan koneksi. Coba lagi."
        else -> "Terjadi kesalahan. Coba lagi."
    }
}

