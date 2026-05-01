package id.rancak.app.presentation.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Supplier
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.viewmodel.SupplierFormField
import id.rancak.app.presentation.viewmodel.SupplierUiState
import id.rancak.app.presentation.viewmodel.SupplierViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun SupplierScreen(
    onBack: () -> Unit,
    viewModel: SupplierViewModel = koinViewModel()
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

    SupplierContent(
        uiState           = uiState,
        onBack            = onBack,
        onAdd             = viewModel::openCreateForm,
        onEdit            = viewModel::openEditForm,
        onDelete          = viewModel::openDeleteDialog,
        onSave            = viewModel::saveSupplier,
        onCloseForm       = viewModel::closeFormDialog,
        onConfirmDelete   = viewModel::confirmDelete,
        onCloseDelete     = viewModel::closeDeleteDialog,
        onFormChange      = viewModel::onFormChange,
        snackbarHostState = snackbarHostState
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Content (pure UI)
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierContent(
    uiState: SupplierUiState,
    onBack: () -> Unit = {},
    onAdd: () -> Unit = {},
    onEdit: (Supplier) -> Unit = {},
    onDelete: (Supplier) -> Unit = {},
    onSave: () -> Unit = {},
    onCloseForm: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onCloseDelete: () -> Unit = {},
    onFormChange: (SupplierFormField, String) -> Unit = { _, _ -> },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (uiState.showFormDialog) {
        ModalBottomSheet(
            onDismissRequest = onCloseForm,
            sheetState       = bottomSheetState
        ) {
            SupplierFormContent(
                uiState        = uiState,
                onSave         = onSave,
                onDismiss      = onCloseForm,
                onFormChange   = onFormChange
            )
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onCloseDelete,
            title  = { Text("Hapus Supplier") },
            text   = { Text("Yakin ingin menghapus supplier \"${uiState.selectedSupplier?.name}\"?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onCloseDelete) { Text("Batal") } }
        )
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Supplier",
                icon     = Icons.Default.LocalShipping,
                subtitle = "${uiState.suppliers.size} supplier",
                onMenu   = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Tambah supplier")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null && uiState.suppliers.isEmpty() ->
                ErrorScreen(uiState.error, modifier = Modifier.padding(padding))
            uiState.suppliers.isEmpty() ->
                EmptyScreen("Belum ada supplier", modifier = Modifier.padding(padding))
            else -> BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
                if (maxWidth >= 600.dp) {
                    TabletSupplierList(uiState.suppliers, onEdit, onDelete)
                } else {
                    PhoneSupplierList(uiState.suppliers, onEdit, onDelete)
                }
            }
        }
    }
}

@Composable
private fun TabletSupplierList(
    suppliers: List<Supplier>,
    onEdit: (Supplier) -> Unit,
    onDelete: (Supplier) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suppliers) { supplier ->
            SupplierListItem(supplier, onEdit, onDelete)
        }
    }
}

@Composable
private fun PhoneSupplierList(
    suppliers: List<Supplier>,
    onEdit: (Supplier) -> Unit,
    onDelete: (Supplier) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suppliers) { supplier ->
            SupplierListItem(supplier, onEdit, onDelete)
        }
    }
}

@Composable
private fun SupplierListItem(
    supplier: Supplier,
    onEdit: (Supplier) -> Unit,
    onDelete: (Supplier) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(supplier.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                supplier.contactName?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                supplier.phone?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!supplier.isActive) {
                    Text("Nonaktif", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Row {
                IconButton(onClick = { onEdit(supplier) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(supplier) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun SupplierFormContent(
    uiState: SupplierUiState,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onFormChange: (SupplierFormField, String) -> Unit
) {
    val title = if (uiState.selectedSupplier == null) "Tambah Supplier" else "Edit Supplier"

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val formModifier = if (maxWidth >= 600.dp)
            Modifier.widthIn(max = 560.dp).align(Alignment.Center)
        else
            Modifier.fillMaxWidth()

        Column(
            modifier            = formModifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value         = uiState.formName,
                onValueChange = { onFormChange(SupplierFormField.NAME, it) },
                label         = { Text("Nama Supplier *") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formContactName,
                onValueChange = { onFormChange(SupplierFormField.CONTACT_NAME, it) },
                label         = { Text("Nama Kontak") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formPhone,
                onValueChange = { onFormChange(SupplierFormField.PHONE, it) },
                label         = { Text("Nomor Telepon") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formEmail,
                onValueChange = { onFormChange(SupplierFormField.EMAIL, it) },
                label         = { Text("Email") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formAddress,
                onValueChange = { onFormChange(SupplierFormField.ADDRESS, it) },
                label         = { Text("Alamat") },
                minLines      = 2,
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formNpwp,
                onValueChange = { onFormChange(SupplierFormField.NPWP, it) },
                label         = { Text("NPWP") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = uiState.formNotes,
                onValueChange = { onFormChange(SupplierFormField.NOTES, it) },
                label         = { Text("Catatan") },
                minLines      = 2,
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth()
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Batal") }
                TextButton(
                    onClick = onSave,
                    enabled = uiState.formName.isNotBlank() && !uiState.isSaving
                ) { Text("Simpan") }
            }
        }
    }
}
