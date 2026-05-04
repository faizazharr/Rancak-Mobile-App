package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Tenant
import id.rancak.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Jenis usaha yang tersedia untuk pengajuan outlet baru. */
enum class BusinessType(val label: String) {
    RESTAURANT("Restoran"),
    CAFE("Kafe / Kedai Kopi"),
    FAST_FOOD("Makanan Cepat Saji"),
    BAKERY("Bakery / Roti"),
    RETAIL("Retail / Minimarket"),
    FASHION("Fashion / Pakaian"),
    SERVICE("Jasa & Layanan"),
    OTHER("Lainnya")
}

/** State form pengajuan outlet. */
data class OutletSubmissionFormState(
    val isFormOpen: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val gmapsUrl: String = "",
    val nib: String = "",
    val businessType: BusinessType? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                phone.isNotBlank() &&
                address.isNotBlank() &&
                nib.isNotBlank() &&
                businessType != null
}

/** Jenis masalah billing yang mencegah akses masuk ke aplikasi. */
enum class BillingIssue {
    /** Langganan telah habis masa berlakunya. */
    EXPIRED,

    /** Langganan belum aktif — belum ada pembayaran sama sekali. */
    INACTIVE
}

data class TenantPickerUiState(
    val tenants: List<Tenant> = emptyList(),
    val selectedTenant: Tenant? = null,
    val isLoading: Boolean = false,
    /** True saat data sudah ada dan di-refresh di belakang layar (no full-screen spinner). */
    val isRefreshing: Boolean = false,
    val isConfirmed: Boolean = false,
    val error: String? = null,
    val submission: OutletSubmissionFormState = OutletSubmissionFormState(),
    /** Non-null ketika tenant terpilih memiliki masalah billing (kedaluwarsa / belum bayar). */
    val billingIssue: BillingIssue? = null,
    /** True saat user memilih "Bayar Billing" — dipakai Screen untuk trigger navigasi. */
    val isNavigatingToBilling: Boolean = false
)

class TenantPickerViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantPickerUiState())
    val uiState: StateFlow<TenantPickerUiState> = _uiState.asStateFlow()

    fun loadTenants(autoConfirmSingle: Boolean = true) {
        val isFirstLoad = _uiState.value.tenants.isEmpty()
        viewModelScope.launch {
            if (isFirstLoad) {
                // Muat pertama kali — tampilkan loading penuh dan reset semua state
                _uiState.update { it.copy(isLoading = true, isConfirmed = false, billingIssue = null) }
            } else {
                // Refresh di belakang layar — jangan tampilkan loading penuh agar konten
                // yang sedang tampil (termasuk BillingIssueContent) tidak berkedip
                _uiState.update { it.copy(isRefreshing = true, isConfirmed = false) }
            }
            when (val result = authRepository.getMyTenants()) {
                is Resource.Success -> {
                    val tenants = result.data
                    _uiState.update { it.copy(tenants = tenants, isLoading = false, isRefreshing = false) }
                    if (autoConfirmSingle && tenants.size == 1) {
                        selectTenant(tenants.first())
                        confirm()
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false, isRefreshing = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun selectTenant(tenant: Tenant) {
        _uiState.update { it.copy(selectedTenant = tenant) }
    }

    fun confirm() {
        val tenant = _uiState.value.selectedTenant ?: return
        authRepository.setTenant(tenant.uuid, tenant.name)
        tenant.role?.let { authRepository.setUserRole(it) }

        val issue = detectBillingIssue(tenant.subscriptionStatus)
        if (issue != null) {
            _uiState.update { it.copy(billingIssue = issue) }
        } else {
            _uiState.update { it.copy(isConfirmed = true) }
        }
    }

    /**
     * Deteksi masalah billing dari status langganan tenant.
     * Status `null`, `"active"`, dan `"trial"` dianggap tidak bermasalah.
     */
    private fun detectBillingIssue(status: String?): BillingIssue? = when (status?.lowercase()) {
        "expired", "past_due" -> BillingIssue.EXPIRED
        "inactive"            -> BillingIssue.INACTIVE
        else                  -> null
    }

    /**
     * Dipanggil saat user memilih "Bayar Billing".
     * Context tenant sudah di-set di [confirm] — tinggal trigger navigasi ke layar Billing.
     *
     * Sengaja TIDAK menghapus [billingIssue] di sini agar:
     * 1. BillingIssueContent tetap tampil saat navigasi berlangsung (tidak ada flash ke daftar outlet).
     * 2. Saat user kembali (Back) dari BillingScreen, [billingIssue] masih terset sehingga
     *    BillingIssueContent langsung tampil kembali tanpa flash ke daftar outlet.
     * [billingIssue] akan otomatis null saat [loadTenants] + [confirm] mendapati status sudah aktif.
     */
    fun continueToBilling() {
        _uiState.update { it.copy(isNavigatingToBilling = true) }
    }

    /** Dipanggil oleh Screen setelah navigasi ke Billing terpicu — reset flag. */
    fun clearNavigatingToBilling() {
        _uiState.update { it.copy(isNavigatingToBilling = false) }
    }

    /**
     * Dipanggil saat user memilih "Pilih Outlet Lain" dari layar billing issue.
     * Menghapus tenant aktif dari state sehingga daftar tenant ditampilkan kembali.
     */
    fun dismissBillingIssue() {
        _uiState.update { it.copy(billingIssue = null, selectedTenant = null) }
    }

    // ── Outlet submission ─────────────────────────────────────────────────────

    fun updateSubmissionName(value: String) =
        _uiState.update { it.copy(submission = it.submission.copy(name = value, error = null)) }

    fun updateSubmissionPhone(value: String) =
        _uiState.update {
            it.copy(submission = it.submission.copy(phone = value.filter { c -> c.isDigit() || c == '+' }, error = null))
        }

    fun updateSubmissionAddress(value: String) =
        _uiState.update { it.copy(submission = it.submission.copy(address = value, error = null)) }

    fun updateSubmissionGmapsUrl(value: String) =
        _uiState.update { it.copy(submission = it.submission.copy(gmapsUrl = value, error = null)) }

    fun updateSubmissionNib(value: String) =
        _uiState.update {
            it.copy(submission = it.submission.copy(nib = value.filter { c -> c.isDigit() }, error = null))
        }

    fun updateSubmissionBusinessType(value: BusinessType) =
        _uiState.update { it.copy(submission = it.submission.copy(businessType = value, error = null)) }

    fun openSubmissionForm() =
        _uiState.update { it.copy(submission = it.submission.copy(isFormOpen = true, error = null)) }

    fun closeSubmissionForm() =
        _uiState.update { it.copy(submission = it.submission.copy(isFormOpen = false, error = null)) }

    fun resetSubmission() =
        _uiState.update { it.copy(submission = OutletSubmissionFormState()) }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    /**
     * Kirim pengajuan outlet baru ke `POST /applications`.
     *
     * Backend auto-approve — saat sukses, outlet langsung jadi dengan demo
     * trial 14 hari. Bila `approvedTenantUuid` terisi, langsung set tenant
     * aktif sehingga user tidak perlu refresh manual.
     */
    fun submitOutletRequest() {
        val form = _uiState.value.submission
        if (!form.isValid) {
            _uiState.update {
                it.copy(submission = it.submission.copy(error = "Lengkapi semua kolom wajib"))
            }
            return
        }
        val businessType = form.businessType ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(submission = it.submission.copy(isSubmitting = true, error = null))
            }
            val result = authRepository.submitOutletApplication(
                outletName    = form.name.trim(),
                phone         = form.phone.trim(),
                address       = form.address.trim(),
                nib           = form.nib.trim(),
                businessType  = businessType.label,
                googleMapsUrl = form.gmapsUrl.trim().ifBlank { null }
            )
            when (result) {
                is Resource.Success -> {
                    val app = result.data
                    // Auto-approve: outlet langsung aktif → set sebagai tenant terpilih
                    app.approvedTenantUuid?.let { tenantUuid ->
                        authRepository.setTenant(tenantUuid, app.outletName)
                        authRepository.setUserRole("owner")
                    }
                    _uiState.update {
                        it.copy(submission = it.submission.copy(isSubmitting = false, isSubmitted = true))
                    }
                    // Refresh daftar tenant agar muncul di picker
                    loadTenants()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            submission = it.submission.copy(
                                isSubmitting = false,
                                error = result.message
                            )
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}

