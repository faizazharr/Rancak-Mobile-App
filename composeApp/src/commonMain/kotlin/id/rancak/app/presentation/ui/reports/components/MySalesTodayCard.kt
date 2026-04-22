package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.MySalesReport
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/** Kartu ringkas penjualan kasir saat ini di hari ini. */
@Composable
internal fun MySalesTodayCard(mySales: MySalesReport) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Penjualan Saya Hari Ini",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            FinanceRow(
                label = "Total Penjualan",
                value = formatRupiah(mySales.totalSales),
                color = MaterialTheme.colorScheme.primary
            )
            FinanceRow(
                label = "Jumlah Transaksi",
                value = "${mySales.totalTransactions}x",
                color = MaterialTheme.colorScheme.onSurface
            )
            FinanceRow(
                label = "Total Tunai",
                value = formatRupiah(mySales.cashTotal),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun MySalesTodayCardPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp)) {
            MySalesTodayCard(
                MySalesReport(totalSales = 1_250_000, totalTransactions = 14, cashTotal = 650_000)
            )
        }
    }
}
