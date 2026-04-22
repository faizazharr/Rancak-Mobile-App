package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Baris `label | value` bergaya ringan yang dipakai oleh hampir semua kartu
 * rincian di `ReportScreen` (finansial, info shift, per-kategori, dsb.).
 *
 * Sengaja tanpa `@Preview` — dilihat selalu di dalam konteks kartu di atasnya.
 */
@Composable
internal fun FinanceRow(
    label: String,
    value: String,
    color: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodySmall,
            color      = if (bold) MaterialTheme.colorScheme.onSurface
                         else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color      = color
        )
    }
}
