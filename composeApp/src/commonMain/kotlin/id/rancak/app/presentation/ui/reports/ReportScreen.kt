package id.rancak.app.presentation.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.DailyCategoryReport
import id.rancak.app.domain.model.MySalesReport
import id.rancak.app.domain.model.PaymentMethodReport
import id.rancak.app.domain.model.ShiftSummary
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.ReportUiState
import id.rancak.app.presentation.viewmodel.ReportViewModel
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

// ── Period selector ──────────────────────────────────────────────────────────

private enum class ReportPeriod(val label: String) {
    TODAY("Hari Ini"),
    WEEK("7 Hari"),
    THIS_MONTH("Bulan Ini"),
    LAST_MONTH("Bulan Lalu"),
    CUSTOM("Pilih")
}

private fun ReportPeriod.toDateRange(): Pair<String, String>? {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (this) {
        ReportPeriod.TODAY ->
            today.toString() to today.toString()

        ReportPeriod.WEEK ->
            today.minus(DatePeriod(days = 6)).toString() to today.toString()

        ReportPeriod.THIS_MONTH -> {
            val from = LocalDate(today.year, today.monthNumber, 1)
            from.toString() to today.toString()
        }

        ReportPeriod.LAST_MONTH -> {
            val firstThisMonth = LocalDate(today.year, today.monthNumber, 1)
            val firstLastMonth = firstThisMonth.minus(DatePeriod(months = 1))
            val lastLastMonth  = firstThisMonth.minus(DatePeriod(days = 1))
            firstLastMonth.toString() to lastLastMonth.toString()
        }

        ReportPeriod.CUSTOM -> null
    }
}

// ── Chart palette ────────────────────────────────────────────────────────────

private val chartColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFE91E63),
    Color(0xFF795548)
)

// ── Main screen ──────────────────────────────────────────────────────────────

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
            val range = selectedPeriod.toDateRange()
            if (range != null) viewModel.setDateRange(range.first, range.second)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            RancakTopBar(
                title = "Laporan",
                icon = Icons.Default.BarChart,
                subtitle = if (uiState.dateFrom.isNotBlank())
                    "${uiState.dateFrom}  –  ${uiState.dateTo}"
                else
                    "Statistik penjualan",
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
                        val range = period.toDateRange()
                        if (range != null) viewModel.setDateRange(range.first, range.second)
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

// ── Period chips ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelectorRow(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ReportPeriod.entries.forEach { period ->
            FilterChip(
                selected  = selected == period,
                onClick   = { onSelect(period) },
                label     = {
                    Text(
                        period.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                modifier  = Modifier.weight(1f),
                colors    = FilterChipDefaults.filterChipColors(
                    selectedContainerColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    selectedLabelColor       = MaterialTheme.colorScheme.primary
                ),
                border    = FilterChipDefaults.filterChipBorder(
                    enabled             = true,
                    selected            = selected == period,
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    selectedBorderWidth = 1.2.dp,
                    borderColor         = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    borderWidth         = 0.8.dp
                )
            )
        }
    }
}

// ── Layout ───────────────────────────────────────────────────────────────────

@Composable
private fun ReportScreenContent(
    uiState: ReportUiState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            Row(Modifier.fillMaxSize()) {

                // Left — KPIs + financial breakdown + donut chart
                LazyColumn(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(12.dp),
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

                // Right — shift info
                LazyColumn(
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(12.dp),
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
        } else {
            // Phone — single column
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
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
    }
}

// ── My Sales Today Card ──────────────────────────────────────────────────────

@Composable
private fun MySalesTodayCard(mySales: MySalesReport) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Penjualan Saya Hari Ini",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            FinanceRow(
                label = "Total Penjualan",
                value = formatRupiah(mySales.totalSales),
                color = MaterialTheme.colorScheme.primary
            )
            FinanceRow(
                label = "Jumlah Transaksi",
                value = "${mySales.totalTransactions}x",
                color = MaterialTheme.colorScheme.onSurface
            )
            FinanceRow(
                label = "Total Tunai",
                value = formatRupiah(mySales.cashTotal),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Daily Category Breakdown Card ────────────────────────────────────────────

@Composable
private fun DailyCategoryCard(categories: List<DailyCategoryReport>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Penjualan per Kategori",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            categories.forEach { cat ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text     = cat.categoryName,
                            style    = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text  = "${cat.totalQty.toInt()} item",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text       = formatRupiah(cat.totalSales),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── KPI Cards 2 × 2 ─────────────────────────────────────────────────────────

@Composable
private fun KpiCardsGrid(summary: ShiftSummary) {
    val avgPerTx = if (summary.totalTransactions > 0)
        summary.totalSales / summary.totalTransactions else 0L

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Total Penjualan",
                value     = formatRupiah(summary.totalSales),
                icon      = Icons.Default.BarChart,
                iconColor = MaterialTheme.colorScheme.primary
            )
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Transaksi",
                value     = "${summary.totalTransactions}x",
                icon      = Icons.Default.ShoppingCart,
                iconColor = Color(0xFF2196F3)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Total Pengeluaran",
                value     = formatRupiah(summary.totalExpenses),
                icon      = Icons.Default.Star,
                iconColor = Color(0xFF4CAF50)
            )
            KpiCard(
                modifier  = Modifier.weight(1f),
                label     = "Rata-rata/Transaksi",
                value     = formatRupiah(avgPerTx),
                icon      = Icons.AutoMirrored.Filled.TrendingUp,
                iconColor = Color(0xFF00ACC1)
            )
        }
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(17.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Financial Breakdown ──────────────────────────────────────────────────────

@Composable
private fun FinancialBreakdownCard(summary: ShiftSummary) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Rincian Keuangan",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            FinanceRow(
                label = "Total Penjualan",
                value = formatRupiah(summary.totalSales),
                color = MaterialTheme.colorScheme.onSurface
            )
            FinanceRow(
                label = "Total Kas Masuk",
                value = formatRupiah(summary.totalCashIn),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (summary.totalExpenses > 0) {
                FinanceRow(
                    label = "Total Pengeluaran",
                    value = "-${formatRupiah(summary.totalExpenses)}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            FinanceRow(
                label = "Kas Bersih",
                value = formatRupiah(summary.totalSales + summary.totalCashIn - summary.totalExpenses),
                color = MaterialTheme.colorScheme.primary,
                bold  = true
            )
        }
    }
}

