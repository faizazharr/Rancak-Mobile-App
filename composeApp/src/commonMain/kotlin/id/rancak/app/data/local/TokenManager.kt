package id.rancak.app.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages authentication tokens with persistent storage via multiplatform-settings.
 * On Android: backed by SharedPreferences.
 * On iOS: backed by NSUserDefaults.
 * Tokens survive app restarts — no re-login required after closing the app.
 */
class TokenManager {

    private val settings = Settings()

    private val _accessToken = MutableStateFlow<String?>(settings.getStringOrNull(KEY_ACCESS_TOKEN))
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    val refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    val tenantUuid: String?
        get() = settings.getStringOrNull(KEY_TENANT_UUID)

    val tenantName: String?
        get() = settings.getStringOrNull(KEY_TENANT_NAME)

    // ── User info ──
    val userUuid: String?
        get() = settings.getStringOrNull(KEY_USER_UUID)

    val userName: String?
        get() = settings.getStringOrNull(KEY_USER_NAME)

    val userEmail: String?
        get() = settings.getStringOrNull(KEY_USER_EMAIL)

    val userRole: String?
        get() = settings.getStringOrNull(KEY_USER_ROLE)

    // ── Sync timestamps (for delta sync) ──
    val lastSyncTime: String?
        get() = settings.getStringOrNull(KEY_LAST_SYNC_TIME)

    fun saveTokens(accessToken: String, refreshToken: String) {
        _accessToken.value = accessToken
        settings[KEY_ACCESS_TOKEN] = accessToken
        settings[KEY_REFRESH_TOKEN] = refreshToken
    }

    fun saveUser(uuid: String, name: String, email: String) {
        settings[KEY_USER_UUID] = uuid
        settings[KEY_USER_NAME] = name
        settings[KEY_USER_EMAIL] = email
    }

    fun setTenant(uuid: String, name: String) {
        settings[KEY_TENANT_UUID] = uuid
        settings[KEY_TENANT_NAME] = name
    }

    fun setUserRole(role: String) {
        settings[KEY_USER_ROLE] = role
    }

    fun saveLastSyncTime(time: String) {
        settings[KEY_LAST_SYNC_TIME] = time
    }

    fun clear() {
        _accessToken.value = null
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_TENANT_UUID)
        settings.remove(KEY_TENANT_NAME)
        settings.remove(KEY_USER_UUID)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_ROLE)
        settings.remove(KEY_LAST_SYNC_TIME)
    }

    /**
     * A stable device identifier generated once and persisted permanently.
     * Used as the `device_id` field in sales to track which physical device
     * created a transaction (important for multi-device setups).
     */
    val deviceId: String
        get() {
            val existing = settings.getStringOrNull(KEY_DEVICE_ID)
            if (existing != null) return existing
            // Generate and persist on first access
            @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
            val newId = kotlin.uuid.Uuid.random().toString()
            settings[KEY_DEVICE_ID] = newId
            return newId
        }

    val isLoggedIn: Boolean
        get() = _accessToken.value != null

    companion object {
        private const val KEY_ACCESS_TOKEN  = "rancak_access_token"
        private const val KEY_REFRESH_TOKEN = "rancak_refresh_token"
        private const val KEY_TENANT_UUID   = "rancak_tenant_uuid"
        private const val KEY_TENANT_NAME   = "rancak_tenant_name"
        private const val KEY_DEVICE_ID     = "rancak_device_id"
        private const val KEY_USER_UUID     = "rancak_user_uuid"
        private const val KEY_USER_NAME     = "rancak_user_name"
        private const val KEY_USER_EMAIL    = "rancak_user_email"
        private const val KEY_USER_ROLE     = "rancak_user_role"
        private const val KEY_LAST_SYNC_TIME = "rancak_last_sync_time"
    }
}
