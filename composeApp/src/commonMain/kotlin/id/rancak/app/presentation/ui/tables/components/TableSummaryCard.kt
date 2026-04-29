package id.rancak.app.presentation.ui.tables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.designsystem.RancakColors

/**
 * Ringkasan statistik meja: total, tersedia, terisi, tidak aktif.
 */
@Composable
fun TableSummaryCard(
    total: Int,
    available: Int,
    occupied: Int,
    inactive: Int,
    modifier: Modifier = Modifier
) {
    val semantic = RancakColors.semantic
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Meja", style = MaterialTheme.typography.bodySmall)
                Text("$total", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
            TableStatusRow(
                label = "Tersedia",
                count = available,
                color = semantic.statusAvailable
            )
            TableStatusRow(
                label = "Terisi",
                count = occupied,
                color = semantic.statusOccupied
            )
            TableStatusRow(
                label = "Tidak Aktif",
                count = inactive,
                color = semantic.statusMaintenance
            )
        }
    }
}

@Composable
private fun TableStatusRow(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(10.dp).background(color, MaterialTheme.shapes.small))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

/**
 * Ringkasan per area: nama area, jumlah meja, dan jumlah terisi.
 */
@Composable
fun AreaSummaryCard(
    area: String,
    totalCount: Int,
    occupiedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(area, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "$totalCount meja · $occupiedCount terisi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
