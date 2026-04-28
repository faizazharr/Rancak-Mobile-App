package id.rancak.app.presentation.ui.tables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun TableCell(
    table: Table,
    size: Dp = 100.dp,
    onClick: () -> Unit
) {
    val semantic = RancakColors.semantic
    val bg = when (table.status) {
        TableStatus.AVAILABLE -> semantic.statusAvailable
        TableStatus.OCCUPIED  -> semantic.statusOccupied
        TableStatus.INACTIVE  -> semantic.statusMaintenance
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.medium)
            .background(bg.copy(alpha = 0.15f))
            .clickable(enabled = table.status == TableStatus.AVAILABLE) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(table.name, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = bg, textAlign = TextAlign.Center)
            Text(table.status.value.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall, color = bg)
            table.capacity?.let {
                Text("$it kursi", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TableCellAvailablePreview() {
    RancakTheme {
        TableCell(
            table = Table(uuid = "1", name = "A1", area = "Indoor", capacity = 4,
                status = TableStatus.AVAILABLE, isActive = true, sortOrder = 1, activeSaleUuid = null),
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun TableCellOccupiedPreview() {
    RancakTheme {
        TableCell(
            table = Table(uuid = "2", name = "B3", area = "Outdoor", capacity = 6,
                status = TableStatus.OCCUPIED, isActive = true, sortOrder = 2, activeSaleUuid = "sale-1"),
            onClick = {}
        )
    }
}
