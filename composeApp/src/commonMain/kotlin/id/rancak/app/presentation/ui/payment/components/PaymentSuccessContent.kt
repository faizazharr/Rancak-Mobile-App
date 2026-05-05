package id.rancak.app.presentation.ui.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterManager
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleItem
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.components.PrintDialog
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/**
 * Kartu sukses transaksi: header hijau + centang, total, kembalian (opsional),
 * dan dua tombol aksi (Cetak Struk + Transaksi Baru). Dialog cetak memakai
 * [PrintDialog] bersama dari [id.rancak.app.presentation.components].
 */
@Composable
internal fun PaymentSuccessContent(
    sale: Sale,
    printerManager: PrinterManager,
    settingsStore: SettingsStore,
    onNewTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrintDialog by remember { mutableStateOf(false) }

    PaymentSuccessCard(
        invoiceNo    = sale.invoiceNo,
        total        = sale.total,
        changeAmount = sale.changeAmount,
        onPrint      = { showPrintDialog = true },
        onNewTx      = onNewTransaction,
        modifier     = modifier
    )

    if (showPrintDialog) {
        PrintDialog(
            sale           = sale,
            printerManager = printerManager,
            settingsStore  = settingsStore,
            onDismiss      = { showPrintDialog = false }
        )
    }
}

@Composable
private fun PaymentSuccessCard(
    invoiceNo: String?,
    total: Long,
    changeAmount: Long,
    onPrint: () -> Unit,
    onNewTx: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 520.dp)
                .fillMaxWidth(0.88f),
            shape     = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SuccessHeader(invoiceNo = invoiceNo)
                SuccessDetails(
                    total        = total,
                    changeAmount = changeAmount,
                    onPrint      = onPrint,
                    onNewTx      = onNewTx
                )
            }
        }
    }
}

@Composable
private fun SuccessHeader(invoiceNo: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 28.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Check, contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Text(
                "Transaksi Berhasil!",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary
            )
            if (invoiceNo != null) {
                Text(
                    invoiceNo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SuccessDetails(
    total: Long,
    changeAmount: Long,
    onPrint: () -> Unit,
    onNewTx: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        DetailRow(label = "Total Pembayaran", value = formatRupiah(total), isTotal = true)

        if (changeAmount > 0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ChangeRow(changeAmount = changeAmount)
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 20.dp),
            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick  = onPrint,
                modifier = Modifier.weight(1f),
                shape    = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cetak Struk")
            }
            RancakButton(
                text     = "Transaksi Baru",
                onClick  = onNewTx,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isTotal: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style      = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ChangeRow(changeAmount: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                Icons.Default.SwapHoriz, contentDescription = null,
                tint     = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                "Kembalian",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            formatRupiah(changeAmount),
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.secondary
        )
    }
}

// ── Previews (Koin-free via PaymentSuccessCard) ─────────────────────────────

@Preview
@Composable
private fun PaymentSuccessPreview_WithChange() {
    RancakTheme {
        PaymentSuccessCard(
            invoiceNo    = "INV-2024-0001",
            total        = 75_000L,
            changeAmount = 25_000L,
            onPrint      = {},
            onNewTx      = {},
            modifier     = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun PaymentSuccessPreview_Exact() {
    RancakTheme {
        PaymentSuccessCard(
            invoiceNo    = "INV-2024-0002",
            total        = 42_000L,
            changeAmount = 0L,
            onPrint      = {},
            onNewTx      = {},
            modifier     = Modifier.fillMaxSize()
        )
    }
}

/**
 * Preview-only rendering of the success card; kept for backwards compatibility
 * with callers that invoked `PaymentSuccessPreviewContent`.
 */
@Composable
internal fun PaymentSuccessPreviewContent(modifier: Modifier = Modifier) {
    PaymentSuccessCard(
        invoiceNo    = "INV-2024-0001",
        total        = 75_000L,
        changeAmount = 25_000L,
        onPrint      = {},
        onNewTx      = {},
        modifier     = modifier
    )
}

@Suppress("unused")
private fun previewSale(): Sale = Sale(
    uuid          = "s1",
    invoiceNo     = "INV-2024-0001",
    orderType     = OrderType.DINE_IN,
    queueNumber   = 1,
    status        = SaleStatus.PAID,
    customerName  = null,
    subtotal      = 75_000L,
    discount      = 0L,
    surcharge     = 0L,
    tax           = 0L,
    total         = 75_000L,
    paymentMethod = PaymentMethod.CASH,
    paidAmount    = 100_000L,
    changeAmount  = 25_000L,
    items = listOf(
        SaleItem(
            uuid = "i1", productUuid = "p1", productName = "Kopi",
            qty = "1", price = 75_000L, subtotal = 75_000L,
            variantName = null, note = null
        )
    ),
    createdAt = null
)
