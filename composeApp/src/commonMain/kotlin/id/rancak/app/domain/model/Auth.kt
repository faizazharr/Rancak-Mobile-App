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
    val logoUrl: String?,
    val email: String?,
    val website: String?,
    val npwp: String?,
    val receiptHeader: String?,
    val receiptFooter: String?,
    val receiptFooter2: String?,
    val logoPosition: String?,
    val logoSizePct: Int?,
    val receiptNameSize: String?,
    val separatorStyle: String?,
    val separatorCount: Int?,
    val footerPosition: String?,
    val receiptInstagram: String?,
    val receiptFacebook: String?,
    val receiptWifiSsid: String?,
    val receiptWifiPassword: String?
)

data class Session(
    val sessionId: String,
    val userAgent: String?,
    val issuedAt: String?,
    val lastUsedAt: String?,
    val expiresAt: String?,
    val current: Boolean
)
