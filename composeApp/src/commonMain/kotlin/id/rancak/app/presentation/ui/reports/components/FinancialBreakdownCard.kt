package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/** Kartu rincian keuangan shift saat ini — total jual, kas masuk, pengeluaran, kas bersih. */
@Composable
internal fun FinancialBreakdownCard(summary: ShiftSummary) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Rincian Keuangan",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            FinanceRow(
                label = "Total Penjualan",
                value = formatRupiah(summary.totalSales),
                color = MaterialTheme.colorScheme.onSurface
            )
            FinanceRow(
                label = "Total Kas Masuk",
                value = formatRupiah(summary.totalCashIn),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (summary.totalExpenses > 0) {
                FinanceRow(
                    label = "Total Pengeluaran",
                    value = "-${formatRupiah(summary.totalExpenses)}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FinanceRow(
                label = "Kas Bersih",
                value = formatRupiah(summary.totalSales + summary.totalCashIn - summary.totalExpenses),
                color = MaterialTheme.colorScheme.primary,
                bold  = true
            )
        }
    }
}

@Preview
@Composable
private fun FinancialBreakdownCardPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp)) {
            FinancialBreakdownCard(
                summary = ShiftSummary(
                    uuid              = "preview",
                    openedAt          = null,
                    closedAt          = null,
                    status            = "open",
                    openingCash       = "500000",
                    closingCash       = null,
                    expectedCash      = null,
                    cashDifference    = null,
                    cashierName       = "Admin",
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
