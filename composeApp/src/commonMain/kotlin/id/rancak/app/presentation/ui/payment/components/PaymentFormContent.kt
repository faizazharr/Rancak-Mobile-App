package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Item baris untuk ditampilkan di ringkasan pembayaran (tidak membawa logika bisnis). */
data class OrderLineItem(
    val name: String,
    val variantName: String? = null,
    val qty: Int,
    val price: Long,
    val subtotal: Long
)

/**
 * Baris bernama untuk breakdown pajak / surcharge di ringkasan pembayaran,
 * agar persis match dengan tampilan kasir (mis. "PPN (11%)" → 5.500,
 * "Service Charge (5%)" → 2.500).
 */
data class NamedAmount(
    val label: String,
    val amount: Long
)

/**
 * Form utama halaman pembayaran: ringkasan pesanan di kiri, pilihan metode,
 * input jumlah bayar, dan numpad di kanan. Layout 2-kolom yang sesuai untuk
 * tablet; pada phone juga tetap layak karena menggunakan scroll di kolom kanan.
 */
@Composable
internal fun PaymentFormContent(
    itemCount: Int,
    subtotal: Long,
    selectedMethod: PaymentMethod,
    onSelectMethod: (PaymentMethod) -> Unit,
    paidAmount: String,
    onPaidAmountChange: (String) -> Unit,
    isCashSelected: Boolean,
    isProcessing: Boolean,
    onProcessPayment: () -> Unit,
    onQrisSelected: () -> Unit = {},
    isSplit: Boolean = false,
    onToggleMode: () -> Unit = {},
    isQrisWaiting: Boolean = false,
    qrisQrString: String? = null,
    qrisAmount: Long = 0L,
    isQrisPolling: Boolean = false,
    onCancelQris: () -> Unit = {},
    discount: Long = 0L,
    tax: Long = 0L,
    adminFee: Long = 0L,
    deliveryFee: Long = 0L,
    tip: Long = 0L,
    /** Detail setiap baris item untuk ditampilkan di ringkasan — opsional. */
    orderItems: ImmutableList<OrderLineItem> = persistentListOf(),
    /**
     * Breakdown pajak per-konfigurasi (manual + auto dari Pricing Settings).
     * Bila kosong, fallback ke single-line dengan label "Pajak".
     */
    taxLines: ImmutableList<NamedAmount> = persistentListOf(),
    /**
     * Breakdown surcharge per-konfigurasi (manual + auto dari Pricing Settings).
     * Bila kosong, fallback ke single-line dengan label "Biaya Admin".
     */
    surchargeLines: ImmutableList<NamedAmount> = persistentListOf(),
    /** Konteks pesanan agar match dengan kasir. */
    orderTypeLabel: String? = null,
    customerName: String? = null,
    tableLabel: String? = null,
    pax: Int = 0,
    voucherCode: String? = null,
    modifier: Modifier = Modifier
) {
    val total = subtotal - discount + tax + adminFee + deliveryFee + tip

    val changeAmount = remember(paidAmount, total) {
        val paid = paidAmount.toLongOrNull() ?: 0L
        if (paid > total) paid - total else 0L
    }

    val quickAmounts = remember(total) {
        listOf(
            total,
            ((total / 10_000) + 1) * 10_000,
            ((total / 50_000) + 1) * 50_000,
            ((total / 100_000) + 1) * 100_000
        ).distinct().sorted().toImmutableList()
    }

    Row(
        modifier              = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PaymentReceiptPanel(
            itemCount      = itemCount,
            subtotal       = subtotal,
            total          = total,
            discount       = discount,
            tax            = tax,
            adminFee       = adminFee,
            deliveryFee    = deliveryFee,
            tip            = tip,
            changeAmount   = changeAmount,
            isCashSelected = isCashSelected,
            paidAmount     = paidAmount,
            orderItems     = orderItems,
            taxLines       = taxLines,
            surchargeLines = surchargeLines,
            orderTypeLabel = orderTypeLabel,
            customerName   = customerName,
            tableLabel     = tableLabel,
            pax            = pax,
            voucherCode    = voucherCode,
            selectedMethod = selectedMethod,
            isSplit        = isSplit,
            onToggleMode   = onToggleMode,
            modifier       = Modifier.weight(0.42f).fillMaxHeight()
        )
        PaymentInputPanel(
            selectedMethod     = selectedMethod,
            onSelectMethod     = onSelectMethod,
            paidAmount         = paidAmount,
            onPaidAmountChange = onPaidAmountChange,
            isCashSelected     = isCashSelected,
            isProcessing       = isProcessing,
            onProcessPayment   = onProcessPayment,
            onQrisSelected     = onQrisSelected,
            quickAmounts       = quickAmounts,
            isQrisWaiting      = isQrisWaiting,
            qrisQrString       = qrisQrString,
            qrisAmount         = qrisAmount,
            isQrisPolling      = isQrisPolling,
            onCancelQris       = onCancelQris,
            modifier           = Modifier.weight(0.58f).fillMaxHeight()
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PaymentFormPreview_Cash() {
    RancakTheme {
        PaymentFormContent(
            itemCount       = 3,
            subtotal        = 75_000L,
            selectedMethod  = PaymentMethod.CASH,
            onSelectMethod  = {},
            paidAmount      = "100000",
            onPaidAmountChange = {},
            isCashSelected  = true,
            isProcessing    = false,
            onProcessPayment = {},
            modifier        = Modifier.fillMaxSize().padding(12.dp)
        )
    }
}

@Preview
@Composable
private fun PaymentFormPreview_Qris() {
    RancakTheme {
        PaymentFormContent(
            itemCount       = 2,
            subtotal        = 42_000L,
            selectedMethod  = PaymentMethod.QRIS,
            onSelectMethod  = {},
            paidAmount      = "",
            onPaidAmountChange = {},
            isCashSelected  = false,
            isProcessing    = false,
            onProcessPayment = {},
            modifier        = Modifier.fillMaxSize().padding(12.dp)
        )
    }
}
