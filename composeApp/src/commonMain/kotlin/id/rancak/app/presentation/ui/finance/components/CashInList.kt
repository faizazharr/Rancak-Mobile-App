package id.rancak.app.presentation.ui.finance.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.CashIn
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatDateFriendly
import id.rancak.app.presentation.util.formatRupiah
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CashInItemCard(
    item: CashIn,
    modifier: Modifier = Modifier
) {
    val semantic = RancakColors.semantic
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
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
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
                item.source?.let {
                    Text(
                        text = "Sumber: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                val displayDate = formatDateFriendly(item.cashInDate ?: item.createdAt)
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
                color = semantic.success
            )
        }
    }
}

@Composable
fun CashInList(
    items: ImmutableList<CashIn>,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        EmptyScreen("Belum ada kas masuk")
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.uuid }) { item ->
                CashInItemCard(item = item)
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun CashInListPreview() {
    RancakTheme {
        CashInList(
            items = listOf(
                CashIn(uuid = "1", amount = 500000, source = "Modal", description = "Kas Awal", note = null,
                    cashierUuid = null, cashierName = null, shiftUuid = null, cashInDate = "2026-04-29", createdAt = "2026-04-29T07:00:00Z"),
                CashIn(uuid = "2", amount = 200000, source = "Pinjaman", description = "Tambahan Modal", note = "Dari owner",
                    cashierUuid = null, cashierName = null, shiftUuid = null, cashInDate = null, createdAt = "2026-04-28T09:15:00Z")
            ).toImmutableList(),
            onDelete = {}
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

