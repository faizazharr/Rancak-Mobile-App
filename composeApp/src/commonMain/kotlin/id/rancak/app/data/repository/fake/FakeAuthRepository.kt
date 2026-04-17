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

    override suspend fun getMyTenants(): Resource<List<Tenant>> =
        Resource.Success(demoUser.tenants)

    override suspend fun getTenantSettings(): Resource<TenantSettings> =
        Resource.Success(TenantSettings(
            uuid = "demo-tenant", name = "Rancak Demo", address = "Jl. Demo 123",
            phone = "08123456789", isActive = true, subscriptionStatus = "active",
            subscriptionPlan = "pro", subscriptionExpiresAt = null,
            maxUsers = 10, currentUsers = 1
        ))

    override suspend fun getReceiptSettings(): Resource<ReceiptSettings> =
        Resource.Success(ReceiptSettings())

    override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun getSessions(): Resource<List<Session>> =
        Resource.Success(listOf(
            Session(sessionId = "sess-1", userAgent = "Android", issuedAt = "2026-04-18", lastUsedAt = "2026-04-18", expiresAt = "2026-05-18", current = true)
        ))

    override suspend fun revokeSession(sessionId: String): Resource<Unit> =
        Resource.Success(Unit)
}
