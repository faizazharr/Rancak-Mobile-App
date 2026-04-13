package id.rancak.app.presentation.ui.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.designsystem.*
import id.rancak.app.presentation.viewmodel.TableViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableMapScreen(
    onBack: () -> Unit,
    onTableSelect: ((String) -> Unit)? = null,
    viewModel: TableViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadTables() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Denah Meja") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadTables, modifier = Modifier.padding(padding))
            uiState.tables.isEmpty() -> EmptyScreen("Belum ada meja", Modifier.padding(padding))
            else -> {
                val areas = uiState.tables.groupBy { it.area ?: "Umum" }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                    modifier = Modifier.padding(padding),
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
                            TableCell(table) { onTableSelect?.invoke(table.uuid) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(table: Table, onClick: () -> Unit) {
    val bg = when (table.status) {
        TableStatus.AVAILABLE -> StatusAvailable
        TableStatus.OCCUPIED -> StatusOccupied
        TableStatus.RESERVED -> StatusReserved
        TableStatus.MAINTENANCE -> StatusMaintenance
    }
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(bg.copy(alpha = 0.15f))
            .clickable(enabled = table.status == TableStatus.AVAILABLE) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(table.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = bg, textAlign = TextAlign.Center)
            Text(
                table.status.value.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = bg
            )
            table.capacity?.let {
                Text("$it kursi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
