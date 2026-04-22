package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.PaymentMethodChip
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.SummaryRow
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/** Metode yang ditampilkan — hanya Cash dan QRIS. */
private val visiblePaymentMethods = listOf(PaymentMethod.CASH, PaymentMethod.QRIS)

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
    modifier: Modifier = Modifier
) {
    val changeAmount = remember(paidAmount, subtotal) {
        val paid = paidAmount.toLongOrNull() ?: 0L
        if (paid > subtotal) paid - subtotal else 0L
    }

    val quickAmounts = remember(subtotal) {
        listOf(
            subtotal,
            ((subtotal / 10_000) + 1) * 10_000,
            ((subtotal / 50_000) + 1) * 50_000,
            ((subtotal / 100_000) + 1) * 100_000
        ).distinct().sorted()
    }

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OrderSummaryColumn(
            itemCount      = itemCount,
            subtotal       = subtotal,
            isCashSelected = isCashSelected,
            changeAmount   = changeAmount,
            modifier       = Modifier.weight(0.42f).fillMaxHeight()
        )
        PaymentInputColumn(
            selectedMethod    = selectedMethod,
            onSelectMethod    = onSelectMethod,
            paidAmount        = paidAmount,
            onPaidAmountChange = onPaidAmountChange,
            isCashSelected    = isCashSelected,
            isProcessing      = isProcessing,
            onProcessPayment  = onProcessPayment,
            quickAmounts      = quickAmounts,
            modifier          = Modifier.weight(0.58f).fillMaxHeight()
        )
    }
}

// ── Left column ─────────────────────────────────────────────────────────────

@Composable
private fun OrderSummaryColumn(
    itemCount: Int,
    subtotal: Long,
    isCashSelected: Boolean,
    changeAmount: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeroTotalCard(itemCount, subtotal)
        SummaryCard(itemCount, subtotal)
        if (isCashSelected && changeAmount > 0) ChangeDueCard(changeAmount)
    }
}

@Composable
private fun HeroTotalCard(itemCount: Int, subtotal: Long) {
    Card(
        shape  = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    "$itemCount item",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
                )
            }
            Text(
                "Total Pembayaran",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Text(
                formatRupiah(subtotal),
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun SummaryCard(itemCount: Int, subtotal: Long) {
    Card(
        shape  = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Ringkasan",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            SummaryRow(label = "$itemCount item", value = formatRupiah(subtotal))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            SummaryRow(
                label      = "Total",
                value      = formatRupiah(subtotal),
                isBold     = true,
                valueColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChangeDueCard(changeAmount: Long) {
    Card(
        shape  = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.SwapHoriz, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Kembalian",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                formatRupiah(changeAmount),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Right column ────────────────────────────────────────────────────────────

@Composable
private fun PaymentInputColumn(
    selectedMethod: PaymentMethod,
    onSelectMethod: (PaymentMethod) -> Unit,
    paidAmount: String,
    onPaidAmountChange: (String) -> Unit,
    isCashSelected: Boolean,
    isProcessing: Boolean,
    onProcessPayment: () -> Unit,
    quickAmounts: List<Long>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MethodSelector(selectedMethod = selectedMethod, onSelectMethod = onSelectMethod)

        if (isCashSelected) {
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
                onKey = { key ->
                    val current = paidAmount
                    val next = when (key) {
                        "⌫"   -> current.dropLast(1)
                        "000" -> if (current.isEmpty()) current else (current + "000").take(10)
                        else  -> if (current.isEmpty() && key == "0") current
                                 else (current + key).take(10)
                    }
                    onPaidAmountChange(next)
                }
            )
        }

        Spacer(Modifier.height(4.dp))
        RancakButton(
            text      = "Proses Pembayaran",
            onClick   = onProcessPayment,
            isLoading = isProcessing,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

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
            modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun PaidAmountDisplay(paidAmount: String, onClear: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
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
                    text = if (paidAmount.isEmpty()) "Rp 0"
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAmountRow(
    quickAmounts: List<Long>,
    paidAmount: String,
    onSelect: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        quickAmounts.take(4).forEach { amount ->
            val isActive = paidAmount == amount.toString()
            FilledTonalButton(
                onClick = { onSelect(amount) },
                modifier = Modifier.weight(1f),
                shape    = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
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
                    color = if (isActive)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
