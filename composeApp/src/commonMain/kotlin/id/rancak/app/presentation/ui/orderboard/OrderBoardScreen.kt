package id.rancak.app.presentation.ui.orderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.OrderBoardViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBoardScreen(
    onBack: () -> Unit,
    viewModel: OrderBoardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOrders() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Board") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadOrders) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadOrders, modifier = Modifier.padding(padding))
            uiState.orders.isEmpty() -> EmptyScreen("Tidak ada order aktif", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.orders, key = { it.uuid }) { order ->
                    OrderBoardCard(
                        order = order,
                        onServe = { viewModel.serveOrder(order.uuid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderBoardCard(order: Sale, onServe: () -> Unit) {
    val semantic = id.rancak.app.presentation.designsystem.RancakColors.semantic
    val statusColor = when (order.status) {
        SaleStatus.HELD -> semantic.warning
        SaleStatus.SERVED -> semantic.success
        else -> semantic.statusMaintenance
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        order.queueNumber?.let {
                            Text("#$it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        StatusChip(order.status.value.replaceFirstChar { it.uppercase() }, statusColor)
                    }
                    Text(
                        order.invoiceNo ?: "-",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(formatRupiah(order.total), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            if (order.items.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    order.items.joinToString(", ") { "${it.qty}x ${it.productName}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            if (order.status == SaleStatus.HELD) {
                Spacer(Modifier.height(8.dp))
                RancakOutlinedButton(
                    text = "Tandai Diantar",
                    onClick = onServe,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun OrderBoardCardPreview() {
    RancakTheme {
        OrderBoardCard(
            order = Sale(
                uuid = "1",
                invoiceNo = "INV-2024-001",
                orderType = id.rancak.app.domain.model.OrderType.DINE_IN,
                queueNumber = 5,
                status = SaleStatus.HELD,
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
            ),
            onServe = {}
        )
    }
}
