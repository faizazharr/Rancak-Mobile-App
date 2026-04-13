package id.rancak.app.domain.model

data class User(
    val uuid: String,
    val name: String,
    val email: String,
    val tenants: List<Tenant> = emptyList()
)

data class Tenant(
    val uuid: String,
    val name: String
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
