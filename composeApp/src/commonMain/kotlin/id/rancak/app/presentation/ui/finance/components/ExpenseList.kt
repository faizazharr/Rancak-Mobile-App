package id.rancak.app.presentation.ui.finance.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Expense
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

@Composable
fun ExpenseList(
    items: List<Expense>,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        EmptyScreen("Belum ada pengeluaran")
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.uuid }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.description ?: "-", style = MaterialTheme.typography.bodyMedium)
                            item.note?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Text(formatRupiah(item.amount), style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ExpenseListPreview() {
    RancakTheme {
        ExpenseList(
            items = listOf(
                Expense(uuid = "1", amount = 50000, description = "Beli Gas", note = "2 tabung",
                    categoryUuid = null, categoryName = null, cashierUuid = null, cashierName = null,
                    expenseDate = null, createdAt = null, updatedAt = null),
                Expense(uuid = "2", amount = 25000, description = "Beli Tisu", note = null,
                    categoryUuid = null, categoryName = null, cashierUuid = null, cashierName = null,
                    expenseDate = null, createdAt = null, updatedAt = null)
            ),
            onDelete = {}
        )
    }
}
