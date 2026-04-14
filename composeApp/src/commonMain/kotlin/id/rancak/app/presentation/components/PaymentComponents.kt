package id.rancak.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun PaymentMethodChip(
    method: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = id.rancak.app.presentation.designsystem.RancakColors.semantic
    val color = when (method.lowercase()) {
        "cash" -> semantic.paymentCash
        "card" -> semantic.paymentCard
        "qris" -> semantic.paymentQris
        "transfer" -> semantic.paymentTransfer
        else -> MaterialTheme.colorScheme.outline
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                method.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.15f),
            selectedLabelColor = color
        ),
        modifier = modifier
    )
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

// ── Previews ──

@Preview
@Composable
private fun PaymentMethodChipPreview() {
    RancakTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentMethodChip(method = "cash", isSelected = true, onClick = {})
            PaymentMethodChip(method = "qris", isSelected = false, onClick = {})
            PaymentMethodChip(method = "card", isSelected = false, onClick = {})
            PaymentMethodChip(method = "transfer", isSelected = false, onClick = {})
        }
    }
}

@Preview
@Composable
private fun SummaryRowPreview() {
    RancakTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                SummaryRow(label = "Subtotal", value = "Rp 70.000")
                SummaryRow(label = "Diskon", value = "- Rp 5.000", valueColor = MaterialTheme.colorScheme.error)
                SummaryRow(label = "Pajak (10%)", value = "Rp 6.500")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SummaryRow(label = "Total", value = "Rp 71.500", isBold = true, valueColor = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
