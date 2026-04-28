package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.DiscountRule
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

@Composable
fun DiscountTab(
    rules: List<DiscountRule>,
    onEdit: (DiscountRule) -> Unit,
    onDelete: (DiscountRule) -> Unit,
    modifier: Modifier = Modifier
) {
    if (rules.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada aturan diskon", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(rules, key = { it.uuid }) { item ->
            val valueLabel = if (item.discountType == "pct") "${item.discountValue}%" else formatRupiah(item.discountValue.toLong())
            PricingCard(
                title    = item.name,
                subtitle = "$valueLabel · ${item.ruleType}",
                isActive = item.isActive,
                onEdit   = { onEdit(item) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun DiscountTabPreview() {
    RancakTheme {
        DiscountTab(
            rules = listOf(
                DiscountRule("1", "Diskon Happy Hour", null, "time_based", "pct",  10.0,   null, null, null, null, 0, false, null, true),
                DiscountRule("2", "Member Discount",   null, "always",     "flat", 5000.0, null, null, null, null, 0, false, null, true)
            ),
            onEdit   = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun DiscountTabEmptyPreview() {
    RancakTheme {
        DiscountTab(rules = emptyList(), onEdit = {}, onDelete = {})
    }
}
