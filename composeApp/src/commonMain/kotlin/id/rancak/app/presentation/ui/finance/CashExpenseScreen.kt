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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import id.rancak.app.presentation.viewmodel.CashExpenseUiState
import id.rancak.app.presentation.viewmodel.CashExpenseViewModel
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/** Semua callback dari [CashExpenseViewModel] — memudahkan preview. */
data class CashExpenseActions(
    val onRetry: () -> Unit = {},
    val onToggleCashInForm: () -> Unit = {},
    val onToggleExpenseForm: () -> Unit = {},
    val onAmountChange: (String) -> Unit = {},
    val onSourceChange: (String) -> Unit = {},
    val onDescriptionChange: (String) -> Unit = {},
    val onNoteChange: (String) -> Unit = {},
    val onSubmitCashIn: () -> Unit = {},
    val onSubmitExpense: () -> Unit = {},
    val onDeleteCashIn: (String) -> Unit = {},
    val onDeleteExpense: (String) -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashExpenseScreen(
    onBack: () -> Unit,
    viewModel: CashExpenseViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadAll() }

    CashExpenseScreenContent(
        uiState = uiState,
        onBack  = onBack,
        actions = CashExpenseActions(
            onRetry              = viewModel::loadAll,
            onToggleCashInForm   = viewModel::toggleCashInForm,
            onToggleExpenseForm  = viewModel::toggleExpenseForm,
            onAmountChange       = viewModel::onAmountChange,
            onSourceChange       = viewModel::onSourceChange,
            onDescriptionChange  = viewModel::onDescriptionChange,
            onNoteChange         = viewModel::onNoteChange,
            onSubmitCashIn       = viewModel::submitCashIn,
            onSubmitExpense      = viewModel::submitExpense,
            onDeleteCashIn       = viewModel::deleteCashIn,
            onDeleteExpense      = viewModel::deleteExpense
        )
    )
}

/** Pure-UI content — tanpa ViewModel, aman di-preview. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashExpenseScreenContent(
    uiState: CashExpenseUiState,
    onBack: () -> Unit,
    actions: CashExpenseActions
) {
    var selectedTab by remember { mutableStateOf(0) }

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
                if (selectedTab == 0) actions.onToggleCashInForm() else actions.onToggleExpenseForm()
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
                uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = actions.onRetry)
                else -> {
                    if (selectedTab == 0) {
                        if (uiState.showCashInForm) CashInForm(uiState, actions)
                        CashInList(uiState.cashIns, onDelete = actions.onDeleteCashIn)
                    } else {
                        if (uiState.showExpenseForm) ExpenseForm(uiState, actions)
                        ExpenseList(uiState.expenses, onDelete = actions.onDeleteExpense)
                    }
                }
            }
        }
    }
}

@Composable
private fun CashInForm(
    uiState: CashExpenseUiState,
    actions: CashExpenseActions
) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tambah Kas Masuk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            RancakTextField(value = uiState.formAmount,      onValueChange = actions.onAmountChange,      label = "Jumlah (Rp)")
            RancakTextField(value = uiState.formSource,      onValueChange = actions.onSourceChange,      label = "Sumber")
            RancakTextField(value = uiState.formDescription, onValueChange = actions.onDescriptionChange, label = "Keterangan")
            RancakTextField(value = uiState.formNote,        onValueChange = actions.onNoteChange,        label = "Catatan (opsional)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RancakOutlinedButton("Batal",  onClick = actions.onToggleCashInForm, modifier = Modifier.weight(1f))
                RancakButton       ("Simpan", onClick = actions.onSubmitCashIn,     modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ExpenseForm(
    uiState: CashExpenseUiState,
    actions: CashExpenseActions
) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tambah Pengeluaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            RancakTextField(value = uiState.formAmount,      onValueChange = actions.onAmountChange,      label = "Jumlah (Rp)")
            RancakTextField(value = uiState.formDescription, onValueChange = actions.onDescriptionChange, label = "Keterangan")
            RancakTextField(value = uiState.formNote,        onValueChange = actions.onNoteChange,        label = "Catatan (opsional)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RancakOutlinedButton("Batal",  onClick = actions.onToggleExpenseForm, modifier = Modifier.weight(1f))
                RancakButton       ("Simpan", onClick = actions.onSubmitExpense,     modifier = Modifier.weight(1f))
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
                CashIn(uuid = "1", amount = 500000, source = "Modal", description = "Kas Awal", note = null, cashierUuid = null, cashierName = null, shiftUuid = null, cashInDate = null, createdAt = null),
                CashIn(uuid = "2", amount = 200000, source = "Pinjaman", description = "Tambahan Modal", note = "Dari owner", cashierUuid = null, cashierName = null, shiftUuid = null, cashInDate = null, createdAt = null)
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
                Expense(uuid = "1", amount = 50000, description = "Beli Gas", note = "2 tabung", categoryUuid = null, categoryName = null, cashierUuid = null, cashierName = null, expenseDate = null, createdAt = null, updatedAt = null),
                Expense(uuid = "2", amount = 25000, description = "Beli Tisu", note = null, categoryUuid = null, categoryName = null, cashierUuid = null, cashierName = null, expenseDate = null, createdAt = null, updatedAt = null)
            ),
            onDelete = {}
        )
    }
}

@Preview(name = "Cash & Expense – Full Screen", widthDp = 390, heightDp = 844)
@Composable
private fun CashExpenseScreenPreview() {
    RancakTheme {
        CashExpenseScreenContent(
            uiState = CashExpenseUiState(
                cashIns = listOf(
                    CashIn(uuid = "1", amount = 500_000, source = "Modal",
                        description = "Kas Awal", note = null,
                        cashierUuid = null, cashierName = null, shiftUuid = null,
                        cashInDate = null, createdAt = null),
                    CashIn(uuid = "2", amount = 200_000, source = "Pinjaman",
                        description = "Tambahan Modal", note = "Dari owner",
                        cashierUuid = null, cashierName = null, shiftUuid = null,
                        cashInDate = null, createdAt = null)
                )
            ),
            onBack  = {},
            actions = CashExpenseActions()
        )
    }
}
