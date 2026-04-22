package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.reports.ReportPeriod

/** Baris FilterChip horizontal untuk memilih periode laporan. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PeriodSelectorRow(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ReportPeriod.entries.forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick  = { onSelect(period) },
                label    = {
                    Text(
                        period.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    selectedLabelColor     = MaterialTheme.colorScheme.primary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled             = true,
                    selected            = selected == period,
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    selectedBorderWidth = 1.2.dp,
                    borderColor         = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    borderWidth         = 0.8.dp
                )
            )
        }
    }
}

@Preview
@Composable
private fun PeriodSelectorRowPreview() {
    RancakTheme {
        PeriodSelectorRow(selected = ReportPeriod.THIS_MONTH, onSelect = {})
    }
}
