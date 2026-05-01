package id.rancak.app.presentation.ui.modifiers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Modifier as DomainModifier
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.viewmodel.ModifierManagementUiState
import id.rancak.app.presentation.viewmodel.ModifierManagementViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ModifierManagementScreen(
    onBack: () -> Unit,
    viewModel: ModifierManagementViewModel = koinViewModel()
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

    ModifierManagementContent(
        uiState            = uiState,
        onBack             = onBack,
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
// Content (pure UI — previewable)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ModifierManagementContent(
    uiState: ModifierManagementUiState,
    onBack: () -> Unit = {},
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
    if (uiState.showFormDialog) {
        ModifierFormDialog(
            uiState            = uiState,
            onSave             = onSave,
            onDismiss          = onCloseForm,
            onNameChange       = onFormNameChange,
            onSortOrderChange  = onFormSortChange,
            onIsActiveChange   = onFormActiveChange
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onCloseDelete,
            title  = { Text("Hapus Modifier") },
            text   = { Text("Yakin ingin menghapus modifier \"${uiState.selectedModifier?.name}\"?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = onCloseDelete) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Modifier",
                icon     = Icons.Default.Tune,
                subtitle = "${uiState.modifiers.size} modifier",
                onMenu   = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddModifier) {
                Icon(Icons.Default.Add, contentDescription = "Tambah modifier")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null && uiState.modifiers.isEmpty() ->
                ErrorScreen(uiState.error, modifier = Modifier.padding(padding))
            uiState.modifiers.isEmpty() ->
                EmptyScreen("Belum ada modifier", modifier = Modifier.padding(padding))
            else -> BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
                if (maxWidth >= 600.dp) {
                    TabletModifierList(
                        modifiers        = uiState.modifiers,
                        onEdit           = onEditModifier,
                        onDelete         = onDeleteModifier
                    )
                } else {
                    PhoneModifierList(
                        modifiers        = uiState.modifiers,
                        onEdit           = onEditModifier,
                        onDelete         = onDeleteModifier
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletModifierList(
    modifiers: List<DomainModifier>,
    onEdit: (DomainModifier) -> Unit,
    onDelete: (DomainModifier) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier            = Modifier.weight(0.6f).fillMaxSize(),
            contentPadding      = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(modifiers) { modifier ->
                ModifierListItem(modifier = modifier, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun PhoneModifierList(
    modifiers: List<DomainModifier>,
    onEdit: (DomainModifier) -> Unit,
    onDelete: (DomainModifier) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(modifiers) { modifier ->
            ModifierListItem(modifier = modifier, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
private fun ModifierListItem(
    modifier: DomainModifier,
    onEdit: (DomainModifier) -> Unit,
    onDelete: (DomainModifier) -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(modifier.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text  = if (modifier.isActive) "Aktif" else "Nonaktif",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (modifier.isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onEdit(modifier) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(modifier) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ModifierFormDialog(
    uiState: ModifierManagementUiState,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onSortOrderChange: (Int) -> Unit,
    onIsActiveChange: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (uiState.selectedModifier == null) "Tambah Modifier" else "Edit Modifier")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = uiState.formName,
                    onValueChange = onNameChange,
                    label         = { Text("Nama Modifier") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = uiState.formSortOrder.toString(),
                    onValueChange = { onSortOrderChange(it.toIntOrNull() ?: 0) },
                    label         = { Text("Urutan") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.formIsActive, onCheckedChange = onIsActiveChange)
                    Text("Aktif", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = uiState.formName.isNotBlank() && !uiState.isSaving
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
