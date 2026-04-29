package id.rancak.app.domain.repository

import id.rancak.app.domain.model.UserRole

/**
 * Kontrak untuk membaca dan menulis sesi pengguna aktif (data lokal — tidak ada I/O jaringan).
 *
 * Dipisah dari [AuthRepository] untuk mematuhi ISP:
 * - [AuthRepository] : operasi async ke backend (login, logout, refresh, dll.)
 * - [UserSessionProvider] : baca/tulis sesi lokal yang tersimpan di device
 *
 * Seluruh metode bersifat synchronous karena data dibaca dari penyimpanan
 * lokal yang cepat (SharedPreferences / Keychain).
 */
interface UserSessionProvider {
    /** True jika access-token tersedia — tidak memvalidasi ke server. */
    fun isLoggedIn(): Boolean

    /** UUID tenant yang sedang aktif, atau null jika belum dipilih. */
    fun getCurrentTenantUuid(): String?

    /** Nama tenant yang sedang aktif, atau null jika belum dipilih. */
    fun getCurrentTenantName(): String?

    /** Simpan UUID dan nama tenant yang dipilih user. */
    fun setTenant(uuid: String, name: String)

    /** Simpan peran user berdasarkan string dari backend (mis. "admin"). */
    fun setUserRole(role: String)

    /** Peran user saat ini. [UserRole.STAFF] sebagai fallback aman bila belum ter-set. */
    fun getUserRole(): UserRole
}
