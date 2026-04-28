package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OpnameItem
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun ProductSearchSection(
    products: List<Product>,
    stockInputs: MutableMap<String, String>,
    detail: StockOpnameDetail,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val existing = detail.items.map { it.productUuid }.toSet()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Tambah produk ke opname…") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        if (query.isNotBlank()) {
            val filtered = products
                .filter { it.name.contains(query, ignoreCase = true) && it.uuid !in existing }
                .take(5)
            filtered.forEach { product ->
                ListItem(
                    headlineContent = { Text(product.name) },
                    supportingContent = { Text("Stok saat ini: ${product.stock}") },
                    trailingContent = {
                        IconButton(onClick = {
                            stockInputs[product.uuid] = product.stock.toString()
                            query = ""
                        }) { Icon(Icons.Default.Add, null) }
                    }
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ProductSearchSectionPreview() {
    RancakTheme {
        val detail = StockOpnameDetail(
            opname = StockOpname("1", "OP-001", "draft", createdAt = "2024-01-01"),
            items = emptyList(), shortageCount = 0, surplusCount = 0
        )
        ProductSearchSection(
            products = emptyList(),
            stockInputs = mutableMapOf(),
            detail = detail
        )
    }
}
