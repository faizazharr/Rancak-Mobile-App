package id.rancak.app.presentation.ui.sales

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
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.SalesHistoryViewModel
import androidx.compose.ui.tooling.preview.Preview
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
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.sales, key = { it.uuid }) { sale ->
                        SaleCard(sale)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleCard(sale: Sale) {
    val semantic = id.rancak.app.presentation.designsystem.RancakColors.semantic
    val statusColor = when (sale.status) {
        SaleStatus.PAID -> semantic.success
        SaleStatus.HELD -> semantic.warning
        SaleStatus.VOID, SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.SERVED -> semantic.info
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(1.dp)
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

@Preview
@Composable
private fun SaleCardPaidPreview() {
    RancakTheme {
        SaleCard(
            sale = Sale(
                uuid = "1",
                invoiceNo = "INV-2024-001",
                orderType = id.rancak.app.domain.model.OrderType.DINE_IN,
                queueNumber = 5,
                status = SaleStatus.PAID,
                subtotal = 75000,
                discount = 0,
                surcharge = 0,
                tax = 7500,
                total = 82500,
                paymentMethod = id.rancak.app.domain.model.PaymentMethod.CASH,
                paidAmount = 100000,
                changeAmount = 17500,
                items = listOf(
                    id.rancak.app.domain.model.SaleItem("i1", "Nasi Goreng", "2", 25000, 50000, null, null),
                    id.rancak.app.domain.model.SaleItem("i2", "Es Teh", "1", 8000, 8000, null, null)
                ),
                createdAt = "2024-01-15T10:30:00"
            )
        )
    }
}

@Preview
@Composable
private fun SaleCardVoidPreview() {
    RancakTheme {
        SaleCard(
            sale = Sale(
                uuid = "2",
                invoiceNo = "INV-2024-002",
                orderType = id.rancak.app.domain.model.OrderType.TAKEAWAY,
                queueNumber = 6,
                status = SaleStatus.VOID,
                subtotal = 30000,
                discount = 0,
                surcharge = 0,
                tax = 3000,
                total = 33000,
                paymentMethod = null,
                paidAmount = 0,
                changeAmount = 0,
                items = emptyList(),
                createdAt = "2024-01-15T11:00:00"
            )
        )
    }
}
