package id.rancak.app.domain.repository

import id.rancak.app.domain.model.LoginResult
import id.rancak.app.domain.model.ReceiptSettings
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Session
import id.rancak.app.domain.model.Tenant
import id.rancak.app.domain.model.TenantApplication
import id.rancak.app.domain.model.TenantSettings
import id.rancak.app.domain.model.User

/**
 * Kontrak auth: operasi async ke backend.
 * Mewarisi [UserSessionProvider] sehingga VM yang butuh keduanya cukup
 * menyuntikkan satu dependensi. VM yang hanya butuh baca/tulis sesi lokal
 * (mis. [RoleGate]) menyuntikkan [UserSessionProvider] langsung.
 */
interface AuthRepository : UserSessionProvider {
    suspend fun login(email: String, password: String): Resource<LoginResult>
    suspend fun loginWithGoogle(idToken: String): Resource<LoginResult>
    suspend fun refreshToken(): Resource<LoginResult>
    suspend fun logout(): Resource<Unit>
    suspend fun getMe(): Resource<User>
    suspend fun getMyTenants(): Resource<List<Tenant>>
    suspend fun getTenantSettings(): Resource<TenantSettings>
    suspend fun getReceiptSettings(): Resource<ReceiptSettings>
    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>
    /** Minta token reset password — selalu sukses (anti-enumeration). */
    suspend fun forgotPassword(email: String): Resource<Unit>
    /** Reset password dengan token email. Semua sesi user akan di-revoke. */
    suspend fun resetPassword(token: String, newPassword: String): Resource<Unit>
    suspend fun getSessions(): Resource<List<Session>>
    suspend fun revokeSession(sessionId: String): Resource<Unit>

    /**
     * Submit pengajuan outlet baru. Backend auto-approve — outlet langsung jadi
     * dengan demo trial 14 hari, dan [TenantApplication.approvedTenantUuid]
     * langsung terisi.
     */
    suspend fun submitOutletApplication(
        outletName: String,
        phone: String,
        address: String,
        nib: String,
        businessType: String,
        googleMapsUrl: String? = null
    ): Resource<TenantApplication>

    /** Riwayat pengajuan outlet milik user yang login. */
    suspend fun getMyApplications(): Resource<List<TenantApplication>>
}
