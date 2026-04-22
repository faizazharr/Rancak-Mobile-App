package id.rancak.app.presentation.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethodReport
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.reports.components.DailyCategoryCard
import id.rancak.app.presentation.ui.reports.components.EmptySummaryPlaceholder
import id.rancak.app.presentation.ui.reports.components.FinancialBreakdownCard
import id.rancak.app.presentation.ui.reports.components.KpiCardsGrid
import id.rancak.app.presentation.ui.reports.components.MySalesTodayCard
import id.rancak.app.presentation.ui.reports.components.PaymentDonutCard
import id.rancak.app.presentation.ui.reports.components.PeriodSelectorRow
import id.rancak.app.presentation.ui.reports.components.ShiftInfoCard
import id.rancak.app.presentation.viewmodel.ReportUiState
import id.rancak.app.presentation.viewmodel.ReportViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point — Scaffold + period selector + konten laporan.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBack: () -> Unit,
    viewModel: ReportViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.THIS_MONTH) }

    LaunchedEffect(Unit) {
        if (uiState.dateFrom.isBlank()) {
            selectedPeriod.toDateRange()?.let { (from, to) ->
                viewModel.setDateRange(from, to)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            RancakTopBar(
                title    = "Laporan",
                icon     = Icons.Default.BarChart,
                subtitle = if (uiState.dateFrom.isNotBlank())
                    "${uiState.dateFrom}  –  ${uiState.dateTo}"
                else "Statistik penjualan",
                onMenu = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            PeriodSelectorRow(
                selected = selectedPeriod,
                onSelect = { period ->
                    selectedPeriod = period
                    if (period != ReportPeriod.CUSTOM) {
                        period.toDateRange()?.let { (from, to) ->
                            viewModel.setDateRange(from, to)
                        }
                    }
                }
            )
            HorizontalDivider()

            val error = uiState.error
            when {
                uiState.isLoading -> LoadingScreen(Modifier.weight(1f))
                error != null     -> ErrorScreen(error, onRetry = viewModel::loadReport, modifier = Modifier.weight(1f))
                else              -> ReportScreenContent(uiState = uiState, modifier = Modifier.weight(1f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure UI body — responsive layout tablet/phone untuk kartu-kartu laporan.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReportScreenContent(
    uiState: ReportUiState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= 600.dp) {
            TabletLayout(uiState)
        } else {
            PhoneLayout(uiState)
        }
    }
}

@Composable
private fun TabletLayout(uiState: ReportUiState) {
    Row(Modifier.fillMaxSize()) {
        // Kiri — KPI + rincian + donut
        LazyColumn(
            modifier = Modifier
                .weight(0.52f)
                .fillMaxHeight(),
            contentPadding      = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val summary = uiState.summary
            if (summary != null) {
                item { KpiCardsGrid(summary) }
                item { FinancialBreakdownCard(summary) }
                if (summary.paymentSummary.isNotEmpty()) {
                    item { PaymentDonutCard(summary.paymentSummary) }
                }
            } else {
                item { EmptySummaryPlaceholder() }
            }
        }

        VerticalDivider()

        // Kanan — info shift + penjualan saya + per kategori
        LazyColumn(
            modifier = Modifier
                .weight(0.48f)
                .fillMaxHeight(),
            contentPadding      = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val summary = uiState.summary
            if (summary != null) {
                item {
                    Text(
                        "Info Shift",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(bottom = 2.dp)
                    )
                }
                item { ShiftInfoCard(summary) }
                uiState.mySalesToday?.let { mySales ->
                    item { MySalesTodayCard(mySales) }
                }
                if (uiState.dailyByCategory.isNotEmpty()) {
                    item { DailyCategoryCard(uiState.dailyByCategory) }
                }
            } else {
                item {
                    Box(
                        Modifier.fillParentMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) { EmptySummaryPlaceholder() }
                }
            }
        }
    }
}

@Composable
private fun PhoneLayout(uiState: ReportUiState) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val summary = uiState.summary
        if (summary != null) {
            item { KpiCardsGrid(summary) }
            item { FinancialBreakdownCard(summary) }
            if (summary.paymentSummary.isNotEmpty()) {
                item { PaymentDonutCard(summary.paymentSummary) }
            }
            item {
                Text(
                    "Info Shift",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }
            item { ShiftInfoCard(summary) }
            uiState.mySalesToday?.let { mySales ->
                item { MySalesTodayCard(mySales) }
            }
            if (uiState.dailyByCategory.isNotEmpty()) {
                item { DailyCategoryCard(uiState.dailyByCategory) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview — hanya body pure-UI tanpa Koin.
// ─────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ReportScreenPreview() {
    RancakTheme {
        val mockState = ReportUiState(
            summary = ShiftSummary(
                uuid              = "shift-preview",
                openedAt          = "2026-04-18 08:00:00",
                closedAt          = null,
                status            = "open",
                openingCash       = "500000",
                closingCash       = null,
                expectedCash      = null,
                cashDifference    = null,
                cashierName       = "Admin Demo",
                totalSales        = 8_750_000L,
                totalTransactions = 64,
                totalExpenses     = 325_000L,
                totalCashIn       = 500_000L,
                paymentSummary    = listOf(
                    PaymentMethodReport("cash", 4_500_000, 32),
                    PaymentMethodReport("qris", 2_800_000, 22),
                    PaymentMethodReport("card", 1_450_000, 10)
                )
            ),
            dateFrom = "2026-04-01",
            dateTo   = "2026-04-15"
        )
        ReportScreenContent(uiState = mockState, modifier = Modifier.fillMaxSize())
    }
}
