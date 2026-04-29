package id.rancak.app.presentation.ui.pricing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Voucher
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.ui.pricing.components.VoucherCard
import id.rancak.app.presentation.ui.pricing.components.VoucherFormContent
import id.rancak.app.presentation.ui.pricing.components.VoucherFormPanel
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
        uiState.successMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearSuccessMessage()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar("⚠ $it") }
            viewModel.clearError()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        // Phone only: form opens full-screen, replacing this entire screen
        if (!isTablet && uiState.showFormDialog) {
            VoucherFormContent(
                editing      = uiState.editingVoucher,
                isSubmitting = uiState.isSubmitting,
                onBack       = viewModel::closeForm,
                onConfirm    = { code, name, dt, dv, vf, desc, md, mp, ul, vu, active ->
                    viewModel.save(code, name, dt, dv, vf, desc, md, mp, ul, vu, active)
                }
            )
            return@BoxWithConstraints
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
            if (isTablet) {
                // ── Tablet: split layout ─────────────────────────────────────
                Row(Modifier.padding(padding).fillMaxSize()) {

                    // Left panel: filter chips + voucher list
                    Column(
                        Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                    ) {
                        VoucherFilterRow(
                            filterActive = uiState.filterActive,
                            onFilter     = viewModel::setFilter
                        )
                        HorizontalDivider()
                        VoucherListContent(
                            isLoading = uiState.isLoading,
                            vouchers  = uiState.vouchers,
                            onEdit    = { viewModel.openForm(it) },
                            onDelete  = { viewModel.openDeleteConfirm(it) },
                            modifier  = Modifier.fillMaxSize()
                        )
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))

                    // Right panel: form or empty placeholder
                    Box(
                        Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    ) {
                        if (uiState.showFormDialog) {
                            VoucherFormPanel(
                                editing      = uiState.editingVoucher,
                                isSubmitting = uiState.isSubmitting,
                                onClose      = viewModel::closeForm,
                                onConfirm    = { code, name, dt, dv, vf, desc, md, mp, ul, vu, active ->
                                    viewModel.save(code, name, dt, dv, vf, desc, md, mp, ul, vu, active)
                                }
                            )
                        } else {
                            Column(
                                Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.LocalOffer, null,
                                    Modifier.size(72.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Pilih voucher untuk diedit",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "atau tekan + untuk menambah baru",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Phone: list only (form is handled via early return above) ─
                Column(Modifier.padding(padding).fillMaxSize()) {
                    VoucherFilterRow(
                        filterActive = uiState.filterActive,
                        onFilter     = viewModel::setFilter
                    )
                    HorizontalDivider()
                    VoucherListContent(
                        isLoading = uiState.isLoading,
                        vouchers  = uiState.vouchers,
                        onEdit    = { viewModel.openForm(it) },
                        onDelete  = { viewModel.openDeleteConfirm(it) },
                        modifier  = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Delete confirmation dialog (rendered above Scaffold on both layouts)
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
                    TextButton(
                        onClick  = viewModel::closeDeleteConfirm,
                        enabled  = !uiState.isSubmitting
                    ) { Text("Batal") }
                }
            )
        }
    }
}

// ── Filter chips row ──────────────────────────────────────────────────────────

@Composable
private fun VoucherFilterRow(
    filterActive: Boolean?,
    onFilter: (Boolean?) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(null to "Semua", true to "Aktif", false to "Nonaktif").forEach { (value, label) ->
            FilterChip(
                selected = filterActive == value,
                onClick  = { onFilter(value) },
                label    = { Text(label, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

// ── Voucher list content ──────────────────────────────────────────────────────

@Composable
private fun VoucherListContent(
    isLoading: Boolean,
    vouchers: List<Voucher>,
    onEdit: (Voucher) -> Unit,
    onDelete: (Voucher) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> LoadingScreen(modifier)
        vouchers.isEmpty() -> Box(modifier, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.LocalOffer, null,
                    Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Belum ada voucher",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier
        ) {
            items(vouchers, key = { it.uuid }) { voucher ->
                VoucherCard(
                    voucher  = voucher,
                    onEdit   = { onEdit(voucher) },
                    onDelete = { onDelete(voucher) }
                )
            }
        }
    }
}
