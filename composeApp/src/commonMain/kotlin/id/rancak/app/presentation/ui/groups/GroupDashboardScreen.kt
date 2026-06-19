package id.rancak.app.presentation.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.BranchReport
import id.rancak.app.domain.model.GroupOverview
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.LocalSizes
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.GroupDashboardViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun GroupDashboardScreen(onBack: () -> Unit) {
    val viewModel: GroupDashboardViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar("⚠ $it") }
            viewModel.clearError()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sizes = LocalSizes.current
        val isTablet = maxWidth >= sizes.tabletBreakpoint

        Scaffold(
            topBar = {
                RancakTopBar(
                    title = "Multi-Outlet",
                    icon = Icons.Default.Store,
                    onMenu = onBack,
                    subtitle = "${uiState.groups.size} grup",
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            when {
                uiState.isLoadingGroups -> LoadingScreen()
                uiState.groups.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.StoreMallDirectory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Belum ada grup outlet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
                isTablet -> {
                    // ── Tablet: split layout — overview left, branches right ─
                    Column(Modifier.fillMaxSize().padding(paddingValues)) {
                        // Group selector spans full width at top
                        if (uiState.groups.size > 1) {
                            SingleChoiceSegmentedButtonRow(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                uiState.groups.forEachIndexed { index, group ->
                                    SegmentedButton(
                                        selected = uiState.selectedGroup?.uuid == group.uuid,
                                        onClick = { viewModel.selectGroup(group) },
                                        shape = SegmentedButtonDefaults.itemShape(index, uiState.groups.size),
                                        label = { Text(group.name, maxLines = 1) },
                                    )
                                }
                            }
                            HorizontalDivider()
                        }

                        if (uiState.isLoadingDetail) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Row(Modifier.fillMaxSize()) {
                                // Left: KPI overview
                                LazyColumn(
                                    modifier =
                                        Modifier
                                            .weight(0.45f)
                                            .fillMaxHeight(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    uiState.overview?.let { overview ->
                                        item { OverviewSection(overview) }
                                    }
                                }

                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))

                                // Right: branch list
                                LazyColumn(
                                    modifier =
                                        Modifier
                                            .weight(0.55f)
                                            .fillMaxHeight(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (uiState.branches.isNotEmpty()) {
                                        item {
                                            Text(
                                                "Performa Per Outlet",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                        items(uiState.branches, key = { it.tenantUuid }) { branch ->
                                            BranchCard(branch)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // ── Phone: single scrollable column ─────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.groups.size > 1) {
                            item {
                                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                                    uiState.groups.forEachIndexed { index, group ->
                                        SegmentedButton(
                                            selected = uiState.selectedGroup?.uuid == group.uuid,
                                            onClick = { viewModel.selectGroup(group) },
                                            shape = SegmentedButtonDefaults.itemShape(index, uiState.groups.size),
                                            label = { Text(group.name, maxLines = 1) },
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.isLoadingDetail) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center,
                                ) { CircularProgressIndicator() }
                            }
                        } else {
                            uiState.overview?.let { overview ->
                                item { OverviewSection(overview) }
                            }
                            if (uiState.branches.isNotEmpty()) {
                                item {
                                    Text(
                                        "Performa Per Outlet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                items(uiState.branches, key = { it.tenantUuid }) { branch ->
                                    BranchCard(branch)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(overview: GroupOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Ringkasan Grup",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KpiCard(
                modifier = Modifier.weight(1f),
                label = "Total Pendapatan",
                value = formatRupiah(overview.totalRevenue.toLong()).replace("Rp", "Rp "),
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                label = "Transaksi",
                value = "${overview.totalTransactions}",
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KpiCard(
                modifier = Modifier.weight(1f),
                label = "Rata-rata Order",
                value = formatRupiah(overview.avgOrderValue.toLong()).replace("Rp", "Rp "),
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                label = "Outlet Aktif",
                value = "${overview.totalOutlets}",
            )
        }
        overview.growthPct?.let { growth ->
            val growthText = if (growth >= 0) "+${growth.format1Dec()}%" else "-${growth.format1Dec()}%"
            val color = if (growth >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Text(
                text = "Pertumbuhan: $growthText",
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BranchCard(branch: BranchReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(branch.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${branch.transactions} transaksi · AOV ${formatRupiah(branch.aov.toLong()).replace("Rp", "Rp ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Text(
                formatRupiah(branch.revenue.toLong()).replace("Rp", "Rp "),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun Double.format1Dec(): String {
    val rounded = (abs(this) * 10.0).roundToLong() / 10.0
    val str = rounded.toString()
    return if (str.contains('.')) str else "$str.0"
}
