package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<LoginResult>
    suspend fun loginWithGoogle(idToken: String): Resource<LoginResult>
    suspend fun refreshToken(): Resource<LoginResult>
    suspend fun logout(): Resource<Unit>
    suspend fun getMe(): Resource<User>
    suspend fun getMyTenants(): Resource<List<Tenant>>
    suspend fun getTenantSettings(): Resource<TenantSettings>
    suspend fun getReceiptSettings(): Resource<ReceiptSettings>
    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>
    suspend fun getSessions(): Resource<List<Session>>
    suspend fun revokeSession(sessionId: String): Resource<Unit>
    fun isLoggedIn(): Boolean
    fun getCurrentTenantUuid(): String?
    fun getCurrentTenantName(): String?
    fun setTenant(uuid: String, name: String)
    fun setUserRole(role: String)
}
