package id.rancak.app.presentation.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.sales.components.SaleCard
import id.rancak.app.presentation.ui.sales.components.SaleDetailPanel
import id.rancak.app.presentation.ui.sales.components.SalesSummaryPanel
import id.rancak.app.presentation.ui.sales.components.SearchAndFilterBar
import id.rancak.app.presentation.viewmodel.DateFilter
import id.rancak.app.presentation.viewmodel.SalesHistoryUiState
import id.rancak.app.presentation.viewmodel.SalesHistoryViewModel
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.OrderType
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Bundel callback dari [SalesHistoryViewModel] — dipakai oleh layout phone/
 * tablet sehingga keduanya bisa dirender tanpa Koin untuk keperluan preview.
 */
data class SalesHistoryActions(
    val onSearch: (String) -> Unit = {},
    val onDateFilter: (DateFilter) -> Unit = {},
    val onStatusFilter: (SaleStatus?) -> Unit = {},
    val onCustomRange: (Long, Long) -> Unit = { _, _ -> },
    val onClearFilters: () -> Unit = {},
    val onSelect: (Sale?) -> Unit = {},
    val onPayHeldOrder: (String) -> Unit = {},
    val onSplitBill: (String) -> Unit = {},
    val onAddItems: (String) -> Unit = {}
)

