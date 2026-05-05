package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.presentation.components.PaymentMethodChip
import id.rancak.app.presentation.components.QrisQrCode
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

private val splitMethods = listOf(PaymentMethod.CASH, PaymentMethod.QRIS)

/**
 * Kolom kanan panel pembayaran terpisah.
 *
 * Menampilkan:
 * - Header pelanggan N + nominal grup
 * - Banner biaya proporsional (jika ada pajak / surcharge)
 * - Pilihan metode Cash / QRIS
 * - Input cash (display jumlah + nominal cepat + numpad) atau info QRIS
 * - Tombol konfirmasi grup dan proses pembayaran akhir
 */
@Composable
internal fun SplitPaymentColumn(
    groupNumber:        Int,
    currentItemSubtotal: Long,
    groupActualTotal:   Long,
    hasFeeShare:        Boolean,
    currentMethod:      PaymentMethod,
    currentCashInput:   String,
    change:             Long,
    canConfirm:         Boolean,
    allAssigned:        Boolean,
    hasConfirmedGroups: Boolean,
    isProcessing:       Boolean,
    anyItemSelected:    Boolean,
    merchantQrisString: String,
    onSetMethod:        (PaymentMethod) -> Unit,
    onSetCashInput:     (String) -> Unit,
    onConfirmGroup:     () -> Unit,
    onConfirmAndPrint:  () -> Unit,
    onProcess:          () -> Unit,
    modifier:           Modifier = Modifier
) {
    var showQrisConfirmDialog by remember { mutableStateOf(false) }

    if (showQrisConfirmDialog) {
        QrisConfirmDialog(
            groupNumber        = groupNumber,
            groupActualTotal   = groupActualTotal,
            merchantQrisString = merchantQrisString,
            onConfirmAndPrint  = { showQrisConfirmDialog = false; onConfirmAndPrint() },
            onDismiss          = { showQrisConfirmDialog = false }
        )
    }

    Column(modifier = modifier) {
        SplitGroupHeader(
            groupNumber      = groupNumber,
            anyItemSelected  = anyItemSelected,
            groupActualTotal = groupActualTotal
        )

        if (anyItemSelected && hasFeeShare) {
            SplitFeeShareBanner(
                currentItemSubtotal = currentItemSubtotal,
                groupActualTotal    = groupActualTotal
            )
        }

        SplitMethodSelector(currentMethod = currentMethod, onSetMethod = onSetMethod)
        Spacer(Modifier.height(8.dp))

        when (currentMethod) {
            PaymentMethod.CASH -> {
                SplitCashInput(
                    currentCashInput = currentCashInput,
                    change           = change,
                    anyItemSelected  = anyItemSelected
                )
                SplitQuickAmounts(
                    groupActualTotal = groupActualTotal,
                    currentCashInput = currentCashInput,
                    onSetCashInput   = onSetCashInput
                )
                PaymentNumpad(
                    modifier = Modifier.weight(1f),
                    onKey    = { key ->
                        val next = when (key) {
                            "\u232b" -> currentCashInput.dropLast(1)
                            "000"    -> if (currentCashInput.isEmpty()) currentCashInput
                                        else (currentCashInput + "000").take(10)
                            else     -> if (currentCashInput.isEmpty() && key == "0") currentCashInput
                                        else (currentCashInput + key).take(10)
                        }
                        onSetCashInput(next)
                    }
                )
            }
            PaymentMethod.QRIS -> {
                if (anyItemSelected) SplitQrisInfo(groupActualTotal = groupActualTotal)
                Spacer(Modifier.weight(1f))
            }
            else -> Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        SplitActionButtons(
            canConfirm        = canConfirm,
            currentMethod     = currentMethod,
            onConfirmGroup    = onConfirmGroup,
            onConfirmAndPrint = onConfirmAndPrint,
            onShowQrisDialog  = { showQrisConfirmDialog = true }
        )

        Spacer(Modifier.height(8.dp))

        SplitProcessButton(
            allAssigned        = allAssigned,
            hasConfirmedGroups = hasConfirmedGroups,
            isProcessing       = isProcessing,
            onProcess          = onProcess
        )
    }
}

// ── Group header ──────────────────────────────────────────────────────────────

@Composable
private fun SplitGroupHeader(
    groupNumber:      Int,
    anyItemSelected:  Boolean,
    groupActualTotal: Long
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            "Pelanggan $groupNumber",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (anyItemSelected) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    formatRupiah(groupActualTotal),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        } else {
            Text(
                "Pilih item di sebelah kiri",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Fee share banner ──────────────────────────────────────────────────────────

@Composable
private fun SplitFeeShareBanner(
    currentItemSubtotal: Long,
    groupActualTotal:    Long
) {
    val feeShare = groupActualTotal - currentItemSubtotal
    Surface(
        shape    = MaterialTheme.shapes.small,
        color    = if (feeShare >= 0)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Item: ${formatRupiah(currentItemSubtotal)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val feeLabel = if (feeShare >= 0) "+ ${formatRupiah(feeShare)} biaya"
                           else "− ${formatRupiah(-feeShare)} diskon"
            Text(
                feeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (feeShare >= 0) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Method selector ───────────────────────────────────────────────────────────

@Composable
private fun SplitMethodSelector(
    currentMethod: PaymentMethod,
    onSetMethod:   (PaymentMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Metode Pembayaran",
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            splitMethods.forEach { m ->
                PaymentMethodChip(
                    method     = m.value,
                    isSelected = currentMethod == m,
                    onClick    = { onSetMethod(m) },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── Cash input display ────────────────────────────────────────────────────────

@Composable
private fun SplitCashInput(
    currentCashInput: String,
    change:           Long,
    anyItemSelected:  Boolean
) {
    Surface(
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Uang Diterima",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (currentCashInput.isEmpty()) "Rp 0"
                    else formatRupiah(currentCashInput.toLongOrNull() ?: 0L),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = if (currentCashInput.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            if (anyItemSelected && currentCashInput.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Kembalian",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        formatRupiah(change),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = if (change >= 0) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ── Quick amounts ─────────────────────────────────────────────────────────────

@Composable
private fun SplitQuickAmounts(
    groupActualTotal: Long,
    currentCashInput: String,
    onSetCashInput:   (String) -> Unit
) {
    val quickAmounts = remember(groupActualTotal) {
        listOf(
            groupActualTotal,
            ((groupActualTotal / 10_000) + 1) * 10_000,
            ((groupActualTotal / 50_000) + 1) * 50_000
        ).distinct().sorted()
    }
    Spacer(Modifier.height(4.dp))
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        quickAmounts.forEach { amount ->
            val isActive = currentCashInput == amount.toString()
            FilledTonalButton(
                onClick        = { onSetCashInput(amount.toString()) },
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
    Spacer(Modifier.height(4.dp))
}

// ── QRIS info card ────────────────────────────────────────────────────────────

@Composable
private fun SplitQrisInfo(groupActualTotal: Long) {
    Card(
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.QrCode2, null,
                Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    "Total QRIS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatRupiah(groupActualTotal),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Nominal otomatis dari item yang dipilih",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ── QRIS confirm dialog ───────────────────────────────────────────────────────

@Composable
private fun QrisConfirmDialog(
    groupNumber:        Int,
    groupActualTotal:   Long,
    merchantQrisString: String,
    onConfirmAndPrint:  () -> Unit,
    onDismiss:          () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.QrCode2, null,
                    Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("QRIS — Pelanggan $groupNumber")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    shape    = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nominal yang harus dibayar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            formatRupiah(groupActualTotal),
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (merchantQrisString.isNotBlank()) {
                    QrisQrCode(
                        qrString = merchantQrisString,
                        size     = 220.dp,
                        label    = "Scan, lalu masukkan nominal di atas"
                    )
                } else {
                    Surface(
                        color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape    = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "QRIS merchant belum diatur. Buka Pengaturan › Informasi Toko " +
                            "untuk menambahkan QRIS statis Anda, atau minta customer transfer manual.",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Text(
                    "Setelah customer selesai bayar, tekan \"Sudah Bayar & Cetak\" " +
                    "untuk konfirmasi grup ini dan mencetak struk.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmAndPrint) {
                Icon(Icons.Default.Print, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sudah Bayar & Cetak")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ── Action buttons ────────────────────────────────────────────────────────────

@Composable
private fun SplitActionButtons(
    canConfirm:        Boolean,
    currentMethod:     PaymentMethod,
    onConfirmGroup:    () -> Unit,
    onConfirmAndPrint: () -> Unit,
    onShowQrisDialog:  () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick  = onConfirmGroup,
            enabled  = canConfirm,
            modifier = Modifier.weight(1f)
        ) { Text("Tambah Saja") }

        Button(
            onClick  = {
                if (currentMethod == PaymentMethod.QRIS) onShowQrisDialog()
                else onConfirmAndPrint()
            },
            enabled  = canConfirm,
            modifier = Modifier.weight(1f),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Print, null, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Bayar & Cetak")
        }
    }
}

// ── Process button ────────────────────────────────────────────────────────────

@Composable
private fun SplitProcessButton(
    allAssigned:        Boolean,
    hasConfirmedGroups: Boolean,
    isProcessing:       Boolean,
    onProcess:          () -> Unit
) {
    RancakButton(
        text      = "Proses Pembayaran",
        onClick   = onProcess,
        isLoading = isProcessing,
        enabled   = allAssigned && hasConfirmedGroups && !isProcessing,
        modifier  = Modifier.fillMaxWidth()
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 90)
@Composable
private fun SplitGroupHeaderPreview_WithItem() {
    RancakTheme {
        Surface(Modifier.padding(12.dp)) {
            SplitGroupHeader(groupNumber = 2, anyItemSelected = true, groupActualTotal = 36_000L)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 90)
@Composable
private fun SplitGroupHeaderPreview_Empty() {
    RancakTheme {
        Surface(Modifier.padding(12.dp)) {
            SplitGroupHeader(groupNumber = 1, anyItemSelected = false, groupActualTotal = 0L)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 60)
@Composable
private fun SplitFeeShareBannerPreview() {
    RancakTheme {
        SplitFeeShareBanner(currentItemSubtotal = 36_000L, groupActualTotal = 39_600L)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 90)
@Composable
private fun SplitCashInputPreview() {
    RancakTheme {
        SplitCashInput(currentCashInput = "50000", change = 14_000L, anyItemSelected = true)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
private fun SplitQrisInfoPreview() {
    RancakTheme {
        SplitQrisInfo(groupActualTotal = 36_000L)
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 600)
@Composable
private fun SplitPaymentColumnPreview_Cash() {
    RancakTheme {
        SplitPaymentColumn(
            groupNumber          = 2,
            currentItemSubtotal  = 36_000L,
            groupActualTotal     = 39_600L,
            hasFeeShare          = true,
            currentMethod        = PaymentMethod.CASH,
            currentCashInput     = "50000",
            change               = 10_400L,
            canConfirm           = true,
            allAssigned          = false,
            hasConfirmedGroups   = true,
            isProcessing         = false,
            anyItemSelected      = true,
            merchantQrisString   = "",
            onSetMethod          = {},
            onSetCashInput       = {},
            onConfirmGroup       = {},
            onConfirmAndPrint    = {},
            onProcess            = {},
            modifier             = Modifier.padding(16.dp).fillMaxHeight()
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 600)
@Composable
private fun SplitPaymentColumnPreview_Qris() {
    RancakTheme {
        SplitPaymentColumn(
            groupNumber          = 1,
            currentItemSubtotal  = 22_000L,
            groupActualTotal     = 22_000L,
            hasFeeShare          = false,
            currentMethod        = PaymentMethod.QRIS,
            currentCashInput     = "",
            change               = 0L,
            canConfirm           = true,
            allAssigned          = false,
            hasConfirmedGroups   = false,
            isProcessing         = false,
            anyItemSelected      = true,
            merchantQrisString   = "",
            onSetMethod          = {},
            onSetCashInput       = {},
            onConfirmGroup       = {},
            onConfirmAndPrint    = {},
            onProcess            = {},
            modifier             = Modifier.padding(16.dp).fillMaxHeight()
        )
    }
}
