package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
internal fun OrderPanelHeader(
    hasItems:   Boolean,
    itemCount:  Int,
    surface:    Color,
    primary:    Color,
    onSurface:  Color,
    onClearCart: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text(
                    "Hapus semua pesanan?",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "$itemCount item akan dihapus dari keranjang.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(onClick = { onClearCart(); showClearConfirm = false }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Batalkan") }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Receipt, null, Modifier.size(18.dp), tint = primary)
            Text(
                "Pesanan",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = onSurface
            )
            if (hasItems) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(primary)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        "$itemCount",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (hasItems) {
            IconButton(
                onClick  = { showClearConfirm = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Hapus Semua",
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun OrderPanelHeaderPreview_WithItems() {
    RancakTheme {
        OrderPanelHeader(
            hasItems    = true,
            itemCount   = 5,
            surface     = MaterialTheme.colorScheme.surface,
            primary     = MaterialTheme.colorScheme.primary,
            onSurface   = MaterialTheme.colorScheme.onSurface,
            onClearCart = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderPanelHeaderPreview_Empty() {
    RancakTheme {
        OrderPanelHeader(
            hasItems    = false,
            itemCount   = 0,
            surface     = MaterialTheme.colorScheme.surface,
            primary     = MaterialTheme.colorScheme.primary,
            onSurface   = MaterialTheme.colorScheme.onSurface,
            onClearCart = {}
        )
    }
}
