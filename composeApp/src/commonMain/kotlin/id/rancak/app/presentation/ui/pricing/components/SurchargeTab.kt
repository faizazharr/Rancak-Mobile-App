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
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah

@Composable
fun SurchargeTab(
    surcharges: List<Surcharge>,
    onEdit: (Surcharge) -> Unit,
    onDelete: (Surcharge) -> Unit,
    modifier: Modifier = Modifier
) {
    if (surcharges.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada surcharge", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(surcharges, key = { it.uuid }) { item ->
            val valueLabel = if (item.isPercentage) "${item.amount}%" else formatRupiah(item.amount)
            PricingCard(
                title    = item.name,
                subtitle = "$valueLabel · ${item.orderType ?: "Semua"}",
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
private fun SurchargeTabPreview() {
    RancakTheme {
        SurchargeTab(
            surcharges = listOf(
                Surcharge("1", "all",      "Biaya Layanan",   5L,    true,  null, true, 0),
                Surcharge("2", "delivery", "Biaya Pengiriman", 5000L, false, null, true, 1)
            ),
            onEdit   = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun SurchargeTabEmptyPreview() {
    RancakTheme {
        SurchargeTab(surcharges = emptyList(), onEdit = {}, onDelete = {})
    }
}
