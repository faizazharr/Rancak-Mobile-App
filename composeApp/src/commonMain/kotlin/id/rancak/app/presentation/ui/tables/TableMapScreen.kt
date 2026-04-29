package id.rancak.app.presentation.ui.tables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.tables.components.TableCell
import id.rancak.app.presentation.ui.tables.components.TableSummaryCard
import id.rancak.app.presentation.ui.tables.components.AreaSummaryCard
import id.rancak.app.presentation.viewmodel.TableUiState
import id.rancak.app.presentation.viewmodel.TableViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TableMapScreen(
    onBack: () -> Unit,
    onTableSelect: ((String) -> Unit)? = null,
    viewModel: TableViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadTables() }

    TableMapScreenContent(
        uiState       = uiState,
        onBack        = onBack,
        onRetry       = viewModel::loadTables,
        onTableSelect = onTableSelect
    )
}

/** Pure-UI content — tanpa ViewModel, aman di-preview. */
@Composable
fun TableMapScreenContent(
    uiState: TableUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onTableSelect: ((String) -> Unit)? = null
) {
    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Denah Meja",
                icon = Icons.Default.TableBar,
                subtitle = "Manajemen meja",
                onMenu = onBack
            )
        }
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = onRetry)
                uiState.tables.isEmpty() -> EmptyScreen("Belum ada meja")
                isTablet -> TabletTableLayout(uiState, onTableSelect)
                else -> {
                    val areas = uiState.tables.groupBy { it.area ?: "Umum" }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        areas.forEach { (area, tables) ->
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Text(
                                    area,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(tables.sortedBy { it.sortOrder }, key = { it.uuid }) { table ->
                                TableCell(table, size = 100.dp) { onTableSelect?.invoke(table.uuid) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletTableLayout(
    uiState: TableUiState,
    onTableSelect: ((String) -> Unit)?
) {
    val available = uiState.tables.count { it.status == TableStatus.AVAILABLE }
    val occupied  = uiState.tables.count { it.status == TableStatus.OCCUPIED }
    val inactive  = uiState.tables.count { it.status == TableStatus.INACTIVE }
    val areas = uiState.tables.groupBy { it.area ?: "Umum" }

    Row(Modifier.fillMaxSize()) {
        // Kiri — grid meja
        LazyVerticalGrid(
            columns = GridCells.Adaptive(130.dp),
            modifier = Modifier.weight(0.65f).fillMaxHeight(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            areas.forEach { (area, tables) ->
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Text(
                        area,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(tables.sortedBy { it.sortOrder }, key = { it.uuid }) { table ->
                    TableCell(table, size = 130.dp) { onTableSelect?.invoke(table.uuid) }
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        // Kanan — statistik + legenda
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ringkasan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            TableSummaryCard(
                total     = uiState.tables.size,
                available = available,
                occupied  = occupied,
                inactive  = inactive
            )

            Text("Area", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            areas.forEach { (area, tables) ->
                val areaOccupied = tables.count { it.status == TableStatus.OCCUPIED }
                AreaSummaryCard(
                    area          = area,
                    totalCount    = tables.size,
                    occupiedCount = areaOccupied
                )
            }
        }
    }
}

// TableCell extracted to tables/components/TableCell.kt

@Preview
@Composable
private fun TableCellAvailablePreview() {
    RancakTheme {
        TableCell(
            table = Table(uuid = "1", name = "A1", area = "Indoor", capacity = 4, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 1, activeSaleUuid = null),
            onClick = {}
        )    }
}

@Preview
@Composable
private fun TableCellOccupiedPreview() {
    RancakTheme {
        TableCell(
            table = Table(uuid = "2", name = "B3", area = "Outdoor", capacity = 6, status = TableStatus.OCCUPIED, isActive = true, sortOrder = 2, activeSaleUuid = "sale-1"),
            onClick = {}
        )
    }
}

@Preview(name = "Table Map – Full Screen", widthDp = 600, heightDp = 800)
@Composable
private fun TableMapScreenPreview() {
    val tables = listOf(
        Table(uuid = "t1", name = "Meja 1", area = "Indoor", capacity = 4, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 1, activeSaleUuid = null),
        Table(uuid = "t2", name = "Meja 2", area = "Indoor", capacity = 2, status = TableStatus.OCCUPIED,  isActive = true, sortOrder = 2, activeSaleUuid = "sale-1"),
        Table(uuid = "t3", name = "Meja 3", area = "Indoor", capacity = 4, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 3, activeSaleUuid = null),
        Table(uuid = "t4", name = "Meja 4", area = "Outdoor",capacity = 6, status = TableStatus.AVAILABLE, isActive = true, sortOrder = 4, activeSaleUuid = null),
        Table(uuid = "t5", name = "Meja 5", area = "Outdoor",capacity = 2, status = TableStatus.INACTIVE,  isActive = false, sortOrder = 5, activeSaleUuid = null)
    )
    RancakTheme {
        TableMapScreenContent(
            uiState = TableUiState(tables = tables),
            onBack  = {},
            onRetry = {}
        )
    }
}
