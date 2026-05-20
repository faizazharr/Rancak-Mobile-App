package id.rancak.app.presentation.ui.modifiers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import id.rancak.app.presentation.components.RancakFormDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Modifier as DomainModifier
import id.rancak.app.domain.model.Product
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.components.StatusChip
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.viewmodel.ModifierManagementUiState
import id.rancak.app.presentation.viewmodel.ModifierManagementViewModel
import id.rancak.app.presentation.viewmodel.ModifierTab
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ModifierManagementScreen(
    onBack: () -> Unit
) {
    val viewModel: ModifierManagementViewModel = koinViewModel()
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

    ModifierManagementContent(
        uiState            = uiState,
        onBack             = onBack,
        onSelectTab        = viewModel::selectTab,
        onSelectProduct    = viewModel::selectProduct,
        onAddModifier      = viewModel::openCreateForm,
        onEditModifier     = viewModel::openEditForm,
        onDeleteModifier   = viewModel::openDeleteDialog,
        onSave             = viewModel::saveModifier,
        onCloseForm        = viewModel::closeFormDialog,
        onConfirmDelete    = viewModel::confirmDelete,
        onCloseDelete      = viewModel::closeDeleteDialog,
        onFormNameChange   = viewModel::onFormNameChange,
        onFormSortChange   = viewModel::onFormSortOrderChange,
        onFormActiveChange = viewModel::onFormIsActiveChange,
        snackbarHostState  = snackbarHostState
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Content (pure UI)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ModifierManagementContent(
    uiState: ModifierManagementUiState,
    onBack: () -> Unit = {},
    onSelectTab: (ModifierTab) -> Unit = {},
    onSelectProduct: (Product?) -> Unit = {},
    onAddModifier: () -> Unit = {},
    onEditModifier: (DomainModifier) -> Unit = {},
    onDeleteModifier: (DomainModifier) -> Unit = {},
    onSave: () -> Unit = {},
    onCloseForm: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onCloseDelete: () -> Unit = {},
    onFormNameChange: (String) -> Unit = {},
    onFormSortChange: (Int) -> Unit = {},
    onFormActiveChange: (Boolean) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // Form dialog (global + per-produk berbagi form yang sama)
    if (uiState.showFormDialog) {
        ModifierFormDialog(
            uiState           = uiState,
            onSave            = onSave,
            onDismiss         = onCloseForm,
            onNameChange      = onFormNameChange,
            onSortOrderChange = onFormSortChange,
            onIsActiveChange  = onFormActiveChange
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onCloseDelete,
            title  = { Text("Hapus Modifier") },
            text   = { Text("Yakin ingin menghapus modifier \"${uiState.selectedModifier?.name}\"?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCloseDelete) { Text("Batal") }
            }
        )
    }

    // Tentukan list aktif berdasarkan tab
    val activeList = when (uiState.activeTab) {
        ModifierTab.GLOBAL      -> uiState.modifiers
        ModifierTab.PER_PRODUCT -> uiState.productModifiers
    }.toImmutableList()
    val isLoadingActive = when (uiState.activeTab) {
        ModifierTab.GLOBAL      -> uiState.isLoading
        ModifierTab.PER_PRODUCT -> uiState.isLoadingProductModifiers
    }
    // Pada tab per-produk, FAB hanya aktif setelah produk dipilih
    val canAdd = uiState.activeTab == ModifierTab.GLOBAL ||
                 uiState.selectedProduct != null

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                RancakTopBar(
                    title    = "Modifier",
                    icon     = Icons.Default.Tune,
                    subtitle = "${uiState.modifiers.size} global · ${uiState.productModifiers.size} per-produk",
                    onMenu   = onBack
                )
            },
            floatingActionButton = {
                // FAB hanya di phone — tablet pakai inline button di panel kanan
                if (!isTablet && canAdd) {
                    FloatingActionButton(onClick = onAddModifier) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah modifier")
                    }
                }
            },
            snackbarHost   = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (isTablet) {
                TabletLayout(
                    uiState          = uiState,
                    activeList       = activeList,
                    isLoadingActive  = isLoadingActive,
                    activeError      = if (uiState.activeTab == ModifierTab.GLOBAL) uiState.error else null,
                    canAdd           = canAdd,
                    onSelectTab      = onSelectTab,
                    onSelectProduct  = onSelectProduct,
                    onAddModifier    = onAddModifier,
                    onEditModifier   = onEditModifier,
                    onDeleteModifier = onDeleteModifier,
                    modifier         = Modifier.padding(padding).fillMaxSize()
                )
            } else {
                PhoneLayout(
                    uiState          = uiState,
                    activeList       = activeList,
                    isLoadingActive  = isLoadingActive,
                    activeError      = if (uiState.activeTab == ModifierTab.GLOBAL) uiState.error else null,
                    onSelectTab      = onSelectTab,
                    onSelectProduct  = onSelectProduct,
                    onEditModifier   = onEditModifier,
                    onDeleteModifier = onDeleteModifier,
                    modifier         = Modifier.padding(padding).fillMaxSize()
                )
            }
        }
    }
}

