package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.PaymentMethodChip
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Metode yang ditampilkan — hanya Cash dan QRIS. */
internal val visiblePaymentMethods = listOf(PaymentMethod.CASH, PaymentMethod.QRIS)

/**
 * Sisi kanan layar pembayaran — pilihan metode, input nominal, numpad, dan tombol proses.
 * Tidak memiliki state sendiri — semua state diteruskan dari ViewModel melalui PaymentFormContent.
 */
@Composable
internal fun PaymentInputPanel(
    selectedMethod:    PaymentMethod,
    onSelectMethod:    (PaymentMethod) -> Unit,
    paidAmount:        String,
    onPaidAmountChange:(String) -> Unit,
    isCashSelected:    Boolean,
    isProcessing:      Boolean,
    onProcessPayment:  () -> Unit,
    quickAmounts:      ImmutableList<Long>,
    onQrisSelected:    () -> Unit           = {},
    isQrisWaiting:     Boolean              = false,
    qrisQrString:      String?              = null,
    qrisAmount:        Long                 = 0L,
    isQrisPolling:     Boolean              = false,
    onCancelQris:      () -> Unit           = {},
    modifier:          Modifier             = Modifier
) {
    val isQris = selectedMethod == PaymentMethod.QRIS

    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MethodSelector(
            selectedMethod = selectedMethod,
            onSelectMethod = { method ->
                if (isQrisWaiting && method != PaymentMethod.QRIS) onCancelQris()
                onSelectMethod(method)
                if (method == PaymentMethod.QRIS) onQrisSelected()
            }
        )

        when {
            isQrisWaiting && qrisQrString != null -> {
                QrisWaitingContent(
                    qrString  = qrisQrString,
                    amount    = qrisAmount,
                    isPolling = isQrisPolling,
                    onCancel  = onCancelQris,
                    modifier  = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.shapes.large
                        )
                        .clip(MaterialTheme.shapes.large)
                )
            }
            isCashSelected -> {
                PaidAmountDisplay(
                    paidAmount = paidAmount,
                    onClear    = { onPaidAmountChange("") }
                )
                QuickAmountRow(
                    quickAmounts = quickAmounts,
                    paidAmount   = paidAmount,
                    onSelect     = { onPaidAmountChange(it.toString()) }
                )
                PaymentNumpad(
                    modifier = Modifier.weight(1f),
                    onKey    = { key ->
                        val next = when (key) {
                            "⌫"   -> paidAmount.dropLast(1)
                            "000" -> if (paidAmount.isEmpty()) paidAmount
                                     else (paidAmount + "000").take(10)
                            else  -> if (paidAmount.isEmpty() && key == "0") paidAmount
                                     else (paidAmount + key).take(10)
                        }
                        onPaidAmountChange(next)
                    }
                )
            }
            else -> Spacer(Modifier.weight(1f))
        }

        if (!isQrisWaiting) {
            if (!isQris) {
                RancakButton(
                    text      = "Proses Pembayaran",
                    onClick   = onProcessPayment,
                    isLoading = isProcessing,
                    modifier  = Modifier.fillMaxWidth()
                )
            } else {
                RancakButton(
                    text      = if (isProcessing) "Membuat QR Code QRIS..." else "Bayar dengan QRIS",
                    onClick   = onQrisSelected,
                    isLoading = isProcessing,
                    enabled   = !isProcessing,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Method selector ───────────────────────────────────────────────────────────

@Composable
private fun MethodSelector(
    selectedMethod: PaymentMethod,
    onSelectMethod: (PaymentMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Metode Pembayaran",
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            visiblePaymentMethods.forEach { method ->
                PaymentMethodChip(
                    method     = method.value,
                    isSelected = selectedMethod == method,
                    onClick    = { onSelectMethod(method) },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── Paid amount display ───────────────────────────────────────────────────────

@Composable
private fun PaidAmountDisplay(paidAmount: String, onClear: () -> Unit) {
    Surface(
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Jumlah Bayar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = if (paidAmount.isEmpty()) "Rp 0"
                                 else formatRupiah(paidAmount.toLongOrNull() ?: 0L),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = if (paidAmount.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            if (paidAmount.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Hapus semua",
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Quick amount row ──────────────────────────────────────────────────────────

@Composable
private fun QuickAmountRow(
    quickAmounts: ImmutableList<Long>,
    paidAmount:   String,
    onSelect:     (Long) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        quickAmounts.take(4).forEach { amount ->
            val isActive = paidAmount == amount.toString()
            FilledTonalButton(
                onClick        = { onSelect(amount) },
                modifier       = Modifier.weight(1f),
                shape          = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                colors         = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    formatRupiah(amount),
                    style     = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines  = 1,
                    color     = if (isActive)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PaymentInputPanelPreview_Cash() {
    RancakTheme {
        PaymentInputPanel(
            selectedMethod     = PaymentMethod.CASH,
            onSelectMethod     = {},
            paidAmount         = "100000",
            onPaidAmountChange = {},
            isCashSelected     = true,
            isProcessing       = false,
            onProcessPayment   = {},
            quickAmounts       = persistentListOf(70_000L, 80_000L, 100_000L, 150_000L),
            modifier           = Modifier.padding(16.dp).fillMaxSize()
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PaymentInputPanelPreview_Qris() {
    RancakTheme {
        PaymentInputPanel(
            selectedMethod     = PaymentMethod.QRIS,
            onSelectMethod     = {},
            paidAmount         = "",
            onPaidAmountChange = {},
            isCashSelected     = false,
            isProcessing       = false,
            onProcessPayment   = {},
            quickAmounts       = persistentListOf(),
            modifier           = Modifier.padding(16.dp).fillMaxSize()
        )
    }
}
