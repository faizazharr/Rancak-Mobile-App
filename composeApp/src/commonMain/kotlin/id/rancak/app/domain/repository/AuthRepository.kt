package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<LoginResult>
    suspend fun loginWithGoogle(idToken: String): Resource<LoginResult>
    suspend fun refreshToken(): Resource<LoginResult>
    suspend fun logout(): Resource<Unit>
    suspend fun getMe(): Resource<User>
    fun isLoggedIn(): Boolean
    fun getCurrentTenantUuid(): String?
    fun getCurrentTenantName(): String?
    fun setTenant(uuid: String, name: String)
}
