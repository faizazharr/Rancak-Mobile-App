package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.OpnameItem
import id.rancak.app.domain.model.OpnameItemEntry
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.StockOpname
import id.rancak.app.domain.model.StockOpnameDetail
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun OpnameDetailContent(
    detail: StockOpnameDetail,
    products: List<Product>,
    isSubmitting: Boolean,
    isLoading: Boolean,
    showFinalizeConfirm: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSaveItems: (List<OpnameItemEntry>) -> Unit,
    onFinalizeClick: () -> Unit,
    onFinalizeConfirm: () -> Unit,
    onFinalizeDismiss: () -> Unit
) {
    val stockInputs = remember(detail.opname.uuid) {
        mutableStateMapOf<String, String>().apply {
            detail.items.forEach { put(it.productUuid, it.actualStock.toString()) }
        }
    }
    val isDraft = detail.opname.status == "draft"

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Opname #${detail.opname.opnameNo}",
                icon     = Icons.Default.Inventory,
                onBack   = onBack,
                subtitle = "${detail.items.size} item · ${if (isDraft) "Draft" else "Final"}"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isDraft) {
                Surface(shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val entries = stockInputs.entries.mapNotNull { (uuid, text) ->
                                    text.toDoubleOrNull()?.let { OpnameItemEntry(uuid, it) }
                                }
                                onSaveItems(entries)
                            },
                            modifier = Modifier.weight(1f),
                            enabled  = !isSubmitting
                        ) { Text("Simpan") }
                        Button(
                            onClick  = onFinalizeClick,
                            modifier = Modifier.weight(1f),
                            enabled  = !isSubmitting && detail.items.isNotEmpty()
                        ) { Text("Finalisasi") }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            LoadingScreen(Modifier.padding(padding))
        } else {
            Column(Modifier.padding(padding).fillMaxSize()) {
                if (!isDraft) {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            SummaryChip("Kurang", detail.shortageCount, MaterialTheme.colorScheme.error)
                            SummaryChip("Lebih", detail.surplusCount, MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (isDraft) {
                    ProductSearchSection(products, stockInputs, detail)
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detail.items, key = { it.productUuid }) { item ->
                        OpnameItemCard(
                            item = item,
                            isDraft = isDraft,
                            stockInputValue = stockInputs[item.productUuid] ?: item.actualStock.toString(),
                            onStockInputChange = { stockInputs[item.productUuid] = it }
                        )
                    }
                }
            }
        }

        if (showFinalizeConfirm) {
            AlertDialog(
                onDismissRequest = { if (!isSubmitting) onFinalizeDismiss() },
                title = { Text("Finalisasi Opname") },
                text  = { Text("Stok sistem akan disesuaikan berdasarkan hasil hitung fisik. Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = {
                    Button(onClick = onFinalizeConfirm, enabled = !isSubmitting) {
                        if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Finalisasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onFinalizeDismiss, enabled = !isSubmitting) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
private fun SummaryChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun OpnameDetailContentPreview() {
    RancakTheme {
        OpnameDetailContent(
            detail = StockOpnameDetail(
                opname = StockOpname("1", "OP-001", "draft", "Opname bulanan", 2, createdAt = "2024-01-15"),
                items = listOf(
                    OpnameItem("p1", "Kopi Arabika", systemStock = 50.0, actualStock = 47.0, difference = -3.0)
                ),
                shortageCount = 1, surplusCount = 0
            ),
            products = emptyList(),
            isSubmitting = false, isLoading = false, showFinalizeConfirm = false,
            snackbarHostState = SnackbarHostState(),
            onBack = {}, onSaveItems = {}, onFinalizeClick = {}, onFinalizeConfirm = {}, onFinalizeDismiss = {}
        )
    }
}