/**
 * Entry point for the **Riwayat Penjualan** flow.
 *
 * The screen orchestrates a list/detail split for tablet and a single-column
 * list + dialog for phone. All heavy UI pieces (top filter bar, sale cards,
 * receipt detail, and summary panel) live in
 * [id.rancak.app.presentation.ui.sales.components].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    onBack: () -> Unit,
    onPayHeldOrder: (String) -> Unit = {},
    onSplitBill: (String) -> Unit = {},
    onAddItems: (String) -> Unit = {},
    viewModel: SalesHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadSales() }

    SalesHistoryScreenContent(
        uiState   = uiState,
        onBack    = onBack,
        onRetry   = viewModel::loadSales,
        actions   = SalesHistoryActions(
            onSearch        = viewModel::setSearchQuery,
            onDateFilter    = viewModel::setDateFilter,
            onStatusFilter  = viewModel::setStatusFilter,
            onCustomRange   = viewModel::setCustomDateRange,
            onClearFilters  = viewModel::clearFilters,
            onSelect        = viewModel::selectSale,
            onPayHeldOrder  = onPayHeldOrder,
            onSplitBill     = onSplitBill,
            onAddItems      = onAddItems
        )
    )
}

/** Pure-UI content — tanpa ViewModel, aman di-preview. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreenContent(
    uiState: SalesHistoryUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    actions: SalesHistoryActions
) {
    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Riwayat Penjualan",
                icon     = Icons.Default.Receipt,
                subtitle = "Catatan seluruh transaksi",
                onMenu   = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(
                uiState.error!!,
                onRetry  = onRetry,
                modifier = Modifier.padding(padding)
            )
            uiState.allSales.isEmpty() -> EmptyScreen(
                "Belum ada transaksi",
                Modifier.padding(padding)
            )
            else -> BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                val isTablet = maxWidth >= 600.dp
                if (isTablet) TabletLayout(uiState, actions)
                else          PhoneLayout(uiState, actions)
            }
        }
    }
}

// ── Layouts ─────────────────────────────────────────────────────────────────

@Composable
private fun TabletLayout(
    uiState: SalesHistoryUiState,
    actions: SalesHistoryActions
) {
    Row(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(0.38f).fillMaxHeight()) {
            SalesFilterBar(uiState, actions)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            SalesList(
                uiState       = uiState,
                actions       = actions,
                reflectSelect = true,
                modifier      = Modifier.weight(1f).fillMaxHeight()
            )
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Box(modifier = Modifier.weight(0.62f).fillMaxHeight()) {
            val selected = uiState.selectedSale
            if (selected != null) {
                SaleDetailPanel(
                    sale           = selected,
                    onPayHeldOrder = actions.onPayHeldOrder,
                    onSplitBill    = actions.onSplitBill,
                    onAddItems     = actions.onAddItems,
                    modifier       = Modifier.fillMaxSize()
                )
            } else {
                SalesSummaryPanel(sales = uiState.sales, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PhoneLayout(
    uiState: SalesHistoryUiState,
    actions: SalesHistoryActions
) {
    Column(Modifier.fillMaxSize()) {
        SalesFilterBar(uiState, actions)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        SalesList(
            uiState       = uiState,
            actions       = actions,
            reflectSelect = false,
            modifier      = Modifier.weight(1f).fillMaxSize()
        )
    }

    uiState.selectedSale?.let { sale ->
        AlertDialog(
            onDismissRequest = { actions.onSelect(null) },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Detail Transaksi",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { actions.onSelect(null) }) {
                        Icon(Icons.Default.Close, "Tutup")
                    }
                }
            },
            text = {
                SaleDetailPanel(
                    sale           = sale,
                    onPayHeldOrder = actions.onPayHeldOrder,
                    onSplitBill    = actions.onSplitBill,
                    onAddItems     = actions.onAddItems,
                    modifier       = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { actions.onSelect(null) }) { Text("Tutup") }
            }
        )
    }
}

// ── Shared pieces ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesFilterBar(
    uiState: SalesHistoryUiState,
    actions: SalesHistoryActions
) {
    SearchAndFilterBar(
        query             = uiState.searchQuery,
        dateFilter        = uiState.dateFilter,
        statusFilter      = uiState.statusFilter,
        customDateFrom    = uiState.customDateFrom,
        customDateTo      = uiState.customDateTo,
        onQueryChange     = actions.onSearch,
        onDateFilter      = actions.onDateFilter,
        onStatusFilter    = actions.onStatusFilter,
        onCustomDateRange = actions.onCustomRange,
        onClear           = actions.onClearFilters,
        hasActiveFilter   = uiState.searchQuery.isNotBlank() ||
                            uiState.dateFilter != DateFilter.ALL ||
                            uiState.statusFilter != null
    )
}

@Composable
private fun SalesList(
    uiState: SalesHistoryUiState,
    actions: SalesHistoryActions,
    reflectSelect: Boolean,
    modifier: Modifier = Modifier
) {
    if (uiState.sales.isEmpty()) {
        NoResultsBox(
            onClear  = actions.onClearFilters,
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier            = modifier,
            contentPadding      = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(uiState.sales, key = { it.uuid }) { sale ->
                SaleCard(
                    sale       = sale,
                    isSelected = reflectSelect && sale.uuid == uiState.selectedSale?.uuid,
                    onClick    = { actions.onSelect(sale) }
                )
            }
        }
    }
}

@Composable
private fun NoResultsBox(
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.SearchOff, contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint     = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                "Tidak ada hasil",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Coba ubah kata kunci atau filter",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onClear) { Text("Reset Filter") }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews — memanggil SalesHistoryScreenContent langsung
// ─────────────────────────────────────────────────────────────────────────────

private val previewSales = listOf(
    Sale(
        uuid = "s1", invoiceNo = "INV-001", orderType = OrderType.DINE_IN,
        queueNumber = 1, status = SaleStatus.PAID, customerName = "Andi",
        subtotal = 50_000, discount = 0, surcharge = 0,
        tax = 5_000, total = 55_000, paymentMethod = PaymentMethod.CASH,
        paidAmount = 60_000, changeAmount = 5_000,
        items = emptyList(), createdAt = "2026-01-01T10:00:00"
    ),
    Sale(
        uuid = "s2", invoiceNo = "INV-002", orderType = OrderType.TAKEAWAY,
        queueNumber = 2, status = SaleStatus.PAID, customerName = "Budi",
        subtotal = 30_000, discount = 0, surcharge = 0,
        tax = 3_000, total = 33_000, paymentMethod = PaymentMethod.QRIS,
        paidAmount = 33_000, changeAmount = 0,
        items = emptyList(), createdAt = "2026-01-01T11:00:00"
    )
)

@Preview(name = "Sales – Empty", widthDp = 390, heightDp = 844)
@Composable
private fun SalesHistoryEmptyPreview() {
    RancakTheme {
        SalesHistoryScreenContent(
            uiState = SalesHistoryUiState(),
            onBack  = {},
            onRetry = {},
            actions = SalesHistoryActions()
        )
    }
}

@Preview(name = "Sales – Phone", widthDp = 390, heightDp = 844)
@Composable
private fun SalesHistoryPhonePreview() {
    RancakTheme {
        SalesHistoryScreenContent(
            uiState = SalesHistoryUiState(allSales = previewSales, sales = previewSales),
            onBack  = {},
            onRetry = {},
            actions = SalesHistoryActions()
        )
    }
}

@Preview(name = "Sales – Tablet", widthDp = 1024, heightDp = 768)
@Composable
private fun SalesHistoryTabletPreview() {
    RancakTheme {
        SalesHistoryScreenContent(
            uiState = SalesHistoryUiState(allSales = previewSales, sales = previewSales),
            onBack  = {},
            onRetry = {},
            actions = SalesHistoryActions()
        )
    }
}
