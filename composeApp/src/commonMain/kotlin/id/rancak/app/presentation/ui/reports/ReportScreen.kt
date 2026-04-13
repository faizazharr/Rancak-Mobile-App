package id.rancak.app.presentation.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.ReportViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBack: () -> Unit,
    viewModel: ReportViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.dateFrom.isBlank()) {
            viewModel.setDateRange("2026-04-01", "2026-04-30")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan") },
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
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadReport, modifier = Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.summary?.let { summary ->
                    item {
                        Text("Ringkasan Penjualan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SummaryRow("Total Penjualan", formatRupiah(summary.totalSales), MaterialTheme.colorScheme.primary)
                                SummaryRow("Jumlah Transaksi", summary.totalTransactions.toString(), MaterialTheme.colorScheme.onSurface)
                                SummaryRow("Total Diskon", formatRupiah(summary.totalDiscount), MaterialTheme.colorScheme.error)
                                SummaryRow("Total Pajak", formatRupiah(summary.totalTax), MaterialTheme.colorScheme.onSurfaceVariant)
                                HorizontalDivider()
                                SummaryRow("Pendapatan Bersih", formatRupiah(summary.totalNet), MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (summary.paymentMethods.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("Per Metode Bayar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        item {
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    summary.paymentMethods.forEach { pm ->
                                        SummaryRow(
                                            pm.method.replaceFirstChar { it.uppercase() },
                                            "${formatRupiah(pm.total)} (${pm.count}x)",
                                            MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.topProducts.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("Produk Terlaris", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.topProducts.take(10)) { product ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(product.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${product.qtySold} terjual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Text(formatRupiah(product.totalRevenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Preview
@Composable
private fun ReportScreenPreview() {
    RancakTheme {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Ringkasan Penjualan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("Total Penjualan", "Rp 5.250.000", MaterialTheme.colorScheme.primary)
                        SummaryRow("Jumlah Transaksi", "48", MaterialTheme.colorScheme.onSurface)
                        SummaryRow("Total Diskon", "Rp 150.000", MaterialTheme.colorScheme.error)
                        SummaryRow("Total Pajak", "Rp 525.000", MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider()
                        SummaryRow("Pendapatan Bersih", "Rp 5.625.000", MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text("Per Metode Bayar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SummaryRow("Cash", "Rp 3.000.000 (25x)", MaterialTheme.colorScheme.onSurface)
                        SummaryRow("Qris", "Rp 1.500.000 (15x)", MaterialTheme.colorScheme.onSurface)
                        SummaryRow("Card", "Rp 750.000 (8x)", MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text("Produk Terlaris", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(listOf("Nasi Goreng" to "Rp 1.250.000", "Ayam Bakar" to "Rp 875.000", "Es Teh" to "Rp 400.000")) { (name, revenue) ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("50 terjual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(revenue, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
