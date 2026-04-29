package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    onQrisSelected: () -> Unit = {},
    isSplit: Boolean = false,
    onToggleMode: () -> Unit = {},
    onHoldOrder: (() -> Unit)? = null,
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
        ).distinct().sorted()
    }

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OrderSummaryColumn(
            itemCount      = itemCount,
            subtotal       = subtotal,
            total          = total,
            discount       = discount,
            tax            = tax,
            adminFee       = adminFee,
            deliveryFee    = deliveryFee,
            tip            = tip,
            isCashSelected = isCashSelected,
            changeAmount   = changeAmount,
            isSplit        = isSplit,
            onToggleMode   = onToggleMode,
            modifier       = Modifier.weight(0.42f).fillMaxHeight()
        )
        PaymentInputColumn(
            selectedMethod     = selectedMethod,
            onSelectMethod     = onSelectMethod,
            paidAmount         = paidAmount,
            onPaidAmountChange = onPaidAmountChange,
            isCashSelected     = isCashSelected,
            isProcessing       = isProcessing,
            onProcessPayment   = onProcessPayment,
            onQrisSelected     = onQrisSelected,
            onHoldOrder        = onHoldOrder,
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

// ── Left column ─────────────────────────────────────────────────────────────

@Composable
private fun OrderSummaryColumn(
    itemCount: Int,
    subtotal: Long,
    total: Long,
    discount: Long,
    tax: Long,
    adminFee: Long,
    deliveryFee: Long,
    tip: Long,
    isCashSelected: Boolean,
    changeAmount: Long,
    isSplit: Boolean,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeroTotalCard(itemCount, total)
        SummaryCard(itemCount, subtotal, discount, tax, adminFee, deliveryFee, tip, total)
        if (isCashSelected && changeAmount > 0) ChangeDueCard(changeAmount)
        Spacer(Modifier.weight(1f))
        PaymentModeToggleColumn(isSplit = isSplit, onToggle = onToggleMode)
    }
}

@Composable
private fun PaymentModeToggleColumn(isSplit: Boolean, onToggle: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Mode Pembayaran",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick = { if (isSplit) onToggle() },
                label = { Text("Tunggal") },
                leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f),
                colors = if (!isSplit) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) else AssistChipDefaults.assistChipColors()
            )
            AssistChip(
                onClick = { if (!isSplit) onToggle() },
                label = { Text("Terpisah") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.CallSplit, null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f),
                colors = if (isSplit) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) else AssistChipDefaults.assistChipColors()
            )
        }
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
private fun SummaryCard(
    itemCount: Int,
    subtotal: Long,
    discount: Long,
    tax: Long,
    adminFee: Long,
    deliveryFee: Long,
    tip: Long,
    total: Long
) {
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
            if (discount > 0) SummaryRow(
                label      = "Diskon",
                value      = "− ${formatRupiah(discount)}",
                valueColor = MaterialTheme.colorScheme.error
            )
            if (tax > 0) SummaryRow(label = "Pajak", value = formatRupiah(tax))
            if (adminFee > 0) SummaryRow(label = "Biaya Admin", value = formatRupiah(adminFee))
            if (deliveryFee > 0) SummaryRow(label = "Ongkir", value = formatRupiah(deliveryFee))
            if (tip > 0) SummaryRow(label = "Tip", value = formatRupiah(tip))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            SummaryRow(
                label      = "Total",
                value      = formatRupiah(total),
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
    onQrisSelected: () -> Unit = {},
    onHoldOrder: (() -> Unit)? = null,
    quickAmounts: List<Long>,
    isQrisWaiting: Boolean = false,
    qrisQrString: String? = null,
    qrisAmount: Long = 0L,
    isQrisPolling: Boolean = false,
    onCancelQris: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isQris = selectedMethod == PaymentMethod.QRIS
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MethodSelector(
            selectedMethod = selectedMethod,
            onSelectMethod = { method ->
                // Jika sedang menunggu QRIS dan user pindah ke metode lain, batalkan QRIS dulu
                if (isQrisWaiting && method != PaymentMethod.QRIS) onCancelQris()
                onSelectMethod(method)
                // Auto-trigger QRIS saat chip dipilih
                if (method == PaymentMethod.QRIS) onQrisSelected()
            }
        )

        if (isQrisWaiting && qrisQrString != null) {
            // QRIS QR code tampil inline di kolom kanan — tidak replace halaman
            QrisWaitingContent(
                qrString  = qrisQrString,
                amount    = qrisAmount,
                isPolling = isQrisPolling,
                onCancel  = onCancelQris,
                modifier  = Modifier.weight(1f).fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        MaterialTheme.shapes.large
                    )
                    .clip(MaterialTheme.shapes.large)
            )
        } else if (isCashSelected) {
            PaidAmountDisplay(
                paidAmount = paidAmount,
                onClear    = { onPaidAmountChange("") }
            )
            QuickAmountRow(
                quickAmounts = quickAmounts,
                paidAmount   = paidAmount,
                onSelect     = { onPaidAmountChange(it.toString()) }
            )
            // Numpad mengisi sisa tinggi kolom — tidak pernah keluar dari viewport
            PaymentNumpad(
                modifier = Modifier.weight(1f),
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
        } else {
            Spacer(Modifier.weight(1f))
        }

        // Tombol sticky di bawah — disembunyikan saat menunggu QR QRIS
        if (!isQrisWaiting) {
            if (!isQris) {
                RancakButton(
                    text      = "Proses Pembayaran",
                    onClick   = onProcessPayment,
                    isLoading = isProcessing,
                    modifier  = Modifier.fillMaxWidth()
                )
            } else {
                // QRIS: tombol eksplisit sebagai trigger utama (auto-trigger saat chip diklik,
                // tombol ini sebagai fallback jika auto-trigger belum berhasil)
                RancakButton(
                    text      = if (isProcessing) "Membuat QR Code QRIS..." else "Bayar dengan QRIS",
                    onClick   = onQrisSelected,
                    isLoading = isProcessing,
                    enabled   = !isProcessing,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
        if (onHoldOrder != null && !isQrisWaiting) {
            OutlinedButton(
                onClick = onHoldOrder,
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Tahan Pesanan (Open Bill)")
            }
        }
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
