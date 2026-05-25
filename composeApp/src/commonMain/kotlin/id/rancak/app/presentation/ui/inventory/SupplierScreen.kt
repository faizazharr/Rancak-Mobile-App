package id.rancak.app.presentation.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Supplier
import id.rancak.app.presentation.components.EmptyScreen
import id.rancak.app.presentation.components.ErrorScreen
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakButton
import id.rancak.app.presentation.components.RancakOutlinedButton
import id.rancak.app.presentation.components.RancakTextField
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.components.StatusChip
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.viewmodel.SupplierFormField
import id.rancak.app.presentation.viewmodel.SupplierUiState
import id.rancak.app.presentation.viewmodel.SupplierViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val SupplierGradientEnd = Color(0xFF0B7A60)

// ──────────────────────────────────────────────────────────────────────────────
// Screen (stateful)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun SupplierScreen(
    onBack: () -> Unit
) {
    val viewModel: SupplierViewModel = koinViewModel()
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        // HP: form menggantikan seluruh screen (tanpa overlay/dialog)
        if (!isTablet && uiState.showFormDialog) {
            SupplierFormContent(
                uiState      = uiState,
                onSave       = onSave,
                onDismiss    = onCloseForm,
                onFormChange = onFormChange,
                fullScreen   = true
            )
            return@BoxWithConstraints
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
                // FAB hanya di phone — tablet pakai tombol inline di header panel kiri
                if (!isTablet) {
                    ExtendedFloatingActionButton(
                        onClick = onAdd,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Tambah Supplier") }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            when {
                uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
                uiState.error != null && uiState.suppliers.isEmpty() && !uiState.showFormDialog ->
                    ErrorScreen(uiState.error, modifier = Modifier.padding(padding))
                isTablet -> {
                    // Tablet: split layout — list kiri, form kanan
                    Row(Modifier.padding(padding).fillMaxSize()) {
                        Column(Modifier.weight(0.42f).fillMaxHeight()) {
                            // ── Header panel kiri dengan tombol Tambah ───────────────────────
                            Row(
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Primary)
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text(
                                        "Daftar Supplier",
                                        style      = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier         = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick  = onAdd,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Tambah supplier",
                                            tint     = Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            if (uiState.suppliers.isEmpty()) {
                                EmptyScreen("Belum ada supplier", modifier = Modifier.fillMaxSize())
                            } else {
                                LazyColumn(
                                    modifier            = Modifier.fillMaxSize(),
                                    contentPadding      = PaddingValues(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(uiState.suppliers) { supplier ->
                                        SupplierListItem(supplier, onEdit, onDelete)
                                    }
                                }
                            }
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Box(Modifier.weight(0.58f).fillMaxHeight()) {
                            if (uiState.showFormDialog) {
                                SupplierFormContent(
                                    uiState      = uiState,
                                    onSave       = onSave,
                                    onDismiss    = onCloseForm,
                                    onFormChange = onFormChange,
                                    fullScreen   = false
                                )
                            } else {
                                // Empty state: tampilkan petunjuk + tombol tambah
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier         = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .background(Primary.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.LocalShipping, null,
                                                Modifier.size(36.dp),
                                                tint = Primary.copy(alpha = 0.55f)
                                            )
                                        }
                                        Text(
                                            "Pilih supplier untuk diedit",
                                            style      = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "atau tekan + untuk menambah baru",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // HP: list saja (form ditangani via early return di atas)
                    if (uiState.suppliers.isEmpty()) {
                        EmptyScreen("Belum ada supplier", modifier = Modifier.padding(padding).fillMaxSize())
                    } else {
                        LazyColumn(
                            modifier            = Modifier.padding(padding).fillMaxSize(),
                            contentPadding      = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.suppliers) { supplier ->
                                SupplierListItem(supplier, onEdit, onDelete)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// List item
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SupplierListItem(
    supplier: Supplier,
    onEdit: (Supplier) -> Unit,
    onDelete: (Supplier) -> Unit
) {
    // Avatar color derived from supplier name initial
    val initial = supplier.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val avatarHue = (supplier.name.hashCode() and 0xFF) / 255f
    val avatarBg = Primary.copy(alpha = 0.13f + avatarHue * 0.07f)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = MaterialTheme.shapes.medium,
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initial,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Primary
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    supplier.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                // Kontak & telepon dalam satu baris jika keduanya ada
                val contactLine = listOfNotNull(supplier.contactName, supplier.phone)
                    .joinToString(" · ")
                if (contactLine.isNotEmpty()) {
                    Text(
                        contactLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                supplier.email?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!supplier.isActive) {
                    StatusChip(
                        text  = "Nonaktif",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Row {
                IconButton(onClick = { onEdit(supplier) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { onDelete(supplier) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Form content — dipakai di HP (full-screen) maupun tablet (panel kanan)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SupplierFormContent(
    uiState: SupplierUiState,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onFormChange: (SupplierFormField, String) -> Unit,
    fullScreen: Boolean
) {
    val title = if (uiState.selectedSupplier == null) "Tambah Supplier" else "Edit Supplier"

    Column(Modifier.fillMaxSize()) {
        // ── Header dengan gradient teal ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Primary, SupplierGradientEnd))
                )
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        if (fullScreen) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                        contentDescription = if (fullScreen) "Kembali" else "Tutup",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        if (uiState.selectedSupplier == null) "Isi data supplier baru"
                        else "Ubah informasi supplier",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.78f)
                    )
                }
            }
        }

        // ── Form fields ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Identitas ─────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint     = Primary.copy(0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "Identitas",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RancakTextField(
                value         = uiState.formName,
                onValueChange = { onFormChange(SupplierFormField.NAME, it) },
                label         = "Nama Supplier *",
                singleLine    = true
            )
            Spacer(Modifier.height(10.dp))
            // Untuk tablet: Phone + Email berdampingan
            if (!fullScreen) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RancakTextField(
                        value         = uiState.formPhone,
                        onValueChange = { onFormChange(SupplierFormField.PHONE, it) },
                        label         = "Nomor Telepon",
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                    RancakTextField(
                        value         = uiState.formEmail,
                        onValueChange = { onFormChange(SupplierFormField.EMAIL, it) },
                        label         = "Email",
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                }
            } else {
                RancakTextField(
                    value         = uiState.formPhone,
                    onValueChange = { onFormChange(SupplierFormField.PHONE, it) },
                    label         = "Nomor Telepon",
                    singleLine    = true
                )
                Spacer(Modifier.height(10.dp))
                RancakTextField(
                    value         = uiState.formEmail,
                    onValueChange = { onFormChange(SupplierFormField.EMAIL, it) },
                    label         = "Email",
                    singleLine    = true
                )
            }

            // ── Kontak ────────────────────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.6f))
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.ContactMail,
                    contentDescription = null,
                    tint     = Primary.copy(0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "Kontak",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RancakTextField(
                value         = uiState.formContactName,
                onValueChange = { onFormChange(SupplierFormField.CONTACT_NAME, it) },
                label         = "Nama Kontak",
                singleLine    = true
            )
            Spacer(Modifier.height(10.dp))
            RancakTextField(
                value         = uiState.formAddress,
                onValueChange = { onFormChange(SupplierFormField.ADDRESS, it) },
                label         = "Alamat",
                singleLine    = false,
                minLines      = 2,
                maxLines      = 4
            )

            // ── Lainnya ───────────────────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.6f))
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                    tint     = Primary.copy(0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "Lainnya",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RancakTextField(
                value         = uiState.formNpwp,
                onValueChange = { onFormChange(SupplierFormField.NPWP, it) },
                label         = "NPWP",
                singleLine    = true
            )
            Spacer(Modifier.height(10.dp))
            RancakTextField(
                value         = uiState.formNotes,
                onValueChange = { onFormChange(SupplierFormField.NOTES, it) },
                label         = "Catatan",
                singleLine    = false,
                minLines      = 2,
                maxLines      = 4
            )
        }

        // ── Bottom actions ────────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RancakOutlinedButton(
                text     = "Batal",
                onClick  = onDismiss,
                modifier = Modifier.weight(1f)
            )
            RancakButton(
                text      = "Simpan",
                onClick   = onSave,
                enabled   = uiState.formName.isNotBlank(),
                isLoading = uiState.isSaving,
                modifier  = Modifier.weight(1f)
            )
        }
    }
}
