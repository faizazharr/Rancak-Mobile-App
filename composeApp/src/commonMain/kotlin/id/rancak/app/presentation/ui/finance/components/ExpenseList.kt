package id.rancak.app.presentation.ui.finance.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
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
import id.rancak.app.presentation.util.formatDateFriendly
import id.rancak.app.presentation.util.formatRupiah
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ExpenseItemCard(
    item: Expense,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.description ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                item.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                item.categoryName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                val displayDate = formatDateFriendly(item.expenseDate ?: item.createdAt)
                Text(
                    text = displayDate ?: "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = formatRupiah(item.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ExpenseList(
    items: ImmutableList<Expense>,
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
                ExpenseItemCard(item = item)
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
                    categoryUuid = null, categoryName = "Operasional", cashierUuid = null, cashierName = null,
                    expenseDate = "2026-04-29", createdAt = "2026-04-29T10:00:00Z", updatedAt = null),
                Expense(uuid = "2", amount = 25000, description = "Beli Tisu", note = null,
                    categoryUuid = null, categoryName = null, cashierUuid = null, cashierName = null,
                    expenseDate = null, createdAt = "2026-04-28T08:30:00Z", updatedAt = null)
            ).toImmutableList(),
            onDelete = {}
        )
    }
}
