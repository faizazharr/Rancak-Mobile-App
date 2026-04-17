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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.OrderBoardViewModel
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Color helpers — status-based header colors
// ─────────────────────────────────────────────────────────────────────────────

private fun headerColorForStatus(status: SaleStatus): Color = when (status) {
    SaleStatus.HELD      -> Color(0xFFF57C00)  // Oranye — menunggu antar
    SaleStatus.PAID      -> Color(0xFF2E7D32)  // Hijau — sudah bayar
    else                 -> Color(0xFF757575)  // Abu-abu
}

@Composable
private fun rememberElapsed(createdAt: String?): Pair<String, Long> {
    val nowMillis = remember { Clock.System.now().toEpochMilliseconds() }
    if (createdAt.isNullOrBlank()) return "" to 0L
    return remember(createdAt) {
        try {
            val cleaned = createdAt
                .replace("T", " ")
                .take(19)
            val parts = cleaned.split(" ")
            val dateParts = parts[0].split("-").map { it.toInt() }
            val timeParts = parts[1].split(":").map { it.toInt() }

            val y = dateParts[0]; val m = dateParts[1]; val d = dateParts[2]
            val h = timeParts[0]; val min = timeParts[1]; val s = timeParts[2]

            val daysFromYear = (y - 1970) * 365L + ((y - 1969) / 4) - ((y - 1901) / 100) + ((y - 1601) / 400)
            val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            val isLeap = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
            if (isLeap) daysInMonth[2] = 29
            val daysFromMonth = (1 until m).sumOf { daysInMonth[it].toLong() }
            val totalDays = daysFromYear + daysFromMonth + (d - 1)
            val createdMs = (totalDays * 86400L + h * 3600L + min * 60L + s) * 1000L

            val diffSec = ((nowMillis - createdMs) / 1000L).coerceAtLeast(0)
            val mins = diffSec / 60
            val secs = diffSec % 60
            "${mins}:${secs.toString().padStart(2, '0')}" to mins
        } catch (_: Exception) { "" to 0L }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

private const val PAGE_SIZE = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBoardScreen(
    onBack: () -> Unit,
    viewModel: OrderBoardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var page by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadOrders() }

    // Reset page when switching tabs
    LaunchedEffect(uiState.showCompleted) { page = 0 }

    val orders = uiState.displayOrders
    val totalPages = ((orders.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
    val pagedOrders = orders.drop(page * PAGE_SIZE).take(PAGE_SIZE)

    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Order Board",
                icon = Icons.Default.Dashboard,
                subtitle = "Status pesanan aktif",
                onMenu = onBack,
                actions = {
                    IconButton(onClick = viewModel::loadOrders) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> LoadingScreen(Modifier.weight(1f))
                uiState.error != null -> ErrorScreen(
                    uiState.error!!,
                    onRetry = viewModel::loadOrders,
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
                                onServe = { viewModel.serveOrder(order.uuid) }
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
                            onClick = { viewModel.toggleTab(false) },
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
                            onClick = { viewModel.toggleTab(true) },
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
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Order Board Card — SimpleKDS style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OrderBoardCard(order: Sale, onServe: () -> Unit) {
    val (elapsed, _) = rememberElapsed(order.createdAt)
    val headerColor = headerColorForStatus(order.status)

    val orderTypeLabel = when (order.orderType) {
        OrderType.DINE_IN -> "Dine In"
        OrderType.TAKEAWAY -> "Take Away"
        OrderType.DELIVERY -> "Delivery"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (order.status == SaleStatus.HELD)
                    Modifier.clickable(onClick = onServe)
                else Modifier
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // ── Colored header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "#${order.queueNumber ?: "-"}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            orderTypeLabel,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (elapsed.isNotBlank()) {
                        Text(
                            elapsed,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Item list (big font) ──
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            item.qty,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.width(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.productName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            item.variantName?.let {
                                Text(
                                    "- $it",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                            item.note?.let {
                                Text(
                                    "- $it",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                // Total + action hint
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatRupiah(order.total),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (order.status == SaleStatus.HELD) {
                        Text(
                            "Ketuk untuk antar ✓",
                            fontSize = 12.sp,
                            color = headerColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
