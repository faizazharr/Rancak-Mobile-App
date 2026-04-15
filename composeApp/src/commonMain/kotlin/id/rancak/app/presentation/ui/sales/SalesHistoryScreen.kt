package id.rancak.app.presentation.ui.sales

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.SalesHistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    onBack: () -> Unit,
    viewModel: SalesHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSales() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Penjualan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadSales, modifier = Modifier.padding(padding))
            uiState.sales.isEmpty() -> EmptyScreen("Belum ada transaksi", Modifier.padding(padding))
            else -> {
                BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                    val isTablet = maxWidth >= 600.dp

                    if (isTablet) {
                        // ── Tablet: master-detail side by side ──
                        Row(Modifier.fillMaxSize()) {
                            // Left — sale list
                            LazyColumn(
                                modifier = Modifier.weight(0.4f).fillMaxHeight(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.sales, key = { it.uuid }) { sale ->
                                    SaleCard(
                                        sale = sale,
                                        isSelected = sale.uuid == uiState.selectedSale?.uuid,
                                        onClick = { viewModel.selectSale(sale) }
                                    )
                                }
                            }

                            VerticalDivider()

                            // Right — detail
                            Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                                val selected = uiState.selectedSale
                                if (selected != null) {
                                    SaleDetailPanel(
                                        sale = selected,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    EmptyDetailPlaceholder(Modifier.fillMaxSize())
                                }
                            }
                        }
                    } else {
                        // ── Phone: list with bottom sheet detail ──
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.sales, key = { it.uuid }) { sale ->
                                SaleCard(
                                    sale = sale,
                                    isSelected = false,
                                    onClick = { viewModel.selectSale(sale) }
                                )
                            }
                        }

                        // Detail dialog for phone
                        uiState.selectedSale?.let { sale ->
                            AlertDialog(
                                onDismissRequest = { viewModel.selectSale(null) },
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Detail Pesanan", style = MaterialTheme.typography.titleMedium)
                                        IconButton(onClick = { viewModel.selectSale(null) }) {
                                            Icon(Icons.Default.Close, "Tutup")
                                        }
                                    }
                                },
                                text = {
                                    SaleDetailPanel(
                                        sale = sale,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.selectSale(null) }) {
                                        Text("Tutup")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sale Card (list item)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SaleCard(
    sale: Sale,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val semantic = RancakColors.semantic
    val statusColor = when (sale.status) {
        SaleStatus.PAID -> semantic.success
        SaleStatus.HELD -> semantic.warning
        SaleStatus.VOID, SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.SERVED -> semantic.info
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(sale.invoiceNo ?: "-", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                StatusChip(sale.status.value.uppercase(), statusColor)
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${sale.items.size} item  •  ${sale.orderType.value.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(formatRupiah(sale.total), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            sale.createdAt?.take(16)?.replace("T", "  ")?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sale Detail Panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SaleDetailPanel(sale: Sale, modifier: Modifier = Modifier) {
    val semantic = RancakColors.semantic
    val statusColor = when (sale.status) {
        SaleStatus.PAID -> semantic.success
        SaleStatus.HELD -> semantic.warning
        SaleStatus.VOID, SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.SERVED -> semantic.info
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: invoice + status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                sale.invoiceNo ?: "-",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            StatusChip(sale.status.value.uppercase(), statusColor)
        }

        // Meta info
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sale.createdAt?.take(16)?.replace("T", "  ")?.let {
                    DetailRow("Waktu", it)
                }
                DetailRow("Tipe Pesanan", sale.orderType.value.replace("_", " ").replaceFirstChar { it.uppercase() })
                sale.queueNumber?.let { DetailRow("No. Antrian", "#$it") }
                sale.paymentMethod?.let {
                    DetailRow("Metode Bayar", it.value.replaceFirstChar { c -> c.uppercase() })
                }
            }
        }

        // Items
        Text(
            "Daftar Item",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Card {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                sale.items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            val sub = buildList {
                                if (!item.variantName.isNullOrBlank()) add(item.variantName)
                                add("${item.qty}x @ ${formatRupiah(item.price)}")
                            }.joinToString("  •  ")
                            Text(
                                sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!item.note.isNullOrBlank()) {
                                Text(
                                    "Catatan: ${item.note}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Text(
                            formatRupiah(item.subtotal),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (index < sale.items.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }

        // Totals
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DetailRow("Subtotal", formatRupiah(sale.subtotal))
                if (sale.discount > 0) {
                    DetailRow("Diskon", "- ${formatRupiah(sale.discount)}", MaterialTheme.colorScheme.error)
                }
                if (sale.surcharge > 0) {
                    DetailRow("Biaya Tambahan", formatRupiah(sale.surcharge))
                }
                if (sale.tax > 0) {
                    DetailRow("Pajak", formatRupiah(sale.tax))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        formatRupiah(sale.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (sale.paidAmount > 0) {
                    DetailRow("Dibayar", formatRupiah(sale.paidAmount))
                }
                if (sale.changeAmount > 0) {
                    DetailRow("Kembalian", formatRupiah(sale.changeAmount))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun EmptyDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Pilih transaksi untuk\nmelihat detail",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
