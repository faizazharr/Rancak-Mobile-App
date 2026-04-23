package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.repository.SplitPaymentEntry
import id.rancak.app.presentation.components.PaymentMethodChip
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.SummaryRow
import id.rancak.app.presentation.util.formatRupiah

/** Metode yang tersedia untuk split payment. */
private val splitableMethods = listOf(PaymentMethod.CASH, PaymentMethod.QRIS)

/**
 * Panel split payment — ganti UI pembayaran tunggal saat mode split aktif.
 */
@Composable
internal fun SplitPaymentPanel(
    itemCount: Int,
    orderTotal: Long,
    splitPayments: List<SplitPaymentEntry>,
    isProcessing: Boolean,
    onAddPayment: (PaymentMethod, Long) -> Unit,
    onRemovePayment: (Int) -> Unit,
    onProcess: () -> Unit,
    isSplit: Boolean = true,
    onToggleMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val paidSoFar = splitPayments.sumOf { it.amount }
    val remaining = (orderTotal - paidSoFar).coerceAtLeast(0L)

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Left: Summary ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier.weight(0.42f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "$itemCount item • Split",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
                    )
                    Text(
                        "Total Pembayaran",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        formatRupiah(orderTotal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ringkasan Pembayaran",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SummaryRow(label = "Total tagihan", value = formatRupiah(orderTotal))
                    SummaryRow(label = "Sudah dibayar", value = formatRupiah(paidSoFar))
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SummaryRow(
                        label = if (remaining > 0) "Sisa" else "Kembalian",
                        value = formatRupiah(
                            if (remaining > 0) remaining else paidSoFar - orderTotal
                        ),
                        isBold = true,
                        valueColor = if (remaining > 0) MaterialTheme.colorScheme.error
                                     else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Mode toggle (di kiri bawah) ───────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Mode Pembayaran",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { if (isSplit) onToggleMode() },
                        label = { Text("Tunggal") },
                        leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f),
                        colors = if (!isSplit) AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) else AssistChipDefaults.assistChipColors()
                    )
                    AssistChip(
                        onClick = { if (!isSplit) onToggleMode() },
                        label = { Text("Terpisah") },
                        leadingIcon = { Icon(Icons.Default.CallSplit, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f),
                        colors = if (isSplit) AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) else AssistChipDefaults.assistChipColors()
                    )
                }
            }
        }

        // ── Right: Entry list + add form + sticky button ─────────────────────
        Column(
            modifier = Modifier.weight(0.58f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Daftar Pembayaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                if (splitPayments.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Belum ada pembayaran. Tambahkan minimal satu metode di bawah.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(splitPayments) { index, entry ->
                            SplitPaymentRow(
                                index = index,
                                entry = entry,
                                onRemove = { onRemovePayment(index) }
                            )
                        }
                    }
                }

                HorizontalDivider()

                AddPaymentForm(
                    maxAmount = remaining.takeIf { it > 0 } ?: 0L,
                    onAdd = onAddPayment
                )
            }

            // Tombol sticky di bawah — selalu terlihat
            RancakButton(
                text = "Proses Pembayaran",
                onClick = onProcess,
                isLoading = isProcessing,
                enabled = splitPayments.isNotEmpty() && paidSoFar >= orderTotal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SplitPaymentRow(
    index: Int,
    entry: SplitPaymentEntry,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "${index + 1}.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.method.value.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatRupiah(entry.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPaymentForm(
    maxAmount: Long,
    onAdd: (PaymentMethod, Long) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var amountText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Tambah Pembayaran",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            splitableMethods.forEach { method ->
                PaymentMethodChip(
                    method = method.value,
                    isSelected = selectedMethod == method,
                    onClick = { selectedMethod = method },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Amount display — same pattern as single payment
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
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
                        text = if (amountText.isEmpty()) "Rp 0"
                               else formatRupiah(amountText.toLongOrNull() ?: 0L),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = if (amountText.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (amountText.isNotEmpty()) {
                    IconButton(onClick = { amountText = "" }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (maxAmount > 0) {
            FilledTonalButton(
                onClick = { amountText = maxAmount.toString() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Isi Sisa (${formatRupiah(maxAmount)})") }
        }

        PaymentNumpad(
            onKey = { key ->
                val current = amountText
                val next = when (key) {
                    "⌫"   -> current.dropLast(1)
                    "000" -> if (current.isEmpty()) current else (current + "000").take(10)
                    else  -> if (current.isEmpty() && key == "0") current
                             else (current + key).take(10)
                }
                amountText = next
            }
        )

        RancakButton(
            text = "Tambah",
            onClick = {
                val amount = amountText.toLongOrNull() ?: 0L
                if (amount > 0) {
                    onAdd(selectedMethod, amount)
                    amountText = ""
                }
            },
            enabled = (amountText.toLongOrNull() ?: 0L) > 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
