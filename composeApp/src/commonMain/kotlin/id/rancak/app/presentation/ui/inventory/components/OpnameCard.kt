package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.presentation.components.StatusChip
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun OpnameCard(
    opname: StockOpname,
    onOpen: () -> Unit,
    onCancel: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sem = RancakColors.semantic
    val statusColor = when (opname.status) {
        "finalized" -> sem.success
        "cancelled" -> MaterialTheme.colorScheme.error
        else        -> sem.warning
    }
    val statusLabel = when (opname.status) {
        "finalized" -> "Final"
        "cancelled" -> "Dibatalkan"
        else        -> "Draft"
    }

    val statusIcon = when (opname.status) {
        "finalized" -> Icons.Default.Visibility
        "cancelled" -> Icons.Default.DoNotDisturb
        else        -> Icons.Default.Edit
    }

    Card(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 1.dp
        ),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.10f)
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left status strip
            Box(
                modifier = Modifier.width(3.dp).fillMaxHeight()
                    .background(if (isSelected) Primary else statusColor)
            )
            Row(
                modifier              = Modifier.weight(1f).padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier         = Modifier.size(20.dp).background(
                                statusColor.copy(alpha = 0.14f), MaterialTheme.shapes.extraSmall
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(11.dp))
                        }
                        Text(
                            "#${opname.opnameNo}",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${opname.itemCount} item · ${formatOpnameDate(opname.createdAt)}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                    if (!opname.note.isNullOrBlank()) {
                        Text(
                            opname.note,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    StatusChip(text = statusLabel, color = statusColor)
                }
                if (opname.status == "draft") {
                    IconButton(
                        onClick  = onCancel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Batalkan",
                            tint     = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
