package id.rancak.app.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages authentication tokens with persistent storage via multiplatform-settings.
 *
 * Storage:
 *  - Android → EncryptedSharedPreferences (AES-256 GCM, MasterKey via Android Keystore).
 *  - iOS     → KeychainSettings (Keychain Services).
 *
 * Token & user info selamat dari app restart tanpa re-login. Data terenkripsi
 * at-rest; hanya dapat dibaca oleh app ini di device yang sama.
 *
 * **Migrasi:** Saat pertama kali dijalankan setelah upgrade dari versi yang
 * menyimpan token plain, [migrateFromLegacy] memindahkan nilai lama dari
 * `Settings()` default ke secure storage kemudian menghapus yang plain.
 */
class TokenManager(private val settings: Settings) {

    init {
        migrateFromLegacy()
    }

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

    /**
     * Pindahkan token + user info dari storage plain (versi lama app) ke
     * storage terenkripsi. Hanya berjalan sekali — setelah sukses, flag
     * [KEY_MIGRATION_DONE] di-set agar tidak dipanggil lagi.
     *
     * Semua akses ke `Settings()` plain dibungkus try/catch supaya error
     * pada platform tertentu (iOS tanpa NSUserDefaults lama, dll.) tidak
     * memblokir app.
     */
    private fun migrateFromLegacy() {
        if (settings.getBoolean(KEY_MIGRATION_DONE, false)) return
        try {
            val legacy = Settings()
            val keysToMigrate = listOf(
                KEY_ACCESS_TOKEN, KEY_REFRESH_TOKEN, KEY_TENANT_UUID, KEY_TENANT_NAME,
                KEY_USER_UUID, KEY_USER_NAME, KEY_USER_EMAIL, KEY_USER_ROLE,
                KEY_DEVICE_ID, KEY_LAST_SYNC_TIME
            )
            keysToMigrate.forEach { key ->
                legacy.getStringOrNull(key)?.let { value ->
                    if (settings.getStringOrNull(key) == null) {
                        settings[key] = value
                    }
                    legacy.remove(key)
                }
            }
        } catch (_: Throwable) {
            // Migration bersifat best-effort — jangan gagalkan init.
        }
        settings[KEY_MIGRATION_DONE] = true
    }

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
        private const val KEY_MIGRATION_DONE = "rancak_secure_migration_done"
    }
}
