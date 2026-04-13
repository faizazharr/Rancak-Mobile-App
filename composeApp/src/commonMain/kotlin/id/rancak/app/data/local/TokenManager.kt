package id.rancak.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager {
    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private var _refreshToken: String? = null
    val refreshToken: String? get() = _refreshToken

    private var _tenantUuid: String? = null
    val tenantUuid: String? get() = _tenantUuid

    fun saveTokens(accessToken: String, refreshToken: String) {
        _accessToken.value = accessToken
        _refreshToken = refreshToken
    }

    fun setTenant(uuid: String) {
        _tenantUuid = uuid
    }

    fun clear() {
        _accessToken.value = null
        _refreshToken = null
        _tenantUuid = null
    }

    val isLoggedIn: Boolean get() = _accessToken.value != null
}
