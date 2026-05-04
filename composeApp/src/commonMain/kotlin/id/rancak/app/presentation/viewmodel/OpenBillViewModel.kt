package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.data.local.LocalOpenBill
import id.rancak.app.data.local.OpenBillStore
import id.rancak.app.data.local.toLocalOpenBillItem
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.time.Clock

@Immutable
data class OpenBillUiState(
    val bills: List<LocalOpenBill> = emptyList(),
    /** True saat dialog nama open bill sedang ditampilkan. */
    val showNameDialog: Boolean = false,
    /** Nama awal yang diisi di dialog (kosong = buat baru). */
    val dialogInitialName: String = "",
    /** ID open bill yang sedang diperbarui; null = buat baru. */
    val editingBillId: String? = null,
    /** True saat sedang sinkron ke backend (create / cancel held sale). */
    val isSyncing: Boolean = false,
    /** Pesan error sinkron ke backend (KDS). Null jika tidak ada error. */
    val syncError: String? = null
)

/**
 * Mengelola daftar open bill lokal dan sinkronisasi ke backend (KDS).
 *
 * Setiap open bill yang disimpan akan **juga** dikirim ke backend sebagai sale
 * dengan status HELD, sehingga langsung tampil di Kitchen Display. Saat dihapus
 * tanpa dibayar, sale di backend akan di-cancel agar KDS card ikut tertutup.
 */
