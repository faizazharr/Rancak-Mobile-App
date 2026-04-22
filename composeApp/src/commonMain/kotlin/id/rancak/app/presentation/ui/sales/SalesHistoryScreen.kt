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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.sales.components.SaleCard
import id.rancak.app.presentation.ui.sales.components.SaleDetailPanel
import id.rancak.app.presentation.ui.sales.components.SalesSummaryPanel
import id.rancak.app.presentation.ui.sales.components.SearchAndFilterBar
import id.rancak.app.presentation.viewmodel.DateFilter
import id.rancak.app.presentation.viewmodel.SalesHistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

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
    viewModel: SalesHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSales() }

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
                onRetry  = viewModel::loadSales,
                modifier = Modifier.padding(padding)
            )
            uiState.allSales.isEmpty() -> EmptyScreen(
                "Belum ada transaksi",
                Modifier.padding(padding)
            )
            else -> BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                val isTablet = maxWidth >= 600.dp
                if (isTablet) TabletLayout(uiState, viewModel)
                else          PhoneLayout(uiState, viewModel)
            }
        }
    }
}

// ── Layouts ─────────────────────────────────────────────────────────────────

@Composable
private fun TabletLayout(
    uiState: id.rancak.app.presentation.viewmodel.SalesHistoryUiState,
    viewModel: SalesHistoryViewModel
) {
    Row(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(0.38f).fillMaxHeight()) {
            SalesFilterBar(uiState, viewModel)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            SalesList(
                uiState       = uiState,
                viewModel     = viewModel,
                reflectSelect = true,
                modifier      = Modifier.weight(1f).fillMaxHeight()
            )
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Box(modifier = Modifier.weight(0.62f).fillMaxHeight()) {
            val selected = uiState.selectedSale
            if (selected != null) {
                SaleDetailPanel(sale = selected, modifier = Modifier.fillMaxSize())
            } else {
                SalesSummaryPanel(sales = uiState.sales, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PhoneLayout(
    uiState: id.rancak.app.presentation.viewmodel.SalesHistoryUiState,
    viewModel: SalesHistoryViewModel
) {
    Column(Modifier.fillMaxSize()) {
        SalesFilterBar(uiState, viewModel)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        SalesList(
            uiState       = uiState,
            viewModel     = viewModel,
            reflectSelect = false,
            modifier      = Modifier.weight(1f).fillMaxSize()
        )
    }

    uiState.selectedSale?.let { sale ->
        AlertDialog(
            onDismissRequest = { viewModel.selectSale(null) },
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
                    IconButton(onClick = { viewModel.selectSale(null) }) {
                        Icon(Icons.Default.Close, "Tutup")
                    }
                }
            },
            text = { SaleDetailPanel(sale = sale, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = { viewModel.selectSale(null) }) { Text("Tutup") }
            }
        )
    }
}

// ── Shared pieces ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesFilterBar(
    uiState: id.rancak.app.presentation.viewmodel.SalesHistoryUiState,
    viewModel: SalesHistoryViewModel
) {
    SearchAndFilterBar(
        query             = uiState.searchQuery,
        dateFilter        = uiState.dateFilter,
        statusFilter      = uiState.statusFilter,
        customDateFrom    = uiState.customDateFrom,
        customDateTo      = uiState.customDateTo,
        onQueryChange     = viewModel::setSearchQuery,
        onDateFilter      = viewModel::setDateFilter,
        onStatusFilter    = viewModel::setStatusFilter,
        onCustomDateRange = viewModel::setCustomDateRange,
        onClear           = viewModel::clearFilters,
        hasActiveFilter   = uiState.searchQuery.isNotBlank() ||
                            uiState.dateFilter != DateFilter.ALL ||
                            uiState.statusFilter != null
    )
}

@Composable
private fun SalesList(
    uiState: id.rancak.app.presentation.viewmodel.SalesHistoryUiState,
    viewModel: SalesHistoryViewModel,
    reflectSelect: Boolean,
    modifier: Modifier = Modifier
) {
    if (uiState.sales.isEmpty()) {
        NoResultsBox(
            onClear  = viewModel::clearFilters,
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
                    onClick    = { viewModel.selectSale(sale) }
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
