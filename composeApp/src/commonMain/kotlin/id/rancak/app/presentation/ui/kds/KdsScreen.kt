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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.*
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.viewmodel.KdsViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Color helpers — status-based header colors
// ─────────────────────────────────────────────────────────────────────────────

private fun headerColorForStatus(status: KdsStatus): Color = when (status) {
    KdsStatus.NEW     -> Color(0xFF1565C0)   // Biru — baru masuk
    KdsStatus.COOKING -> Color(0xFFE65100)   // Oranye — sedang dimasak
    KdsStatus.READY   -> Color(0xFF2E7D32)   // Hijau — siap antar
    KdsStatus.DONE    -> Color(0xFF757575)   // Abu-abu — selesai
}

private fun orderTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val t = createdAt.replace("T", " ")
        if (t.length >= 16) t.substring(11, 16) else ""
    } catch (_: Exception) { "" }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

private const val PAGE_SIZE = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KdsScreen(
    onBack: () -> Unit,
    viewModel: KdsViewModel = koinViewModel()
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
                                onAdvance = { next ->
                                    viewModel.updateOrderStatus(order.uuid, next)
                                }
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
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KDS Order Card — SimpleKDS style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KdsOrderCard(order: KdsOrder, onAdvance: (KdsStatus) -> Unit) {
    val nextStatus = when (order.status) {
        KdsStatus.NEW -> KdsStatus.COOKING
        KdsStatus.COOKING -> KdsStatus.READY
        KdsStatus.READY -> KdsStatus.DONE
        KdsStatus.DONE -> null
    }

    val headerColor = headerColorForStatus(order.status)
    val time = orderTime(order.createdAt)

    val orderTypeLabel = when (order.orderType) {
        OrderType.DINE_IN -> "Dine In"
        OrderType.TAKEAWAY -> "Take Away"
        OrderType.DELIVERY -> "Delivery"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (nextStatus != null)
                    Modifier.clickable { onAdvance(nextStatus) }
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (!order.invoiceNo.isNullOrBlank()) {
                            Text(
                                order.invoiceNo,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            "$orderTypeLabel${order.tableName?.let { " · $it" } ?: ""}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (time.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                time,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                when (order.status) {
                                    KdsStatus.NEW     -> "BARU"
                                    KdsStatus.COOKING -> "MASAK"
                                    KdsStatus.READY   -> "SIAP"
                                    KdsStatus.DONE    -> "SELESAI"
                                },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

                // Action hint at bottom
                if (nextStatus != null) {
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        when (nextStatus) {
                            KdsStatus.COOKING -> "Ketuk untuk mulai masak ▶"
                            KdsStatus.READY -> "Ketuk jika siap antar ✓"
                            KdsStatus.DONE -> "Ketuk untuk selesaikan ✓✓"
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = headerColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                }
            }
        }
    }
}
