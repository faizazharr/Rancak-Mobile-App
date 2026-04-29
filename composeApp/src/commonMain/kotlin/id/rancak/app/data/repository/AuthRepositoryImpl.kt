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
import id.rancak.app.data.remote.dto.auth.SubmitApplicationRequest
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.AuthRepository
import id.rancak.app.domain.repository.UserSessionProvider

class AuthRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : AuthRepository, UserSessionProvider {

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

    override suspend fun getMe(): Resource<User> = safe(
        block    = { api.getMe() },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengambil data pengguna"
    )

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn

    override fun getCurrentTenantUuid(): String? = tokenManager.tenantUuid

    override fun getCurrentTenantName(): String? = tokenManager.tenantName

    override fun setTenant(uuid: String, name: String) = tokenManager.setTenant(uuid, name)

    override fun setUserRole(role: String) = tokenManager.setUserRole(role)

    override fun getUserRole(): UserRole = UserRole.from(tokenManager.userRole)

    override suspend fun getMyTenants(): Resource<List<Tenant>> = safe(
        block    = { api.getMyTenants() },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil data tenant"
    )

    override suspend fun getTenantSettings(): Resource<TenantSettings> = safe(
        block    = { api.getTenantSettings(tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengambil pengaturan tenant"
    )

    override suspend fun getReceiptSettings(): Resource<ReceiptSettings> = safe(
        block    = { api.getReceiptSettings(tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengambil pengaturan struk"
    )

    override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> = safeUnit(
        block    = { api.changePassword(id.rancak.app.data.remote.dto.auth.ChangePasswordRequest(currentPassword, newPassword)) },
        errorMsg = "Gagal mengubah password"
    )

    override suspend fun getSessions(): Resource<List<Session>> = safe(
        block    = { api.getSessions() },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil daftar sesi"
    )

    override suspend fun revokeSession(sessionId: String): Resource<Unit> = safeUnit(
        block    = { api.revokeSession(sessionId) },
        errorMsg = "Gagal menghapus sesi"
    )

    override suspend fun submitOutletApplication(
        outletName: String,
        phone: String,
        address: String,
        nib: String,
        businessType: String,
        googleMapsUrl: String?
    ): Resource<TenantApplication> {
        return try {
            val response = api.submitApplication(
                SubmitApplicationRequest(
                    outletName    = outletName,
                    phone         = phone,
                    address       = address,
                    nib           = nib,
                    businessType  = businessType,
                    googleMapsUrl = googleMapsUrl?.takeIf { it.isNotBlank() }
                )
            )
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                val msg = when (response.statusCode) {
                    400 -> response.message ?: "Data pengajuan tidak valid"
                    409 -> "Anda sudah punya outlet aktif dengan nama yang sama"
                    else -> response.message ?: "Gagal mengirim pengajuan outlet"
                }
                Resource.Error(msg, response.statusCode)
            }
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage())
        }
    }

    override suspend fun getMyApplications(): Resource<List<TenantApplication>> {
        return try {
            val response = api.getMyApplications()
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil riwayat pengajuan")
            }
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage())
        }
    }

    override suspend fun forgotPassword(email: String): Resource<Unit> {
        return try {
            // Anti-enumeration: BE selalu kembalikan 200 walau email tidak terdaftar.
            api.forgotPassword(id.rancak.app.data.remote.dto.auth.ForgotPasswordRequest(email))
            Resource.Success(Unit)
        } catch (e: Exception) {
            // Bahkan kalau network error pun, jangan kasih tahu user — anggap sukses.
            Resource.Success(Unit)
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Resource<Unit> {
        return try {
            val response = api.resetPassword(
                id.rancak.app.data.remote.dto.auth.ResetPasswordRequest(token, newPassword)
            )
            if (response.isSuccess) Resource.Success(Unit)
            else {
                val msg = when (response.statusCode) {
                    400 -> "Token reset tidak valid atau sudah kedaluwarsa."
                    422 -> response.message ?: "Password tidak memenuhi persyaratan."
                    else -> response.message ?: "Gagal reset password."
                }
                Resource.Error(msg, response.statusCode)
            }
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage())
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