@Composable
private fun FinanceRow(
    label: String,
    value: String,
    color: Color,
    bold: Boolean = false
) {
    Row(
        modifier                = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement   = Arrangement.SpaceBetween,
        verticalAlignment       = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodySmall,
            color      = if (bold) MaterialTheme.colorScheme.onSurface
                         else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color      = color
        )
    }
}

// ── Donut Chart ──────────────────────────────────────────────────────────────

@Composable
private fun PaymentDonutCard(methods: List<PaymentMethodReport>) {
    val total = methods.sumOf { it.total }.takeIf { it > 0L } ?: return

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Metode Pembayaran",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier             = Modifier.fillMaxWidth(),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Donut
                DonutChart(
                    slices = methods.mapIndexed { i, m ->
                        m.total.toFloat() / total.toFloat() to chartColors[i % chartColors.size]
                    },
                    modifier = Modifier.size(100.dp)
                )

                // Legend
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    methods.forEachIndexed { i, method ->
                        val pct = (method.total.toFloat() / total.toFloat() * 100).toInt()
                        val dot = chartColors[i % chartColors.size]

                        Row(
                            verticalAlignment    = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(9.dp)
                                    .background(dot, CircleShape)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text     = method.method.replaceFirstChar { it.uppercase() },
                                    style    = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text     = "${formatRupiah(method.total)} · ${method.count}x · $pct%",
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val stroke      = Stroke(width = strokeWidth)
        val diameter    = size.minDimension - strokeWidth
        val topLeft     = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize     = Size(diameter, diameter)

        var startAngle = -90f
        slices.forEach { (fraction, color) ->
            val sweep = fraction * 360f
            drawArc(
                color      = color,
                startAngle = startAngle,
                sweepAngle = (sweep - 1.5f).coerceAtLeast(0f),
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = stroke
            )
            startAngle += sweep
        }
    }
}

// ── Shift Info Card ──────────────────────────────────────────────────────────

@Composable
private fun ShiftInfoCard(summary: ShiftSummary) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            summary.cashierName?.let {
                FinanceRow(label = "Kasir", value = it, color = MaterialTheme.colorScheme.onSurface)
            }
            FinanceRow(label = "Status", value = summary.status.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurface)
            FinanceRow(label = "Kas Pembukaan", value = formatRupiah(summary.openingCash.toLongOrNull() ?: 0L), color = MaterialTheme.colorScheme.onSurface)
            summary.closingCash?.let {
                FinanceRow(label = "Kas Penutupan", value = formatRupiah(it.toLongOrNull() ?: 0L), color = MaterialTheme.colorScheme.onSurface)
            }
            summary.expectedCash?.let {
                FinanceRow(label = "Kas Diharapkan", value = formatRupiah(it.toLongOrNull() ?: 0L), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            summary.cashDifference?.let {
                FinanceRow(label = "Selisih Kas", value = formatRupiah(it.toLongOrNull() ?: 0L), color = MaterialTheme.colorScheme.primary, bold = true)
            }
        }
    }
}

// ── Empty states ─────────────────────────────────────────────────────────────

@Composable
private fun EmptySummaryPlaceholder() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint     = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tidak ada data untuk periode ini",
                style  = MaterialTheme.typography.bodyMedium,
                color  = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

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
                    PaymentMethodReport("cash",     4_500_000, 32),
                    PaymentMethodReport("qris",     2_800_000, 22),
                    PaymentMethodReport("card",     1_450_000, 10)
                )
            ),
            dateFrom = "2026-04-01",
            dateTo   = "2026-04-15"
        )
        ReportScreenContent(uiState = mockState, modifier = Modifier.fillMaxSize())
    }
}
