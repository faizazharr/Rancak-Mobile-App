package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.domain.repository.SaleRepository
import id.rancak.app.domain.repository.SplitPaymentEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Interval polling status QRIS (ms) */
private const val QRIS_POLL_INTERVAL_MS = 3_000L

/**
 * Batas atas nominal yang diterima pada input pembayaran (≈ 1 triliun Rupiah).
 * Mencegah:
 *  - Integer overflow saat menjumlah pembayaran split atau kembalian.
 *  - User iseng paste angka jutaan digit yang bisa memperlambat UI.
 */
private const val MAX_AMOUNT: Long = 999_999_999_999L

data class PaymentUiState(
    val selectedMethod: PaymentMethod = PaymentMethod.CASH,
    val paidAmount: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val completedSale: Sale? = null,

    // ── Split-payment ─────────────────────────────────────────────────────────
    /** True berarti mode pembayaran terbagi aktif. */
    val isSplitPayment: Boolean = false,
    /** Daftar entri pembayaran terbagi. */
    val splitPayments: List<SplitPaymentEntry> = emptyList(),

    // ── QRIS-specific ─────────────────────────────────────────────────────────
    /** QR string dari Xendit — non-null berarti tampilkan layar QR. */
    val qrisQrString: String? = null,
    /** UUID sale yang sedang menunggu pembayaran QRIS. */
    val qrisSaleUuid: String? = null,
    /** Nominal QRIS yang harus dibayar. */
    val qrisAmount: Long = 0,
    /** True saat polling aktif. */
    val isQrisPolling: Boolean = false
) {
    val paidAmountLong: Long get() = paidAmount.toLongOrNull() ?: 0L

    /** Total semua entri split payment. */
    val splitPaymentTotal: Long get() = splitPayments.sumOf { it.amount }

    /** Apakah sedang dalam layar tunggu QR QRIS. */
    val isQrisWaiting: Boolean get() = qrisQrString != null && completedSale == null
}

class PaymentViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    /** Job polling QRIS — dibatalkan saat layar ditutup atau pembayaran sukses. */
    private var qrisPollingJob: Job? = null

    fun selectMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method, error = null) }
    }

    fun setPaidAmount(amount: String) {
        // Hanya simpan digit, lalu clamp ke MAX_AMOUNT untuk cegah overflow.
        val digitsOnly = amount.filter { c -> c.isDigit() }
        val clamped = digitsOnly.toLongOrNull()?.let {
            if (it > MAX_AMOUNT) MAX_AMOUNT.toString() else digitsOnly
        } ?: digitsOnly
        _uiState.update { it.copy(paidAmount = clamped, error = null) }
    }

    fun processPayment(
        items: List<CartItem>,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String?,
        note: String?,
        pax: Int = 1,
        discount: Long = 0,
        tax: Long = 0,
        adminFee: Long = 0,
        deliveryFee: Long = 0,
        tip: Long = 0,
        voucherCode: String? = null
    ) {
        val state = _uiState.value
        if (state.paidAmountLong <= 0 && state.selectedMethod == PaymentMethod.CASH) {
            _uiState.update { it.copy(error = "Masukkan jumlah pembayaran") }
            return
        }

        // Validasi diskon tidak boleh melebihi subtotal item.
        val subtotal = items.sumOf { it.price * it.qty }
        if (discount < 0 || discount > subtotal) {
            _uiState.update { it.copy(error = "Diskon tidak valid") }
            return
        }
        // Cegah overflow pada penjumlahan total (defensive — backend juga validasi).
        if (subtotal > MAX_AMOUNT || state.paidAmountLong > MAX_AMOUNT) {
            _uiState.update { it.copy(error = "Nominal melebihi batas") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }

            // Untuk QRIS, paid_amount = 0 (belum dibayar, menunggu QR)
            val actualPaidAmount = if (state.selectedMethod == PaymentMethod.QRIS) 0L
                                   else state.paidAmountLong

            val total = items.sumOf { it.price * it.qty } - discount + tax + adminFee + deliveryFee + tip

            when (val result = saleRepository.createSale(
                items         = items,
                paymentMethod = state.selectedMethod,
                paidAmount    = actualPaidAmount,
                orderType     = orderType,
                tableUuid     = tableUuid,
                customerName  = customerName?.takeIf { it.isNotBlank() },
                note          = note?.takeIf { it.isNotBlank() },
                hold          = false,
                pax           = pax,
                discount      = discount,
                tax           = tax,
                adminFee      = adminFee,
                deliveryFee   = deliveryFee,
                tip           = tip,
                voucherCode   = voucherCode?.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    val sale = result.data
                    if (state.selectedMethod == PaymentMethod.QRIS) {
                        // Lanjut ke pembuatan QR Xendit
                        initiateQrisPayment(sale.uuid, sale.total.takeIf { it > 0 } ?: total)
                    } else {
                        _uiState.update { it.copy(isProcessing = false, completedSale = sale) }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isProcessing = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    /**
     * Tahan pesanan sebagai **open bill** (status HELD di backend).
     * Tidak memerlukan jumlah bayar / metode pembayaran. Stok belum dipotong;
     * stok baru dipotong saat pesanan dibayar via [SaleRepository.payHeldOrder].
     */
    fun holdOrder(
        items: List<CartItem>,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String?,
        note: String?,
        pax: Int = 1,
        discount: Long = 0,
        tax: Long = 0,
        adminFee: Long = 0,
        deliveryFee: Long = 0,
        tip: Long = 0,
        voucherCode: String? = null
    ) {
        val subtotal = items.sumOf { it.price * it.qty }
        if (discount < 0 || discount > subtotal) {
            _uiState.update { it.copy(error = "Diskon tidak valid") }
            return
        }
        if (subtotal > MAX_AMOUNT) {
            _uiState.update { it.copy(error = "Nominal melebihi batas") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            when (val result = saleRepository.createSale(
                items         = items,
                paymentMethod = PaymentMethod.CASH, // placeholder; backend abaikan saat hold
                paidAmount    = 0L,
                orderType     = orderType,
                tableUuid     = tableUuid,
                customerName  = customerName?.takeIf { it.isNotBlank() },
                note          = note?.takeIf { it.isNotBlank() },
                hold          = true,
                pax           = pax,
                discount      = discount,
                tax           = tax,
                adminFee      = adminFee,
                deliveryFee   = deliveryFee,
                tip           = tip,
                voucherCode   = voucherCode?.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isProcessing = false, completedSale = result.data)
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isProcessing = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    /** Dipanggil setelah sale dibuat dengan QRIS — membuat QR lalu mulai polling. */
    private suspend fun initiateQrisPayment(saleUuid: String, amount: Long) {
        when (val qrResult = saleRepository.createQrPayment(saleUuid)) {
            is Resource.Success -> {
                val qr = qrResult.data
                _uiState.update {
                    it.copy(
                        isProcessing   = false,
                        qrisQrString   = qr.qrString,
                        qrisSaleUuid   = saleUuid,
                        qrisAmount     = amount,
                        isQrisPolling  = true
                    )
                }
                startQrisPolling(saleUuid)
            }
            is Resource.Error -> {
                _uiState.update { it.copy(isProcessing = false, error = qrResult.message) }
            }
            is Resource.Loading -> {}
        }
    }

    /** Polling status QR setiap [QRIS_POLL_INTERVAL_MS] ms. */
    private fun startQrisPolling(saleUuid: String) {
        qrisPollingJob?.cancel()
        qrisPollingJob = viewModelScope.launch {
            while (true) {
                delay(QRIS_POLL_INTERVAL_MS)
                when (val statusResult = saleRepository.getQrPaymentStatus(saleUuid)) {
                    is Resource.Success -> {
                        when (statusResult.data.status) {
                            QrPaymentStatus.SUCCEEDED -> {
                                // Ambil detail sale yang sudah lunas
                                val saleResult = saleRepository.getSaleDetail(saleUuid)
                                val completedSale = if (saleResult is Resource.Success) saleResult.data
                                                   else null
                                _uiState.update {
                                    it.copy(
                                        isQrisPolling = false,
                                        completedSale = completedSale ?: it.completedSale,
                                        // Buat dummy Sale jika getSaleDetail gagal tapi QR sudah succeeded
                                        qrisQrString  = null
                                    )
                                }
                                break
                            }
                            QrPaymentStatus.EXPIRED, QrPaymentStatus.FAILED -> {
                                _uiState.update {
                                    it.copy(
                                        isQrisPolling = false,
                                        error         = "QR QRIS kadaluarsa. Silakan coba lagi.",
                                        qrisQrString  = null,
                                        qrisSaleUuid  = null
                                    )
                                }
                                break
                            }
                            QrPaymentStatus.PENDING -> { /* terus polling */ }
                        }
                    }
                    is Resource.Error -> {
                        // Jika terjadi network error, tetap polling (jangan stop)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    /** Batalkan pembayaran QRIS yang sedang menunggu. */
    fun cancelQrisPayment() {
        qrisPollingJob?.cancel()
        qrisPollingJob = null
        _uiState.update {
            it.copy(
                isQrisPolling = false,
                qrisQrString  = null,
                qrisSaleUuid  = null,
                qrisAmount    = 0,
                error         = null,
                isProcessing  = false
            )
        }
    }

    // ── Split Payment ─────────────────────────────────────────────────────────

    fun toggleSplitPayment() {
        _uiState.update { state ->
            state.copy(
                isSplitPayment = !state.isSplitPayment,
                splitPayments  = emptyList(),
                error          = null
            )
        }
    }

    fun addSplitPaymentEntry(method: PaymentMethod, amount: Long) {
        if (amount <= 0) return
        _uiState.update { state ->
            state.copy(splitPayments = state.splitPayments + SplitPaymentEntry(method, amount))
        }
    }

    fun removeSplitPaymentEntry(index: Int) {
        _uiState.update { state ->
            val updated = state.splitPayments.toMutableList().also { it.removeAt(index) }
            state.copy(splitPayments = updated)
        }
    }

    /**
     * Proses pembayaran split untuk order baru dari cart.
     * Validasi: total split harus >= total order.
     */
    fun processPaymentWithSplit(
        items: List<CartItem>,
        orderTotal: Long,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String?,
        note: String?,
        pax: Int = 1,
        discount: Long = 0,
        tax: Long = 0,
        adminFee: Long = 0,
        deliveryFee: Long = 0,
        tip: Long = 0,
        voucherCode: String? = null
    ) {
        val state = _uiState.value
        if (state.splitPayments.isEmpty()) {
            _uiState.update { it.copy(error = "Tambahkan minimal satu metode pembayaran") }
            return
        }
        if (state.splitPaymentTotal < orderTotal) {
            _uiState.update { it.copy(error = "Total pembayaran belum mencukupi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            when (val result = saleRepository.createSaleWithSplitPayment(
                items       = items,
                payments    = state.splitPayments,
                orderType   = orderType,
                tableUuid   = tableUuid,
                customerName = customerName?.takeIf { it.isNotBlank() },
                note        = note?.takeIf { it.isNotBlank() },
                pax         = pax,
                discount    = discount,
                tax         = tax,
                adminFee    = adminFee,
                deliveryFee = deliveryFee,
                tip         = tip,
                voucherCode = voucherCode?.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success ->
                    _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                is Resource.Error ->
                    _uiState.update { it.copy(isProcessing = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    /**
     * Bayar held order (single method).
     */
    fun processHeldOrderPayment(saleUuid: String) {
        val state = _uiState.value
        if (state.paidAmountLong <= 0 && state.selectedMethod == PaymentMethod.CASH) {
            _uiState.update { it.copy(error = "Masukkan jumlah pembayaran") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            when (val result = saleRepository.paySale(
                saleUuid      = saleUuid,
                paymentMethod = state.selectedMethod,
                paidAmount    = state.paidAmountLong
            )) {
                is Resource.Success ->
                    _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                is Resource.Error ->
                    _uiState.update { it.copy(isProcessing = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    /**
     * Bayar held order dengan split payment.
     */
    fun processHeldOrderPaymentWithSplit(saleUuid: String, orderTotal: Long) {
        val state = _uiState.value
        if (state.splitPayments.isEmpty()) {
            _uiState.update { it.copy(error = "Tambahkan minimal satu metode pembayaran") }
            return
        }
        if (state.splitPaymentTotal < orderTotal) {
            _uiState.update { it.copy(error = "Total pembayaran belum mencukupi") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            when (val result = saleRepository.paySaleWithSplitPayment(saleUuid, state.splitPayments)) {
                is Resource.Success ->
                    _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                is Resource.Error ->
                    _uiState.update { it.copy(isProcessing = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun reset() {
        qrisPollingJob?.cancel()
        qrisPollingJob = null
        _uiState.value = PaymentUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        qrisPollingJob?.cancel()
    }
}
