package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/** Grid 2×2 KPI utama (penjualan, transaksi, pengeluaran, rata-rata). */
@Composable
internal fun KpiCardsGrid(summary: ShiftSummary) {
    val avgPerTx = if (summary.totalTransactions > 0)
        summary.totalSales / summary.totalTransactions else 0L

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Total Penjualan",
                value     = formatRupiah(summary.totalSales),
                icon      = Icons.Default.BarChart,
                iconColor = MaterialTheme.colorScheme.primary
            )
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Transaksi",
                value     = "${summary.totalTransactions}x",
                icon      = Icons.Default.ShoppingCart,
                iconColor = Color(0xFF2196F3)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Total Pengeluaran",
                value     = formatRupiah(summary.totalExpenses),
                icon      = Icons.Default.Star,
                iconColor = Color(0xFF4CAF50)
            )
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Rata-rata/Transaksi",
                value     = formatRupiah(avgPerTx),
                icon      = Icons.AutoMirrored.Filled.TrendingUp,
                iconColor = Color(0xFF00ACC1)
            )
        }
    }
}

/** Satu kartu KPI — icon bulat + label + nilai besar. */
@Composable
internal fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(17.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
private fun KpiCardsGridPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp)) {
            KpiCardsGrid(
                summary = ShiftSummary(
                    uuid              = "preview",
                    openedAt          = "2026-04-18 08:00:00",
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
