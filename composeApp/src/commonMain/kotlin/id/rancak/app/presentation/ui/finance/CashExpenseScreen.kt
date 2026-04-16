package id.rancak.app.presentation.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.CashIn
import id.rancak.app.domain.model.Expense
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CashExpenseViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashExpenseScreen(
    onBack: () -> Unit,
    viewModel: CashExpenseViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadAll() }

    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Kas & Pengeluaran",
                icon = Icons.Default.AccountBalance,
                subtitle = "Kelola arus kas",
                onMenu = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (selectedTab == 0) viewModel.toggleCashInForm() else viewModel.toggleExpenseForm()
            }) {
                Icon(Icons.Default.Add, "Tambah")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            PrimaryTabRow(selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Kas Masuk") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Pengeluaran") })
            }

            when {
                uiState.isLoading -> LoadingScreen()
                uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadAll)
                else -> {
                    if (selectedTab == 0) {
                        if (uiState.showCashInForm) CashInForm(uiState, viewModel)
                        CashInList(uiState.cashIns, onDelete = viewModel::deleteCashIn)
                    } else {
                        if (uiState.showExpenseForm) ExpenseForm(uiState, viewModel)
                        ExpenseList(uiState.expenses, onDelete = viewModel::deleteExpense)
                    }
                }
            }
        }
    }
}

@Composable
private fun CashInForm(
    uiState: id.rancak.app.presentation.viewmodel.CashExpenseUiState,
    viewModel: CashExpenseViewModel
) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tambah Kas Masuk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            RancakTextField(value = uiState.formAmount, onValueChange = viewModel::onAmountChange, label = "Jumlah (Rp)")
            RancakTextField(value = uiState.formSource, onValueChange = viewModel::onSourceChange, label = "Sumber")
            RancakTextField(value = uiState.formDescription, onValueChange = viewModel::onDescriptionChange, label = "Keterangan")
            RancakTextField(value = uiState.formNote, onValueChange = viewModel::onNoteChange, label = "Catatan (opsional)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RancakOutlinedButton("Batal", onClick = viewModel::toggleCashInForm, modifier = Modifier.weight(1f))
                RancakButton("Simpan", onClick = viewModel::submitCashIn, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ExpenseForm(
    uiState: id.rancak.app.presentation.viewmodel.CashExpenseUiState,
    viewModel: CashExpenseViewModel
) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tambah Pengeluaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            RancakTextField(value = uiState.formAmount, onValueChange = viewModel::onAmountChange, label = "Jumlah (Rp)")
            RancakTextField(value = uiState.formDescription, onValueChange = viewModel::onDescriptionChange, label = "Keterangan")
            RancakTextField(value = uiState.formNote, onValueChange = viewModel::onNoteChange, label = "Catatan (opsional)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RancakOutlinedButton("Batal", onClick = viewModel::toggleExpenseForm, modifier = Modifier.weight(1f))
                RancakButton("Simpan", onClick = viewModel::submitExpense, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CashInList(items: List<CashIn>, onDelete: (String) -> Unit) {
    if (items.isEmpty()) {
        EmptyScreen("Belum ada kas masuk")
    } else {
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.uuid }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.description ?: "-", style = MaterialTheme.typography.bodyMedium)
                            item.source?.let { Text("Sumber: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline) }
                        }
                        Text(formatRupiah(item.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = id.rancak.app.presentation.designsystem.RancakColors.semantic.success)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseList(items: List<Expense>, onDelete: (String) -> Unit) {
    if (items.isEmpty()) {
        EmptyScreen("Belum ada pengeluaran")
    } else {
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.uuid }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.description ?: "-", style = MaterialTheme.typography.bodyMedium)
                            item.note?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline) }
                        }
                        Text(formatRupiah(item.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CashInListPreview() {
    RancakTheme {
        CashInList(
            items = listOf(
                CashIn(uuid = "1", amount = 500000, source = "Modal", description = "Kas Awal", note = null, createdAt = null),
                CashIn(uuid = "2", amount = 200000, source = "Pinjaman", description = "Tambahan Modal", note = "Dari owner", createdAt = null)
            ),
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun ExpenseListPreview() {
    RancakTheme {
        ExpenseList(
            items = listOf(
                Expense(uuid = "1", amount = 50000, description = "Beli Gas", note = "2 tabung", categoryUuid = null, expenseDate = null, createdAt = null),
                Expense(uuid = "2", amount = 25000, description = "Beli Tisu", note = null, categoryUuid = null, expenseDate = null, createdAt = null)
            ),
            onDelete = {}
        )
    }
}
