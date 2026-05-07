package id.rancak.app.presentation.ui.kds

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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.domain.model.KdsItem
import id.rancak.app.domain.model.KdsItemStatus
import id.rancak.app.domain.model.KdsOrder
import id.rancak.app.domain.model.KdsStatus
import id.rancak.app.domain.model.OrderType
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.KdsUiState
import id.rancak.app.presentation.ui.kds.components.KdsOrderCard
import id.rancak.app.presentation.viewmodel.KdsViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

private const val PAGE_SIZE = 6
private const val PAGE_SIZE_TABLET = 12

@Composable
fun KdsScreen(
    onBack: () -> Unit
) {
    val viewModel: KdsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadOrders() }

    KdsScreenContent(
        uiState       = uiState,
        onBack        = onBack,
        onReload      = viewModel::loadOrders,
        onToggleTab   = viewModel::toggleTab,
        onAdvance     = { uuid, next -> viewModel.updateOrderStatus(uuid, next) }
    )
}

/** Pure-UI content — tidak mengakses ViewModel, aman di-preview. */
@Composable
fun KdsScreenContent(
    uiState: KdsUiState,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onToggleTab: (Boolean) -> Unit,
    onAdvance: (String, KdsStatus) -> Unit
) {
    var page by remember { mutableStateOf(0) }

    // Reset page when switching tabs
    LaunchedEffect(uiState.showCompleted) { page = 0 }

    val orders = uiState.displayOrders

    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Kitchen Display",
                icon = Icons.Default.Restaurant,
                subtitle = "Antrian pesanan dapur",
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
            // Memoize: hanya hitung ulang bila orders atau pageSize berubah.
            // Tanpa ini, drop()+take() mengalokasikan list baru setiap rekomposisi.
            val totalPages = remember(orders.size, pageSize) {
                ((orders.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            }
            val pagedOrders = remember(orders, page, pageSize) {
                orders.drop(page * pageSize).take(pageSize)
            }
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
                        else "Tidak ada order di dapur",
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
                            KdsOrderCard(
                                order = order,
                                onAdvance = { next -> onAdvance(order.uuid, next) }
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
                    // Active / Completed tabs
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

                    // Pagination
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
// Previews — memanggil KdsScreenContent langsung, tidak menduplikasi UI
// ─────────────────────────────────────────────────────────────────────────────

// Previews — memanggil KdsScreenContent langsung, tidak menduplikasi UI
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "KDS – Empty",  widthDp = 1024, heightDp = 768)
@Composable
private fun KdsScreenEmptyPreview() {
    RancakTheme {
        KdsScreenContent(
            uiState     = KdsUiState(),
            onBack      = {},
            onReload    = {},
            onToggleTab = {},
            onAdvance   = { _, _ -> }
        )
    }
}

@Preview(name = "KDS – With Orders", widthDp = 1024, heightDp = 768)
@Composable
private fun KdsScreenWithOrdersPreview() {
    val sample = listOf(
        KdsOrder(
            uuid = "1", invoiceNo = "ORD-001", orderType = OrderType.DINE_IN,
            tableName = "Meja 3", queueNumber = 1, customerName = "Andi",
            note = null, status = KdsStatus.NEW,
            items = listOf(
                KdsItem("i1", "Nasi Goreng", "2", null, null, KdsItemStatus.PENDING)
            ),
            createdAt = "2026-01-01T10:15:00"
        ),
        KdsOrder(
            uuid = "2", invoiceNo = "ORD-002", orderType = OrderType.TAKEAWAY,
            tableName = null, queueNumber = 2, customerName = "Budi",
            note = "Tidak pedas", status = KdsStatus.COOKING,
            items = listOf(
                KdsItem("i2", "Mie Ayam", "1", null, null, KdsItemStatus.COOKING)
            ),
            createdAt = "2026-01-01T10:20:00"
        ),
        KdsOrder(
            uuid = "3", invoiceNo = "ORD-003", orderType = OrderType.DINE_IN,
            tableName = "Meja 5", queueNumber = 3, customerName = null,
            note = null, status = KdsStatus.READY,
            items = listOf(
                KdsItem("i3", "Es Teh", "4", null, null, KdsItemStatus.READY)
            ),
            createdAt = "2026-01-01T10:25:00"
        )
    )
    RancakTheme {
        KdsScreenContent(
            uiState     = KdsUiState(activeOrders = sample),
            onBack      = {},
            onReload    = {},
            onToggleTab = {},
            onAdvance   = { _, _ -> }
        )
    }
}
