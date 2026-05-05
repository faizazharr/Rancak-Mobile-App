package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.QrPayment
import id.rancak.app.domain.model.QrPaymentStatus
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SplitPaymentEntry
import id.rancak.app.domain.model.User
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Interval polling status QRIS (ms) — sesuai rekomendasi API docs */
private const val QRIS_POLL_INTERVAL_MS = 2_000L

/** Maksimum polling sebelum timeout (~5 menit @ 2s/poll) */
private const val QRIS_MAX_POLLS = 150

/**
 * Batas atas nominal yang diterima pada input pembayaran (≈ 1 triliun Rupiah).
 * Mencegah:
 *  - Integer overflow saat menjumlah pembayaran split atau kembalian.
 *  - User iseng paste angka jutaan digit yang bisa memperlambat UI.
 */
private const val MAX_AMOUNT: Long = 999_999_999_999L

/** Satu item yang dapat dibagi dalam split bill. */
@Immutable
data class SplitableItem(
    val index: Int,
    val name: String,
    val qty: Int,
    val price: Long,
    val variantName: String? = null
) {
    val subtotal: Long get() = price * qty
}

/** Satu grup pelanggan dalam split bill.
 *  [itemQtys]: itemIndex → jumlah qty yang dibayar oleh grup ini.
 *  [groupActualTotal]: total yang harus dibayar grup ini (item subtotal + biaya proporsional).
 */
@Immutable
data class SplitGroup(
    val id: Int,
    val itemQtys: Map<Int, Int>,
    val method: PaymentMethod,
    val cashPaid: Long = 0L,  // hanya untuk CASH; QRIS menggunakan groupActualTotal
    val groupActualTotal: Long = 0L  // item subtotal + biaya proporsional
)

@Immutable
data class PaymentUiState(
    val selectedMethod: PaymentMethod = PaymentMethod.CASH,
    val paidAmount: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val completedSale: Sale? = null,

    // ── Split-payment (item-based) ────────────────────────────────────────────
    /** True berarti mode pembayaran terbagi aktif. */
    val isSplitPayment: Boolean = false,
    /** Semua item order yang dapat dibagi. */
    val splitableItems: List<SplitableItem> = emptyList(),
    /** Grup pelanggan yang sudah dikonfirmasi. */
    val splitGroups: List<SplitGroup> = emptyList(),
    /** Qty setiap item yang sedang dipilih untuk grup berikutnya (itemIndex → qty). */
    val currentSplitItemQtys: PersistentMap<Int, Int> = persistentMapOf(),
    /** Metode bayar untuk grup yang sedang dibuat. */
    val currentSplitMethod: PaymentMethod = PaymentMethod.CASH,
    /** Input nominal uang tunai untuk grup saat ini. */
    val currentSplitCashInput: String = "",

    // ── QRIS-specific ─────────────────────────────────────────────────────────
    /** QR string dari Xendit — non-null berarti tampilkan layar QR. */
    val qrisQrString: String? = null,
    /** UUID sale yang sedang menunggu pembayaran QRIS. */
    val qrisSaleUuid: String? = null,
    /** Nominal QRIS yang harus dibayar. */
    val qrisAmount: Long = 0,
    /** True saat polling aktif. */
    val isQrisPolling: Boolean = false,

    // ── Held-order loading ────────────────────────────────────────────────────
    /** Sale yang sedang dibayar dari held order — dimuat via [PaymentViewModel.loadHeldSale]. */
    val heldSale: Sale? = null,
    /** Pesan error saat memuat held sale, jika ada. */
    val heldSaleError: String? = null,
    /**
     * True saat server menolak pembayaran karena order sudah berstatus 'paid'.
     * Ini berarti ada stale bill di local store — user perlu diarahkan untuk
     * menghapusnya dari daftar.
     */
    val saleAlreadyPaid: Boolean = false
) {
    val paidAmountLong: Long get() = paidAmount.toLongOrNull() ?: 0L

    /** Total subtotal item yang dipilih untuk grup saat ini. */
    val currentSplitSubtotal: Long get() {
        val priceMap = splitableItems.associate { it.index to it.price }
        return currentSplitItemQtys.entries.sumOf { (idx, qty) ->
            (priceMap[idx] ?: 0L) * qty
        }
    }

    /** Jumlah qty yang sudah dikonfirmasi ke grup per item (itemIndex → totalQty). */
    val confirmedQtyMap: Map<Int, Int> get() {
        val result = mutableMapOf<Int, Int>()
        splitGroups.forEach { group ->
            group.itemQtys.forEach { (idx, qty) ->
                result[idx] = (result[idx] ?: 0) + qty
            }
        }
        return result
    }

    /** True ketika semua item qty sudah dibagi penuh ke grup yang ada. */
    val allItemsAssigned: Boolean get() {
        if (splitableItems.isEmpty()) return false
        val confirmed = confirmedQtyMap
        return splitableItems.all { item -> (confirmed[item.index] ?: 0) >= item.qty }
    }

    /** Hitung subtotal item untuk satu grup berdasarkan qty masing-masing. */
    fun splitGroupSubtotal(group: SplitGroup): Long {
        val priceMap = splitableItems.associate { it.index to it.price }
        return group.itemQtys.entries.sumOf { (idx, qty) ->
            (priceMap[idx] ?: 0L) * qty
        }
    }

    /** Apakah sedang dalam layar tunggu QR QRIS. */
    val isQrisWaiting: Boolean get() = qrisQrString?.isNotBlank() == true && completedSale == null
}

class PaymentViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    /** Job polling QRIS — dibatalkan saat layar ditutup atau pembayaran sukses. */
    private var qrisPollingJob: Job? = null

    fun selectMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    fun setPaidAmount(amount: String) {
        // Hanya simpan digit, lalu clamp ke MAX_AMOUNT untuk cegah overflow.
        val digitsOnly = amount.filter { c -> c.isDigit() }
        val clamped = digitsOnly.toLongOrNull()?.let {
            if (it > MAX_AMOUNT) MAX_AMOUNT.toString() else digitsOnly
        } ?: digitsOnly
        _uiState.update { it.copy(paidAmount = clamped) }
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
        val subtotal = items.sumOf { it.price * it.qty }

        // ── QRIS: create held order first, backend pays via Xendit webhook ────
        // Held orders don't require paid_amount — avoids backend rejection.
        if (state.selectedMethod == PaymentMethod.QRIS) {
            if (subtotal > MAX_AMOUNT) {
                _uiState.update { it.copy(error = "Nominal melebihi batas") }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isProcessing = true, error = null) }
                try {
                    when (val result = saleRepository.createSale(
                        items        = items,
                        paymentMethod = PaymentMethod.CASH, // placeholder; backend ignores for held
                        paidAmount   = 0L,
                        orderType    = orderType,
                        tableUuid    = tableUuid,
                        customerName = customerName?.takeIf { it.isNotBlank() },
                        note         = note?.takeIf { it.isNotBlank() },
                        hold         = true,
                        pax          = pax,
                        discount     = discount,
                        tax          = tax,
                        adminFee     = adminFee,
                        deliveryFee  = deliveryFee,
                        tip          = tip,
                        voucherCode  = voucherCode?.takeIf { it.isNotBlank() }
                    )) {
                        is Resource.Success ->
                            initiateQrisPayment(result.data.uuid, result.data.total)
                        is Resource.Error ->
                            _uiState.update { it.copy(isProcessing = false, error = result.message) }
                        is Resource.Loading -> {}
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isProcessing = false, error = e.message ?: "Terjadi kesalahan, silakan coba lagi") }
                }
            }
            return
        }

        // ── Non-QRIS: normal direct payment flow ──────────────────────────────
        if (state.paidAmountLong <= 0 && state.selectedMethod == PaymentMethod.CASH) {
            _uiState.update { it.copy(error = "Masukkan jumlah pembayaran") }
            return
        }
        val total = subtotal - discount + tax + adminFee + deliveryFee + tip
        if (state.selectedMethod == PaymentMethod.CASH && state.paidAmountLong < total) {
            _uiState.update { it.copy(error = "Jumlah bayar kurang dari total transaksi") }
            return
        }
        if (discount < 0 || discount > subtotal) {
            _uiState.update { it.copy(error = "Diskon tidak valid") }
            return
        }
        if (subtotal > MAX_AMOUNT || state.paidAmountLong > MAX_AMOUNT) {
            _uiState.update { it.copy(error = "Nominal melebihi batas") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                when (val result = saleRepository.createSale(
                    items         = items,
                    paymentMethod = state.selectedMethod,
                    paidAmount    = state.paidAmountLong,
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
                    is Resource.Success ->
                        _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                    is Resource.Error ->
                        _uiState.update { it.copy(isProcessing = false, error = result.message) }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = e.message ?: "Terjadi kesalahan, silakan coba lagi") }
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
                if (qr.qrString.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isProcessing  = false,
                            error         = "QR string kosong — pastikan Xendit API key sudah dikonfigurasi"
                        )
                    }
                    return
                }
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

    /** Polling status QR setiap [QRIS_POLL_INTERVAL_MS] ms hingga [QRIS_MAX_POLLS] kali. */
    private fun startQrisPolling(saleUuid: String) {
        qrisPollingJob?.cancel()
        qrisPollingJob = viewModelScope.launch {
            var pollCount  = 0
            var errorCount = 0

            while (pollCount < QRIS_MAX_POLLS) {
                delay(QRIS_POLL_INTERVAL_MS)
                pollCount++

                when (val statusResult = saleRepository.getQrPaymentStatus(saleUuid)) {
                    is Resource.Success -> {
                        errorCount = 0
                        when (statusResult.data.status) {
                            QrPaymentStatus.SUCCEEDED -> {
                                val saleResult = saleRepository.getSaleDetail(saleUuid)
                                _uiState.update {
                                    it.copy(
                                        isQrisPolling = false,
                                        qrisQrString  = null,
                                        completedSale = (saleResult as? Resource.Success)?.data
                                    )
                                }
                                return@launch
                            }
                            QrPaymentStatus.EXPIRED, QrPaymentStatus.FAILED -> {
                                _uiState.update {
                                    it.copy(
                                        isQrisPolling = false,
                                        qrisQrString  = null,
                                        qrisSaleUuid  = null,
                                        error         = "QR QRIS kadaluarsa atau gagal. Silakan coba lagi."
                                    )
                                }
                                return@launch
                            }
                            QrPaymentStatus.PENDING -> { /* lanjut polling */ }
                        }
                    }
                    is Resource.Error -> {
                        errorCount++
                        // Setiap 5 error berturut-turut, beri tahu user tapi tetap polling
                        if (errorCount >= 5) {
                            _uiState.update { it.copy(error = "Koneksi bermasalah, tetap mencoba...") }
                            errorCount = 0
                        }
                    }
                    is Resource.Loading -> {}
                }
            }

            // Timeout — QR mungkin sudah expired di sisi server
            _uiState.update {
                it.copy(
                    isQrisPolling = false,
                    qrisQrString  = null,
                    qrisSaleUuid  = null,
                    error         = "Waktu tunggu QR habis. Silakan coba lagi."
                )
            }
        }
    }

    /** Batalkan pembayaran QRIS yang sedang menunggu. */
    fun cancelQrisPayment() {
        qrisPollingJob?.cancel()
        qrisPollingJob = null
        val pendingSaleUuid = _uiState.value.qrisSaleUuid
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
        // Batalkan held order di server agar tidak meninggalkan sale "phantom"
        if (pendingSaleUuid != null) {
            viewModelScope.launch {
                saleRepository.cancelSale(pendingSaleUuid, "Pembayaran QRIS dibatalkan oleh kasir")
            }
        }
    }

    // ── Split Payment (item-based) ────────────────────────────────────────────

    fun toggleSplitPayment() {
        _uiState.update { state ->
            state.copy(
                isSplitPayment        = !state.isSplitPayment,
                splitableItems        = emptyList(),
                splitGroups           = emptyList(),
                currentSplitItemQtys  = persistentMapOf(),
                currentSplitMethod    = PaymentMethod.CASH,
                currentSplitCashInput = "",
                error                 = null
            )
        }
    }

    /** Inisialisasi daftar item yang bisa dibagi; dipanggil saat mode split aktif. */
    fun initSplitItems(items: List<SplitableItem>) {
        _uiState.update { state ->
            state.copy(
                splitableItems        = items,
                splitGroups           = emptyList(),
                currentSplitItemQtys  = persistentMapOf(),
                currentSplitMethod    = PaymentMethod.CASH,
                currentSplitCashInput = "",
                error                 = null
            )
        }
    }

    /**
     * Set qty untuk item tertentu pada grup yang sedang dibangun.
     * qty = 0 menghapus item dari seleksi saat ini.
     */
    fun setCurrentSplitItemQty(index: Int, qty: Int) {
        _uiState.update { state ->
            val updated = if (qty <= 0)
                state.currentSplitItemQtys.remove(index)
            else
                state.currentSplitItemQtys.put(index, qty)
            state.copy(currentSplitItemQtys = updated)
        }
    }

    fun setCurrentSplitMethod(method: PaymentMethod) {
        _uiState.update { it.copy(currentSplitMethod = method, currentSplitCashInput = "", error = null) }
    }

    fun setCurrentSplitCashInput(input: String) {
        val digits = input.filter { c -> c.isDigit() }
        val clamped = digits.toLongOrNull()?.let {
            if (it > MAX_AMOUNT) MAX_AMOUNT.toString() else digits
        } ?: digits
        _uiState.update { it.copy(currentSplitCashInput = clamped, error = null) }
    }

    /** Konfirmasi grup saat ini dan tambahkan ke daftar split groups.
     *  [groupActualTotal]: total yang harus dibayar grup ini (item subtotal + biaya proporsional).
     *  Gunakan 0 sebagai default saat tidak ada biaya tambahan.
     */
    fun confirmCurrentSplitGroup(groupActualTotal: Long = 0L) {
        val state = _uiState.value
        if (state.currentSplitItemQtys.isEmpty()) {
            _uiState.update { it.copy(error = "Pilih minimal satu item untuk grup ini") }
            return
        }
        val subtotal = state.currentSplitSubtotal
        val expectedAmount = if (groupActualTotal > 0) groupActualTotal else subtotal
        if (state.currentSplitMethod == PaymentMethod.CASH) {
            val cashPaid = state.currentSplitCashInput.toLongOrNull() ?: 0L
            if (cashPaid < expectedAmount) {
                _uiState.update { it.copy(error = "Uang yang diberikan kurang dari total item") }
                return
            }
        }
        val newId = (state.splitGroups.maxOfOrNull { it.id } ?: 0) + 1
        val newGroup = SplitGroup(
            id               = newId,
            itemQtys         = state.currentSplitItemQtys,
            method           = state.currentSplitMethod,
            cashPaid         = if (state.currentSplitMethod == PaymentMethod.CASH)
                                   state.currentSplitCashInput.toLongOrNull() ?: 0L
                               else 0L,
            groupActualTotal = if (groupActualTotal > 0) groupActualTotal else subtotal
        )
        _uiState.update {
            it.copy(
                splitGroups           = it.splitGroups + newGroup,
                currentSplitItemQtys  = persistentMapOf(),
                currentSplitMethod    = PaymentMethod.CASH,
                currentSplitCashInput = "",
                error                 = null
            )
        }
    }

    /** Hapus grup dan kembalikan item-nya ke status belum dibagi. */
    fun removeSplitGroup(groupId: Int) {
        _uiState.update { state ->
            state.copy(splitGroups = state.splitGroups.filter { it.id != groupId })
        }
    }

    /**
     * Proses pembayaran split untuk order baru dari cart.
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
        if (state.splitGroups.isEmpty()) {
            _uiState.update { it.copy(error = "Tambahkan minimal satu grup pembayaran") }
            return
        }
        if (!state.allItemsAssigned) {
            _uiState.update { it.copy(error = "Masih ada item yang belum dibagi ke grup") }
            return
        }
        val payments = state.splitGroups.map { group ->
            val amount = if (group.method == PaymentMethod.CASH && group.cashPaid > 0)
                group.cashPaid
            else
                group.groupActualTotal.takeIf { it > 0 } ?: state.splitGroupSubtotal(group)
            SplitPaymentEntry(group.method, amount)
        }
        // Catatan: untuk split bill, QRIS dibayar per pelanggan via QRIS statis merchant
        // (di luar backend). Tidak perlu generate QR dinamis Xendit di sini.

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                when (val result = saleRepository.createSaleWithSplitPayment(
                    items        = items,
                    payments     = payments,
                    orderType    = orderType,
                    tableUuid    = tableUuid,
                    customerName = customerName?.takeIf { it.isNotBlank() },
                    note         = note?.takeIf { it.isNotBlank() },
                    pax          = pax,
                    discount     = discount,
                    tax          = tax,
                    adminFee     = adminFee,
                    deliveryFee  = deliveryFee,
                    tip          = tip,
                    voucherCode  = voucherCode?.takeIf { it.isNotBlank() }
                )) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                    }
                    is Resource.Error ->
                        _uiState.update { it.copy(isProcessing = false, error = result.message) }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = e.message ?: "Terjadi kesalahan, silakan coba lagi") }
            }
        }
    }

    /**
     * Bayar held order (single method).
     * [saleTotal] dipakai sebagai nominal QR QRIS (tampilan di layar tunggu).
     */
    fun processHeldOrderPayment(saleUuid: String, saleTotal: Long = 0L) {
        val state = _uiState.value

        // QRIS: langsung buat QR — tidak perlu memanggil /pay terlebih dahulu.
        // Pembayaran dikonfirmasi via Xendit webhook, bukan oleh client.
        if (state.selectedMethod == PaymentMethod.QRIS) {
            viewModelScope.launch {
                _uiState.update { it.copy(isProcessing = true, error = null) }
                initiateQrisPayment(saleUuid, saleTotal)
            }
            return
        }

        if (state.paidAmountLong <= 0 && state.selectedMethod == PaymentMethod.CASH) {
            _uiState.update { it.copy(error = "Masukkan jumlah pembayaran") }
            return
        }
        if (state.selectedMethod == PaymentMethod.CASH && saleTotal > 0 && state.paidAmountLong < saleTotal) {
            _uiState.update { it.copy(error = "Jumlah bayar kurang dari total transaksi") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                when (val result = saleRepository.paySale(
                    saleUuid      = saleUuid,
                    paymentMethod = state.selectedMethod,
                    paidAmount    = state.paidAmountLong
                )) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                    }
                    is Resource.Error -> {
                        // 400 dengan pesan 'paid' berarti order sudah dibayar sebelumnya — ini
                        // stale bill di local store, bukan kesalahan user.
                        if (result.message?.contains("paid", ignoreCase = true) == true) {
                            _uiState.update { it.copy(isProcessing = false, saleAlreadyPaid = true) }
                        } else {
                            _uiState.update { it.copy(isProcessing = false, error = result.message) }
                        }
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = e.message ?: "Terjadi kesalahan, silakan coba lagi") }
            }
        }
    }

    /**
     * Bayar held order dengan split payment (item-based groups).
     */
    fun processHeldOrderPaymentWithSplit(saleUuid: String, orderTotal: Long) {
        val state = _uiState.value
        if (state.splitGroups.isEmpty()) {
            _uiState.update { it.copy(error = "Tambahkan minimal satu grup pembayaran") }
            return
        }
        if (!state.allItemsAssigned) {
            _uiState.update { it.copy(error = "Masih ada item yang belum dibagi ke grup") }
            return
        }
        val payments = state.splitGroups.map { group ->
            val amount = if (group.method == PaymentMethod.CASH && group.cashPaid > 0)
                group.cashPaid
            else
                group.groupActualTotal.takeIf { it > 0 } ?: state.splitGroupSubtotal(group)
            SplitPaymentEntry(group.method, amount)
        }
        // Catatan: QRIS pada split bill dibayar via QRIS statis merchant per pelanggan
        // (lihat dialog QRIS di SplitPaymentColumn). Tidak perlu generate QR dinamis di sini.

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                when (val result = saleRepository.paySaleWithSplitPayment(saleUuid, payments)) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isProcessing = false, completedSale = result.data) }
                    }
                    is Resource.Error -> {
                        if (result.message?.contains("paid", ignoreCase = true) == true) {
                            _uiState.update { it.copy(isProcessing = false, saleAlreadyPaid = true) }
                        } else {
                            _uiState.update { it.copy(isProcessing = false, error = result.message) }
                        }
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = e.message ?: "Terjadi kesalahan, silakan coba lagi") }
            }
        }
    }

    /** Muat detail held sale berdasarkan [saleUuid] — hasilnya tersedia di [PaymentUiState.heldSale]. */
    fun loadHeldSale(saleUuid: String) {
        viewModelScope.launch {
            when (val result = saleRepository.getSaleDetail(saleUuid)) {
                is Resource.Success -> _uiState.update { it.copy(heldSale = result.data, heldSaleError = null) }
                is Resource.Error   -> _uiState.update { it.copy(heldSaleError = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun reset() {
        qrisPollingJob?.cancel()
        qrisPollingJob = null
        _uiState.value = PaymentUiState()
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearAlreadyPaid() = _uiState.update { it.copy(saleAlreadyPaid = false) }

    override fun onCleared() {
        super.onCleared()
        qrisPollingJob?.cancel()
    }
}
