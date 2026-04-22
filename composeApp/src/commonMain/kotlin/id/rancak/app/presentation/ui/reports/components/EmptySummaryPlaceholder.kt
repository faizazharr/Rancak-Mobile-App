package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

/** Placeholder saat belum ada data laporan untuk periode yang dipilih. */
@Composable
internal fun EmptySummaryPlaceholder() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint     = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tidak ada data untuk periode ini",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun EmptySummaryPlaceholderPreview() {
    RancakTheme { EmptySummaryPlaceholder() }
}
