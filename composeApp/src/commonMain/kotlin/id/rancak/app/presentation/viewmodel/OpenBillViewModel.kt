package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import id.rancak.app.data.local.LocalOpenBill
import id.rancak.app.data.local.OpenBillStore
import id.rancak.app.data.local.toLocalOpenBillItem
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.time.Clock

data class OpenBillUiState(
    val bills: List<LocalOpenBill> = emptyList(),
    /** True saat dialog nama open bill sedang ditampilkan. */
    val showNameDialog: Boolean = false,
    /** Nama awal yang diisi di dialog (kosong = buat baru). */
    val dialogInitialName: String = "",
    /** ID open bill yang sedang diperbarui; null = buat baru. */
    val editingBillId: String? = null
)

/**
 * Mengelola daftar open bill lokal (tersimpan di perangkat, tanpa koneksi internet).
 */
class OpenBillViewModel(private val store: OpenBillStore) : ViewModel() {

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

    // ── Store operations ──────────────────────────────────────────────────────

    /**
     * Simpan snapshot keranjang saat ini sebagai open bill lokal.
     *
     * Jika [editingBillId] tidak null, open bill yang sudah ada akan diperbarui
     * (termasuk mempertahankan [LocalOpenBill.createdAt] aslinya).
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
        editingBillId: String? = null
    ) {
        val now       = Clock.System.now().toEpochMilliseconds()
        val existingCreatedAt = editingBillId?.let { store.get(it)?.createdAt } ?: now
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
            createdAt         = existingCreatedAt
        )
        store.save(bill)
        refreshList()
    }

    fun remove(id: String) {
        store.remove(id)
        refreshList()
    }

    fun refresh() = refreshList()

    // ── private ───────────────────────────────────────────────────────────────

    private fun refreshList() {
        _uiState.update { it.copy(bills = store.getAll()) }
    }
}
