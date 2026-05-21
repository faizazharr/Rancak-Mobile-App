package id.rancak.app.presentation.ui.pricing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Bundle
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorBanner
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.components.StatusChip
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.BundleManagementUiState
import id.rancak.app.presentation.viewmodel.BundleManagementViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BundleManagementScreen(
    onBack: () -> Unit
) {
    val viewModel: BundleManagementViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BundleManagementContent(
        uiState           = uiState,
        onBack            = onBack,
        onRetry           = viewModel::loadBundles,
        onAddBundle       = viewModel::openCreateDialog,
        onEditBundle      = viewModel::openEditDialog,
        onDeleteBundle    = viewModel::showDeleteConfirm,
        onToggleActive    = viewModel::toggleActive,
        onClearError      = viewModel::clearError,
        // Dialog actions
        onDismissDialog   = viewModel::closeDialog,
        onNameChange      = viewModel::onNameChange,
        onPriceChange     = viewModel::onPriceChange,
        onSkuChange       = viewModel::onSkuChange,
        onIsActiveChange  = viewModel::onIsActiveChange,
        onSaveBundle      = viewModel::saveBundle,
        onDismissDelete   = viewModel::dismissDeleteConfirm,
        onConfirmDelete   = viewModel::deleteBundle
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure UI content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun BundleManagementContent(
    uiState: BundleManagementUiState,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onAddBundle: () -> Unit = {},
    onEditBundle: (Bundle) -> Unit = {},
    onDeleteBundle: (Bundle) -> Unit = {},
    onToggleActive: (Bundle) -> Unit = {},
    onClearError: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onPriceChange: (String) -> Unit = {},
    onSkuChange: (String) -> Unit = {},
    onIsActiveChange: (Boolean) -> Unit = {},
    onSaveBundle: () -> Unit = {},
    onDismissDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {}
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Manajemen Bundle",
                icon     = Icons.Default.Inventory2,
                subtitle = "Kelola paket produk",
                onMenu   = onBack
            )
        },
        floatingActionButton = {
            // FAB hanya di phone — tablet pakai inline button di header
            if (!isTablet) {
                FloatingActionButton(onClick = onAddBundle) {
                    Icon(Icons.Default.Add, "Tambah Bundle")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ErrorBanner(
                error     = uiState.error,
                onDismiss = onClearError,
                modifier  = Modifier.fillMaxWidth().align(Alignment.TopCenter).zIndex(10f)
            )

            Column(Modifier.fillMaxSize()) {
                // Tablet: inline header row dengan tombol tambah
                if (isTablet) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "${uiState.bundles.size} bundle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(onClick = onAddBundle) {
                            Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tambah Bundle")
                        }
                    }
                }

                when {
                    uiState.isLoading -> LoadingScreen()
                    uiState.error != null && uiState.bundles.isEmpty() -> ErrorScreen(
                        uiState.error, onRetry = onRetry
                    )
                    uiState.bundles.isEmpty() -> EmptyScreen(
                        "Belum ada bundle. Tambahkan paket produk baru.",
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> BundleList(
                        bundles        = uiState.bundles,
                        onEdit         = onEditBundle,
                        onDelete       = onDeleteBundle,
                        onToggleActive = onToggleActive
                    )
                }
            }
        }

        // Create / Edit dialog
        if (uiState.showCreateDialog) {
            BundleFormDialog(
                uiState          = uiState,
                onDismiss        = onDismissDialog,
                onNameChange     = onNameChange,
                onPriceChange    = onPriceChange,
                onSkuChange      = onSkuChange,
                onIsActiveChange = onIsActiveChange,
                onSave           = onSaveBundle
            )
        }

        // Delete confirm dialog
        uiState.deletingBundle?.let { bundle ->
            AlertDialog(
                onDismissRequest = onDismissDelete,
                title   = { Text("Hapus Bundle?") },
                text    = { Text("Bundle \"${bundle.name}\" akan dihapus secara permanen.") },
                confirmButton = {
                    TextButton(
                        onClick = onConfirmDelete,
                        colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Hapus") }
                },
                dismissButton = { TextButton(onClick = onDismissDelete) { Text("Batal") } }
            )
        }
    } // end Scaffold
    } // end BoxWithConstraints
}

@Composable
private fun BundleList(
    bundles: List<Bundle>,
    onEdit: (Bundle) -> Unit,
    onDelete: (Bundle) -> Unit,
    onToggleActive: (Bundle) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bundles, key = { it.uuid }) { bundle ->
            BundleCard(
                bundle   = bundle,
                onEdit   = { onEdit(bundle) },
                onDelete = { onDelete(bundle) }
            )
        }
    }
}

@Composable
private fun BundleCard(
    bundle: Bundle,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape  = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(bundle.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        formatRupiah(bundle.price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (bundle.items.isNotEmpty()) {
                        Text(
                            "${bundle.items.size} item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(
                        text  = if (bundle.isActive) "Aktif" else "Nonaktif",
                        color = if (bundle.isActive) RancakColors.semantic.success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit bundle")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Hapus bundle", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun BundleFormDialog(
    uiState: BundleManagementUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onSkuChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    val isEdit = uiState.editingBundle != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(if (isEdit) "Edit Bundle" else "Tambah Bundle") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = uiState.formName,
                    onValueChange = onNameChange,
                    label         = { Text("Nama Bundle*") },
                    shape         = MaterialTheme.shapes.medium,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value           = uiState.formPrice,
                    onValueChange   = onPriceChange,
                    label           = { Text("Harga (Rp)*") },
                    prefix          = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape           = MaterialTheme.shapes.medium,
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = uiState.formSku,
                    onValueChange = onSkuChange,
                    label         = { Text("SKU (opsional)") },
                    shape         = MaterialTheme.shapes.medium,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Aktif", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = uiState.formIsActive, onCheckedChange = onIsActiveChange)
                }

                if (uiState.saveError != null) {
                    Text(uiState.saveError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            RancakButton(
                text      = if (isEdit) "Simpan" else "Tambah",
                onClick   = onSave,
                isLoading = uiState.isSaving,
                enabled   = uiState.formName.isNotBlank() && uiState.formPrice.isNotBlank() && !uiState.isSaving
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
