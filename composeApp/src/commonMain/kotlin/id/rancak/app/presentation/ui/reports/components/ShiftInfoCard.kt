package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/** Kartu info shift: kasir, status, kas pembukaan/penutupan, selisih. */
@Composable
internal fun ShiftInfoCard(summary: ShiftSummary) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            summary.cashierName?.let {
                FinanceRow(
                    label = "Kasir", value = it,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            FinanceRow(
                label = "Status",
                value = summary.status.replaceFirstChar { it.uppercase() },
                color = MaterialTheme.colorScheme.onSurface
            )
            FinanceRow(
                label = "Kas Pembukaan",
                value = formatRupiah(summary.openingCash.toLongOrNull() ?: 0L),
                color = MaterialTheme.colorScheme.onSurface
            )
            summary.closingCash?.let {
                FinanceRow(
                    label = "Kas Penutupan",
                    value = formatRupiah(it.toLongOrNull() ?: 0L),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            summary.expectedCash?.let {
                FinanceRow(
                    label = "Kas Diharapkan",
                    value = formatRupiah(it.toLongOrNull() ?: 0L),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            summary.cashDifference?.let {
                FinanceRow(
                    label = "Selisih Kas",
                    value = formatRupiah(it.toLongOrNull() ?: 0L),
                    color = MaterialTheme.colorScheme.primary,
                    bold  = true
                )
            }
        }
    }
}

@Preview
@Composable
private fun ShiftInfoCardPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp)) {
            ShiftInfoCard(
                summary = ShiftSummary(
                    uuid              = "preview",
                    openedAt          = "2026-04-18 08:00:00",
                    closedAt          = null,
                    status            = "open",
                    openingCash       = "500000",
                    closingCash       = "8800000",
                    expectedCash      = "8925000",
                    cashDifference    = "-125000",
                    cashierName       = "Andi",
                    totalSales        = 8_750_000L,
                    totalTransactions = 64,
                    totalExpenses     = 325_000L,
                    totalCashIn       = 500_000L,
                    paymentSummary    = emptyList()
                )
            )
        }
    }
}
