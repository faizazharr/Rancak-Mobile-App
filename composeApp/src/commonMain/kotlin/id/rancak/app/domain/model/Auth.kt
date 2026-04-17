package id.rancak.app.domain.model

data class User(
    val uuid: String,
    val name: String,
    val email: String,
    val tenants: List<Tenant> = emptyList()
)

data class Tenant(
    val uuid: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val subscriptionStatus: String? = null,
    val subscriptionExpiresAt: String? = null
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class LoginResult(
    val tokens: AuthTokens,
    val user: User
)

data class TenantSettings(
    val uuid: String,
    val name: String,
    val address: String?,
    val phone: String?,
    val isActive: Boolean,
    val subscriptionStatus: String?,
    val subscriptionPlan: String?,
    val subscriptionExpiresAt: String?,
    val maxUsers: Int?,
    val currentUsers: Int?
)

data class ReceiptSettings(
    val logoUrl: String? = null,
    val email: String? = null,
    val website: String? = null,
    val npwp: String? = null,
    val receiptHeader: String? = null,
    val receiptFooter: String? = null,
    val receiptFooter2: String? = null,
    val logoPosition: String? = null,
    val logoSizePct: Int? = null,
    val receiptNameSize: String? = null,
    val separatorStyle: String? = null,
    val separatorCount: Int? = null,
    val footerPosition: String? = null,
    val receiptInstagram: String? = null,
    val receiptFacebook: String? = null,
    val receiptWifiSsid: String? = null,
    val receiptWifiPassword: String? = null
)

data class Session(
    val sessionId: String,
    val userAgent: String?,
    val issuedAt: String?,
    val lastUsedAt: String?,
    val expiresAt: String?,
    val current: Boolean
)
