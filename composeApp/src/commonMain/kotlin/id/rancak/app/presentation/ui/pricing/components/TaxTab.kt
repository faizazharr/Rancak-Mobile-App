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
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun TaxTab(
    taxConfigs: List<TaxConfig>,
    onEdit: (TaxConfig) -> Unit,
    onDelete: (TaxConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    if (taxConfigs.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada konfigurasi pajak", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(taxConfigs, key = { it.uuid }) { item ->
            PricingCard(
                title    = item.name,
                subtitle = "${item.rate}% · ${item.applyTo}",
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
private fun TaxTabPreview() {
    RancakTheme {
        TaxTab(
            taxConfigs = listOf(
                TaxConfig("1", "PPN", 11.0, "after_discount", 1, true),
                TaxConfig("2", "PPn BM", 20.0, "before_discount", 2, false)
            ),
            onEdit   = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun TaxTabEmptyPreview() {
    RancakTheme {
        TaxTab(taxConfigs = emptyList(), onEdit = {}, onDelete = {})
    }
}
