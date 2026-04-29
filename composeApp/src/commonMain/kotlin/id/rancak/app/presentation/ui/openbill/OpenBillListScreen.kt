package id.rancak.app.presentation.ui.openbill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.data.local.LocalOpenBill
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.OpenBillViewModel
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar daftar open bill lokal.
 * Kasir bisa melanjutkan (lanjutkan ke kasir) atau menghapus tagihan yang tersimpan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenBillListScreen(
    onBack: () -> Unit,
    onResume: (LocalOpenBill) -> Unit,
    viewModel: OpenBillViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val amber  = Color(0xFFF59E0B)
    val amber2 = Color(0xFFFBBF24)

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, null, tint = amber, modifier = Modifier.size(22.dp))
                        Text(
                            "Open Bill",
                            fontWeight = FontWeight.ExtraBold,
                            style      = MaterialTheme.typography.titleLarge
                        )
                        if (state.bills.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = amber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "${state.bills.size}",
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color      = amber,
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.bills.isEmpty()) {
            EmptyOpenBillState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Summary banner ────────────────────────────────────────
                item {
                    SummaryBanner(
                        bills = state.bills,
                        amber = amber,
                        amber2 = amber2
                    )
                    Spacer(Modifier.height(16.dp))
                }

                items(state.bills, key = { it.id }) { bill ->
                    OpenBillCard(
                        bill     = bill,
                        amber    = amber,
                        onResume = {
                            viewModel.remove(bill.id)
                            onResume(bill)
                        },
                        onDelete = { viewModel.remove(bill.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun SummaryBanner(
    bills: List<LocalOpenBill>,
    amber: Color,
    amber2: Color
) {
    val totalValue = bills.sumOf { it.subtotal }
    val totalItems = bills.sumOf { it.itemCount }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(listOf(amber, amber2))
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${bills.size} tagihan aktif",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    formatRupiah(totalValue),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White
                )
                Text(
                    "$totalItems item total",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun OpenBillCard(
    bill: LocalOpenBill,
    amber: Color,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val surface          = MaterialTheme.colorScheme.surface
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon    = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title   = { Text("Hapus Open Bill?", fontWeight = FontWeight.Bold) },
            text    = { Text("\"${bill.name}\" akan dihapus permanen dari daftar.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape   = RoundedCornerShape(8.dp)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Surface(
        shape           = RoundedCornerShape(14.dp),
        color           = surface,
        shadowElevation = 3.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // ── Amber left accent bar ─────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(amber, amber.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp)) {

                // ── Header: name + time ───────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier             = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(amber.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BookmarkBorder, null,
                                tint     = amber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text       = bill.name,
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = onSurface,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            if (bill.customerName.isNotBlank()) {
                                Text(
                                    bill.customerName,
                                    style  = MaterialTheme.typography.labelSmall,
                                    color  = onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    // Time chip
                    val timeStr = formatCreatedAt(bill.createdAt)
                    if (timeStr.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                timeStr,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style      = MaterialTheme.typography.labelSmall,
                                color      = onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Item rows ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        bill.items.take(3).forEach { item ->
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier              = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = amber.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "${item.qty}×",
                                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            style      = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color      = amber
                                        )
                                    }
                                    Text(
                                        "${item.productName}${item.variantName?.let { " ($it)" } ?: ""}",
                                        style    = MaterialTheme.typography.bodySmall,
                                        color    = onSurface.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    formatRupiah(item.price * item.qty),
                                    style      = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        if (bill.items.size > 3) {
                            Text(
                                "+ ${bill.items.size - 3} item lainnya",
                                style  = MaterialTheme.typography.labelSmall,
                                color  = onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Footer: subtotal + actions ────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            "${bill.itemCount} item · ${bill.items.size} produk",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant.copy(alpha = 0.55f)
                        )
                        Text(
                            formatRupiah(bill.subtotal),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = amber
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Hapus
                        OutlinedButton(
                            onClick       = { showDeleteDialog = true },
                            shape         = RoundedCornerShape(8.dp),
                            colors        = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border        = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                width = 1.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                        }
                        // Lanjutkan
                        Button(
                            onClick        = onResume,
                            shape          = RoundedCornerShape(8.dp),
                            colors         = ButtonDefaults.buttonColors(
                                containerColor = amber,
                                contentColor   = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Lanjutkan",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyOpenBillState(modifier: Modifier = Modifier) {
    val amber = Color(0xFFF59E0B)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(amber.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier           = Modifier.size(40.dp),
                    tint               = amber.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Belum ada open bill",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                "Tekan tombol \"Open Bill\" di layar kasir\nuntuk menyimpan tagihan yang belum selesai.",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatCreatedAt(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val local   = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val today   = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val h = local.hour.toString().padStart(2, '0')
        val m = local.minute.toString().padStart(2, '0')
        if (local.date == today) "$h:$m"
        else "${local.dayOfMonth}/${local.monthNumber} $h:$m"
    } catch (_: Exception) { "" }
}
