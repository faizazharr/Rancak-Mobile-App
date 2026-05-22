package id.rancak.app.presentation.ui.orderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.OrderBoardOrder
import id.rancak.app.domain.model.OrderBoardItem
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.OrderBoardUiState
import id.rancak.app.presentation.viewmodel.OrderBoardViewModel
import id.rancak.app.presentation.ui.orderboard.components.OrderBoardCard
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

private const val PAGE_SIZE = 6
private const val PAGE_SIZE_TABLET = 12

@Composable
fun OrderBoardScreen(
    onBack: () -> Unit
) {
    val viewModel: OrderBoardViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadOrders() }

    OrderBoardScreenContent(
        uiState     = uiState,
        onBack      = onBack,
        onReload    = viewModel::loadOrders,
        onToggleTab = viewModel::toggleTab,
        onServe     = viewModel::serveOrder
    )
}

/** Pure-UI content — tanpa ViewModel, aman di-preview. */
@Composable
fun OrderBoardScreenContent(
    uiState: OrderBoardUiState,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onToggleTab: (Boolean) -> Unit,
    onServe: (String) -> Unit
) {
    var page by remember { mutableStateOf(0) }

    // Reset page when switching tabs
    LaunchedEffect(uiState.showCompleted) { page = 0 }

    val orders = uiState.displayOrders

    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Order Board",
                icon = Icons.Default.Dashboard,
                subtitle = "Status pesanan aktif",
                onMenu = onBack,
                actions = {
                    IconButton(onClick = onReload) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isTablet = maxWidth >= 600.dp
            val pageSize = if (isTablet) PAGE_SIZE_TABLET else PAGE_SIZE
            val totalPages  = ((orders.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            val pagedOrders = orders.drop(page * pageSize).take(pageSize)
        Column(Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> LoadingScreen(Modifier.weight(1f))
                uiState.error != null -> ErrorScreen(
                    uiState.error,
                    onRetry = onReload,
                    modifier = Modifier.weight(1f)
                )
                orders.isEmpty() -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (uiState.showCompleted) "Belum ada order selesai"
                        else "Tidak ada order aktif",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 260.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pagedOrders, key = { it.uuid }) { order ->
                            OrderBoardCard(
                                order = order,
                                onServe = { onServe(order.uuid) }
                            )
                        }
                    }
                }
            }

            // ── Bottom bar: tab + pagination ──
            Surface(
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !uiState.showCompleted,
                            onClick = { onToggleTab(false) },
                            label = {
                                Text(
                                    "${uiState.activeOrders.size} Aktif",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            selected = uiState.showCompleted,
                            onClick = { onToggleTab(true) },
                            label = { Text("Selesai") },
                            leadingIcon = if (uiState.showCompleted) {
                                { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${page + 1} / $totalPages",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = { if (page > 0) page-- },
                            enabled = page > 0
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                "Sebelumnya",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (page > 0) 1f else 0.3f)
                            )
                        }
                        IconButton(
                            onClick = { if (page < totalPages - 1) page++ },
                            enabled = page < totalPages - 1
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                "Selanjutnya",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (page < totalPages - 1) 1f else 0.3f)
                            )
                        }
                    }
                }
            }
        } // end Column
        } // end BoxWithConstraints
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Order Board – Empty", widthDp = 1024, heightDp = 768)
@Composable
private fun OrderBoardScreenEmptyPreview() {
    RancakTheme {
        OrderBoardScreenContent(
            uiState     = OrderBoardUiState(),
            onBack      = {},
            onReload    = {},
            onToggleTab = {},
            onServe     = {}
        )
    }
}

@Preview(name = "Order Board – With Orders", widthDp = 1024, heightDp = 768)
@Composable
private fun OrderBoardScreenWithOrdersPreview() {
    val sample = persistentListOf(
        OrderBoardOrder(
            uuid = "1", invoiceNo = "INV-001", queueNumber = 1,
            orderType = OrderType.DINE_IN, customerName = "Andi",
            status = SaleStatus.HELD, createdAt = "2026-01-01T10:15:00",
            servedAt = null,
            items = persistentListOf(OrderBoardItem("Nasi Goreng", 2, null))
        ),
        OrderBoardOrder(
            uuid = "2", invoiceNo = "INV-002", queueNumber = 2,
            orderType = OrderType.TAKEAWAY, customerName = "Budi",
            status = SaleStatus.PAID, createdAt = "2026-01-01T10:20:00",
            servedAt = null,
            items = persistentListOf(OrderBoardItem("Mie Ayam", 1, "Tidak pedas"))
        )
    )
    RancakTheme {
        OrderBoardScreenContent(
            uiState     = OrderBoardUiState(activeOrders = sample),
            onBack      = {},
            onReload    = {},
            onToggleTab = {},
            onServe     = {}
        )
    }
}
