package id.rancak.app.presentation.viewmodel

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
    val isPaymentComplete: Boolean = false
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
     * Polling 2 detik ke `GET /billing/invoices/{uuid}` menunggu status berubah.
     * Berhenti saat: paid (sukses), cancelled/expired (gagal), atau [dismissQrPayment].
     */
    private fun startPolling(invoiceUuid: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true) }
            while (isActive) {
                delay(2_000L)
                val result = billingRepository.getInvoice(invoiceUuid)
                if (result is Resource.Success) {
                    val updated = result.data
                    // Selalu update data invoice di list agar status terbaru tampil.
                    _uiState.update { state ->
                        state.copy(
                            invoices = state.invoices.map {
                                if (it.uuid == invoiceUuid) updated else it
                            }
                        )
                    }
                    when (updated.status) {
                        "paid" -> {
                            // Pembayaran dikonfirmasi Xendit — tutup QR, trigger nav ke POS.
                            _uiState.update {
                                it.copy(
                                    isPolling = false,
                                    qrInvoice = null,
                                    isPaymentComplete = true
                                )
                            }
                            break
                        }
                        "cancelled", "expired" -> {
                            _uiState.update {
                                it.copy(
                                    isPolling = false,
                                    qrInvoice = null,
                                    error = "Invoice dibatalkan atau kedaluwarsa."
                                )
                            }
                            break
                        }
                        // "pending" → lanjut polling
                    }
                }
            }
            _uiState.update { it.copy(isPolling = false) }
        }
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

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
}
