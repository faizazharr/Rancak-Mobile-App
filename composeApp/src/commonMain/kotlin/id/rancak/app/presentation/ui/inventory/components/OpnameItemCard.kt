package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OpnameItem
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun OpnameItemCard(
    item: OpnameItem,
    isDraft: Boolean,
    stockInputValue: String,
    onStockInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val diffColor = when {
        item.difference < 0 -> MaterialTheme.colorScheme.error
        item.difference > 0 -> MaterialTheme.colorScheme.primary
        else                -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("Sistem: ${item.systemStock}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!isDraft) {
                    Text("Aktual: ${item.actualStock}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Selisih: ${if (item.difference >= 0) "+${item.difference}" else "${item.difference}"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = diffColor, fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (isDraft) {
                OutlinedTextField(
                    value = stockInputValue,
                    onValueChange = { onStockInputChange(it.filter { c -> c.isDigit() || c == '.' }) },
                    label = { Text("Aktual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
            }
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
private fun OpnameItemCardDraftPreview() {
    RancakTheme {
        OpnameItemCard(item = sampleItem, isDraft = true, stockInputValue = "47",
            onStockInputChange = {})
    }
}

@Preview
@Composable
private fun OpnameItemCardFinalizedPreview() {
    RancakTheme {
        OpnameItemCard(item = sampleItem, isDraft = false, stockInputValue = "47",
            onStockInputChange = {})
    }
}
