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
import id.rancak.app.domain.model.PaymentMethodReport
import id.rancak.app.domain.model.ProductReport
import id.rancak.app.domain.model.ReportSummary
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.ReportUiState
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
        ReportScreenContent(
            uiState = uiState,
            onRetry = viewModel::loadReport,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun ReportScreenContent(
    uiState: ReportUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> LoadingScreen(modifier)
        uiState.error != null -> ErrorScreen(uiState.error, onRetry = onRetry, modifier = modifier)
        else -> LazyColumn(
            modifier = modifier,
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
                            ReportSummaryRow("Total Penjualan", formatRupiah(summary.totalSales), MaterialTheme.colorScheme.primary)
                            ReportSummaryRow("Jumlah Transaksi", summary.totalTransactions.toString(), MaterialTheme.colorScheme.onSurface)
                            ReportSummaryRow("Total Diskon", formatRupiah(summary.totalDiscount), MaterialTheme.colorScheme.error)
                            ReportSummaryRow("Total Pajak", formatRupiah(summary.totalTax), MaterialTheme.colorScheme.onSurfaceVariant)
                            HorizontalDivider()
                            ReportSummaryRow("Pendapatan Bersih", formatRupiah(summary.totalNet), MaterialTheme.colorScheme.primary)
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
                                    ReportSummaryRow(
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

@Composable
private fun ReportSummaryRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Preview
@Composable
private fun ReportScreenPreview() {
    RancakTheme {
        val mockUiState = ReportUiState(
            summary = ReportSummary(
                totalSales = 5250000,
                totalTransactions = 48,
                totalDiscount = 150000,
                totalTax = 525000,
                totalNet = 5625000,
                paymentMethods = listOf(
                    PaymentMethodReport("cash", 3000000, 25),
                    PaymentMethodReport("qris", 1500000, 15),
                    PaymentMethodReport("card", 750000, 8)
                )
            ),
            topProducts = listOf(
                ProductReport("prod-1", "Nasi Goreng", 50, 1250000),
                ProductReport("prod-2", "Ayam Bakar", 35, 875000),
                ProductReport("prod-3", "Es Teh", 80, 400000)
            ),
            isLoading = false,
            error = null,
            dateFrom = "2026-04-01",
            dateTo = "2026-04-30"
        )

        ReportScreenContent(
            uiState = mockUiState,
            onRetry = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
