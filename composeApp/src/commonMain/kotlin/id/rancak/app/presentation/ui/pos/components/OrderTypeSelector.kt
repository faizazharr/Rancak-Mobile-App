package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
internal fun OrderTypeSelector(
    selected:        OrderType,
    surface:         Color,
    primary:         Color,
    onSurfaceVariant: Color,
    onSelect:        (OrderType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OrderType.entries.forEach { type ->
            val isSelected = selected == type
            val (icon, label) = when (type) {
                OrderType.DINE_IN  -> Icons.Default.Restaurant    to "Dine In"
                OrderType.TAKEAWAY -> Icons.Default.ShoppingBag   to "Takeaway"
                OrderType.DELIVERY -> Icons.Default.DeliveryDining to "Delivery"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(if (isSelected) primary else surface)
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.shapes.large
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        icon, null,
                        Modifier.size(16.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else onSurfaceVariant
                    )
                    Text(
                        label,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) MaterialTheme.colorScheme.onPrimary else onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun OrderTypeSelectorPreview_DineIn() {
    RancakTheme {
        OrderTypeSelector(
            selected         = OrderType.DINE_IN,
            surface          = MaterialTheme.colorScheme.surface,
            primary          = MaterialTheme.colorScheme.primary,
            onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
            onSelect         = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderTypeSelectorPreview_Takeaway() {
    RancakTheme {
        OrderTypeSelector(
            selected         = OrderType.TAKEAWAY,
            surface          = MaterialTheme.colorScheme.surface,
            primary          = MaterialTheme.colorScheme.primary,
            onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
            onSelect         = {}
        )
    }
}
