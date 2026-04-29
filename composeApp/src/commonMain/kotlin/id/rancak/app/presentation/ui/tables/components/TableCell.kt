package id.rancak.app.presentation.ui.tables.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun TableCell(
    table: Table,
    size: Dp = 100.dp,
    enabled: Boolean = table.status == TableStatus.AVAILABLE,
    onClick: () -> Unit
) {
    val semantic = RancakColors.semantic
    val statusColor by animateColorAsState(
        targetValue = when (table.status) {
            TableStatus.AVAILABLE -> semantic.statusAvailable
            TableStatus.OCCUPIED  -> semantic.statusOccupied
            TableStatus.INACTIVE  -> semantic.statusMaintenance
        },
        animationSpec = tween(300),
        label = "tableStatusColor"
    )

    val isInactive = table.status == TableStatus.INACTIVE
    val bgAlpha   = if (isInactive) 0.05f else 0.10f
    val elevation = if (table.status == TableStatus.AVAILABLE && enabled) 2.dp else 0.dp

    Box(modifier = Modifier.size(size)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation, shape = MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (isInactive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = if (table.status == TableStatus.AVAILABLE && enabled) 1.5.dp else 1.dp,
                    color = statusColor.copy(alpha = if (isInactive) 0.25f else 0.6f),
                    shape = MaterialTheme.shapes.medium
                )
                .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
        ) {
        // Status accent strip di atas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(statusColor.copy(alpha = if (isInactive) 0.25f else 1f))
            )

            // Konten meja — dipusatkan dalam sisa ruang
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                // Nama meja
                Text(
                    text      = table.name,
                    style     = MaterialTheme.typography.titleSmall.copy(fontSize = if (size >= 120.dp) 14.sp else 12.sp),
                    fontWeight = FontWeight.Bold,
                    color     = if (isInactive)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )

                // Status label chip
                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = bgAlpha + 0.05f)
                ) {
                    Text(
                        text  = statusLabel(table.status),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = statusColor.copy(alpha = if (isInactive) 0.5f else 1f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }

                // Kapasitas
                table.capacity?.let { cap ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Chair,
                            contentDescription = null,
                            modifier           = Modifier.size(10.dp),
                            tint               = MaterialTheme.colorScheme.outline.copy(alpha = if (isInactive) 0.4f else 1f)
                        )
                        Text(
                            text  = "$cap",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = if (isInactive) 0.4f else 1f)
                        )
                    }
                }

                // Dot "ada transaksi aktif" saat occupied
                if (table.status == TableStatus.OCCUPIED) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                }
            }
        }

        // Overlay scrim + ikon Block untuk meja INACTIVE
        if (isInactive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Block,
                    contentDescription = "Nonaktif",
                    modifier           = Modifier.size(if (size >= 120.dp) 28.dp else 22.dp),
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                )
            }
        }
    }
}

private fun statusLabel(status: TableStatus) = when (status) {
    TableStatus.AVAILABLE -> "Tersedia"
    TableStatus.OCCUPIED  -> "Dipakai"
    TableStatus.INACTIVE  -> "Nonaktif"
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun TableCellAvailablePreview() {
    RancakTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            TableCell(
                table = Table(uuid = "1", name = "Meja 1", area = "Indoor", capacity = 4,
                    status = TableStatus.AVAILABLE, isActive = true, sortOrder = 1, activeSaleUuid = null),
                onClick = {}
            )
            TableCell(
                table = Table(uuid = "2", name = "B3", area = "Outdoor", capacity = 6,
                    status = TableStatus.OCCUPIED, isActive = true, sortOrder = 2, activeSaleUuid = "sale-1"),
                onClick = {}
            )
            TableCell(
                table = Table(uuid = "3", name = "VIP", area = "Private", capacity = 8,
                    status = TableStatus.INACTIVE, isActive = false, sortOrder = 3, activeSaleUuid = null),
                onClick = {}
            )
        }
    }
}
