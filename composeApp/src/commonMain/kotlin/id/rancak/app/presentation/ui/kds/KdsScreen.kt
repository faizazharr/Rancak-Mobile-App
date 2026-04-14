package id.rancak.app.presentation.ui.kds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.*
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.KdsViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KdsScreen(
    onBack: () -> Unit,
    viewModel: KdsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOrders() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Display") },
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
            uiState.orders.isEmpty() -> EmptyScreen("Tidak ada order di dapur", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.orders, key = { it.uuid }) { order ->
                    KdsOrderCard(order, onAdvance = { next ->
                        viewModel.updateOrderStatus(order.uuid, next)
                    })
                }
            }
        }
    }
}

@Composable
private fun KdsOrderCard(order: KdsOrder, onAdvance: (KdsStatus) -> Unit) {
    val nextStatus = when (order.status) {
        KdsStatus.NEW -> KdsStatus.COOKING
        KdsStatus.COOKING -> KdsStatus.READY
        KdsStatus.READY -> KdsStatus.DONE
        KdsStatus.DONE -> null
    }
    val semantic = id.rancak.app.presentation.designsystem.RancakColors.semantic
    val statusColor = when (order.status) {
        KdsStatus.NEW -> semantic.info
        KdsStatus.COOKING -> semantic.warning
        KdsStatus.READY -> semantic.success
        KdsStatus.DONE -> semantic.statusMaintenance
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    order.queueNumber?.let {
                        Text("#$it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        order.invoiceNo ?: "-",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                StatusChip(
                    text = order.status.value.replaceFirstChar { it.uppercase() },
                    color = statusColor
                )
            }

            if (order.tableName != null || order.orderType != OrderType.DINE_IN) {
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append(order.orderType.value.replaceFirstChar { it.uppercase() })
                        order.tableName?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text("${item.qty}x", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.productName, style = MaterialTheme.typography.bodySmall)
                        item.variantName?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        item.note?.let {
                            Text("📝 $it", style = MaterialTheme.typography.labelSmall, color = semantic.warning)
                        }
                    }
                }
            }

            if (nextStatus != null) {
                Spacer(Modifier.height(8.dp))
                RancakButton(
                    text = when (nextStatus) {
                        KdsStatus.COOKING -> "Mulai Masak"
                        KdsStatus.READY -> "Siap Antar"
                        KdsStatus.DONE -> "Selesai"
                        else -> ""
                    },
                    onClick = { onAdvance(nextStatus) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun KdsOrderCardPreview() {
    RancakTheme {
        KdsOrderCard(
            order = KdsOrder(
                uuid = "1",
                invoiceNo = "INV-2024-001",
                orderType = OrderType.DINE_IN,
                tableName = "A1",
                queueNumber = 12,
                status = KdsStatus.COOKING,
                items = listOf(
                    KdsItem("i1", "Nasi Goreng", "2", null, "Pedas", KdsItemStatus.PENDING),
                    KdsItem("i2", "Es Teh Manis", "2", null, null, KdsItemStatus.PENDING)
                ),
                createdAt = "2024-01-15T10:30:00"
            ),
            onAdvance = {}
        )
    }
}
