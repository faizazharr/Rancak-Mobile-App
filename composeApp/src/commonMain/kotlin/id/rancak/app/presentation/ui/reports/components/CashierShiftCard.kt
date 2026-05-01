package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.CashierShiftSummary
import id.rancak.app.presentation.util.formatRupiah

/** Kartu ringkasan shift satu kasir untuk tab "Per Kasir" di laporan. */
@Composable
internal fun CashierShiftCard(summary: CashierShiftSummary, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text      = summary.cashierName,
                    style     = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color     = MaterialTheme.colorScheme.onSurface
                )
                val statusColor = if (summary.shiftStatus == "open")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
                Text(
                    text  = if (summary.shiftStatus == "open") "Aktif" else "Selesai",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }

            FinanceRow(
                label = "Transaksi",
                value = "${summary.totalTransactions} transaksi",
                color = MaterialTheme.colorScheme.onSurface
            )
            FinanceRow(
                label = "Total Penjualan",
                value = formatRupiah(summary.grossTotal.toLong()),
                color = MaterialTheme.colorScheme.primary,
                bold  = true
            )
            FinanceRow(
                label = "Tunai",
                value = formatRupiah(summary.cashTotal.toLong()),
                color = MaterialTheme.colorScheme.onSurface
            )
            FinanceRow(
                label = "Non-tunai",
                value = formatRupiah(summary.nonCashTotal.toLong()),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (summary.voidCount > 0) {
                FinanceRow(
                    label = "Void",
                    value = "${summary.voidCount}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            summary.cashDifference?.let { diff ->
                val diffColor = if (diff >= 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
                FinanceRow(
                    label = "Selisih Kas",
                    value = formatRupiah(diff.toLong()),
                    color = diffColor,
                    bold  = true
                )
            }
        }
    }
}
