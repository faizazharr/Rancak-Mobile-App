package id.rancak.app.presentation.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.ProductManagementUiState
import id.rancak.app.presentation.viewmodel.ProductManagementViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductManagementScreen(
    onBack: () -> Unit,
    viewModel: ProductManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadAll() }

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

        Scaffold(
            topBar = {
                RancakTopBar(
                    title    = "Manajemen Produk",
                    icon     = Icons.Default.Inventory2,
                    onMenu   = onBack,
                    subtitle = "${uiState.filteredProducts.size} produk"
                )
            },
            floatingActionButton = {
                if (!isTablet) {
                    FloatingActionButton(onClick = viewModel::openProductForm) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah produk")
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (uiState.isLoading) {
                LoadingScreen(Modifier.padding(padding))
            } else {
                ProductListContent(
                    uiState          = uiState,
                    isTablet         = isTablet,
                    onAddProduct     = viewModel::openProductForm,
                    onSearchChange   = viewModel::setSearchQuery,
                    onCategorySelect = viewModel::setCategory,
                    onAdjustStock    = viewModel::openAdjustDialog,
                    onAddBatch       = viewModel::openBatchDialog,
                    on86Toggle       = viewModel::toggle86,
                    onEditProduct    = viewModel::openProductForm,
                    onDeleteProduct  = viewModel::openDeleteConfirm,
                    onAddCategory    = { viewModel.openCategoryForm() },
                    onEditCategory   = { viewModel.openCategoryForm(it) },
                    onDeleteCategory = { viewModel.deleteCategory(it) },
                    modifier         = Modifier.padding(padding)
                )
            }

            // ── Dialogs ───────────────────────────────────────────────────────

            if (uiState.showAdjustDialog && uiState.actionProduct != null) {
                StockAdjustDialog(
                    product      = uiState.actionProduct!!,
                    isSubmitting = uiState.isSubmitting,
                    onDismiss    = viewModel::closeAdjustDialog,
                    onConfirm    = { type, qty, note ->
                        viewModel.adjustStock(uiState.actionProduct!!.uuid, type, qty, note)
                    }
                )
            }

            if (uiState.showBatchDialog && uiState.actionProduct != null) {
                AddBatchDialog(
                    product      = uiState.actionProduct!!,
                    isSubmitting = uiState.isSubmitting,
                    onDismiss    = viewModel::closeBatchDialog,
                    onConfirm    = { qty, expiry, cost, batch, note ->
                        viewModel.createBatch(uiState.actionProduct!!.uuid, qty, expiry, cost, batch, note)
                    }
                )
            }

            if (uiState.showProductFormDialog) {
                ProductFormDialog(
                    editingProduct = uiState.actionProduct,
                    categories     = uiState.categories,
                    isSubmitting   = uiState.isSubmitting,
                    onDismiss      = viewModel::closeProductForm,
                    onConfirm      = { name, price, desc, sku, barcode, catUuid, unit, stock, hasExpiry ->
                        viewModel.saveProduct(name, price, desc, sku, barcode, catUuid, unit, stock, hasExpiry)
                    }
                )
            }

            if (uiState.showDeleteConfirmDialog && uiState.actionProduct != null) {
                AlertDialog(
                    onDismissRequest = { if (!uiState.isSubmitting) viewModel.closeDeleteConfirm() },
                    title            = { Text("Hapus Produk") },
                    text             = { Text("Hapus produk \"${uiState.actionProduct!!.name}\"? Tindakan ini tidak dapat dibatalkan.") },
                    confirmButton    = {
                        TextButton(onClick = viewModel::deleteProduct, enabled = !uiState.isSubmitting) {
                            if (uiState.isSubmitting) CircularProgressIndicator(Modifier.size(16.dp))
                            else Text("Hapus", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::closeDeleteConfirm, enabled = !uiState.isSubmitting) {
                            Text("Batal")
                        }
                    }
                )
            }

            if (uiState.showCategoryFormDialog) {
                CategoryFormDialog(
                    editingCategory = uiState.editingCategory,
                    isSubmitting    = uiState.isSubmitting,
                    onDismiss       = viewModel::closeCategoryForm,
                    onConfirm       = { name, desc -> viewModel.saveCategory(name, desc) }
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ProductManagementScreenPreview() {
    RancakTheme {
        Scaffold(
            topBar = {
                RancakTopBar(
                    title    = "Manajemen Produk",
                    icon     = Icons.Default.Inventory2,
                    onBack   = {},
                    subtitle = "0 produk"
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah produk")
                }
            }
        ) { padding ->
            ProductListContent(
                uiState          = ProductManagementUiState(),
                isTablet         = false,
                onAddProduct     = {},
                onSearchChange   = {},
                onCategorySelect = {},
                onAdjustStock    = {},
                onAddBatch       = {},
                on86Toggle       = {},
                onEditProduct    = {},
                onDeleteProduct  = {},
                onAddCategory    = {},
                onEditCategory   = {},
                onDeleteCategory = {},
                modifier         = Modifier.padding(padding)
            )
        }
    }
}
