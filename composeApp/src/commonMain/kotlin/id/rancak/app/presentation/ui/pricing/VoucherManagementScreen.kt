package id.rancak.app.presentation.ui.pricing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.pricing.components.VoucherCard
import id.rancak.app.presentation.ui.pricing.components.VoucherFormDialog
import id.rancak.app.presentation.viewmodel.VoucherManagementViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VoucherManagementScreen(
    onBack: () -> Unit,
    viewModel: VoucherManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { scope.launch { snackbarHostState.showSnackbar(it) }; viewModel.clearSuccessMessage() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { scope.launch { snackbarHostState.showSnackbar("⚠ $it") }; viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Voucher",
                icon     = Icons.Default.LocalOffer,
                onBack   = onBack,
                subtitle = "${uiState.vouchers.size} voucher"
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openForm() }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah voucher")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(null to "Semua", true to "Aktif", false to "Nonaktif").forEach { (value, label) ->
                        FilterChip(
                            selected = uiState.filterActive == value,
                            onClick  = { viewModel.setFilter(value) },
                            label    = { Text(label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
                HorizontalDivider()

                when {
                    uiState.isLoading -> LoadingScreen()
                    uiState.vouchers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalOffer, null, Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp))
                            Text("Belum ada voucher", style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    isTablet -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        gridItems(uiState.vouchers, key = { it.uuid }) { voucher ->
                            VoucherCard(voucher = voucher,
                                onEdit   = { viewModel.openForm(voucher) },
                                onDelete = { viewModel.openDeleteConfirm(voucher) })
                        }
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.vouchers, key = { it.uuid }) { voucher ->
                            VoucherCard(voucher = voucher,
                                onEdit   = { viewModel.openForm(voucher) },
                                onDelete = { viewModel.openDeleteConfirm(voucher) })
                        }
                    }
                }
            }
        }

        if (uiState.showFormDialog) {
            VoucherFormDialog(
                editing      = uiState.editingVoucher,
                isSubmitting = uiState.isSubmitting,
                onDismiss    = viewModel::closeForm,
                onConfirm    = { code, name, dt, dv, vf, desc, md, mp, ul, vu, active ->
                    viewModel.save(code, name, dt, dv, vf, desc, md, mp, ul, vu, active)
                }
            )
        }

        if (uiState.showDeleteConfirm && uiState.editingVoucher != null) {
            AlertDialog(
                onDismissRequest = { if (!uiState.isSubmitting) viewModel.closeDeleteConfirm() },
                title = { Text("Hapus Voucher") },
                text  = { Text("Hapus voucher \"${uiState.editingVoucher!!.code}\"? Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = {
                    TextButton(onClick = viewModel::delete, enabled = !uiState.isSubmitting) {
                        if (uiState.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp))
                        else Text("Hapus", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeDeleteConfirm, enabled = !uiState.isSubmitting) { Text("Batal") }
                }
            )
        }
    }
}
