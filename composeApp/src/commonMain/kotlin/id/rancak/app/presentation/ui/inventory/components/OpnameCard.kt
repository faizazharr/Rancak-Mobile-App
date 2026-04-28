package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun OpnameCard(
    opname: StockOpname,
    onOpen: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (opname.status) {
        "finalized" -> MaterialTheme.colorScheme.primary
        "cancelled" -> MaterialTheme.colorScheme.error
        else        -> MaterialTheme.colorScheme.secondary
    }
    val statusLabel = when (opname.status) {
        "finalized" -> "Final"
        "cancelled" -> "Dibatalkan"
        else        -> "Draft"
    }

    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Opname #${opname.opnameNo}", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text("${opname.itemCount} item · ${opname.createdAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!opname.note.isNullOrBlank()) {
                    Text(opname.note, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.12f)) {
                    Text(statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (opname.status == "draft") {
                    TextButton(onClick = onOpen) { Text("Buka") }
                    TextButton(onClick = onCancel) {
                        Text("Batalkan", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = onOpen) { Text("Lihat") }
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val sampleOpname = StockOpname(
    uuid = "1", opnameNo = "OP-001", status = "draft", note = "Opname bulanan",
    itemCount = 12, createdAt = "2024-01-15T10:00:00"
)

@Preview
@Composable
private fun OpnameCardDraftPreview() {
    RancakTheme {
        OpnameCard(opname = sampleOpname, onOpen = {}, onCancel = {})
    }
}

@Preview
@Composable
private fun OpnameCardFinalizedPreview() {
    RancakTheme {
        OpnameCard(opname = sampleOpname.copy(status = "finalized"), onOpen = {}, onCancel = {})
    }
}
