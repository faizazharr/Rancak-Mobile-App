package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Invoice
import id.rancak.app.domain.model.Plan
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.SubscriptionState
import id.rancak.app.domain.repository.BillingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Immutable
data class BillingUiState(
    val isLoading: Boolean = false,
    val subscription: SubscriptionState? = null,
    val plans: List<Plan> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val selectedPlan: Plan? = null,
    val showSubscribeDialog: Boolean = false,
    val showCancelDialog: Boolean = false,
    val cancelTargetInvoice: Invoice? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    // ── QR payment ────────────────────────────────────────────────────────────
    /** Non-null → tampilkan dialog QR pembayaran. */
    val qrInvoice: Invoice? = null,
    /** True saat polling ke server setiap 2 detik menunggu status "paid". */
    val isPolling: Boolean = false,
    /** True saat status invoice menjadi "paid" — trigger navigasi ke POS. */
    val isPaymentComplete: Boolean = false,
    /** True saat pull-to-refresh sedang memuat data terbaru. */
    val isRefreshing: Boolean = false
)

class BillingViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val subscriptionResult = billingRepository.getSubscription()
            val plansResult = billingRepository.getBillingPlans()
            val invoicesResult = billingRepository.getInvoices()

            _uiState.update { state ->
                var s = state.copy(isLoading = false)
                if (subscriptionResult is Resource.Success) s = s.copy(subscription = subscriptionResult.data)
                if (plansResult is Resource.Success) s = s.copy(plans = plansResult.data)
                if (invoicesResult is Resource.Success) s = s.copy(invoices = invoicesResult.data)
                s.copy(
                    error = when {
                        subscriptionResult is Resource.Error -> subscriptionResult.message
                        plansResult is Resource.Error -> plansResult.message
                        else -> null
                    }
                )
            }
        }
    }

    fun openSubscribeDialog(plan: Plan) {
        _uiState.update { it.copy(selectedPlan = plan, showSubscribeDialog = true) }
    }

    fun closeSubscribeDialog() {
        _uiState.update { it.copy(selectedPlan = null, showSubscribeDialog = false) }
    }

    fun openCancelDialog(invoice: Invoice) {
        _uiState.update { it.copy(cancelTargetInvoice = invoice, showCancelDialog = true) }
    }

    fun closeCancelDialog() {
        _uiState.update { it.copy(cancelTargetInvoice = null, showCancelDialog = false) }
    }

    fun subscribe() {
        val plan = _uiState.value.selectedPlan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = billingRepository.createInvoice(plan.code)) {
                is Resource.Success -> {
                    val invoice = result.data
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showSubscribeDialog = false,
                            selectedPlan = null,
                            invoices = listOf(invoice) + it.invoices,
                            // Jika QR tersedia → buka dialog QR langsung.
                            // Jika tidak (mis. trial) → tampilkan notifikasi teks biasa.
                            qrInvoice = invoice.takeIf { inv -> inv.qrString != null },
                            successMessage = if (invoice.qrString == null)
                                "Invoice berhasil dibuat. Silakan selesaikan pembayaran."
                            else null
                        )
                    }
                    if (invoice.qrString != null) {
                        startPolling(invoice.uuid)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showSubscribeDialog = false,
                            error = result.message
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    /**
     * Polling 2 detik ke `GET /billing/invoices/{uuid}` menunggu konfirmasi webhook Xendit.
     *
     * Alur normal:
     *   1. Xendit menerima pembayaran → mengirim webhook ke backend Rancak.
     *   2. Backend mengupdate status invoice ke "paid".
     *   3. Polling mendeteksi perubahan → set [BillingUiState.isPaymentComplete] = true.
     *
     * Berhenti saat:
     *   - status = "paid"        → sukses, trigger nav ke POS
     *   - status = "cancelled" / "expired" → gagal, tampilkan pesan
     *   - [MAX_POLL_ITERATIONS] tercapai → timeout 10 menit, tampilkan pesan
     *   - [MAX_CONSECUTIVE_ERRORS] error berturut-turut → koneksi bermasalah
     *   - [dismissQrPayment] dipanggil (pollingJob.cancel()) → isActive = false
     */
    private fun startPolling(invoiceUuid: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true) }
            var iterations = 0
            var consecutiveErrors = 0
            while (isActive && iterations < MAX_POLL_ITERATIONS) {
                delay(2_000L)
                iterations++
                when (val result = billingRepository.getInvoice(invoiceUuid)) {
                    is Resource.Success -> {
                        consecutiveErrors = 0
                        val updated = result.data
                        // Selalu sinkronkan list invoice agar status terbaru tampil.
                        _uiState.update { state ->
                            state.copy(
                                invoices = state.invoices.map {
                                    if (it.uuid == invoiceUuid) updated else it
                                }
                            )
                        }
                        when (updated.status) {
                            "paid" -> {
                                // Webhook Xendit diterima backend → konfirmasi sukses.
                                _uiState.update {
                                    it.copy(
                                        isPolling = false,
                                        qrInvoice = null,
                                        isPaymentComplete = true
                                    )
                                }
                                return@launch
                            }
                            "cancelled", "expired" -> {
                                _uiState.update {
                                    it.copy(
                                        isPolling = false,
                                        qrInvoice = null,
                                        error = "Invoice dibatalkan atau kedaluwarsa."
                                    )
                                }
                                return@launch
                            }
                            // "pending" → lanjut polling
                        }
                    }
                    is Resource.Error -> {
                        consecutiveErrors++
                        if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                            _uiState.update {
                                it.copy(
                                    isPolling = false,
                                    qrInvoice = null,
                                    error = "Koneksi bermasalah. Buka riwayat invoice untuk cek status pembayaran."
                                )
                            }
                            return@launch
                        }
                        // Error sementara (< threshold) → lanjut polling
                    }
                    else -> Unit
                }
            }
            // Loop selesai karena timeout (bukan dari status terminal)
            if (isActive) {
                _uiState.update {
                    it.copy(
                        isPolling = false,
                        qrInvoice = null,
                        error = "Waktu tunggu pembayaran habis. Silakan cek riwayat invoice untuk memverifikasi status."
                    )
                }
            }
        }
    }

    companion object {
        /** Batas iterasi polling: 300 × 2 detik = 10 menit. */
        private const val MAX_POLL_ITERATIONS = 300
        /** Hentikan polling setelah 5 error jaringan berturut-turut. */
        private const val MAX_CONSECUTIVE_ERRORS = 5
    }

    /** Dipanggil saat user menutup dialog QR secara manual. */
    fun dismissQrPayment() {
        pollingJob?.cancel()
        pollingJob = null
        _uiState.update { it.copy(qrInvoice = null, isPolling = false) }
    }

    /** Dipanggil oleh Screen setelah navigasi ke POS terpicu. */
    fun clearPaymentComplete() {
        _uiState.update { it.copy(isPaymentComplete = false) }
    }

    /** Buka kembali dialog QR untuk invoice pending yang sudah punya qrString. */
    fun showQrPayment(invoice: Invoice) {
        if (invoice.qrString == null) return
        _uiState.update { it.copy(qrInvoice = invoice) }
        startPolling(invoice.uuid)
    }

    fun cancelInvoice() {
        val invoice = _uiState.value.cancelTargetInvoice ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = billingRepository.cancelInvoice(invoice.uuid)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            showCancelDialog = false,
                            cancelTargetInvoice = null,
                            successMessage = "Invoice berhasil dibatalkan.",
                            invoices = state.invoices.map {
                                if (it.uuid == invoice.uuid) it.copy(status = "cancelled") else it
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showCancelDialog = false,
                            error = result.message
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    /** Dipanggil oleh pull-to-refresh — hanya memuat ulang invoices tanpa full-screen loader. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            val invoicesResult = billingRepository.getInvoices()
            _uiState.update { state ->
                var s = state.copy(isRefreshing = false)
                if (invoicesResult is Resource.Success) s = s.copy(invoices = invoicesResult.data)
                if (invoicesResult is Resource.Error) s = s.copy(error = invoicesResult.message)
                s
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
}
