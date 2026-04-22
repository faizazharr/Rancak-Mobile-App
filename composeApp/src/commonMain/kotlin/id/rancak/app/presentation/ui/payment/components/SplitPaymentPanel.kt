package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
        }

        // ── Right: Entry list + add form ──────────────────────────────────────
        Column(
            modifier = Modifier.weight(0.58f).fillMaxHeight().verticalScroll(rememberScrollState()),
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

            Spacer(Modifier.height(4.dp))
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

        OutlinedTextField(
            value = amountText,
            onValueChange = { input -> amountText = input.filter { it.isDigit() } },
            label = { Text("Jumlah") },
            placeholder = {
                Text(if (maxAmount > 0) "Sisa: ${formatRupiah(maxAmount)}" else "Rp 0")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (maxAmount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { amountText = maxAmount.toString() },
                    modifier = Modifier.weight(1f)
                ) { Text("Isi Sisa (${formatRupiah(maxAmount)})") }
            }
        }

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
