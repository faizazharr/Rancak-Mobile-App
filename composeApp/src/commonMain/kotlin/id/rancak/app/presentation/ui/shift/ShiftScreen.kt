package id.rancak.app.presentation.ui.shift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import id.rancak.app.presentation.components.*
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.ShiftUiState
import id.rancak.app.presentation.viewmodel.ShiftViewModel
import id.rancak.app.domain.model.Shift
import id.rancak.app.domain.model.ShiftStatus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftScreen(
    onBack: () -> Unit,
    viewModel: ShiftViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentShift()
    }

    ShiftScreenContent(
        uiState          = uiState,
        onBack           = onBack,
        onOpeningCashChange = viewModel::onOpeningCashChange,
        onClosingCashChange = viewModel::onClosingCashChange,
        onClosingNoteChange = viewModel::onClosingNoteChange,
        onOpenShift      = viewModel::openShift,
        onCloseShift     = viewModel::closeShift,
        onClearError     = viewModel::clearError
    )
}

@Composable
fun ShiftScreenContent(
    uiState: ShiftUiState,
    onBack: () -> Unit = {},
    onOpeningCashChange: (String) -> Unit = {},
    onClosingCashChange: (String) -> Unit = {},
    onClosingNoteChange: (String) -> Unit = {},
    onOpenShift: () -> Unit = {},
    onCloseShift: () -> Unit = {},
    onClearError: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            RancakTopBar(
                title = "Shift Kasir",
                icon = Icons.Default.AccessTime,
                subtitle = "Kelola jam operasional",
                onMenu = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Error banner ───────────────────────────────────────────────────
            ErrorBanner(
                error = uiState.error,
                onDismiss = onClearError,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            )

            when {
                uiState.isLoading -> LoadingScreen()
                uiState.currentShift != null -> {
                    // Active Shift - Show close shift UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Shift Aktif",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(12.dp))
                                SummaryRow("Kas Awal", formatRupiah(uiState.currentShift.openingCash.toLongOrNull() ?: 0L))
                                uiState.currentShift.totalSales?.let {
                                    SummaryRow("Total Penjualan", formatRupiah(it))
                                }
                                uiState.currentShift.totalExpenses?.let {
                                    SummaryRow("Total Pengeluaran", formatRupiah(it))
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Text(
                            "Tutup Shift",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.closingCash,
                            onValueChange = onClosingCashChange,
                            label = { Text("Kas Akhir") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.closingNote,
                            onValueChange = onClosingNoteChange,
                            label = { Text("Catatan (opsional)") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))

                        RancakButton(
                            text = "Tutup Shift",
                            onClick = onCloseShift,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> {
                    // No Active Shift - Show open shift UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Buka Shift Baru",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Masukkan jumlah kas awal untuk memulai shift",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(32.dp))

                        OutlinedTextField(
                            value = uiState.openingCash,
                            onValueChange = onOpeningCashChange,
                            label = { Text("Kas Awal") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))

                        RancakButton(
                            text = "Buka Shift",
                            onClick = onOpenShift,
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } // end when
        } // end Box
    }
}

// ── Previews — call actual ShiftScreenContent ──

//@Preview
@Composable
private fun ShiftOpenPreview() {
    RancakTheme {
        ShiftScreenContent(
            uiState = ShiftUiState(openingCash = "500000")
        )
    }
}

//@Preview
@Composable
private fun ShiftActivePreview() {
    RancakTheme {
        ShiftScreenContent(
            uiState = ShiftUiState(
                currentShift = Shift(
                    uuid = "shift-1",
                    openedAt = "2026-04-15T08:00:00",
                    closedAt = null,
                    status = ShiftStatus.OPEN,
                    openingCash = "500000",
                    closingCash = null,
                    expectedCash = null,
                    cashDifference = null,
                    cashierName = null,
                    totalSales = 1250000,
                    totalTransactions = null,
                    totalExpenses = 75000,
                    totalCashIn = null
                )
            )
        )
    }
}
