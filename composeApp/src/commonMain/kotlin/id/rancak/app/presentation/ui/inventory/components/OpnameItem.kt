package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OpnameItem
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme

private fun formatStock(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

@Composable
fun OpnameItem(
    item: OpnameItem,
    isDraft: Boolean,
    stockInputValue: String,
    onStockInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val sem = RancakColors.semantic
    val diffColor = when {
        item.difference == 0.0 -> sem.success
        else                   -> sem.warning
    }

    if (isDraft) {
        Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.productName, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("Sistem: ${formatStock(item.systemStock)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = stockInputValue,
                    onValueChange = { onStockInputChange(it.filter { c -> c.isDigit() || c == '.' }) },
                    label = { Text("Aktual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                if (onDelete != null) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Sistem: ${formatStock(item.systemStock)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Aktual: ${formatStock(item.actualStock)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "Selisih: ${if (item.difference >= 0) "+${formatStock(item.difference)}" else formatStock(item.difference)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = diffColor
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val sampleItem = OpnameItem(
    productUuid = "p1", productName = "Kopi Arabika 1kg",
    systemStock = 50.0, actualStock = 47.0, difference = -3.0
)

@Preview
@Composable
private fun OpnameItemDraftPreview() {
    RancakTheme {
        OpnameItem(item = sampleItem, isDraft = true, stockInputValue = "47",
            onStockInputChange = {})
    }
}

@Preview
@Composable
private fun OpnameItemFinalizedPreview() {
    RancakTheme {
        OpnameItem(item = sampleItem, isDraft = false, stockInputValue = "47",
            onStockInputChange = {})
    }
}
