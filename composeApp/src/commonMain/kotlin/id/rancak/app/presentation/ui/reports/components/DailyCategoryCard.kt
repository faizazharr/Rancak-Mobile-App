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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.DailyCategoryReport
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

/** Kartu rincian penjualan hari ini per kategori produk. */
@Composable
internal fun DailyCategoryCard(categories: List<DailyCategoryReport>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Penjualan per Kategori",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            categories.forEach { cat ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text       = cat.categoryName,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text  = "${cat.totalQty.toInt()} item",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text       = formatRupiah(cat.totalSales),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DailyCategoryCardPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp)) {
            DailyCategoryCard(
                categories = listOf(
                    DailyCategoryReport(categoryName = "Kopi",     totalSales = 4_500_000, totalQty = 28.0),
                    DailyCategoryReport(categoryName = "Makanan",  totalSales = 2_800_000, totalQty = 14.0),
                    DailyCategoryReport(categoryName = "Snack",    totalSales =   750_000, totalQty =  9.0)
                )
            )
        }
    }
}
