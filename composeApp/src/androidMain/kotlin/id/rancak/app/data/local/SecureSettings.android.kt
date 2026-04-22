package id.rancak.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Prefix file SharedPreferences terenkripsi — setiap namespace mendapat
 * file sendiri agar tidak bercampur (mis. `rancak_secure_default`,
 * `rancak_secure_offline_queue`). Terpisah dari plain SharedPreferences
 * default agar migrasi (lihat [TokenManager]) dapat mendeteksi data lama
 * pada `com.russhwolf.settings.no_arg`.
 */
private const val SECURE_PREFS_PREFIX = "rancak_secure_"

/**
 * Holder singleton application context — di-set sekali di
 * [id.rancak.app.RancakApplication.onCreate] sebelum Koin memuat modul data.
 */
internal object SecureContextHolder {
    @Volatile
    var appContext: Context? = null
}

actual fun createSecureSettings(namespace: String): Settings {
    // androidx.security:security-crypto 1.1.x ditandai @Deprecated oleh AndroidX
    // karena akan diganti di masa depan, namun belum ada pengganti stabil dan
    // library ini masih aman digunakan di produksi. Suppress supaya log bersih.
    @Suppress("DEPRECATION")
    val ctx = SecureContextHolder.appContext
        ?: error("SecureContextHolder.appContext belum di-set — panggil di Application.onCreate()")

    @Suppress("DEPRECATION")
    val masterKey = MasterKey.Builder(ctx)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    @Suppress("DEPRECATION")
    val encryptedPrefs = EncryptedSharedPreferences.create(
        ctx,
        SECURE_PREFS_PREFIX + namespace,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    return SharedPreferencesSettings(encryptedPrefs)
}
