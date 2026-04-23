package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tujuan navigasi yang diputuskan oleh SplashViewModel setelah memvalidasi
 * status autentikasi dan ketersediaan outlet pengguna.
 */
enum class SplashDestination { LOGIN, TENANT_PICKER, POS }

/**
 * Mengelola logika navigasi splash:
 * - Belum login → LOGIN
 * - Login tapi belum pilih tenant → TENANT_PICKER
 * - Login + tenant tersimpan, tapi outlet kosong → TENANT_PICKER
 * - Login + tenant valid + outlet ada → POS
 * - Jaringan tidak tersedia (offline) → POS (offline-first fallback)
 *
 * Navigasi baru terjadi setelah minimal [MIN_SPLASH_MS] ms agar animasi
 * splash sempat tampil.
 */
class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // Jalankan minimum delay dan validasi secara paralel
            val minDelay  = async { delay(MIN_SPLASH_MS) }
            val resolved  = async { resolveDestination() }
            minDelay.await()
            _destination.value = resolved.await()
        }
    }

    private suspend fun resolveDestination(): SplashDestination {
        // 1. Belum login → arahkan ke login
        if (!authRepository.isLoggedIn()) return SplashDestination.LOGIN

        // 2. Login tapi tidak ada tenant yang tersimpan → picker
        val storedTenantUuid = authRepository.getCurrentTenantUuid()
            ?: return SplashDestination.TENANT_PICKER

        // 3. Ada tenant tersimpan → validasi ke server
        return when (val result = authRepository.getMyTenants()) {
            is Resource.Success -> {
                val tenants = result.data
                when {
                    // Outlet kosong → harus ke picker (tampilkan form pengajuan)
                    tenants.isEmpty() -> SplashDestination.TENANT_PICKER
                    // Tenant tersimpan masih ada di list → aman masuk POS
                    tenants.any { it.uuid == storedTenantUuid } -> SplashDestination.POS
                    // Tenant tersimpan sudah tidak ada → harus pilih ulang
                    else -> SplashDestination.TENANT_PICKER
                }
            }
            // Jaringan tidak tersedia → percayai data lokal (offline-first)
            is Resource.Error   -> SplashDestination.POS
            is Resource.Loading -> SplashDestination.POS
        }
    }

    private companion object {
        const val MIN_SPLASH_MS = 2_800L
    }
}
