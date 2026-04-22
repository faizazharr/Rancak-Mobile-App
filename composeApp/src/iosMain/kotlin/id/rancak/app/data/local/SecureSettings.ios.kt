package id.rancak.app.data.local

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.ExperimentalSettingsImplementation

/**
 * Prefix service identifier untuk Keychain — setiap namespace mendapat
 * service berbeda. Dengan service unik, data tersimpan ter-scope khusus.
 */
private const val KEYCHAIN_SERVICE_PREFIX = "id.rancak.app.secure."

@OptIn(ExperimentalSettingsImplementation::class)
actual fun createSecureSettings(namespace: String): Settings =
    KeychainSettings(service = KEYCHAIN_SERVICE_PREFIX + namespace)
