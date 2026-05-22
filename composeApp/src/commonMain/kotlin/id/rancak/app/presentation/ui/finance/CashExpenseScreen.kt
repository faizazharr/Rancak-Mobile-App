package id.rancak.app.presentation.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.CashIn
import id.rancak.app.domain.model.Expense
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.ui.finance.components.CashInList
import id.rancak.app.presentation.ui.finance.components.CashInItemCard
import id.rancak.app.presentation.ui.finance.components.CashInFormDialog
import id.rancak.app.presentation.ui.finance.components.ExpenseList
import id.rancak.app.presentation.ui.finance.components.ExpenseItemCard
import id.rancak.app.presentation.ui.finance.components.ExpenseFormDialog
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CashExpenseUiState
import id.rancak.app.presentation.viewmodel.CashExpenseViewModel
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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

@Composable
fun CashExpenseScreen(
    onBack: () -> Unit
) {
    val viewModel: CashExpenseViewModel = koinViewModel()
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
@Composable
fun CashExpenseScreenContent(
    uiState: CashExpenseUiState,
    onBack: () -> Unit,
    actions: CashExpenseActions
) {
    var selectedTab by remember { mutableStateOf(0) }

    CashInFormDialog(uiState, actions)
    ExpenseFormDialog(uiState, actions)

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
            BoxWithConstraints {
                val isTablet = maxWidth >= 600.dp
                if (!isTablet) {
                    FloatingActionButton(onClick = {
                        if (selectedTab == 0) actions.onToggleCashInForm() else actions.onToggleExpenseForm()
                    }) {
                        Icon(Icons.Default.Add, "Tambah")
                    }
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.error != null -> ErrorScreen(uiState.error, onRetry = actions.onRetry)
                isTablet -> Column(Modifier.fillMaxSize()) {
                    FinanceSummaryRow(uiState.cashIns, uiState.expenses)
                    HorizontalDivider()
                    TabletCashLayout(uiState, actions, modifier = Modifier.weight(1f))
                }
                else -> Column(Modifier.fillMaxSize()) {
                    FinanceSummaryRow(uiState.cashIns, uiState.expenses)
                    PhoneCashLayout(uiState, selectedTab, { selectedTab = it }, actions)
                }
            }
        }
    }
}

@Composable
private fun FinanceSummaryRow(
    cashIns: ImmutableList<CashIn>,
    expenses: ImmutableList<Expense>
) {
    val semantic = RancakColors.semantic
    val totalIn = cashIns.sumOf { it.amount }
    val totalOut = expenses.sumOf { it.amount }
    val balance = totalIn - totalOut
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip(
            label = "Kas Masuk",
            amount = totalIn,
            icon = Icons.Default.TrendingUp,
            iconTint = semantic.success,
            amountColor = semantic.success,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Pengeluaran",
            amount = totalOut,
            icon = Icons.Default.TrendingDown,
            iconTint = MaterialTheme.colorScheme.error,
            amountColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Saldo",
            amount = balance,
            icon = Icons.Default.AccountBalance,
            iconTint = if (balance >= 0) semantic.success else MaterialTheme.colorScheme.error,
            amountColor = if (balance >= 0) semantic.success else MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    amount: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    amountColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = formatRupiah(amount),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
private fun TabletCashLayout(
    uiState: CashExpenseUiState,
    actions: CashExpenseActions,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth()) {
        // Kiri — Kas Masuk
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Kas Masuk",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(onClick = actions.onToggleCashInForm) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Tambah")
                }
            }
            if (uiState.cashIns.isEmpty()) {
                EmptyScreen("Belum ada kas masuk")
            } else {
                uiState.cashIns.forEach { item ->
                    CashInItemCard(item = item)
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        // Kanan — Pengeluaran
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pengeluaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(onClick = actions.onToggleExpenseForm) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Tambah")
                }
            }
            if (uiState.expenses.isEmpty()) {
                EmptyScreen("Belum ada pengeluaran")
            } else {
                uiState.expenses.forEach { item ->
                    ExpenseItemCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun PhoneCashLayout(
    uiState: CashExpenseUiState,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    actions: CashExpenseActions
) {
    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { onTabChange(0) }, text = { Text("Kas Masuk") })
            Tab(selected = selectedTab == 1, onClick = { onTabChange(1) }, text = { Text("Pengeluaran") })
        }
        if (selectedTab == 0) {
            CashInList(uiState.cashIns, onDelete = actions.onDeleteCashIn)
        } else {
            ExpenseList(uiState.expenses, onDelete = actions.onDeleteExpense)
        }
    }
}

// CashInList, ExpenseList, CashInItemCard, ExpenseItemCard extracted to finance/components/
// CashInForm, ExpenseForm extracted to finance/components/

@Preview
@Composable
private fun CashInListPreview() {
    RancakTheme {
        CashInList(
            items = persistentListOf(
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
            items = persistentListOf(
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
                cashIns = persistentListOf(
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
