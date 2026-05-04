package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Metode yang ditampilkan — hanya Cash dan QRIS. */
private val visiblePaymentMethods = listOf(PaymentMethod.CASH, PaymentMethod.QRIS)

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
            orderItems     = orderItems,
            taxLines       = taxLines,
            surchargeLines = surchargeLines,
            orderTypeLabel = orderTypeLabel,
            customerName   = customerName,
            tableLabel     = tableLabel,
            pax            = pax,
            voucherCode    = voucherCode,
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
    orderItems: ImmutableList<OrderLineItem> = persistentListOf(),
    taxLines: ImmutableList<NamedAmount> = persistentListOf(),
    surchargeLines: ImmutableList<NamedAmount> = persistentListOf(),
    orderTypeLabel: String? = null,
    customerName: String? = null,
    tableLabel: String? = null,
    pax: Int = 0,
    voucherCode: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeroTotalCard(itemCount, total)
        OrderContextCard(
            orderTypeLabel = orderTypeLabel,
            customerName   = customerName,
            tableLabel     = tableLabel,
            pax            = pax,
            voucherCode    = voucherCode
        )
        SummaryCard(
            itemCount      = itemCount,
            subtotal       = subtotal,
            discount       = discount,
            tax            = tax,
            adminFee       = adminFee,
            deliveryFee    = deliveryFee,
            tip            = tip,
            total          = total,
            orderItems     = orderItems,
            taxLines       = taxLines,
            surchargeLines = surchargeLines
        )
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
    total: Long,
    orderItems: ImmutableList<OrderLineItem> = persistentListOf(),
    taxLines: ImmutableList<NamedAmount> = persistentListOf(),
    surchargeLines: ImmutableList<NamedAmount> = persistentListOf()
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

            // Item detail rows — when available show per-item breakdown
            if (orderItems.isNotEmpty()) {
                orderItems.forEach { line ->
                    val label = if (line.variantName != null)
                        "${line.name} (${line.variantName})"
                    else line.name
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "${line.qty}× $label",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            formatRupiah(line.subtotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                SummaryRow(label = "Subtotal ($itemCount item)", value = formatRupiah(subtotal))
            } else {
                SummaryRow(label = "$itemCount item", value = formatRupiah(subtotal))
            }

            if (discount > 0) SummaryRow(
                label      = "Diskon",
                value      = "− ${formatRupiah(discount)}",
                valueColor = MaterialTheme.colorScheme.error
            )
            // Pajak: per-line breakdown jika tersedia (match dengan tampilan kasir),
            // selain itu fallback ke single-line aggregate.
            if (taxLines.isNotEmpty()) {
                taxLines.filter { it.amount > 0 }.forEach {
                    SummaryRow(label = it.label, value = formatRupiah(it.amount))
                }
            } else if (tax > 0) {
                SummaryRow(label = "Pajak", value = formatRupiah(tax))
            }
            // Surcharge / biaya admin: per-line breakdown jika tersedia.
            if (surchargeLines.isNotEmpty()) {
                surchargeLines.filter { it.amount > 0 }.forEach {
                    SummaryRow(label = it.label, value = formatRupiah(it.amount))
                }
            } else if (adminFee > 0) {
                SummaryRow(label = "Biaya Admin", value = formatRupiah(adminFee))
            }
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

/**
 * Kartu konteks pesanan: Order Type, Pelanggan, Meja, Pax, Voucher.
 * Hanya tampil bila ada minimal satu nilai non-default. Membuat halaman
 * pembayaran match dengan informasi yang sudah diinput di kasir.
 */
@Composable
private fun OrderContextCard(
    orderTypeLabel: String?,
    customerName: String?,
    tableLabel: String?,
    pax: Int,
    voucherCode: String?
) {
    val rows = buildList {
        orderTypeLabel?.takeIf { it.isNotBlank() }?.let { add("Tipe Pesanan" to it) }
        customerName?.takeIf  { it.isNotBlank() }?.let { add("Pelanggan" to it) }
        tableLabel?.takeIf    { it.isNotBlank() }?.let { add("Meja" to it) }
        if (pax > 0) add("Jumlah Tamu" to "$pax orang")
        voucherCode?.takeIf   { it.isNotBlank() }?.let { add("Voucher" to it) }
    }
    if (rows.isEmpty()) return

    Card(
        shape  = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                "Detail Pesanan",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            rows.forEach { (k, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        k,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        v,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
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
    quickAmounts: ImmutableList<Long>,
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
    quickAmounts: ImmutableList<Long>,
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