// ── Tablet: list kiri + detail/form kanan ────────────────────────────────────

@Composable
private fun TabletLayout(
    uiState: ModifierManagementUiState,
    activeList: ImmutableList<DomainModifier>,
    isLoadingActive: Boolean,
    activeError: String?,
    canAdd: Boolean,
    onSelectTab: (ModifierTab) -> Unit,
    onSelectProduct: (Product?) -> Unit,
    onAddModifier: () -> Unit,
    onEditModifier: (DomainModifier) -> Unit,
    onDeleteModifier: (DomainModifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // Panel kiri: tab + list
        Column(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
        ) {
            ModifierTabRow(uiState.activeTab, onSelectTab)
            AnimatedVisibility(uiState.activeTab == ModifierTab.PER_PRODUCT) {
                ProductPickerDropdown(
                    products        = uiState.products.toImmutableList(),
                    selectedProduct = uiState.selectedProduct,
                    onSelectProduct = onSelectProduct,
                    modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            ModifierListBody(
                    modifiers       = activeList,
                    isLoading       = isLoadingActive,
                    error           = activeError,
                    onEdit          = onEditModifier,
                    onDelete        = onDeleteModifier,
                    emptyText       = if (uiState.activeTab == ModifierTab.PER_PRODUCT && uiState.selectedProduct == null)
                                          "Pilih produk terlebih dahulu"
                                      else "Belum ada modifier",
                    modifier        = Modifier.weight(1f)
                )
            }
            HorizontalDivider(modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp)
            .run { this })
        // Panel kanan: tombol tambah (tablet — no FAB)
        Column(
            modifier            = Modifier.weight(0.42f).fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (canAdd) {
                RancakButton(
                    text     = "Tambah Modifier",
                    onClick  = onAddModifier,
                    modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth()
                )
            }
        }
    }
}

// ── Phone: tab + list (FAB di luar) ──────────────────────────────────────────

@Composable
private fun PhoneLayout(
    uiState: ModifierManagementUiState,
    activeList: ImmutableList<DomainModifier>,
    isLoadingActive: Boolean,
    activeError: String?,
    onSelectTab: (ModifierTab) -> Unit,
    onSelectProduct: (Product?) -> Unit,
    onEditModifier: (DomainModifier) -> Unit,
    onDeleteModifier: (DomainModifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ModifierTabRow(uiState.activeTab, onSelectTab)
        AnimatedVisibility(uiState.activeTab == ModifierTab.PER_PRODUCT) {
            ProductPickerDropdown(
                products        = uiState.products.toImmutableList(),
                selectedProduct = uiState.selectedProduct,
                onSelectProduct = onSelectProduct,
                modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        ModifierListBody(
            modifiers  = activeList,
            isLoading  = isLoadingActive,
            error      = activeError,
            onEdit     = onEditModifier,
            onDelete   = onDeleteModifier,
            emptyText  = if (uiState.activeTab == ModifierTab.PER_PRODUCT && uiState.selectedProduct == null)
                             "Pilih produk terlebih dahulu"
                         else "Belum ada modifier",
            modifier   = Modifier.weight(1f)
        )
    }
}

// ── Tab Row ───────────────────────────────────────────────────────────────────

@Composable
private fun ModifierTabRow(activeTab: ModifierTab, onSelectTab: (ModifierTab) -> Unit) {
    PrimaryTabRow(selectedTabIndex = activeTab.ordinal) {
        Tab(
            selected = activeTab == ModifierTab.GLOBAL,
            onClick  = { onSelectTab(ModifierTab.GLOBAL) },
            text     = { Text("Global") }
        )
        Tab(
            selected = activeTab == ModifierTab.PER_PRODUCT,
            onClick  = { onSelectTab(ModifierTab.PER_PRODUCT) },
            text     = { Text("Per Produk") }
        )
    }
}

// ── Product picker dropdown ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductPickerDropdown(
    products: ImmutableList<Product>,
    selectedProduct: Product?,
    onSelectProduct: (Product?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = modifier
    ) {
        OutlinedTextField(
            value            = selectedProduct?.name ?: "",
            onValueChange    = {},
            readOnly         = true,
            label            = { Text("Pilih Produk") },
            trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape            = MaterialTheme.shapes.medium,
            modifier         = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.forEach { product ->
                DropdownMenuItem(
                    text    = { Text(product.name) },
                    onClick = {
                        onSelectProduct(product)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ── List body ─────────────────────────────────────────────────────────────────

@Composable
private fun ModifierListBody(
    modifiers: ImmutableList<DomainModifier>,
    isLoading: Boolean,
    error: String? = null,
    onEdit: (DomainModifier) -> Unit,
    onDelete: (DomainModifier) -> Unit,
    emptyText: String = "Belum ada modifier",
    modifier: Modifier = Modifier
) {
    when {
        isLoading            -> LoadingScreen(modifier)
        error != null && modifiers.isEmpty() -> ErrorScreen(error, modifier = modifier)
        modifiers.isEmpty()  -> EmptyScreen(emptyText, modifier = modifier)
        else -> LazyColumn(
            modifier            = modifier,
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(modifiers, key = { it.uuid }) { mod ->
                ModifierListItem(modifier = mod, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

// ── List item ─────────────────────────────────────────────────────────────────

@Composable
private fun ModifierListItem(
    modifier: DomainModifier,
    onEdit: (DomainModifier) -> Unit,
    onDelete: (DomainModifier) -> Unit
) {
    val semantic = RancakColors.semantic
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    modifier.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(
                    text  = if (modifier.isActive) "Aktif" else "Nonaktif",
                    color = if (modifier.isActive) semantic.success else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onEdit(modifier) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { onDelete(modifier) }) {
                    Icon(
                        Icons.Default.Delete, contentDescription = "Hapus",
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Form dialog ───────────────────────────────────────────────────────────────

@Composable
private fun ModifierFormDialog(
    uiState: ModifierManagementUiState,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onSortOrderChange: (Int) -> Unit,
    onIsActiveChange: (Boolean) -> Unit
) {
    RancakFormDialog(
        icon             = Icons.Default.Tune,
        title            = if (uiState.selectedModifier == null) "Tambah Modifier" else "Edit Modifier",
        subtitle         = if (uiState.selectedModifier == null) "Buat modifier pilihan baru" else "Perbarui informasi modifier",
        onDismissRequest = onDismiss,
        confirmLabel     = "Simpan",
        onConfirm        = onSave,
        confirmEnabled   = uiState.formName.isNotBlank() && !uiState.isSaving,
        isSubmitting     = uiState.isSaving
    ) {
        RancakTextField(
            value         = uiState.formName,
            onValueChange = onNameChange,
            label         = "Nama Modifier",
            placeholder   = "Contoh: Pedas, Tanpa Bawang, Tambah Es"
        )
        RancakTextField(
            value           = uiState.formSortOrder.toString(),
            onValueChange   = { onSortOrderChange(it.toIntOrNull() ?: 0) },
            label           = "Urutan Tampil",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth()
        ) {
            Text("Aktif", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = uiState.formIsActive, onCheckedChange = onIsActiveChange)
        }
    }
}

