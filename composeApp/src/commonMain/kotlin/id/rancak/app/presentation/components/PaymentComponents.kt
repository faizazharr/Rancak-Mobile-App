package id.rancak.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme

private fun paymentIcon(method: String): ImageVector = when (method.lowercase()) {
    "cash"     -> Icons.Default.Payments
    "card"     -> Icons.Default.CreditCard
    "qris"     -> Icons.Default.QrCode2
    "transfer" -> Icons.Default.AccountBalance
    else       -> Icons.Default.MoreHoriz
}

@Composable
fun PaymentMethodChip(
    method: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = RancakColors.semantic
    val accentColor = when (method.lowercase()) {
        "cash"     -> semantic.paymentCash
        "card"     -> semantic.paymentCard
        "qris"     -> semantic.paymentQris
        "transfer" -> semantic.paymentTransfer
        else       -> MaterialTheme.colorScheme.outline
    }
    val icon = paymentIcon(method)

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                accentColor.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor
                    else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = method.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) accentColor
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
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
        Surface(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethodChip(method = "cash",     isSelected = true,  onClick = {}, modifier = Modifier.weight(1f))
                PaymentMethodChip(method = "card",     isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
                PaymentMethodChip(method = "qris",     isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
                PaymentMethodChip(method = "transfer", isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
            }
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
