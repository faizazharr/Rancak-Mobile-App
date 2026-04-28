package id.rancak.app.presentation.ui.pricing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.rancak.app.domain.model.Voucher
import id.rancak.app.presentation.components.LoadingScreen
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.util.formatRupiah
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
            // Active filter
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
                        Icon(Icons.Default.LocalOffer, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text("Belum ada voucher", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                isTablet -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        gridItems(uiState.vouchers, key = { it.uuid }) { voucher ->
                            VoucherCard(
                                voucher   = voucher,
                                onEdit    = { viewModel.openForm(voucher) },
                                onDelete  = { viewModel.openDeleteConfirm(voucher) }
                            )
                        }
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.vouchers, key = { it.uuid }) { voucher ->
                        VoucherCard(
                            voucher   = voucher,
                            onEdit    = { viewModel.openForm(voucher) },
                            onDelete  = { viewModel.openDeleteConfirm(voucher) }
                        )
                    }
                }
            }
        }
        } // end BoxWithConstraints

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
                dismissButton = { TextButton(onClick = viewModel::closeDeleteConfirm, enabled = !uiState.isSubmitting) { Text("Batal") } }
            )
        }
    }
}

@Composable
private fun VoucherCard(voucher: Voucher, onEdit: () -> Unit, onDelete: () -> Unit) {
    val typeLabel = if (voucher.discountType == "pct") "${voucher.discountValue}%" else formatRupiah(voucher.discountValue)
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(voucher.code, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(shape = MaterialTheme.shapes.small,
                        color = if (voucher.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer) {
                        Text(if (voucher.isActive) "Aktif" else "Nonaktif",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (voucher.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Text(voucher.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Diskon: $typeLabel · Min: ${formatRupiah(voucher.minPurchase)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                if (voucher.validFrom != null || voucher.validUntil != null) {
                    Text("${voucher.validFrom?.take(10) ?: "–"} s/d ${voucher.validUntil?.take(10) ?: "∞"}",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (voucher.usageLimit != null) {
                    Text("Dipakai: ${voucher.usageCount} / ${voucher.usageLimit}",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun VoucherFormDialog(
    editing: Voucher?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, discountType: String, discountValue: String,
                validFrom: String, description: String?, maxDiscount: String?,
                minPurchase: String, usageLimit: Int?, validUntil: String?, isActive: Boolean) -> Unit
) {
    var code         by remember(editing) { mutableStateOf(editing?.code ?: "") }
    var name         by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountType by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var discountValue by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var validFrom    by remember(editing) { mutableStateOf(editing?.validFrom?.take(10) ?: "") }
    var validUntil   by remember(editing) { mutableStateOf(editing?.validUntil?.take(10) ?: "") }
    var description  by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount  by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase  by remember(editing) { mutableStateOf(editing?.minPurchase?.toString() ?: "0") }
    var usageLimitText by remember(editing) { mutableStateOf(editing?.usageLimit?.toString() ?: "") }
    var isActive     by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

    val canConfirm = !isSubmitting && code.isNotBlank() && name.isNotBlank() &&
                     discountValue.isNotBlank() && validFrom.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(if (editing == null) "Tambah Voucher" else "Edit Voucher") },
        text  = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Kode *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nama *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)

                // Discount type selector
                Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pct" to "Persen (%)", "flat" to "Nominal (Rp)").forEach { (value, label) ->
                        FilterChip(selected = discountType == value, onClick = { discountType = value }, label = { Text(label) })
                    }
                }

                OutlinedTextField(value = discountValue,
                    onValueChange = { discountValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Nilai Diskon *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = validFrom, onValueChange = { validFrom = it },
                        label = { Text("Berlaku Dari * (YYYY-MM-DD)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = validUntil, onValueChange = { validUntil = it },
                        label = { Text("Berlaku Sampai") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minPurchase,
                        onValueChange = { minPurchase = it.filter { c -> c.isDigit() } },
                        label = { Text("Min. Pembelian") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = maxDiscount,
                        onValueChange = { maxDiscount = it.filter { c -> c.isDigit() } },
                        label = { Text("Maks. Diskon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = usageLimitText,
                    onValueChange = { usageLimitText = it.filter { c -> c.isDigit() } },
                    label = { Text("Batas Penggunaan") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Aktif", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(code.trim(), name.trim(), discountType, discountValue, validFrom,
                    description.ifBlank { null }, maxDiscount.ifBlank { null },
                    minPurchase.ifBlank { "0" }, usageLimitText.toIntOrNull(),
                    validUntil.ifBlank { null }, isActive)
            }, enabled = canConfirm) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Batal") } }
    )
}