class OpenBillViewModel(
    private val store: OpenBillStore,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OpenBillUiState(bills = store.getAll()))
    val uiState: StateFlow<OpenBillUiState> = _uiState.asStateFlow()

    // ── Dialog ────────────────────────────────────────────────────────────────

    fun showDialog(initialName: String = "", editingBillId: String? = null) {
        _uiState.update {
            it.copy(
                showNameDialog    = true,
                dialogInitialName = initialName,
                editingBillId     = editingBillId
            )
        }
    }

    fun hideDialog() {
        _uiState.update {
            it.copy(showNameDialog = false, dialogInitialName = "", editingBillId = null)
        }
    }

    fun clearSyncError() = _uiState.update { it.copy(syncError = null) }

    // ── Store operations ──────────────────────────────────────────────────────

    /**
     * Simpan snapshot keranjang saat ini sebagai open bill.
     *
     * Alur:
     * 1. Jika [existingRemoteSaleUuid] tidak null (resave dari resumed bill), batalkan sale lama
     *    di backend agar KDS card lama tertutup.
     * 2. Buat sale baru di backend dengan `hold = true` — KDS langsung menampilkan card baru.
     * 3. Simpan snapshot lokal beserta `remoteSaleUuid` agar saat dibayar nanti dapat memakai
     *    `paySale` (tidak menduplikasi card di KDS).
     *
     * Jika sinkron backend gagal, snapshot tetap disimpan lokal (tanpa `remoteSaleUuid`)
     * sebagai fallback offline; pesan error ditampilkan via [OpenBillUiState.syncError].
     */
    @OptIn(ExperimentalUuidApi::class)
    fun saveCart(
        name: String,
        items: List<CartItem>,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String,
        note: String,
        pax: Int,
        discountInput: Long,
        discountIsPercent: Boolean,
        taxInput: Long,
        taxIsPercent: Boolean,
        adminFeeInput: Long,
        adminFeeIsPercent: Boolean,
        deliveryFee: Long,
        tip: Long,
        voucherCode: String,
        editingBillId: String? = null,
        existingRemoteSaleUuid: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncError = null) }

            // Hitung Rp aktual dari input (mirip CartUiState).
            val subtotal = items.sumOf { it.price * it.qty }
            val discount = if (discountIsPercent)
                (subtotal * discountInput / 100L).coerceIn(0L, subtotal)
            else discountInput
            val tax = if (taxIsPercent)
                ((subtotal - discount) * taxInput / 100L).coerceAtLeast(0L)
            else taxInput
            val adminFee = if (adminFeeIsPercent)
                ((subtotal - discount) * adminFeeInput / 100L).coerceAtLeast(0L)
            else adminFeeInput

            val now = Clock.System.now().toEpochMilliseconds()
            val existingCreatedAt = editingBillId?.let { store.get(it)?.createdAt } ?: now

            // Jika bill sudah pernah tersinkron ke KDS (punya remoteSaleUuid), jangan
            // batalkan lalu buat ulang — itu akan menutup KDS card yang mungkin sedang dimasak.
            // Simpan lokal saja dengan UUID lama. Modifikasi item ke KDS harus lewat
            // "Tambah Item" (addItemsToHeldOrder) atau "Hapus Item" (removeHeldOrderItem).
            if (!existingRemoteSaleUuid.isNullOrBlank()) {
                val bill = LocalOpenBill(
                    id                = editingBillId ?: Uuid.random().toString(),
                    name              = name.trim(),
                    items             = items.map { it.toLocalOpenBillItem() },
                    orderType         = orderType.name,
                    tableUuid         = tableUuid,
                    customerName      = customerName,
                    note              = note,
                    pax               = pax,
                    discountInput     = discountInput,
                    discountIsPercent = discountIsPercent,
                    taxInput          = taxInput,
                    taxIsPercent      = taxIsPercent,
                    adminFeeInput     = adminFeeInput,
                    adminFeeIsPercent = adminFeeIsPercent,
                    deliveryFee       = deliveryFee,
                    tip               = tip,
                    voucherCode       = voucherCode,
                    createdAt         = existingCreatedAt,
                    remoteSaleUuid    = existingRemoteSaleUuid // tetap pakai UUID lama
                )
                store.save(bill)
                _uiState.update { it.copy(isSyncing = false, bills = store.getAll()) }
                return@launch
            }

            // Bill baru (belum punya remoteSaleUuid) — kirim ke backend agar muncul di KDS.
            val remoteUuid: String? = if (items.isNotEmpty()) {
                val result = saleRepository.createSale(
                    items         = items,
                    paymentMethod = PaymentMethod.CASH, // placeholder; backend abaikan saat hold
                    paidAmount    = 0L,
                    orderType     = orderType,
                    tableUuid     = tableUuid,
                    customerName  = customerName.takeIf { it.isNotBlank() },
                    note          = note.takeIf { it.isNotBlank() },
                    hold          = true,
                    pax           = pax,
                    discount      = discount,
                    tax           = tax,
                    adminFee      = adminFee,
                    deliveryFee   = deliveryFee,
                    tip           = tip,
                    voucherCode   = voucherCode.takeIf { it.isNotBlank() }
                )
                when (result) {
                    is Resource.Success -> result.data.uuid
                    is Resource.Error -> {
                        _uiState.update { it.copy(syncError = result.message) }
                        null
                    }
                    is Resource.Loading -> null
                }
            } else null

            val bill = LocalOpenBill(
                id                = editingBillId ?: Uuid.random().toString(),
                name              = name.trim(),
                items             = items.map { it.toLocalOpenBillItem() },
                orderType         = orderType.name,
                tableUuid         = tableUuid,
                customerName      = customerName,
                note              = note,
                pax               = pax,
                discountInput     = discountInput,
                discountIsPercent = discountIsPercent,
                taxInput          = taxInput,
                taxIsPercent      = taxIsPercent,
                adminFeeInput     = adminFeeInput,
                adminFeeIsPercent = adminFeeIsPercent,
                deliveryFee       = deliveryFee,
                tip               = tip,
                voucherCode       = voucherCode,
                createdAt         = existingCreatedAt,
                remoteSaleUuid    = remoteUuid
            )
            store.save(bill)
            _uiState.update { it.copy(isSyncing = false, bills = store.getAll()) }
        }
    }

    /**
     * Hapus open bill lokal. Jika sudah tersinkron ke backend, juga batalkan sale-nya
     * agar KDS card ikut tertutup.
     */
    fun remove(id: String) {
        val bill = store.get(id)
        store.remove(id)
        refreshList()
        val remote = bill?.remoteSaleUuid
        if (!remote.isNullOrBlank()) {
            viewModelScope.launch {
                saleRepository.cancelSale(remote, reason = "Open bill dihapus kasir")
            }
        }
    }

    fun refresh() = refreshList()

    // ── private ───────────────────────────────────────────────────────────────

    private fun refreshList() {
        _uiState.update { it.copy(bills = store.getAll()) }
    }
}
