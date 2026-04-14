package id.rancak.app.data.repository.fake

import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {

    private var loggedIn       = false
    private var tenantUuid: String? = null
    private var tenantName: String? = null

    // Menerima email apapun; password harus "demo123"
    override suspend fun login(email: String, password: String): Resource<LoginResult> {
        return if (password == DEMO_PASSWORD) {
            loggedIn    = true
            tenantUuid  = demoUser.tenants.firstOrNull()?.uuid
            Resource.Success(demoLoginResult)
        } else {
            Resource.Error("Password salah. Gunakan: $DEMO_PASSWORD")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Resource<LoginResult> {
        loggedIn   = true
        tenantUuid = demoUser.tenants.firstOrNull()?.uuid
        return Resource.Success(demoLoginResult)
    }

    override suspend fun refreshToken(): Resource<LoginResult> =
        Resource.Success(demoLoginResult)

    override suspend fun logout(): Resource<Unit> {
        loggedIn   = false
        tenantUuid = null
        tenantName = null
        return Resource.Success(Unit)
    }

    override suspend fun getMe(): Resource<User> =
        Resource.Success(demoUser)

    override fun isLoggedIn(): Boolean = loggedIn

    override fun getCurrentTenantUuid(): String? = tenantUuid

    override fun getCurrentTenantName(): String? = tenantName

    override fun setTenant(uuid: String, name: String) {
        tenantUuid = uuid
        tenantName = name
    }
}
