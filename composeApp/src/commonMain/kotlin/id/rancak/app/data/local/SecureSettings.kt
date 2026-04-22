package id.rancak.app.data.local

import com.russhwolf.settings.Settings

/**
 * Qualifier / marker untuk Settings yang terenkripsi. Setiap platform
 * menyediakan implementasi [Settings] dengan storage ter-protect:
 *  - Android → EncryptedSharedPreferences (AES-256 GCM, MasterKey via Keystore)
 *  - iOS     → KeychainSettings (Keychain Services)
 *
 * [namespace] digunakan untuk memisahkan scope penyimpanan supaya beberapa
 * domain (misal: token auth vs offline queue) tidak bercampur dalam satu
 * prefs file / keychain collection.
 *
 * Digunakan oleh [TokenManager], [OfflineSaleQueue], dan storage sensitif
 * lainnya via DI.
 */
expect fun createSecureSettings(namespace: String = "default"): Settings
